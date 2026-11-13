package io.beatsync.domain;

public class PlaylistItem {

    private String itemId;
    private String trackId;
    private int position;

    public PlaylistItem() {
    }

    public PlaylistItem(String itemId, String trackId, int position) {
        this.itemId = itemId;
        this.trackId = trackId;
        this.position = position;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
