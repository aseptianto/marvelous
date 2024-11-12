package com.andrioseptianto.marvelous.util;

import com.andrioseptianto.marvelous.MarvelCharactersRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class MarvelRequestDecoderUtil {

    private static final Logger logger = LoggerFactory.getLogger(MarvelRequestDecoderUtil.class);

    public static MarvelCharactersRequest generateRequestFromKey(String key) {
        MarvelCharactersRequest request;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(key.substring("characters_".length()));
            request = MarvelCharactersRequest.parseFrom(decodedBytes);
        } catch (Exception e) {
            logger.error("Error parsing key: {}", key, e);
            return null;
        }
        return request;
    }
}