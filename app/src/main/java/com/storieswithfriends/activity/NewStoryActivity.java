package com.storieswithfriends.activity;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.storieswithfriends.R;
import com.storieswithfriends.data.CurrentUser;
import com.storieswithfriends.fragment.AddParticipantFragment;
import com.storieswithfriends.fragment.MainMenuFragment;
import com.storieswithfriends.fragment.NewStoryFragment;
import com.storieswithfriends.util.ErrorHelper;
import com.storieswithfriends.util.Utils;

import java.util.ArrayList;

public class NewStoryActivity extends ActionBarActivity implements AddParticipantFragment.AddParticipantFragmentListener, NewStoryFragment.NewStoryFragmentListener {

    public final static String USERNAMES_BUNDLE_ID = "USERNAME";
    private final static String NEW_STORY_FRAGMENT_TAG = "NEW_STORY";
    private final static String ADD_PARTICIPANT_FRAGMENT_TAG = "ADD_PARTICIPANT";
    private ArrayList<String> storyParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_story);

        storyParticipants = new ArrayList<String>();

        if (savedInstanceState != null)
            return;

        //Create MainMenu fragment
        NewStoryFragment newStoryFragment = new NewStoryFragment();

        //Add the fragment to the FrameLayout
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainerNewStory, newStoryFragment, NEW_STORY_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void addParticipant(String username) {

        boolean validParticipant = true;
        String errorMessage = "";

        if(userAlreadyAdded(username)) {
            validParticipant = false;
            errorMessage = "User " + username + " already added.";
        }
        else if(username.equals(CurrentUser.getCurrentLoggedInUser().getUsername())) {
            validParticipant = false;
            errorMessage = "You can't add yourself as a participant.";
        }

        if(validParticipant) {
            storyParticipants.add(username);

            NewStoryFragment newStoryFragment = new NewStoryFragment();

            Bundle arguments = new Bundle();
            arguments.putStringArrayList(USERNAMES_BUNDLE_ID, storyParticipants);
            newStoryFragment.setArguments(arguments);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainerNewStory, newStoryFragment, NEW_STORY_FRAGMENT_TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else {
            ErrorHelper.showError(this, errorMessage);
        }
    }

    private boolean userAlreadyAdded(String enteredUsername) {
        boolean userAlreadyAdded = false;

        for(String username : storyParticipants) {
            if(username.equals(enteredUsername)) {
                userAlreadyAdded = true;
                break;
            }
        }

        return userAlreadyAdded;
    }

    @Override
    public void returnToMainMenu() {
        finish();

        //Intent intent = new Intent(NewStoryActivity.this, MainMenuActivity.class);
        //startActivity(intent);
    }

    @Override
    public void addStoryParticipant() {
        AddParticipantFragment addParticipantFragment = new AddParticipantFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerNewStory, addParticipantFragment, ADD_PARTICIPANT_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
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

    @Override
    public void onBackPressed() {
        //I have logic here so that it only does the logout prompt if they are on the main new story fragment
        NewStoryFragment newStoryFragmentCurrent = (NewStoryFragment)getFragmentManager().findFragmentByTag(NEW_STORY_FRAGMENT_TAG);

        if (newStoryFragmentCurrent != null && newStoryFragmentCurrent.isVisible()) {
            finish();
        }
        else {
            NewStoryFragment newStoryFragment = new NewStoryFragment();

            Bundle arguments = new Bundle();
            arguments.putStringArrayList(USERNAMES_BUNDLE_ID, storyParticipants);
            newStoryFragment.setArguments(arguments);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainerNewStory, newStoryFragment, NEW_STORY_FRAGMENT_TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void logoutPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.logout_message));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CurrentUser.setCurrentLoggedInUser(null);

                Intent intent = new Intent(NewStoryActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
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

}
