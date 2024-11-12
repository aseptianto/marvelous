package com.andrioseptianto.marvelous.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class URL {
    private String type;
    private String url;
}