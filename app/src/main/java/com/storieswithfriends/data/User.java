package com.storieswithfriends.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Horton
 */
public class User {

    @Expose
    @SerializedName("username")
    private String username;

    @Expose
    @SerializedName("displayName")
    private String displayName;

    @Expose
    @SerializedName("orderPosition")
    private int orderPosition;

    @Expose
    @SerializedName("isOwner")
    private boolean isOwner;

    @Expose
    @SerializedName("isMyTurn")
    private boolean isMyTurn;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getOrderPosition() {
        return orderPosition;
    }

    public void setOrderPosition(int orderPosition) {
        this.orderPosition = orderPosition;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public void setMyTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
    }

    @Override
    public String toString() {
        return this.getOrderPosition() + " - " + this.getUsername();
    }
}
