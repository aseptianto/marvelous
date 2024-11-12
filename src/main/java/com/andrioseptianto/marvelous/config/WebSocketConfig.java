package com.andrioseptianto.marvelous.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.andrioseptianto.marvelous.service.MarvelWatcherService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MarvelWatcherService marvelWatcherService;

    public WebSocketConfig(MarvelWatcherService marvelWatcherService) {
        this.marvelWatcherService = marvelWatcherService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(marvelWatcherService, "/ws/characters").setAllowedOrigins("*");
    }
}