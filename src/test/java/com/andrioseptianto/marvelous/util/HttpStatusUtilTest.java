package com.andrioseptianto.marvelous.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.HttpStatusCodeException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class HttpStatusUtilTest {

    @Test
    public void testGetStatusMessage_withValidJsonResponse() throws Exception {
        // Mock HttpStatusCodeException
        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        String jsonResponse = "{\"status\": \"Not Found\"}";
        when(exception.getResponseBodyAsString()).thenReturn(jsonResponse);

        // Call the method and assert the result
        String statusMessage = HttpStatusUtil.getStatusMessage(exception);
        assertEquals("Not Found", statusMessage);
    }

    @Test
    public void testGetStatusMessage_withInvalidJsonResponse() throws Exception {
        // Mock HttpStatusCodeException
        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        String invalidJsonResponse = "Invalid JSON";
        when(exception.getResponseBodyAsString()).thenReturn(invalidJsonResponse);

        // Call the method and assert the result
        String statusMessage = HttpStatusUtil.getStatusMessage(exception);
        assertEquals("Unknown error", statusMessage);
    }

    @Test
    public void testGetStatusMessage_withEmptyJsonResponse() throws Exception {
        // Mock HttpStatusCodeException
        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        String emptyJsonResponse = "{}";
        when(exception.getResponseBodyAsString()).thenReturn(emptyJsonResponse);

        // Call the method and assert the result
        String statusMessage = HttpStatusUtil.getStatusMessage(exception);
        assertEquals("", statusMessage);
    }
}