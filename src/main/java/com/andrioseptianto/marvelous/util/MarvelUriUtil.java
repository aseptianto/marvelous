package com.andrioseptianto.marvelous.util;

import com.andrioseptianto.marvelous.MarvelCharactersRequest;
import org.springframework.web.util.UriComponentsBuilder;

public class MarvelUriUtil {

    public static UriComponentsBuilder buildUri(String baseUrl, MarvelCharactersRequest request, String hash, String ts) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/characters")
                .queryParam("apikey", request.getApikey())
                .queryParam("hash", hash)
                .queryParam("ts", ts);

        if (!request.getName().isEmpty()) {
            uriBuilder.queryParam("name", request.getName());
        }
        if (!request.getNameStartsWith().isEmpty()) {
            uriBuilder.queryParam("nameStartsWith", request.getNameStartsWith());
        }
        if (!request.getModifiedSince().isEmpty()) {
            uriBuilder.queryParam("modifiedSince", request.getModifiedSince());
        }
        if (!request.getComicsList().isEmpty()) {
            uriBuilder.queryParam("comics", String.join(",", request.getComicsList().stream().map(String::valueOf).toArray(String[]::new)));
        }
        if (!request.getSeriesList().isEmpty()) {
            uriBuilder.queryParam("series", String.join(",", request.getSeriesList().stream().map(String::valueOf).toArray(String[]::new)));
        }
        if (!request.getEventsList().isEmpty()) {
            uriBuilder.queryParam("events", String.join(",", request.getEventsList().stream().map(String::valueOf).toArray(String[]::new)));
        }
        if (!request.getStoriesList().isEmpty()) {
            uriBuilder.queryParam("stories", String.join(",", request.getStoriesList().stream().map(String::valueOf).toArray(String[]::new)));
        }
        if (!request.getOrderBy().isEmpty()) {
            uriBuilder.queryParam("orderBy", request.getOrderBy());
        }
        if (request.getLimit() != 0) {
            uriBuilder.queryParam("limit", request.getLimit());
        }
        if (request.getOffset() != 0) {
            uriBuilder.queryParam("offset", request.getOffset());
        }

        return uriBuilder;
    }
}