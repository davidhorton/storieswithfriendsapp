package com.storieswithfriends.data;

/**
 * @author David Horton
 */
public class CurrentUser {

    private static User currentLoggedInUser;

    public static User getCurrentLoggedInUser() {
        return currentLoggedInUser;
    }

    public static void setCurrentLoggedInUser(User currentLoggedInUser) {
        CurrentUser.currentLoggedInUser = currentLoggedInUser;
    }
}
