package com.andrioseptianto.marvelous.service;

import com.andrioseptianto.marvelous.*;
import com.andrioseptianto.marvelous.config.MarvelApiConfig;
import com.andrioseptianto.marvelous.metrics.CacheMetrics;
import com.andrioseptianto.marvelous.model.*;
import com.andrioseptianto.marvelous.util.HashUtil;
import com.andrioseptianto.marvelous.util.HttpStatusUtil;
import com.andrioseptianto.marvelous.util.MarvelUriUtil;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static com.andrioseptianto.marvelous.util.MarvelResponseConverter.convertData;

@GrpcService
public class MarvelServiceImpl extends MarvelServiceGrpc.MarvelServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(MarvelServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final MarvelApiConfig marvelApiConfig;

    private final RedisTemplate<String, Object> charactersResponseRedisTemplate;

    private final CacheMetrics cacheMetrics;

    public MarvelServiceImpl(MarvelApiConfig marvelApiConfig, RedisTemplate<String, Object> charactersResponseRedisTemplate, CacheMetrics cacheMetrics) {
        this.marvelApiConfig = marvelApiConfig;
        this.charactersResponseRedisTemplate = charactersResponseRedisTemplate;
        this.cacheMetrics = cacheMetrics;
    }


    @Override
    public void getCharacters(MarvelCharactersRequest request, StreamObserver<MarvelCharactersResponse> responseObserver) {
        logger.info("Received request to get characters: {}", request);
        logger.info("request tostring: {}", request.toString());

        // Check if the request is already in cache
        String cacheKey = "characters_" + Base64.getEncoder().encodeToString(request.toByteArray());
        MarvelCharactersResponse cachedResponse = (MarvelCharactersResponse) charactersResponseRedisTemplate.opsForValue().get(cacheKey);

        if (cachedResponse != null) {
            logger.info("Cache hit for key: {}", cacheKey);
            cacheMetrics.incrementCacheHit();
            responseObserver.onNext(cachedResponse);
            responseObserver.onCompleted();
            return;
        }

        logger.info("Cache miss for key: {}", cacheKey);
        cacheMetrics.incrementCacheMiss();

        String ts = String.valueOf(System.currentTimeMillis());
        String apikey = marvelApiConfig.getPublicKey();
        String hash = HashUtil.generateHash(ts, marvelApiConfig.getPrivateKey(), marvelApiConfig.getPublicKey());


        UriComponentsBuilder uriBuilder = MarvelUriUtil.buildUri(marvelApiConfig.getBaseUrl(), request, hash, ts, apikey);
        String url = uriBuilder.build(false).toUriString();

        try {
            logger.info("Calling Marvel API URL: {}", url);
            MarvelApiResponse apiResponse = restTemplate.getForObject(url, MarvelApiResponse.class);

            // error handling when apiResponse is null
            if (apiResponse == null) {
                MarvelApiError error = MarvelApiError.newBuilder()
                        .setCode(500)
                        .setStatus("Internal Server Error")
                        .build();
                MarvelApiErrorResponse.handleError(responseObserver, error);
                return;
            }


            MarvelCharactersResponse response = MarvelCharactersResponse.newBuilder()
                    .setCode(apiResponse.getCode())
                    .setStatus(apiResponse.getStatus())
                    .setCopyright(apiResponse.getCopyright())
                    .setAttributionText(apiResponse.getAttributionText())
                    .setAttributionHTML(apiResponse.getAttributionHTML())
                    .setEtag(apiResponse.getEtag())
                    .setData(convertData(apiResponse.getData()))
                    .build();

            charactersResponseRedisTemplate.opsForValue().set(cacheKey, response, 5, TimeUnit.MINUTES);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (HttpStatusCodeException e) {
            logger.error("e.getStatusCode().value(): {}", e.getStatusCode().value());
            MarvelApiError error = MarvelApiError.newBuilder()
                    .setCode(e.getStatusCode().value())
                    .setStatus(HttpStatusUtil.getStatusMessage(e))
                    .build();
            MarvelApiErrorResponse.handleError(responseObserver, error);
        } catch (Exception e) {
            logger.error("Error getting characters: ", e);
            MarvelApiError error = MarvelApiError.newBuilder()
                    .setCode(500)
                    .setStatus("Internal Server Error")
                    .build();
            MarvelApiErrorResponse.handleError(responseObserver, error);
        }
    }
}