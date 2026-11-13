package io.beatsync.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TrackAttributesTest {

    @Test
    void bpmIsStableAndWithinGenreRange() {
        int first = TrackAttributes.bpm("Golden Echo Stories (Remix)", "Midnight Runners", "World");
        int second = TrackAttributes.bpm("Golden Echo Stories (Remix)", "Midnight Runners", "World");
        assertThat(first).isEqualTo(second);
        assertThat(first).isBetween(90, 125);
    }

    @Test
    void punkSitsInAFasterRange() {
        int bpm = TrackAttributes.bpm("Echo Secrets", "Mira West", "Punk");
        assertThat(bpm).isBetween(150, 190);
    }

    @Test
    void energyIsBetweenOneAndFive() {
        int energy = TrackAttributes.energy("The River", "Leo North");
        assertThat(energy).isBetween(1, 5);
    }
}
