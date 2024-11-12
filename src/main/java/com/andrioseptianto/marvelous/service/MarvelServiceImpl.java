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

        String ts = request.getTs();
        String hash = request.getHash();
        if (ts.isEmpty() || hash.isEmpty()) {
            ts = String.valueOf(System.currentTimeMillis());
            hash = HashUtil.generateHash(ts, marvelApiConfig.getPrivateKey(), marvelApiConfig.getPublicKey());
        }

        UriComponentsBuilder uriBuilder = MarvelUriUtil.buildUri(marvelApiConfig.getBaseUrl(), request, hash, ts);
        String url = uriBuilder.toUriString();

        try {
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

            logger.info("Received response from Marvel API: {}", apiResponse);

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

    // Convert API response of Data to gRPC response
    private MarvelCharactersResponse.Data convertData(Data apiData) {
        MarvelCharactersResponse.Data.Builder dataBuilder = MarvelCharactersResponse.Data.newBuilder()
                .setOffset(apiData.getOffset())
                .setLimit(apiData.getLimit())
                .setTotal(apiData.getTotal())
                .setCount(apiData.getCount());

        for (Result apiResult : apiData.getResults()) {
            MarvelCharactersResponse.Data.Result.Builder resultBuilder = MarvelCharactersResponse.Data.Result.newBuilder()
                    .setId(apiResult.getId())
                    .setName(apiResult.getName())
                    .setDescription(apiResult.getDescription())
                    .setModified(apiResult.getModified())
                    .setResourceURI(apiResult.getResourceURI())
                    .setThumbnail(convertThumbnail(apiResult.getThumbnail()));

            for (URL apiUrl : apiResult.getUrls()) {
                resultBuilder.addUrls(MarvelCharactersResponse.Data.Result.URL.newBuilder()
                        .setType(apiUrl.getType())
                        .setUrl(apiUrl.getUrl())
                        .build());
            }

            resultBuilder.setComics(convertComics(apiResult.getComics()));
            resultBuilder.setStories(convertStories(apiResult.getStories()));
            resultBuilder.setEvents(convertEvents(apiResult.getEvents()));
            resultBuilder.setSeries(convertSeries(apiResult.getSeries()));

            dataBuilder.addResults(resultBuilder.build());
        }

        return dataBuilder.build();
    }

    // Convert API response of Thumbnail to gRPC response
    private MarvelCharactersResponse.Data.Result.Thumbnail convertThumbnail(Thumbnail apiThumbnail) {
        return MarvelCharactersResponse.Data.Result.Thumbnail.newBuilder()
                .setPath(apiThumbnail.getPath())
                .setExtension(apiThumbnail.getExtension())
                .build();
    }

    // Convert API response of Comics to gRPC response
    private MarvelCharactersResponse.Data.Result.Comics convertComics(Comics apiComics) {
        MarvelCharactersResponse.Data.Result.Comics.Builder comicsBuilder = MarvelCharactersResponse.Data.Result.Comics.newBuilder()
                .setAvailable(apiComics.getAvailable())
                .setReturned(apiComics.getReturned())
                .setCollectionURI(apiComics.getCollectionURI());

        for (Comics.Item apiItem : apiComics.getItems()) {
            comicsBuilder.addItems(MarvelCharactersResponse.Data.Result.Comics.Item.newBuilder()
                    .setResourceURI(apiItem.getResourceURI())
                    .setName(apiItem.getName())
                    .build());
        }

        return comicsBuilder.build();
    }

    // Convert API response of Stories to gRPC response
    private MarvelCharactersResponse.Data.Result.Stories convertStories(Stories apiStories) {
        MarvelCharactersResponse.Data.Result.Stories.Builder storiesBuilder = MarvelCharactersResponse.Data.Result.Stories.newBuilder()
                .setAvailable(apiStories.getAvailable())
                .setReturned(apiStories.getReturned())
                .setCollectionURI(apiStories.getCollectionURI());

        for (Stories.Item apiItem : apiStories.getItems()) {
            storiesBuilder.addItems(MarvelCharactersResponse.Data.Result.Stories.Item.newBuilder()
                    .setResourceURI(apiItem.getResourceURI())
                    .setName(apiItem.getName())
                    .setType(apiItem.getType())
                    .build());
        }

        return storiesBuilder.build();
    }

    // Convert API response of Events to gRPC response
    private MarvelCharactersResponse.Data.Result.Events convertEvents(Events apiEvents) {
        MarvelCharactersResponse.Data.Result.Events.Builder eventsBuilder = MarvelCharactersResponse.Data.Result.Events.newBuilder()
                .setAvailable(apiEvents.getAvailable())
                .setReturned(apiEvents.getReturned())
                .setCollectionURI(apiEvents.getCollectionURI());

        for (Events.Item apiItem : apiEvents.getItems()) {
            eventsBuilder.addItems(MarvelCharactersResponse.Data.Result.Events.Item.newBuilder()
                    .setResourceURI(apiItem.getResourceURI())
                    .setName(apiItem.getName())
                    .build());
        }

        return eventsBuilder.build();
    }

    // Convert API response of Series to gRPC response
    private MarvelCharactersResponse.Data.Result.Series convertSeries(Series apiSeries) {
        MarvelCharactersResponse.Data.Result.Series.Builder seriesBuilder = MarvelCharactersResponse.Data.Result.Series.newBuilder()
                .setAvailable(apiSeries.getAvailable())
                .setReturned(apiSeries.getReturned())
                .setCollectionURI(apiSeries.getCollectionURI());

        for (Series.Item apiItem : apiSeries.getItems()) {
            seriesBuilder.addItems(MarvelCharactersResponse.Data.Result.Series.Item.newBuilder()
                    .setResourceURI(apiItem.getResourceURI())
                    .setName(apiItem.getName())
                    .build());
        }

        return seriesBuilder.build();
    }
}