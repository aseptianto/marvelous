package com.andrioseptianto.marvelous.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("classpath:application.properties")
public class MarvelApiConfig {

    @Value("${marvel.api.baseUrl}")
    private String baseUrl;

    @Value("${marvel.api.publicKey}")
    private String publicKey;

    @Value("${marvel.api.privateKey}")
    private String privateKey;

}