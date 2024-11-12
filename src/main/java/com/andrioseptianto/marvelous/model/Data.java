package com.andrioseptianto.marvelous.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Data {
    private int offset;
    private int limit;
    private int total;
    private int count;
    private List<Result> results;
}