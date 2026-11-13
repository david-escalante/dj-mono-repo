package io.beatsync.repo;

import static org.assertj.core.api.Assertions.assertThat;

import io.beatsync.domain.Track;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "beatsync.seed-resource=classpath:songs-fixture.csv")
class TrackRepositoryMongoTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoUri(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    TrackRepository tracks;

    @Test
    void seedsFixtureCsvIntoMongo() {
        assertThat(tracks.count()).isEqualTo(2);
        assertThat(tracks.findAll())
                .extracting(Track::getTitle)
                .containsExactlyInAnyOrder("Alpha Pulse", "Harbor Folk");
        assertThat(tracks.findAll(PageRequest.of(0, 1)).getTotalElements()).isEqualTo(2);
    }
}
