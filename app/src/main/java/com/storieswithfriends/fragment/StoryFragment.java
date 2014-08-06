package com.storieswithfriends.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.storieswithfriends.R;
import com.storieswithfriends.activity.MainMenuActivity;
import com.storieswithfriends.data.CurrentUser;
import com.storieswithfriends.data.Story;
import com.storieswithfriends.data.Word;
import com.storieswithfriends.http.RESTHelper;
import com.storieswithfriends.http.RandomWordService;
import com.storieswithfriends.http.StoriesService;
import com.storieswithfriends.util.StoryType;
import com.storieswithfriends.util.FlowLayout;
import com.storieswithfriends.util.StoryWordButton;
import com.storieswithfriends.util.Utils;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * @author David Horton
 */
public class StoryFragment extends Fragment {

    public interface StoryFragmentListener {
        public void returnToMain();
    }
    private StoryFragmentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (StoryFragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private Story story;
    private StoryType storyType;
    private ProgressDialogFragment progressDialogFragment;
    private FlowLayout layout;
    private EditText yourTurnWordEdit;
    private LinearLayout yourTurnButtons;
    private final static String LAST_WORD_IN_STORY = "the_end.";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_story, container, false);

        Bundle arguments = getArguments();
        String storyId = arguments.getString(MainMenuActivity.STORY_ID_BUNDLE_KEY);
        String storyTypeString = arguments.getString(MainMenuActivity.STORY_TYPE_BUNDLE_KEY);
        String storyTitle = arguments.getString(MainMenuActivity.STORY_TITLE_BUNDLE_KEY);

        ActionBar ab = ((android.support.v7.app.ActionBarActivity) this.getActivity()).getSupportActionBar();
        ab.setTitle(storyTitle);

        //Get the story type
        if("YOUR_TURN".equalsIgnoreCase(storyTypeString)) {
            storyType = StoryType.YOUR_TURN;
        }
        else if("PAST".equalsIgnoreCase(storyTypeString)) {
            storyType = StoryType.PAST;
        }
        else if("UNFINISHED".equalsIgnoreCase(storyTypeString)) {
            storyType = StoryType.UNFINISHED;
        }

        yourTurnButtons = (LinearLayout)view.findViewById(R.id.yourTurnButtonsContainer);

        //If it's anything besides a your turn scenario, we don't want to display the word submit buttons
        if(storyType != StoryType.YOUR_TURN) {
            yourTurnButtons.setVisibility(View.GONE);
        }

        layout = (FlowLayout)view.findViewById(R.id.flowLayout);

