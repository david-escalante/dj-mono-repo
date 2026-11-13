package io.beatsync.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class CatalogSeedServiceTest {

    @Test
    void mapsCsvRowOntoDerivedTrack() throws Exception {
        var resource = new ClassPathResource("songs-fixture.csv");
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();
        try (var parser = format.parse(new java.io.InputStreamReader(resource.getInputStream()))) {
            var track = CatalogSeedService.fromRow(parser.iterator().next());
            assertThat(track.getTitle()).isEqualTo("Alpha Pulse");
            assertThat(track.getDurationSec()).isEqualTo(200);
            assertThat(track.getBpm()).isEqualTo(TrackAttributes.bpm("Alpha Pulse", "Test Act", "Electronic"));
            assertThat(track.getEnergy()).isEqualTo(TrackAttributes.energy("Alpha Pulse", "Test Act"));
        }
    }
}
