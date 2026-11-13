package io.beatsync.service;

import io.beatsync.domain.Track;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class TrackQueryService {

    private final MongoTemplate mongoTemplate;

    public TrackQueryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Page<Track> search(String q, String genre, Integer bpmMin, Integer bpmMax, Pageable pageable) {
        Query query = new Query();
        if (genre != null && !genre.isBlank()) {
            query.addCriteria(Criteria.where("genre").regex("^" + Pattern.quote(genre.trim()) + "$", "i"));
        }
        if (q != null && !q.isBlank()) {
            String escaped = Pattern.quote(q.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(escaped, "i"),
                    Criteria.where("artist").regex(escaped, "i"),
                    Criteria.where("album").regex(escaped, "i")
            ));
        }
        if (bpmMin != null || bpmMax != null) {
            Criteria bpm = Criteria.where("bpm");
            if (bpmMin != null) {
                bpm.gte(bpmMin);
            }
            if (bpmMax != null) {
                bpm.lte(bpmMax);
            }
            query.addCriteria(bpm);
        }
        long total = mongoTemplate.count(query, Track.class);
        query.with(pageable);
        List<Track> content = mongoTemplate.find(query, Track.class);
        return new PageImpl<>(content, pageable, total);
    }

    public List<String> genres() {
        return mongoTemplate.findDistinct("genre", Track.class, String.class).stream()
                .filter(v -> v != null && !v.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
