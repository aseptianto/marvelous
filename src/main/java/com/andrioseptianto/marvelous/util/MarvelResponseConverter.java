package com.andrioseptianto.marvelous.util;

import com.andrioseptianto.marvelous.MarvelCharactersResponse;
import com.andrioseptianto.marvelous.model.*;

public class MarvelResponseConverter {

    public static MarvelCharactersResponse.Data convertData(Data apiData) {
        MarvelCharactersResponse.Data.Builder dataBuilder = MarvelCharactersResponse.Data.newBuilder()
                .setOffset(apiData.getOffset())
                .setLimit(apiData.getLimit())
                .setTotal(apiData.getTotal())
                .setCount(apiData.getCount());

        for (Result apiResult : apiData.getResults()) {
            MarvelCharactersResponse.Data.Result.Builder resultBuilder = MarvelCharactersResponse.Data.Result.newBuilder()
                    .setId(apiResult.getId())
                    .setName(apiResult.getName())
                    .setDescription(apiResult.getDescription())
                    .setModified(apiResult.getModified())
                    .setResourceURI(apiResult.getResourceURI())
                    .setThumbnail(convertThumbnail(apiResult.getThumbnail()));

            for (URL apiUrl : apiResult.getUrls()) {
                resultBuilder.addUrls(MarvelCharactersResponse.Data.Result.URL.newBuilder()
                        .setType(apiUrl.getType())
                        .setUrl(apiUrl.getUrl())
                        .build());
            }

            resultBuilder.setComics(convertComics(apiResult.getComics()));
            resultBuilder.setStories(convertStories(apiResult.getStories()));
            resultBuilder.setEvents(convertEvents(apiResult.getEvents()));
            resultBuilder.setSeries(convertSeries(apiResult.getSeries()));

            dataBuilder.addResults(resultBuilder.build());
        }

        return dataBuilder.build();
    }

    public static MarvelCharactersResponse.Data.Result.Thumbnail convertThumbnail(Thumbnail apiThumbnail) {
        return MarvelCharactersResponse.Data.Result.Thumbnail.newBuilder()
                .setPath(apiThumbnail.getPath())
                .setExtension(apiThumbnail.getExtension())
                .build();
    }

    public static MarvelCharactersResponse.Data.Result.Comics convertComics(Comics apiComics) {
        MarvelCharactersResponse.Data.Result.Comics.Builder comicsBuilder = MarvelCharactersResponse.Data.Result.Comics.newBuilder()
                .setAvailable(apiComics.getAvailable())
                .setReturned(apiComics.getReturned())
                .setCollectionURI(apiComics.getCollectionURI());

        for (Comics.Item apiItem : apiComics.getItems()) {
            comicsBuilder.addItems(MarvelCharactersResponse.Data.Result.Comics.Item.newBuilder()
                    .setResourceURI(apiItem.getResourceURI())
                    .setName(apiItem.getName())
                    .build());
        }

        return comicsBuilder.build();
    }

    public static MarvelCharactersResponse.Data.Result.Stories convertStories(Stories apiStories) {
        MarvelCharactersResponse.Data.Result.Stories.Builder storiesBuilder = MarvelCharactersResponse.Data.Result.Stories.newBuilder()
                .setAvailable(apiStories.getAvailable())
                .setReturned(apiStories.getReturned())
                .setCollectionURI(apiStories.getCollectionURI());

        for (Stories.Item apiItem : apiStories.getItems()) {
            storiesBuilder.addItems(MarvelCharactersResponse.Data.Result.Stories.Item.newBuilder()
                    .setResourceURI(apiItem.getResourceURI())
                    .setName(apiItem.getName())
                    .setType(apiItem.getType())
                    .build());
        }

        return storiesBuilder.build();
    }

    public static MarvelCharactersResponse.Data.Result.Events convertEvents(Events apiEvents) {
        MarvelCharactersResponse.Data.Result.Events.Builder eventsBuilder = MarvelCharactersResponse.Data.Result.Events.newBuilder()
                .setAvailable(apiEvents.getAvailable())
                .setReturned(apiEvents.getReturned())
                .setCollectionURI(apiEvents.getCollectionURI());

        for (Events.Item apiItem : apiEvents.getItems()) {
            eventsBuilder.addItems(MarvelCharactersResponse.Data.Result.Events.Item.newBuilder()
                    .setResourceURI(apiItem.getResourceURI())
                    .setName(apiItem.getName())
                    .build());
        }

        return eventsBuilder.build();
    }

    public static MarvelCharactersResponse.Data.Result.Series convertSeries(Series apiSeries) {
        MarvelCharactersResponse.Data.Result.Series.Builder seriesBuilder = MarvelCharactersResponse.Data.Result.Series.newBuilder()
                .setAvailable(apiSeries.getAvailable())
                .setReturned(apiSeries.getReturned())
                .setCollectionURI(apiSeries.getCollectionURI());

        for (Series.Item apiItem : apiSeries.getItems()) {
            seriesBuilder.addItems(MarvelCharactersResponse.Data.Result.Series.Item.newBuilder()
                    .setResourceURI(apiItem.getResourceURI())
                    .setName(apiItem.getName())
                    .build());
        }

        return seriesBuilder.build();
    }
}