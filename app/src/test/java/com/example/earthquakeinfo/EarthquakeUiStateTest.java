package com.example.earthquakeinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/*
    Unit tests for EarthquakeUiState: every factory method must create a consistent state.
 */
public class EarthquakeUiStateTest {

    private static Earthquake sampleEarthquake() {
        return new Earthquake(6.5, "73 km WSW of Rumoi, Japan", 1_700_000_000_000L, "https://example.com");
    }

    @Test
    public void loading_withoutList_hasEmptyList() {
        EarthquakeUiState state = EarthquakeUiState.loading();

        assertEquals(EarthquakeUiState.Status.LOADING, state.getStatus());
        assertTrue(state.getEarthquakes().isEmpty());
    }

    @Test
    public void loading_withPreviousList_keepsThatList() {
        List<Earthquake> previous = new ArrayList<>();
        previous.add(sampleEarthquake());

        EarthquakeUiState state = EarthquakeUiState.loading(previous);

        assertEquals(EarthquakeUiState.Status.LOADING, state.getStatus());
        assertEquals(1, state.getEarthquakes().size());
    }

    @Test
    public void success_containsTheEarthquakes() {
        List<Earthquake> earthquakes = new ArrayList<>();
        Earthquake earthquake = sampleEarthquake();
        earthquakes.add(earthquake);

        EarthquakeUiState state = EarthquakeUiState.success(earthquakes);

        assertEquals(EarthquakeUiState.Status.SUCCESS, state.getStatus());
        assertEquals(1, state.getEarthquakes().size());
        assertSame(earthquake, state.getEarthquakes().get(0));
    }

    @Test
    public void success_listCannotBeChangedFromOutside() {
        List<Earthquake> earthquakes = new ArrayList<>();
        earthquakes.add(sampleEarthquake());
        EarthquakeUiState state = EarthquakeUiState.success(earthquakes);

        // Adding to the list of a state must throw an exception (the list is read-only)
        assertThrows(UnsupportedOperationException.class,
                () -> state.getEarthquakes().add(sampleEarthquake()));
    }

    @Test
    public void errorStates_haveNoEarthquakes() {
        assertTrue(EarthquakeUiState.empty().getEarthquakes().isEmpty());
        assertTrue(EarthquakeUiState.noInternet().getEarthquakes().isEmpty());
        assertTrue(EarthquakeUiState.error().getEarthquakes().isEmpty());

        assertEquals(EarthquakeUiState.Status.EMPTY, EarthquakeUiState.empty().getStatus());
        assertEquals(EarthquakeUiState.Status.NO_INTERNET, EarthquakeUiState.noInternet().getStatus());
        assertEquals(EarthquakeUiState.Status.ERROR, EarthquakeUiState.error().getStatus());
    }
}
