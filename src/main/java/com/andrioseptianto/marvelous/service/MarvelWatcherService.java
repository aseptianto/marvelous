package com.andrioseptianto.marvelous.service;

import com.andrioseptianto.marvelous.MarvelCharactersRequest;
import com.andrioseptianto.marvelous.MarvelCharactersResponse;
import com.andrioseptianto.marvelous.config.MarvelApiConfig;
import com.andrioseptianto.marvelous.model.MarvelApiResponse;
import com.andrioseptianto.marvelous.util.HashUtil;
import com.andrioseptianto.marvelous.util.MarvelRequestDecoderUtil;
import com.andrioseptianto.marvelous.util.MarvelUriUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.andrioseptianto.marvelous.util.MarvelResponseConverter.convertData;

@Service
public class MarvelWatcherService extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MarvelWatcherService.class);

    private final MarvelApiConfig marvelApiConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MarvelWatcherService(MarvelApiConfig marvelApiConfig, RedisTemplate<String, Object> redisTemplate) {
        this.marvelApiConfig = marvelApiConfig;
        this.redisTemplate = redisTemplate;
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void watchForChanges() {
        Set<String> keys = redisTemplate.keys("characters_*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            MarvelCharactersResponse cachedResponse = (MarvelCharactersResponse) redisTemplate.opsForValue().get(key);
            if (cachedResponse == null) {
                continue;
            }

            String etag = cachedResponse.getEtag();
            String ts = String.valueOf(System.currentTimeMillis());
            String hash = HashUtil.generateHash(ts, marvelApiConfig.getPrivateKey(), marvelApiConfig.getPublicKey());

            // Deserialize the Base64 encoded string back to MarvelCharactersRequest
            MarvelCharactersRequest request = MarvelRequestDecoderUtil.generateRequestFromKey(key);
            if (request == null) {
                continue;
            }

            UriComponentsBuilder uriBuilder = MarvelUriUtil.buildUri(marvelApiConfig.getBaseUrl(), request, hash, ts);
            String url = uriBuilder.toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("If-None-Match", etag);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info("Calling URL: {}", url);
            logger.info("With headers: {}", entity.getHeaders());

            try {
                ResponseEntity<MarvelApiResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, MarvelApiResponse.class);
                // log response
                logger.info("Response: {}", responseEntity);

                if (responseEntity.getStatusCode().value() == 200) {
                    MarvelApiResponse apiResponse = responseEntity.getBody();
                    if (apiResponse != null) {
                        MarvelCharactersResponse response = MarvelCharactersResponse.newBuilder()
                                .setCode(apiResponse.getCode())
                                .setStatus(apiResponse.getStatus())
                                .setCopyright(apiResponse.getCopyright())
                                .setAttributionText(apiResponse.getAttributionText())
                                .setAttributionHTML(apiResponse.getAttributionHTML())
                                .setEtag(apiResponse.getEtag())
                                .setData(convertData(apiResponse.getData()))
                                .build();

                        redisTemplate.opsForValue().set(key, response, 5, TimeUnit.MINUTES);
                        logger.info("Updated cache for key: {}", key);
                        broadcastUpdate(request);
                    }
                } else if (responseEntity.getStatusCode().value() == 304) {
                    logger.info("No changes for key: {}", key);
                }
            } catch (Exception e) {
                logger.error("Error checking for changes for key: {}", key, e);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established");
        sessions.add(session);
        sendCurrentKeys(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed");
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, String> messageMap = objectMapper.readValue(payload, Map.class);
        String key = "characters_" + messageMap.get("key");
        logger.info("Received message: {}", key);
        MarvelCharactersResponse cachedResponse = (MarvelCharactersResponse) redisTemplate.opsForValue().get(key);
        if (cachedResponse != null) {
            MarvelCharactersResponse updatedResponse = cachedResponse.toBuilder()
                    .setEtag("INVALID_ETAG")
                    .build();
            redisTemplate.opsForValue().set(key, updatedResponse, 5, TimeUnit.MINUTES);
            logger.info("Updated etag for key: {}", key);
        }
    }

    private void sendCurrentKeys(WebSocketSession session) throws Exception {
        Set<String> keys = redisTemplate.keys("characters_*");
        if (keys != null && !keys.isEmpty()) {
            List<Map<String, String>> keyObjects = keys.stream()
                    .map(key -> {
                        String base64Key = key.substring("characters_".length());
                        MarvelCharactersRequest request = MarvelRequestDecoderUtil.generateRequestFromKey(key);
                        if (request != null) {
                            Map<String, String> keyObject = new HashMap<>();
                            keyObject.put("base64Key", base64Key);
                            keyObject.put("value", request.toString());
                            return keyObject;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            String message = objectMapper.writeValueAsString(Map.of("keys", keyObjects));
            session.sendMessage(new TextMessage(message));
        }
    }

    private void broadcastUpdate(MarvelCharactersRequest request) {
        String message = "Marvel Cache Request update -> " + request.toString();
        TextMessage textMessage = new TextMessage(message);
        sessions.forEach(session -> {
            try {
                session.sendMessage(textMessage);
            } catch (Exception e) {
                logger.error("Error sending message to session: {}", session, e);
            }
        });
    }
}