        Button submitButton = (Button)view.findViewById(R.id.btn_submit_word);
        submitButton.setOnClickListener(submitPressed);
        Button randomWordButton = (Button)view.findViewById(R.id.btn_generate_random_word);
        randomWordButton.setOnClickListener(randomWordPressed);
        Button finishStory = (Button)view.findViewById(R.id.btn_finish_story);
        finishStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yourTurnWordEdit.setText(LAST_WORD_IN_STORY);
            }
        });
        Button returnToMain = (Button)view.findViewById(R.id.btn_return_to_main);
        returnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.returnToMain();
            }
        });

        //Go fetch the story JSON
        getStory(true, storyId, storyTitle);

        return view;
    }



    @Override
    public void onPause() {
        super.onPause();

        // Dismiss any dialogs to prevent WindowLeaked exceptions
        dismissDialog();
    }

    private StoryWordButton generateBodyTextView(Word word) {
        StoryWordButton wordBtn = new StoryWordButton(this.getActivity(), word);
        wordBtn.setTextSize(15);
        wordBtn.setText(word.getWordValue());
        wordBtn.setOnClickListener(wordPressed);
        return wordBtn;
    }

    private View.OnClickListener submitPressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("STORIESWITHFRIENDS", "They pressed the submit word button.");

            // Reset errors.
            yourTurnWordEdit.setError(null);

            // Store values at the time of the post attempt.
            String newWord = yourTurnWordEdit.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check the validity of the field
            if (TextUtils.isEmpty(newWord)) {
                yourTurnWordEdit.setError(getString(R.string.error_field_required));
                focusView = yourTurnWordEdit;
                cancel = true;
            } else if (newWord.contains(" ")) {
                yourTurnWordEdit.setError("No spaces allowed");
                focusView = yourTurnWordEdit;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt the post and focus the first form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to post the word
                postNewWord(newWord);
            }
        }
    };

    private void postNewWord(final String word) {
        showProgress("Please wait", "Adding the new word...", "");

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithoutJsonResponse(this.getActivity().getBaseContext());

        StoriesService service = restAdapter.create(StoriesService.class);

        service.addWordToStory(story.getId(), word, CurrentUser.getCurrentLoggedInUser().getUsername(), new Callback<String>() {
            @Override
            public void success(String stringResponse, Response response) {
                Log.d("STORIESWITHFRIENDS", "Callback successful.");

                layout.removeAllViews();
                yourTurnButtons.setVisibility(View.GONE);

                if(LAST_WORD_IN_STORY.equals(word)) {
                    storyType = StoryType.PAST;
                }
                else {
                    storyType = StoryType.UNFINISHED;
                }

                getStory(false, story.getId(), story.getTitle());
            }

            @Override
            public void failure(RetrofitError error) {
                dismissDialog();
                Log.d("STORIESWITHFRIENDS", "Callback failed: " + error.toString());
                error.printStackTrace();
            }
        });
    }

    private View.OnClickListener randomWordPressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("STORIESWITHFRIENDS", "They pressed the random word button.");

            getRandomWord();
        }
    };

    private void getRandomWord() {
        RestAdapter restAdapter = RESTHelper.setupRestAdapterBuilderForRandomWord(this.getActivity().getBaseContext());

        RandomWordService service = restAdapter.create(RandomWordService.class);

        service.getRandomWord(new Callback<String>() {
            @Override
            public void success(String randomString, Response response) {
                Log.d("STORIESWITHFRIENDS", "Here is the random string: " + randomString);

                yourTurnWordEdit.setText(randomString.trim());
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }

    private View.OnClickListener wordPressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StoryWordButton wordButton = (StoryWordButton) view;
            showWordDialog(wordButton.getWord());
        }
    };

    private void showWordDialog(Word word) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setMessage(String.format("Word: %s\nWho added it: %s\nDate Added: %s", word.getWordValue(), word.getUserWhoAddedIt().getDisplayName(), word.getDateAdded()));
        builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Utils.centerDialogMessageAndShow(builder);
    }

    private void getStory(final boolean withDialog, String storyId, String storyTitle) {

        if(withDialog) {
            showProgress("Please wait", "Retrieving " + storyTitle + "...", "");
        }

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithJsonResponse(this.getActivity().getBaseContext(), null);

        StoriesService service = restAdapter.create(StoriesService.class);

        service.getStory(storyId, new Callback<Story>() {
            @Override
            public void success(Story returnedStory, Response response) {
                Log.d("STORIESWITHFRIENDS", "Callback successful.");

                story = returnedStory;

                for(int i = returnedStory.getWords().size() - 1; i >= 0; i--) {
                    layout.addView(generateBodyTextView(returnedStory.getWords().get(i)), 0);
                }

                //If the user clicked on it from "Your turn" stories or got to it from "Unfinished" and it is their turn, we do the same thing with it.
                if(storyType == StoryType.YOUR_TURN || CurrentUser.getCurrentLoggedInUser().getUsername().equals(story.getWhoseTurn().getUsername())) {
                    yourTurnButtons.setVisibility(View.VISIBLE);
                    addYourTurnEditText();
                }
                else if(storyType == StoryType.UNFINISHED) {
                    addUnfinishedText();
                }
                //TODO if it's all done, add some way to share it via Facebook?

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

    private void addYourTurnEditText() {
        yourTurnWordEdit = new EditText(this.getActivity());
        yourTurnWordEdit.setHint("New word goes here");
        layout.addView(yourTurnWordEdit);
    }

    private void addUnfinishedText() {
        Button unfinished = new Button(this.getActivity());
        unfinished.setTextSize(15);
        unfinished.setText("It's " + story.getWhoseTurn().getDisplayName() + "'s turn");
        unfinished.setBackgroundColor(getResources().getColor(R.color.button_background_pressed));
        layout.addView(unfinished);
    }

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
