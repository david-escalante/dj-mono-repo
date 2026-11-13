package io.beatsync.repo;

import io.beatsync.domain.Track;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrackRepository extends MongoRepository<Track, String> {
}
