package com.storieswithfriends.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.storieswithfriends.R;
import com.storieswithfriends.activity.MainMenuActivity;
import com.storieswithfriends.data.CurrentUser;
import com.storieswithfriends.data.StorySummary;
import com.storieswithfriends.http.RESTHelper;
import com.storieswithfriends.http.StoriesService;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author David Horton
 */
public class PastStoriesFragment extends Fragment {

    public interface PastStoriesFragmentListener {
        public void onPastStorySelected(StorySummary selectedStory);
    }
    private PastStoriesFragmentListener listener;

    private ArrayAdapter<StorySummary> adapter;
    private ArrayList<StorySummary> pastStories;
    private ProgressDialogFragment progressDialogFragment;
    private ListView storiesList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (PastStoriesFragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_story_list, container, false);

        ActionBar ab = ((android.support.v7.app.ActionBarActivity) this.getActivity()).getSupportActionBar();
        ab.setTitle("Your Past Stories");

        pastStories = new ArrayList<StorySummary>();
        adapter = new ArrayAdapter<StorySummary>(this.getActivity().getBaseContext(), R.layout.story_line_item, pastStories);

        storiesList = (ListView) view.findViewById(R.id.listView_stories);
        storiesList.setAdapter(adapter);

        TextView noItemsMessage = (TextView) view.findViewById(R.id.empty_story_list_item);
        storiesList.setEmptyView(noItemsMessage);

        getStories();

        return view;
    }



    @Override
    public void onPause() {
        super.onPause();

        // Dismiss any dialogs to prevent WindowLeaked exceptions
        dismissDialog();
    }

    private void getStories() {

        showProgress("Please wait", "Retrieving your past stories...", "");

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithJsonResponse(this.getActivity().getBaseContext(), null);

        StoriesService service = restAdapter.create(StoriesService.class);

        service.getPastStories(CurrentUser.getCurrentLoggedInUser().getUsername(), new Callback<ArrayList<StorySummary>>() {
            @Override
            public void success(ArrayList<StorySummary> storySummaries, Response response) {
                pastStories.addAll(storySummaries);
                adapter.notifyDataSetChanged();

                storiesList.setOnItemClickListener(storiesClickListener);

                dismissDialog();
            }

            @Override
            public void failure(RetrofitError error) {
                dismissDialog();
                Log.d("STORIESWITHFRIENDS", "Callback failed: " + error.toString());
                error.printStackTrace();
            }
        });
    }

    /**
     * Handles the story click events
     */
    private final AdapterView.OnItemClickListener storiesClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listener.onPastStorySelected(pastStories.get(position));
        }
    };

    /**
     * Show a progress dialog for getting stories
     * @param title Title displayed
     * @param message Message displayed
     * @param tag The tag for the dialog
     */
    private void showProgress(String title, String message, String tag) {
        progressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        progressDialogFragment.show(((MainMenuActivity)this.getActivity()).getSupportFragmentManager(), tag);
    }

    /**
     * Dismiss the dialog
     */
    private void dismissDialog() {
        if (progressDialogFragment != null) {
            progressDialogFragment.dismiss();
        }
    }
}