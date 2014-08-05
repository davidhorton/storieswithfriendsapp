package com.storieswithfriends.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * @author David Horton
 */
public class Word {

    @Expose
    @SerializedName("dateAdded")
    private String dateAdded;

    @Expose
    @SerializedName("userWhoAddedIt")
    private User userWhoAddedIt;

    @Expose
    @SerializedName("wordValue")
    private String wordValue;

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public User getUserWhoAddedIt() {
        return userWhoAddedIt;
    }

    public void setUserWhoAddedIt(User userWhoAddedIt) {
        this.userWhoAddedIt = userWhoAddedIt;
    }

    public String getWordValue() {
        return wordValue;
    }

    public void setWordValue(String wordValue) {
        this.wordValue = wordValue;
    }
}
