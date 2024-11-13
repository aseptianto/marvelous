package com.andrioseptianto.marvelous.util;

import com.andrioseptianto.marvelous.MarvelCharactersResponse;
import com.andrioseptianto.marvelous.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarvelResponseConverterTest {

    @Test
    public void testConvertData() {
        Thumbnail apiThumbnail = Thumbnail.builder()
                .path("http://path/to/thumbnail")
                .extension("jpg")
                .build();

        URL apiUrl = URL.builder()
                .type("detail")
                .url("http://gateway.marvel.com/v1/public/characters/1/detail")
                .build();

        Comics.Item apiComicsItem = Comics.Item.builder()
                .resourceURI("http://gateway.marvel.com/v1/public/comics/1")
                .name("Comic 1")
                .build();

        Comics apiComics = Comics.builder()
                .available(10)
                .returned(10)
                .collectionURI("http://gateway.marvel.com/v1/public/comics")
                .items(List.of(apiComicsItem))
                .build();

        Stories.Item apiStoriesItem = Stories.Item.builder()
                .resourceURI("http://gateway.marvel.com/v1/public/stories/1")
                .name("Story 1")
                .type("cover")
                .build();

        Stories apiStories = Stories.builder()
                .available(5)
                .returned(5)
                .collectionURI("http://gateway.marvel.com/v1/public/stories")
                .items(List.of(apiStoriesItem))
                .build();

        Events.Item apiEventsItem = Events.Item.builder()
                .resourceURI("http://gateway.marvel.com/v1/public/events/1")
                .name("Event 1")
                .build();

        Events apiEvents = Events.builder()
                .available(3)
                .returned(3)
                .collectionURI("http://gateway.marvel.com/v1/public/events")
                .items(List.of(apiEventsItem))
                .build();

        Series.Item apiSeriesItem = Series.Item.builder()
                .resourceURI("http://gateway.marvel.com/v1/public/series/1")
                .name("Series 1")
                .build();

        Series apiSeries = Series.builder()
                .available(7)
                .returned(7)
                .collectionURI("http://gateway.marvel.com/v1/public/series")
                .items(List.of(apiSeriesItem))
                .build();

        Result apiResult = Result.builder()
                .id(1)
                .name("Spider-Man")
                .description("A superhero in New York")
                .modified("2023-10-01T00:00:00Z")
                .resourceURI("http://gateway.marvel.com/v1/public/characters/1")
                .thumbnail(apiThumbnail)
                .urls(List.of(apiUrl))
                .comics(apiComics)
                .stories(apiStories)
                .events(apiEvents)
                .series(apiSeries)
                .build();

        Data apiData = Data.builder()
                .offset(0)
                .limit(20)
                .total(100)
                .count(20)
                .results(List.of(apiResult))
                .build();

        MarvelCharactersResponse.Data data = MarvelResponseConverter.convertData(apiData);

        assertEquals(0, data.getOffset());
        assertEquals(20, data.getLimit());
        assertEquals(100, data.getTotal());
        assertEquals(20, data.getCount());
        assertEquals(1, data.getResultsCount());

        MarvelCharactersResponse.Data.Result result = data.getResults(0);
        assertEquals(1, result.getId());
        assertEquals("Spider-Man", result.getName());
        assertEquals("A superhero in New York", result.getDescription());
        assertEquals("2023-10-01T00:00:00Z", result.getModified());
        assertEquals("http://gateway.marvel.com/v1/public/characters/1", result.getResourceURI());
        assertEquals("http://path/to/thumbnail", result.getThumbnail().getPath());
        assertEquals("jpg", result.getThumbnail().getExtension());

        assertEquals(1, result.getUrlsCount());
        MarvelCharactersResponse.Data.Result.URL url = result.getUrls(0);
        assertEquals("detail", url.getType());
        assertEquals("http://gateway.marvel.com/v1/public/characters/1/detail", url.getUrl());

        assertEquals(10, result.getComics().getAvailable());
        assertEquals(10, result.getComics().getReturned());
        assertEquals("http://gateway.marvel.com/v1/public/comics", result.getComics().getCollectionURI());
        assertEquals(1, result.getComics().getItemsCount());
        MarvelCharactersResponse.Data.Result.Comics.Item comicsItem = result.getComics().getItems(0);
        assertEquals("http://gateway.marvel.com/v1/public/comics/1", comicsItem.getResourceURI());
        assertEquals("Comic 1", comicsItem.getName());

        assertEquals(5, result.getStories().getAvailable());
        assertEquals(5, result.getStories().getReturned());
        assertEquals("http://gateway.marvel.com/v1/public/stories", result.getStories().getCollectionURI());
        assertEquals(1, result.getStories().getItemsCount());
        MarvelCharactersResponse.Data.Result.Stories.Item storiesItem = result.getStories().getItems(0);
        assertEquals("http://gateway.marvel.com/v1/public/stories/1", storiesItem.getResourceURI());
        assertEquals("Story 1", storiesItem.getName());
        assertEquals("cover", storiesItem.getType());

        assertEquals(3, result.getEvents().getAvailable());
        assertEquals(3, result.getEvents().getReturned());
        assertEquals("http://gateway.marvel.com/v1/public/events", result.getEvents().getCollectionURI());
        assertEquals(1, result.getEvents().getItemsCount());
        MarvelCharactersResponse.Data.Result.Events.Item eventsItem = result.getEvents().getItems(0);
        assertEquals("http://gateway.marvel.com/v1/public/events/1", eventsItem.getResourceURI());
        assertEquals("Event 1", eventsItem.getName());

        assertEquals(7, result.getSeries().getAvailable());
        assertEquals(7, result.getSeries().getReturned());
        assertEquals("http://gateway.marvel.com/v1/public/series", result.getSeries().getCollectionURI());
        assertEquals(1, result.getSeries().getItemsCount());
        MarvelCharactersResponse.Data.Result.Series.Item seriesItem = result.getSeries().getItems(0);
        assertEquals("http://gateway.marvel.com/v1/public/series/1", seriesItem.getResourceURI());
        assertEquals("Series 1", seriesItem.getName());
    }

    @Test
    public void testConvertThumbnail() {
        Thumbnail apiThumbnail = Thumbnail.builder()
                .path("http://path/to/thumbnail")
                .extension("jpg")
                .build();

        MarvelCharactersResponse.Data.Result.Thumbnail thumbnail = MarvelResponseConverter.convertThumbnail(apiThumbnail);

        assertEquals("http://path/to/thumbnail", thumbnail.getPath());
        assertEquals("jpg", thumbnail.getExtension());
    }

    @Test
    public void testConvertComics() {
        Comics.Item apiItem = Comics.Item.builder()
                .resourceURI("http://gateway.marvel.com/v1/public/comics/1")
                .name("Comic 1")
                .build();

        Comics apiComics = Comics.builder()
                .available(10)
                .returned(10)
                .collectionURI("http://gateway.marvel.com/v1/public/comics")
                .items(List.of(apiItem))
                .build();

        MarvelCharactersResponse.Data.Result.Comics comics = MarvelResponseConverter.convertComics(apiComics);

        assertEquals(10, comics.getAvailable());
        assertEquals(10, comics.getReturned());
        assertEquals("http://gateway.marvel.com/v1/public/comics", comics.getCollectionURI());
        assertEquals(1, comics.getItemsCount());

        MarvelCharactersResponse.Data.Result.Comics.Item item = comics.getItems(0);
        assertEquals("http://gateway.marvel.com/v1/public/comics/1", item.getResourceURI());
        assertEquals("Comic 1", item.getName());
    }

    @Test
    public void testConvertStories() {
        Stories.Item apiItem = Stories.Item.builder()
                .resourceURI("http://gateway.marvel.com/v1/public/stories/1")
                .name("Story 1")
                .type("cover")
                .build();

        Stories apiStories = Stories.builder()
                .available(5)
                .returned(5)
                .collectionURI("http://gateway.marvel.com/v1/public/stories")
                .items(List.of(apiItem))
                .build();


        MarvelCharactersResponse.Data.Result.Stories stories = MarvelResponseConverter.convertStories(apiStories);

        assertEquals(5, stories.getAvailable());
        assertEquals(5, stories.getReturned());
        assertEquals("http://gateway.marvel.com/v1/public/stories", stories.getCollectionURI());
        assertEquals(1, stories.getItemsCount());

        MarvelCharactersResponse.Data.Result.Stories.Item item = stories.getItems(0);
        assertEquals("http://gateway.marvel.com/v1/public/stories/1", item.getResourceURI());
        assertEquals("Story 1", item.getName());
        assertEquals("cover", item.getType());
    }

    @Test
    public void testConvertEvents() {
        Events.Item apiItem = Events.Item.builder()
                .resourceURI("http://gateway.marvel.com/v1/public/events/1")
                .name("Event 1")
                .build();

        Events apiEvents = Events.builder()
                .available(3)
                .returned(3)
                .collectionURI("http://gateway.marvel.com/v1/public/events")
                .items(List.of(apiItem))
                .build();

        MarvelCharactersResponse.Data.Result.Events events = MarvelResponseConverter.convertEvents(apiEvents);

        assertEquals(3, events.getAvailable());
        assertEquals(3, events.getReturned());
        assertEquals("http://gateway.marvel.com/v1/public/events", events.getCollectionURI());
        assertEquals(1, events.getItemsCount());

        MarvelCharactersResponse.Data.Result.Events.Item item = events.getItems(0);
        assertEquals("http://gateway.marvel.com/v1/public/events/1", item.getResourceURI());
        assertEquals("Event 1", item.getName());
    }

    @Test
    public void testConvertSeries() {
        Series.Item apiItem = Series.Item.builder()
                .resourceURI("http://gateway.marvel.com/v1/public/series/1")
                .name("Series 1")
                .build();
        Series apiSeries = Series.builder()
                .available(7)
                .returned(7)
                .collectionURI("http://gateway.marvel.com/v1/public/series")
                .items(List.of(apiItem))
                .build();


        MarvelCharactersResponse.Data.Result.Series series = MarvelResponseConverter.convertSeries(apiSeries);

        assertEquals(7, series.getAvailable());
        assertEquals(7, series.getReturned());
        assertEquals("http://gateway.marvel.com/v1/public/series", series.getCollectionURI());
        assertEquals(1, series.getItemsCount());

        MarvelCharactersResponse.Data.Result.Series.Item item = series.getItems(0);
        assertEquals("http://gateway.marvel.com/v1/public/series/1", item.getResourceURI());
        assertEquals("Series 1", item.getName());
    }
}