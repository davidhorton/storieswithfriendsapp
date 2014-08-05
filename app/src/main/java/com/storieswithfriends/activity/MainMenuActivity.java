package com.storieswithfriends.activity;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.storieswithfriends.R;
import com.storieswithfriends.data.CurrentUser;
import com.storieswithfriends.data.StorySummary;
import com.storieswithfriends.fragment.MainMenuFragment;
import com.storieswithfriends.fragment.PastStoriesFragment;
import com.storieswithfriends.fragment.StoryFragment;
import com.storieswithfriends.fragment.UnfinishedStoriesFragment;
import com.storieswithfriends.util.StoryType;
import com.storieswithfriends.util.Utils;

/**
 * @author David Horton
 */
public class MainMenuActivity extends ActionBarActivity
        implements MainMenuFragment.MainMenuFragmentListener,
        UnfinishedStoriesFragment.UnfinishedStoriesFragmentListener,
        PastStoriesFragment.PastStoriesFragmentListener {

    public final static String STORY_TYPE_BUNDLE_KEY = "STORY_TYPE";
    public final static String STORY_ID_BUNDLE_KEY = "STORY_ID";
    public final static String STORY_TITLE_BUNDLE_KEY = "STORY_TITLE";

    private final static String MAIN_MENU_FRAGMENT_TAG = "MAIN";
    private final static String UNFINISHED_STORIES_FRAGMENT_TAG = "UNFINISHED";
    private final static String PAST_STORIES_FRAGMENT_TAG = "PAST";
    private final static String STORY_MENU_FRAGMENT_TAG = "STORY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
            return;

        //Create CourseListFragment
        MainMenuFragment mainMenuFragment = new MainMenuFragment();

        //Add the fragment to the FrameLayout
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, mainMenuFragment, MAIN_MENU_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_menu_screen, menu);

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
        //I have logic here so that it only does the logout prompt if they are on the main menu.
        MainMenuFragment mainMenuFragment = (MainMenuFragment)getFragmentManager().findFragmentByTag(MAIN_MENU_FRAGMENT_TAG);
        //PastStoriesFragment pastStoriesFragment = (PastStoriesFragment)getFragmentManager().findFragmentByTag(PAST_STORIES_FRAGMENT_TAG);
        //UnfinishedStoriesFragment unfinishedStoriesFragment = (UnfinishedStoriesFragment)getFragmentManager().findFragmentByTag(UNFINISHED_STORIES_FRAGMENT_TAG);

        if (mainMenuFragment!= null && mainMenuFragment.isVisible()) {
            logoutPrompt();
        }
        else {
            getFragmentManager().popBackStack();
        }
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

    @Override
    public void onUnfinishedStoriesSelected() {
        Log.d("STORIESWITHFRIENDS","They selected to get unfinished stories");
        UnfinishedStoriesFragment unfinishedStoriesFragment = new UnfinishedStoriesFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, unfinishedStoriesFragment, UNFINISHED_STORIES_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onPastStoriesSelected() {
        Log.d("STORIESWITHFRIENDS","They selected to get past stories");
        PastStoriesFragment pastStoriesFragment = new PastStoriesFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, pastStoriesFragment, PAST_STORIES_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onUnfinishedStorySelected(StorySummary selectedStory) {
        Log.d("STORIESWITHFRIENDS", "They selected the unfinished story " + selectedStory.getTitle());

        launchStoryFragment(StoryType.UNFINISHED, selectedStory.getId(), selectedStory.getTitle());
    }

    @Override
    public void onPastStorySelected(StorySummary selectedStory) {
        Log.d("STORIESWITHFRIENDS", "They selected the past story " + selectedStory.getTitle());

        launchStoryFragment(StoryType.PAST, selectedStory.getId(), selectedStory.getTitle());
    }

    @Override
    public void onYourTurnStorySelected(StorySummary selectedStory) {
        Log.d("STORIESWITHFRIENDS", "They selected the your-turn story " + selectedStory.getTitle());

        launchStoryFragment(StoryType.YOUR_TURN, selectedStory.getId(), selectedStory.getTitle());
    }


    private void launchStoryFragment(StoryType storyType, String storyId, String storyTitle) {
        StoryFragment storyFragment = new StoryFragment();

        Bundle arguments = new Bundle();
        arguments.putString(STORY_TYPE_BUNDLE_KEY, storyType.toString());
        arguments.putString(STORY_ID_BUNDLE_KEY, storyId);
        arguments.putString(STORY_TITLE_BUNDLE_KEY, storyTitle);
        storyFragment.setArguments(arguments);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, storyFragment, STORY_MENU_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
