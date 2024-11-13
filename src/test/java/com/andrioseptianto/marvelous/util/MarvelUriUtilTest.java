package com.andrioseptianto.marvelous.util;

import com.andrioseptianto.marvelous.MarvelCharactersRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarvelUriUtilTest {

    @Test
    public void testBuildUriWithAllParams() {
        MarvelCharactersRequest request = MarvelCharactersRequest.newBuilder()
                .setName("Spider-Man")
                .setNameStartsWith("Sp")
                .setModifiedSince("2023-01-01")
                .addComics(1)
                .addSeries(2)
                .addEvents(3)
                .addStories(4)
                .setOrderBy("name")
                .setLimit(10)
                .setOffset(0) // skip setting offset when 0
                .build();

        String baseUrl = "http://gateway.marvel.com/v1/public";
        String hash = "testHash";
        String ts = "1";
        String apikey = "testApiKey";

        UriComponentsBuilder uriBuilder = MarvelUriUtil.buildUri(baseUrl, request, hash, ts, apikey);
        String uri = uriBuilder.toUriString();

        assertEquals("http://gateway.marvel.com/v1/public/characters?apikey=testApiKey&hash=testHash&ts=1&name=Spider-Man&nameStartsWith=Sp&modifiedSince=2023-01-01&comics=1&series=2&events=3&stories=4&orderBy=name&limit=10", uri);
    }

    @Test
    public void testBuildUriWithAllParamsAndOffsetSet() {
        MarvelCharactersRequest request = MarvelCharactersRequest.newBuilder()
                .setName("Spider-Man")
                .setNameStartsWith("Sp")
                .setModifiedSince("2023-01-01")
                .addComics(1)
                .addSeries(2)
                .addEvents(3)
                .addStories(4)
                .setOrderBy("name")
                .setLimit(10)
                .setOffset(10)
                .build();

        String baseUrl = "http://gateway.marvel.com/v1/public";
        String hash = "testHash";
        String ts = "1";
        String apikey = "testApiKey";

        UriComponentsBuilder uriBuilder = MarvelUriUtil.buildUri(baseUrl, request, hash, ts, apikey);
        String uri = uriBuilder.toUriString();

        assertEquals("http://gateway.marvel.com/v1/public/characters?apikey=testApiKey&hash=testHash&ts=1&name=Spider-Man&nameStartsWith=Sp&modifiedSince=2023-01-01&comics=1&series=2&events=3&stories=4&orderBy=name&limit=10&offset=10", uri);
    }

    @Test
    public void testBuildUriWithNoOptionalParams() {
        MarvelCharactersRequest request = MarvelCharactersRequest.newBuilder().build();

        String baseUrl = "http://gateway.marvel.com/v1/public";
        String hash = "testHash";
        String ts = "1";
        String apikey = "testApiKey";

        UriComponentsBuilder uriBuilder = MarvelUriUtil.buildUri(baseUrl, request, hash, ts, apikey);
        String uri = uriBuilder.toUriString();

        assertEquals("http://gateway.marvel.com/v1/public/characters?apikey=testApiKey&hash=testHash&ts=1", uri);
    }
}