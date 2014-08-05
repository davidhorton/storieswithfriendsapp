package com.storieswithfriends.util;

/**
 * @author David Horton
 */
public enum StoryType {
    YOUR_TURN ("YOUR_TURN"),
    PAST ("PAST"),
    UNFINISHED ("UNFINISHED");

    private final String storyType;

    private StoryType(String s) {
        storyType = s;
    }

    public boolean equalsName(String test){
        return (test == null) ? false : storyType.equals(test);
    }

    public String toString(){
        return storyType;
    }
}
