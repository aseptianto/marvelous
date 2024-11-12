package com.andrioseptianto.marvelous.service;

import com.andrioseptianto.marvelous.MarvelCharactersRequest;
import com.andrioseptianto.marvelous.MarvelCharactersResponse;
import com.andrioseptianto.marvelous.config.MarvelApiConfig;
import com.andrioseptianto.marvelous.metrics.CacheMetrics;
import com.andrioseptianto.marvelous.model.*;
import com.andrioseptianto.marvelous.util.HashUtil;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarvelServiceImplTest {

    @Mock
    private MarvelApiConfig marvelApiConfig;

    @Mock
    private RedisTemplate<String, Object> charactersResponseRedisTemplate;

    @Mock
    private CacheMetrics cacheMetrics;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private MarvelServiceImpl marvelServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(charactersResponseRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(marvelApiConfig.getBaseUrl()).thenReturn("http://example.com");
        when(marvelApiConfig.getPublicKey()).thenReturn("publicKey");
        when(marvelApiConfig.getPrivateKey()).thenReturn("privateKey");
    }

    @Test
    void testGetCharacters_CacheHit() {
        // Arrange
        MarvelCharactersRequest request = MarvelCharactersRequest.newBuilder().setName("Spider-Man").build();
        MarvelCharactersResponse cachedResponse = MarvelCharactersResponse.newBuilder().setCode(200).build();
        when(valueOperations.get(anyString())).thenReturn(cachedResponse);

        StreamObserver<MarvelCharactersResponse> responseObserver = mock(StreamObserver.class);

        // Act
        marvelServiceImpl.getCharacters(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(cachedResponse);
        verify(responseObserver).onCompleted();
        verify(cacheMetrics).incrementCacheHit();
        verify(cacheMetrics, never()).incrementCacheMiss();
    }

    @Test
    void testGetCharacters_CacheMiss() {
        // Arrange
        MarvelCharactersRequest request = MarvelCharactersRequest.newBuilder().setName("Spider-Man").build();
        when(valueOperations.get(anyString())).thenReturn(null);

        StreamObserver<MarvelCharactersResponse> responseObserver = mock(StreamObserver.class);

        // Act
        marvelServiceImpl.getCharacters(request, responseObserver);

        // Assert
        verify(cacheMetrics).incrementCacheMiss();
        verify(cacheMetrics, never()).incrementCacheHit();
    }
}