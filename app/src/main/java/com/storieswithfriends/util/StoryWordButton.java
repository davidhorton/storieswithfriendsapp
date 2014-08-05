package com.storieswithfriends.util;

import android.content.Context;
import android.widget.Button;

import com.storieswithfriends.data.Word;

/**
 * @author David Horton
 */
public class StoryWordButton extends Button {

    private Word word;

    public StoryWordButton(Context context, Word word) {
        super(context);
        this.word = word;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }
}
