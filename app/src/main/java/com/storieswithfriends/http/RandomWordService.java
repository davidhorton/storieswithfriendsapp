package com.storieswithfriends.http;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * @author David Horton
 */
public interface RandomWordService {

    @GET("/")
    public void getRandomWord(Callback<String> cb);
}
