package io.beatsync.web;

import io.beatsync.service.PlaylistService;
import io.beatsync.web.dto.PlaylistDtos.AddItemRequest;
import io.beatsync.web.dto.PlaylistDtos.CreatePlaylistRequest;
import io.beatsync.web.dto.PlaylistDtos.MoveItemRequest;
import io.beatsync.web.dto.PlaylistDtos.PlaylistDetail;
import io.beatsync.web.dto.PlaylistDtos.PlaylistSummary;
import io.beatsync.web.dto.PlaylistDtos.UpdatePlaylistRequest;
import io.beatsync.web.dto.SuggestionResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlists;

    public PlaylistController(PlaylistService playlists) {
        this.playlists = playlists;
    }

    @GetMapping
    public List<PlaylistSummary> list() {
        return playlists.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistDetail create(@Valid @RequestBody CreatePlaylistRequest request) {
        return playlists.create(request);
    }

    @GetMapping("/{id}")
    public PlaylistDetail get(@PathVariable String id) {
        return playlists.get(id);
    }

    @PatchMapping("/{id}")
    public PlaylistDetail update(@PathVariable String id, @RequestBody UpdatePlaylistRequest request) {
        return playlists.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        playlists.delete(id);
    }

    @PostMapping("/{id}/items")
    public PlaylistDetail addItem(@PathVariable String id, @Valid @RequestBody AddItemRequest request) {
        return playlists.addItem(id, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public PlaylistDetail removeItem(@PathVariable String id, @PathVariable String itemId) {
        return playlists.removeItem(id, itemId);
    }

    @PostMapping("/{id}/items/{itemId}/move")
    public PlaylistDetail moveItem(
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestBody MoveItemRequest request
    ) {
        return playlists.moveItem(id, itemId, request);
    }

    @GetMapping("/{id}/suggestions")
    public List<SuggestionResponse> suggestions(
            @PathVariable String id,
            @RequestParam(required = false) String currentTrackId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return playlists.suggestions(id, currentTrackId, Math.min(Math.max(limit, 1), 25));
    }

    @PostMapping("/{id}/smooth-order")
    public PlaylistDetail smoothOrder(@PathVariable String id) {
        return playlists.smoothOrder(id);
    }
}
