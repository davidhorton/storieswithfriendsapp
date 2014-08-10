package com.storieswithfriends.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.storieswithfriends.R;
import com.storieswithfriends.activity.NewStoryActivity;
import com.storieswithfriends.data.CurrentUser;
import com.storieswithfriends.data.Story;
import com.storieswithfriends.data.User;
import com.storieswithfriends.data.Word;
import com.storieswithfriends.http.RESTHelper;
import com.storieswithfriends.http.StoriesService;
import com.storieswithfriends.util.DragAndDropList;
import com.storieswithfriends.util.ErrorHelper;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author David Horton
 */
public class NewStoryFragment extends Fragment {

    public interface NewStoryFragmentListener {
        public void returnToMainMenu();
        public void addStoryParticipant();
    }

    private NewStoryFragmentListener listener;

    private ProgressDialogFragment progressDialogFragment;

    private EditText editTxtTitle;
    private EditText editTxtFirstWord;

    private DragAndDropList listParticipants;
    private ArrayList<User> storyParticipants;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (NewStoryFragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_story, container, false);

        editTxtTitle = (EditText)view.findViewById(R.id.editText_title);
        editTxtFirstWord = (EditText)view.findViewById(R.id.editText_first_word);
        Button submit = (Button)view.findViewById(R.id.btn_submit_new_story);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptNewStorySubmission();
            }
        });

        ActionBar ab = ((android.support.v7.app.ActionBarActivity) this.getActivity()).getSupportActionBar();
        ab.setTitle("Create New Story");

        ImageButton addParticipant = (ImageButton)view.findViewById(R.id.addParticipantButton);
        addParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.addStoryParticipant();
            }
        });

        storyParticipants = new ArrayList<User>();

        Bundle usernames = getArguments();

        if(usernames != null) {
            ArrayList<String> names = usernames.getStringArrayList(NewStoryActivity.USERNAMES_BUNDLE_ID);

            for(int i = 0; i < names.size(); i++) {
                User tempUser = generateUser(names.get(i));
                tempUser.setOrderPosition(i+2);
                storyParticipants.add(tempUser);
            }
        }

        ArrayAdapter<User> adapter = new ArrayAdapter<User>(this.getActivity().getBaseContext(), R.layout.story_line_item, storyParticipants);

        listParticipants = (DragAndDropList)view.findViewById(R.id.list_participants);
        listParticipants.setAdapter(adapter);
        listParticipants.setDropListener(mDropListener);

        TextView noItemsMessage = (TextView) view.findViewById(R.id.empty_participant_list_item);
        listParticipants.setEmptyView(noItemsMessage);

        return view;
    }

    private User generateUser(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }

    private void attemptNewStorySubmission() {

        if(storyParticipants.size() > 0) {

            // Reset errors.
            editTxtTitle.setError(null);
            editTxtFirstWord.setError(null);

            // Store values at the time of the login attempt.
            String title = editTxtTitle.getText().toString();
            String firstWord = editTxtFirstWord.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check the validity of the fields
            if (TextUtils.isEmpty(title)) {
                editTxtTitle.setError(getString(R.string.error_field_required));
                focusView = editTxtTitle;
                cancel = true;
            } else if (TextUtils.isEmpty(firstWord)) {
                editTxtFirstWord.setError(getString(R.string.error_field_required));
                focusView = editTxtFirstWord;
                cancel = true;
            } else if (firstWord.contains(" ")) {
                editTxtFirstWord.setError("No spaces allowed");
                focusView = editTxtFirstWord;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt submission and focus the first form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to perform submission.
                showProgress("Please wait", "Creating new story...", "");
                createNewStory(title, firstWord);
            }
        }
        else {
            ErrorHelper.showError(this.getActivity(), "You need at least one participant.");
        }
    }

    private void createNewStory(String title, String firstWord) {

        Story story = new Story();
        story.setTitle(title);
        User ownerToSend = CurrentUser.getCurrentLoggedInUser();
        ownerToSend.setOwner(true);
        story.setOwner(ownerToSend);

        story.setParticipants(storyParticipants);

        Word word1 = new Word();
        word1.setUserWhoAddedIt(ownerToSend);
        word1.setWordValue(firstWord);

        story.setWords(new ArrayList<Word>(Arrays.asList(word1)));

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithoutJsonResponseAndGSON(this.getActivity(), gson);

        StoriesService service = restAdapter.create(StoriesService.class);

        service.createNewStory(story, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Log.d("STORIESWITHFRIENDS", "Callback was successful");
                onNewStoryTaskCompleted(true);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("STORIESWITHFRIENDS", "Callback failed: " + retrofitError.toString());
                retrofitError.printStackTrace();
                onNewStoryTaskCompleted(false);
            }
        });
    }

    public void onNewStoryTaskCompleted(boolean successful) {

        dismissDialog();

        if(successful) {
            listener.returnToMainMenu();
        }
        else {
            ErrorHelper.showError(this.getActivity(), "There was a problem creating the new story. Please try again.");
        }
    }

    private DragAndDropList.DropListener mDropListener = new DragAndDropList.DropListener() {

        public void drop(int from, int to) {

            //Pop out the user getting moved
            User target = storyParticipants.remove(from);

            //Insert the user into the new position
            storyParticipants.add(to, target);

            //Resort the order positions of the participants
            resortParticipants();

            ((BaseAdapter) listParticipants.getAdapter()).notifyDataSetChanged();

        }
    };

    private void resortParticipants() {
        Log.d("STORIESWITHFRIENDS","Sorting participants. There are currently " + storyParticipants.size());

        for(int i = 0; i < storyParticipants.size(); i++) {
            storyParticipants.get(i).setOrderPosition(i + 2);
        }
    }

    /**
     * Show a progress dialog for submitting new story.
     * @param title Title displayed
     * @param message Message displayed
     * @param tag The tag for the dialog
     */
    private void showProgress(String title, String message, String tag) {
        progressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        progressDialogFragment.show(((NewStoryActivity)this.getActivity()).getSupportFragmentManager(), tag);
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
