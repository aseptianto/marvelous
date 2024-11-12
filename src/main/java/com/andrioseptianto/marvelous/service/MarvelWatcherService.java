package com.andrioseptianto.marvelous.service;

import com.andrioseptianto.marvelous.MarvelCharactersRequest;
import com.andrioseptianto.marvelous.MarvelCharactersResponse;
import com.andrioseptianto.marvelous.config.MarvelApiConfig;
import com.andrioseptianto.marvelous.util.HashUtil;
import com.andrioseptianto.marvelous.util.MarvelUriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class MarvelWatcherService {

    private static final Logger logger = LoggerFactory.getLogger(MarvelWatcherService.class);

    private final MarvelApiConfig marvelApiConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    public MarvelWatcherService(MarvelApiConfig marvelApiConfig, RedisTemplate<String, Object> redisTemplate) {
        this.marvelApiConfig = marvelApiConfig;
        this.redisTemplate = redisTemplate;
        this.restTemplate = new RestTemplate();
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
            MarvelCharactersRequest request;
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(key.substring("characters_".length()));
                request = MarvelCharactersRequest.parseFrom(decodedBytes);
            } catch (Exception e) {
                logger.error("Error parsing key: {}", key, e);
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
                ResponseEntity<MarvelCharactersResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, MarvelCharactersResponse.class);
                // log response
                logger.info("Response: {}", responseEntity);

                if (responseEntity.getStatusCode().value() == 200) {
                    MarvelCharactersResponse apiResponse = responseEntity.getBody();
                    if (apiResponse != null) {
                        redisTemplate.opsForValue().set(key, apiResponse, 5, TimeUnit.MINUTES);
                        logger.info("Updated cache for key: {}", key);
                    }
                } else if (responseEntity.getStatusCode().value() == 304) {
                    logger.info("No changes for key: {}", key);
                }
            } catch (Exception e) {
                logger.error("Error checking for changes for key: {}", key, e);
            }
        }
    }
}