package com.andrioseptianto.marvelous.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Result {
    private int id;
    private String name;
    private String description;
    private String modified;
    private String resourceURI;
    private List<URL> urls;
    private Thumbnail thumbnail;
    private Comics comics;
    private Stories stories;
    private Events events;
    private Series series;
}