package com.storieswithfriends.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.storieswithfriends.R;
import com.storieswithfriends.activity.MainMenuActivity;
import com.storieswithfriends.http.RESTHelper;
import com.storieswithfriends.http.StoriesService;
import com.storieswithfriends.util.ErrorHelper;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author David Horton
 */
public class AddParticipantFragment extends Fragment {

    public interface AddParticipantFragmentListener {
        public void returnToNewStory(String newParticipantEmail);
    }

    private AddParticipantFragmentListener listener;

    private ProgressDialogFragment progressDialogFragment;
    private EditText editTextParticipantEmail;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (AddParticipantFragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_participant, container, false);

        ActionBar ab = ((android.support.v7.app.ActionBarActivity) this.getActivity()).getSupportActionBar();
        ab.setTitle("Add Participant");

        editTextParticipantEmail = (EditText)view.findViewById(R.id.editText_participant_email);
        Button submitNewParticipant = (Button)view.findViewById(R.id.btn_submit_new_story_participant);
        submitNewParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddParticipantSubmission();
            }
        });

        return view;
    }

    private void attemptAddParticipantSubmission() {
        // Reset errors.
        editTextParticipantEmail.setError(null);

        // Store values at the time of the login attempt.
        String email = editTextParticipantEmail.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check the validity of the fields
        if (TextUtils.isEmpty(email)) {
            editTextParticipantEmail.setError(getString(R.string.error_field_required));
            focusView = editTextParticipantEmail;
            cancel = true;
        } else if(!isEmailValid(email)) {
            editTextParticipantEmail.setError(getString(R.string.error_invalid_email));
            focusView = editTextParticipantEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt submission and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform submission.
            showProgress("Please wait", "Making sure that user exists...", "");
            verifyUsernameExists(email);
        }
    }

    private void verifyUsernameExists(final String email) {

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithoutJsonResponse(this.getActivity());

        StoriesService service = restAdapter.create(StoriesService.class);

        service.usernameExists(email, new Callback<String>() {
            @Override
            public void success(String result, Response response) {
                Log.d("STORIESWITHFRIENDS", "Callback was successful");

                if(result.contains("true")) {
                    onVerifyUsernameExistsComplete(email, true, true);
                }
                else if (result.contains("false")) {
                    onVerifyUsernameExistsComplete(email, true, false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("STORIESWITHFRIENDS", "Callback failed: " + error.toString());
                error.printStackTrace();
                onVerifyUsernameExistsComplete(email, false, false);
            }
        });
    }

    private void onVerifyUsernameExistsComplete(String username, boolean successful, boolean exists) {
        dismissDialog();

        if(successful) {
            if(exists) {
                listener.returnToNewStory(username);
            }
            else {
                ErrorHelper.showError(this.getActivity(), "User " + username + " doesn't exist.");
            }
        }
        else {
            ErrorHelper.showError(this.getActivity(), "Unable to add the participant because there was a problem making sure that username exists. Please try again.");
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    @Override
    public void onPause() {
        super.onPause();

        // Dismiss any dialogs to prevent WindowLeaked exceptions
        dismissDialog();
    }

    /**
     * Show a progress dialog for making sure the new participant exists
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
