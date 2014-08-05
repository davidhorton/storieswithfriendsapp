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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.storieswithfriends.R;
import com.storieswithfriends.activity.MainMenuActivity;
import com.storieswithfriends.data.CurrentUser;
import com.storieswithfriends.data.StorySummary;
import com.storieswithfriends.data.User;
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
public class MainMenuFragment extends Fragment {

    public interface MainMenuFragmentListener {
        public void onYourTurnStorySelected(StorySummary selectedStory);
        public void onUnfinishedStoriesSelected();
        public void onPastStoriesSelected();
    }
    private MainMenuFragmentListener listener;

    private ListView yourTurnStoriesListView;
    private TextView title;

    private ArrayAdapter<StorySummary> adapter;
    private ArrayList<StorySummary> stories;
    private ProgressDialogFragment progressDialogFragment;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (MainMenuFragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);

        ActionBar ab = ((android.support.v7.app.ActionBarActivity) this.getActivity()).getSupportActionBar();
        ab.setTitle("Stories With Friends");

        yourTurnStoriesListView = (ListView) rootView.findViewById(R.id.listView_yourturnstories);
        title = (TextView) rootView.findViewById(R.id.txt_mainMenuTitle);
        TextView noItemsMessage = (TextView) rootView.findViewById(R.id.empty_yourturn_list_item);
        yourTurnStoriesListView.setEmptyView(noItemsMessage);

        stories = new ArrayList<StorySummary>();
        adapter = new ArrayAdapter<StorySummary>(this.getActivity().getBaseContext(), R.layout.story_line_item, stories);
        yourTurnStoriesListView.setAdapter(adapter);

        Button unfinishedStories = (Button) rootView.findViewById(R.id.btn_unfinishedstories);
        unfinishedStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onUnfinishedStoriesSelected();
            }
        });
        Button pastStories = (Button) rootView.findViewById(R.id.btn_paststories);
        pastStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPastStoriesSelected();
            }
        });

        User loggedInUser = CurrentUser.getCurrentLoggedInUser();
        if(loggedInUser != null) {
            if (loggedInUser.getDisplayName() != null & !"".equals(loggedInUser.getDisplayName())) {
                title.setText("Welcome, " + loggedInUser.getDisplayName() + "!");
            } else {
                title.setText("Welcome, " + loggedInUser.getUsername() + "!");
            }

            getStories();
        }
        else {
            title.setText("Welcome!");
        }

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Dismiss any dialogs to prevent WindowLeaked exceptions
        dismissDialog();
    }

    private void getStories() {

        showProgress("Please wait", "Getting it's-your-turn stories...", "");

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithJsonResponse(this.getActivity().getBaseContext(), null);

        StoriesService service = restAdapter.create(StoriesService.class);

        service.getYourTurnStories(CurrentUser.getCurrentLoggedInUser().getUsername(), new Callback<ArrayList<StorySummary>>() {
            @Override
            public void success(ArrayList<StorySummary> storySummaries, Response response) {
                stories.addAll(storySummaries);
                adapter.notifyDataSetChanged();

                yourTurnStoriesListView.setOnItemClickListener(yourTurnStoriesClickListener);

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
    private final AdapterView.OnItemClickListener yourTurnStoriesClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listener.onYourTurnStorySelected(stories.get(position));
        } // end method onItemClick
    };

    /**
     * Show a progress dialog for getting the it's your turn stories
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
