package com.andrioseptianto.marvelous.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpStatusCodeException;

public class HttpStatusUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpStatusUtil.class);

    public static String getStatusMessage(HttpStatusCodeException e) {
        String statusMessage = "Unknown error";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(e.getResponseBodyAsString());
            statusMessage = root.path("status").asText();
        } catch (Exception ex) {
            logger.error("Unable to parse error response", ex);
        }
        return statusMessage;
    }
}