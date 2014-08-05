package com.storieswithfriends.activity;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.storieswithfriends.R;
import com.storieswithfriends.data.CurrentUser;
import com.storieswithfriends.data.User;
import com.storieswithfriends.fragment.MainMenuFragment;
import com.storieswithfriends.fragment.ProgressDialogFragment;
import com.storieswithfriends.http.RESTHelper;
import com.storieswithfriends.http.StoriesService;
import com.storieswithfriends.util.ErrorHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A login screen that offers login via email/password.

 */
public class LoginActivity extends ActionBarActivity implements LoaderCallbacks<Cursor>{

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressDialogFragment progressDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        /*mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });*/

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check the validity of the fields
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if(TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress("Please wait", "Logging in...", "");
            loginToTheApp(email, password);
        }
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                                                                     .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Dismiss any dialogs to prevent WindowLeaked exceptions
        dismissDialog();
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Does a post with the username and password and parses the response to get the authentication token.
     * @param username The username
     * @param password The password
     */
    private void loginToTheApp(String username, String password) {

        RestAdapter restAdapter = RESTHelper.setUpRestAdapterWithJsonResponse(this, null);

        StoriesService service = restAdapter.create(StoriesService.class);

        service.loginToTheApp(username, password, new Callback<User>() {
            @Override
            public void success(User returnedUser, Response response) {
                Log.d("STORIESWITHFRIENDS", "Callback was successful");

                if (returnedUser != null) {
                    onLoginTaskCompleted(true, returnedUser);
                } else {
                    onLoginTaskCompleted(false, null);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("STORIESWITHFRIENDS", "Callback failed: " + error.toString());
                error.printStackTrace();
                onLoginTaskCompleted(false, null);
            }
        });
    }

    /**
     * Either move on or display an error message, depending on if login was successful or not
     * @param successful Whether it was successful or not
     */
    private void onLoginTaskCompleted(final boolean successful, User user) {
        dismissDialog();
        mEmailView.setText("");
        mPasswordView.setText("");

        if(successful) {
            Log.d("STORIESWITHFRIENDS", "Successfully logged in.");

            CurrentUser.setCurrentLoggedInUser(user);

            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
            startActivity(intent);
        }
        else {
            Log.w("STORIESWITHFRIENDS", "There was a problem logging in.");

            ErrorHelper.showError(this, "Incorrect username or password");
        }
    }

    /**
     * Show a progress dialog for logging in.
     * @param title Title displayed
     * @param message Message displayed
     * @param tag The tag for the dialog
     */
    private void showProgress(String title, String message, String tag) {
        progressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        progressDialogFragment.show(getSupportFragmentManager(), tag);
    }

    /**
     * Dismiss the logging in dialog
     */
    private void dismissDialog() {
        if (progressDialogFragment != null) {
            progressDialogFragment.dismiss();
        }
    }
}



