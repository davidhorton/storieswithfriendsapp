package com.storieswithfriends.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.TextView;

import com.storieswithfriends.R;

/**
 * Created by student on 8/4/14.
 */
public class Utils {

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static void showNoInternetDialog(final Activity activity, boolean finishActivityAfterDismiss) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.no_connection));
        builder.setMessage(activity.getString(R.string.error_internet_connection));
        if (finishActivityAfterDismiss) {
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    activity.finish();
                }
            });
        }
        centerDialogMessageAndShow(builder);
    }

    public static void centerDialogMessageAndShow(AlertDialog.Builder builder) {
        Dialog dialog = builder.show();
        TextView textView = (TextView)dialog.findViewById(android.R.id.message);
        textView.setGravity(Gravity.CENTER);
        dialog.show();
    }
}
