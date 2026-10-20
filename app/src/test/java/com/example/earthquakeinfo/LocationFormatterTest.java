package com.example.earthquakeinfo;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/*
    Unit tests for LocationFormatter.
    They run on the computer (JVM), without a phone or emulator,
    because LocationFormatter does not need Android - it gets plain texts in the constructor.
 */
public class LocationFormatterTest {

    private LocationFormatter english;
    private LocationFormatter serbian;

    // Runs before EVERY test, so each test starts with fresh objects
    @Before
    public void setUp() {
        english = new LocationFormatter("NSEW", "%1$s km %2$s of", "Near the");
        serbian = new LocationFormatter("СЈИЗ", "%1$s km %2$s од", "У близини");
    }

    @Test
    public void format_distanceAndDirection_splitsIntoOffsetAndPlace() {
        // Arrange - prepare the input
        String location = "73 km WSW of Rumoi, Japan";

        // Act - call the method we are testing
        LocationFormatter.Parts parts = english.format(location);

        // Assert - check the result (expected value first, actual value second)
        assertEquals("73 km WSW of", parts.getOffset());
        assertEquals("Rumoi, Japan", parts.getPrimaryLocation());
    }

    @Test
    public void format_serbian_translatesDirectionAndWordOf() {
        LocationFormatter.Parts parts = serbian.format("73 km WSW of Rumoi, Japan");

        assertEquals("73 km ЗЈЗ од", parts.getOffset());
        assertEquals("Rumoi, Japan", parts.getPrimaryLocation());
    }

    @Test
    public void format_serbian_translatesEveryCompassLetter() {
        assertEquals("5 km С од", serbian.format("5 km N of A").getOffset());
        assertEquals("5 km Ј од", serbian.format("5 km S of A").getOffset());
        assertEquals("5 km И од", serbian.format("5 km E of A").getOffset());
        assertEquals("5 km З од", serbian.format("5 km W of A").getOffset());
        assertEquals("5 km ССИ од", serbian.format("5 km NNE of A").getOffset());
    }

    @Test
    public void format_noSpaceBeforeKm_isRecognized() {
        // USGS sometimes sends "120km" without a space
        LocationFormatter.Parts parts = serbian.format("120km SE of Hihifo, Tonga");

        assertEquals("120 km ЈИ од", parts.getOffset());
        assertEquals("Hihifo, Tonga", parts.getPrimaryLocation());
    }

    @Test
    public void format_decimalDistanceAndLowercase_isRecognized() {
        LocationFormatter.Parts parts = serbian.format("10.5 km nne of Anchorage, Alaska");

        assertEquals("10.5 km ССИ од", parts.getOffset());
        assertEquals("Anchorage, Alaska", parts.getPrimaryLocation());
    }

    @Test
    public void format_ofWithoutDistance_splitsWithoutTranslation() {
        LocationFormatter.Parts parts = serbian.format("south of the Fiji Islands");

        assertEquals("south of", parts.getOffset());
        assertEquals("the Fiji Islands", parts.getPrimaryLocation());
    }

    @Test
    public void format_placeContainingLettersOf_isNotSplit() {
        // "Tofino" contains the letters "of" - it must NOT be split there
        LocationFormatter.Parts parts = serbian.format("near Tofino, Canada");

        assertEquals("У близини", parts.getOffset());
        assertEquals("near Tofino, Canada", parts.getPrimaryLocation());
    }

    @Test
    public void format_noOffset_usesNearThe() {
        LocationFormatter.Parts parts = serbian.format("Fiji region");

        assertEquals("У близини", parts.getOffset());
        assertEquals("Fiji region", parts.getPrimaryLocation());
    }

    @Test
    public void format_wrongCompassTranslation_keepsEnglishDirection() {
        // Only 3 letters instead of 4 - the formatter must not crash
        LocationFormatter broken = new LocationFormatter("СЈИ", "%1$s km %2$s од", "У близини");

        assertEquals("73 km WSW од", broken.format("73 km WSW of Rumoi, Japan").getOffset());
    }
}