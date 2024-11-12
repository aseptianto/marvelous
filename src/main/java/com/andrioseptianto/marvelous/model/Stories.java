package com.andrioseptianto.marvelous.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Stories {
    private int available;
    private int returned;
    private String collectionURI;
    private List<Item> items;

    @Getter
    @Setter
    @Builder
    public static class Item {
        private String resourceURI;
        private String name;
        private String type;
    }
}