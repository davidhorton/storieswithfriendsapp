package com.storieswithfriends.http;

import com.storieswithfriends.data.Story;
import com.storieswithfriends.data.StorySummary;
import com.storieswithfriends.data.User;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * @author David Horton
 */
public interface StoriesService {

    @GET("/rest/story/{id}")
    public void getStory(@Path("id") String id, Callback<Story> cb);

    @GET("/rest/story/past/{username}")
    public void getPastStories(@Path("username") String username, Callback<ArrayList<StorySummary>> cb);

    @GET("/rest/story/yourturn/{username}")
    public void getYourTurnStories(@Path("username") String username, Callback<ArrayList<StorySummary>> cb);

    @GET("/rest/story/unfinished/{username}")
    public void getUnfinishedStories(@Path("username") String username, Callback<ArrayList<StorySummary>> cb);

    @POST("/rest/story/newstory")
    public void createNewStory(@Body Story newStory, Callback<String> cb);

    @FormUrlEncoded
    @POST("/rest/story/addword")
    public void addWordToStory(@Field("storyId") String storyId, @Field("newWord") String newWord, @Field("username") String username, Callback<String> cb);

    @FormUrlEncoded
    @POST("/rest/story/newuser")
    public void createNewUser(@Field("username") String username, @Field("displayName") String displayName, @Field("password") String password, Callback<String> cb);

    @FormUrlEncoded
    @POST("/rest/story/login")
    public void loginToTheApp(@Field("username") String username, @Field("password") String password, Callback<User> cb);

    @FormUrlEncoded
    @POST("/rest/story/usernameexists")
    public void usernameExists(@Field("username") String username, Callback<String> cb);
}
