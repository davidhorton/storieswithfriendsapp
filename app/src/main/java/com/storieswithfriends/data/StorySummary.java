package com.storieswithfriends.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Horton
 */
public class StorySummary {

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("title")
    private String title;

    @Expose
    @SerializedName("whoseTurn")
    private User whoseTurn;

    @Expose
    @SerializedName("numberOfPlayers")
    private long numberOfPlayers;

    public User getWhoseTurn() {
        return whoseTurn;
    }

    public void setWhoseTurn(User whoseTurn) {
        this.whoseTurn = whoseTurn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(long numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    @Override
    public String toString() {
        return title;
    }
}
