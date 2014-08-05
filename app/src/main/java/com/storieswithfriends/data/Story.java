package com.storieswithfriends.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * @author David Horton
 */
public class Story {

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("owner")
    private User owner;

    @Expose
    @SerializedName("participants")
    private List<User> participants;

    @Expose
    @SerializedName("words")
    private List<Word> words;

    @Expose
    @SerializedName("title")
    private String title;

    @Expose
    @SerializedName("allFinished")
    private boolean allFinished;

    @Expose
    @SerializedName("whoseTurn")
    private User whoseTurn;

    @Expose
    @SerializedName("dateStarted")
    private String dateStarted;

    @Expose
    @SerializedName("dateFinished")
    private String dateFinished;

    public User getWhoseTurn() {
        return whoseTurn;
    }

    public void setWhoseTurn(User whoseTurn) {
        this.whoseTurn = whoseTurn;
    }

    public boolean isAllFinished() {
        return allFinished;
    }

    public void setAllFinished(boolean allFinished) {
        this.allFinished = allFinished;
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public String getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(String dateStarted) {
        this.dateStarted = dateStarted;
    }

    public String getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(String dateFinished) {
        this.dateFinished = dateFinished;
    }
}
