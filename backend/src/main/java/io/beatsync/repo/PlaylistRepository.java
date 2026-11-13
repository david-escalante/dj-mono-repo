package io.beatsync.repo;

import io.beatsync.domain.Playlist;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlaylistRepository extends MongoRepository<Playlist, String> {
}
