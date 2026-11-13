package io.beatsync.web;

import io.beatsync.domain.Track;
import io.beatsync.repo.TrackRepository;
import io.beatsync.service.TrackQueryService;
import io.beatsync.web.dto.PageResponse;
import io.beatsync.web.dto.TrackResponse;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TrackController {

    private final TrackQueryService queries;
    private final TrackRepository tracks;

    public TrackController(TrackQueryService queries, TrackRepository tracks) {
        this.queries = queries;
        this.tracks = tracks;
    }

    @GetMapping("/tracks")
    public PageResponse<TrackResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer bpmMin,
            @RequestParam(required = false) Integer bpmMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        var result = queries.search(q, genre, bpmMin, bpmMax, PageRequest.of(Math.max(page, 0), safeSize, Sort.by("title")));
        return PageResponse.of(result.map(TrackResponse::from));
    }

    @GetMapping("/tracks/{id}")
    public TrackResponse get(@PathVariable String id) {
        Track track = tracks.findById(id).orElseThrow(() -> new NotFoundException("Track not found: " + id));
        return TrackResponse.from(track);
    }

    @GetMapping("/genres")
    public List<String> genres() {
        return queries.genres();
    }
}
