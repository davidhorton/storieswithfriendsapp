package com.storieswithfriends.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.storieswithfriends.R;
import com.storieswithfriends.fragment.ProgressDialogFragment;
import com.storieswithfriends.http.RESTHelper;
import com.storieswithfriends.http.StoriesService;
import com.storieswithfriends.util.ErrorHelper;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NewUserActivity extends ActionBarActivity {

    private EditText editTextEmail;
    private EditText editTextDisplayName;
    private EditText editTextPassword1;
    private EditText editTextPassword2;
    private ProgressDialogFragment progressDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        ActionBar ab = this.getSupportActionBar();
        ab.setTitle("Create New Account");

        editTextEmail = (EditText)findViewById(R.id.editText_email);
        editTextDisplayName = (EditText)findViewById(R.id.editText_display_name);
        editTextPassword1 = (EditText)findViewById(R.id.editText_password1);
        editTextPassword2 = (EditText)findViewById(R.id.editText_password2);
        Button submit = (Button)findViewById(R.id.btn_submit_new_user);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSubmission();
            }
        });

        Bundle bundle = getIntent().getExtras();
        String emailEnteredPreviously = bundle.getString(LoginActivity.EMAIL_INTENT_EXTRA_KEY);
        editTextEmail.setText(emailEnteredPreviously);
    }

    private void attemptSubmission() {
        // Reset errors.
        editTextEmail.setError(null);
        editTextDisplayName.setError(null);
        editTextPassword1.setError(null);
        editTextPassword2.setError(null);

        // Store values at the time of the login attempt.
        String email = editTextEmail.getText().toString();
        String displayName = editTextDisplayName.getText().toString();
        String password1 = editTextPassword1.getText().toString();
        String password2 = editTextPassword2.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check the validity of the fields
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.error_field_required));
            focusView = editTextEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            editTextEmail.setError(getString(R.string.error_invalid_email));
            focusView = editTextEmail;
            cancel = true;
        } else if(TextUtils.isEmpty(displayName)) {
            editTextDisplayName.setError(getString(R.string.error_field_required));
            focusView = editTextDisplayName;
            cancel = true;
        } else if(TextUtils.isEmpty(password1)) {
            editTextPassword1.setError(getString(R.string.error_field_required));
            focusView = editTextPassword1;
            cancel = true;
        } else if(TextUtils.isEmpty(password2)) {
            editTextPassword2.setError(getString(R.string.error_field_required));
            focusView = editTextPassword2;
            cancel = true;
        } else if(!password1.equals(password2)) {
            editTextPassword1.setError("Passwords don't match.");
            focusView = editTextPassword1;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt submission and focus the first form field with an error.
            editTextPassword1.setText("");
            editTextPassword2.setText("");
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform submission.
            showProgress("Please wait", "Creating new account...", "");
            createNewUSer(email, displayName, password1);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private void createNewUSer(String username, String displayName, String password) {

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithoutJsonResponse(this);

        StoriesService service = restAdapter.create(StoriesService.class);

        service.createNewUser(username, displayName, password, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Log.d("STORIESWITHFRIENDS", "Callback was successful");
                onNewUserTaskCompleted(true);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("STORIESWITHFRIENDS", "Callback failed: " + error.toString());
                error.printStackTrace();
                onNewUserTaskCompleted(false);
            }
        });
    }

    public void onNewUserTaskCompleted(boolean successful) {

        dismissDialog();

        if(successful) {
            Intent intent = new Intent(NewUserActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        else {
            ErrorHelper.showError(this, "There was a problem creating the new account. Please try again.");
        }
    }

    /**
     * Show a progress dialog for creating the user.
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
