package io.beatsync.service;

import io.beatsync.domain.Track;
import io.beatsync.repo.TrackRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

@Component
public class CatalogSeedService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeedService.class);
    private static final int BATCH = 500;

    private final TrackRepository tracks;
    private final MongoTemplate mongoTemplate;
    private final Resource seedResource;

    public CatalogSeedService(
            TrackRepository tracks,
            MongoTemplate mongoTemplate,
            @Value("${beatsync.seed-resource}") Resource seedResource
    ) {
        this.tracks = tracks;
        this.mongoTemplate = mongoTemplate;
        this.seedResource = seedResource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ensureIndexes();
        if (tracks.count() > 0) {
            log.info("Track catalog already seeded ({} tracks)", tracks.count());
            return;
        }
        int inserted = seedFrom(seedResource);
        log.info("Seeded {} tracks from {}", inserted, seedResource.getFilename());
    }

    public int seedFrom(Resource resource) throws IOException {
        List<Track> batch = new ArrayList<>(BATCH);
        int inserted = 0;
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            for (CSVRecord row : parser) {
                batch.add(fromRow(row));
                if (batch.size() >= BATCH) {
                    tracks.saveAll(batch);
                    inserted += batch.size();
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) {
            tracks.saveAll(batch);
            inserted += batch.size();
        }
        return inserted;
    }

    private void ensureIndexes() {
        IndexOperations ops = mongoTemplate.indexOps(Track.class);
        ops.createIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("title")
                .onField("artist")
                .onField("album")
                .named("track_text")
                .build());
    }

    static Track fromRow(CSVRecord row) {
        String title = required(row, "Title");
        String artist = required(row, "Artist");
        String album = value(row, "Album");
        String genre = value(row, "Genre");
        int year = Integer.parseInt(required(row, "Release year"));
        int duration = DurationParser.toSeconds(required(row, "Run time"));
        int bpm = TrackAttributes.bpm(title, artist, genre);
        int energy = TrackAttributes.energy(title, artist);
        return new Track(title, artist, album, genre, year, duration, bpm, energy);
    }

    private static String required(CSVRecord row, String header) {
        String value = value(row, header);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Missing " + header + " on CSV row " + row.getRecordNumber());
        }
        return value;
    }

    private static String value(CSVRecord row, String header) {
        if (!row.isMapped(header) || row.get(header) == null) {
            return "";
        }
        return row.get(header);
    }
}
