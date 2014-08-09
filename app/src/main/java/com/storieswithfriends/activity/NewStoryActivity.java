package com.storieswithfriends.activity;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.storieswithfriends.R;
import com.storieswithfriends.data.CurrentUser;
import com.storieswithfriends.data.Story;
import com.storieswithfriends.data.User;
import com.storieswithfriends.data.Word;
import com.storieswithfriends.fragment.AddParticipantFragment;
import com.storieswithfriends.fragment.ProgressDialogFragment;
import com.storieswithfriends.http.RESTHelper;
import com.storieswithfriends.http.StoriesService;
import com.storieswithfriends.util.ErrorHelper;
import com.storieswithfriends.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NewStoryActivity extends ActionBarActivity implements AddParticipantFragment.AddParticipantFragmentListener {

    private ProgressDialogFragment progressDialogFragment;

    private EditText editTxtTitle;
    private EditText editTxtFirstWord;
    private ListView listParticipants;
    private ArrayList<User> storyParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_story);

        editTxtTitle = (EditText)findViewById(R.id.editText_title);
        editTxtFirstWord = (EditText)findViewById(R.id.editText_first_word);
        listParticipants = (ListView)findViewById(R.id.list_participants);
        Button submit = (Button)findViewById(R.id.btn_submit_new_story);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptNewStorySubmission();
            }
        });

        ActionBar ab = this.getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.app_name));

        storyParticipants = new ArrayList<User>();

        //TODO add a button to add story participants, register it here, and add a listener that calls addStoryParticipant

        //TODO add support for the storyParticipants list. Make it a drag and drop list.

    }

    @Override
    public void returnToNewStory(String newParticipantEmail) {
        getFragmentManager().popBackStack();

        storyParticipants.add(generateUser(newParticipantEmail));
    }

    private User generateUser(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }

    private void addStoryParticipant() {
        AddParticipantFragment addParticipantFragment = new AddParticipantFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, addParticipantFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void attemptNewStorySubmission() {
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
        } else if(TextUtils.isEmpty(firstWord)) {
            editTxtFirstWord.setError(getString(R.string.error_field_required));
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

    private void createNewStory(String title, String firstWord) {

        Story story = new Story();
        story.setTitle(title);
        User ownerToSend = CurrentUser.getCurrentLoggedInUser();
        ownerToSend.setOwner(true);
        story.setOwner(ownerToSend);

        //TODO get this from the listview?
        //story.setParticipants(new ArrayList<User>(Arrays.asList(generateTestUser("Betty Bets2"), generateTestUser("Betty Bets3"))));

        Word word1 = new Word();
        word1.setDateAdded(new Date().toString()); //TODO get a proper string representation of the Date?
        word1.setUserWhoAddedIt(ownerToSend);
        word1.setWordValue(firstWord);

        story.setWords(new ArrayList<Word>(Arrays.asList(word1)));

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithoutJsonResponseAndGSON(this, gson);

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
            Intent intent = new Intent(NewStoryActivity.this, MainMenuActivity.class);
            startActivity(intent);
        }
        else {
            ErrorHelper.showError(this, "There was a problem creating the new story. Please try again.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_story, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_logout:
                logoutPrompt();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.logout_message));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CurrentUser.setCurrentLoggedInUser(null);
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Utils.centerDialogMessageAndShow(builder);
    }

    /**
     * Show a progress dialog for submitting new story.
     * @param title Title displayed
     * @param message Message displayed
     * @param tag The tag for the dialog
     */
    private void showProgress(String title, String message, String tag) {
        progressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        progressDialogFragment.show(getSupportFragmentManager(), tag);
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
