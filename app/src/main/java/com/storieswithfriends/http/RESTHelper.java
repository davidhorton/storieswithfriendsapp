package com.storieswithfriends.http;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.storieswithfriends.R;

import java.lang.reflect.Type;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by student on 8/4/14.
 */
public class RESTHelper {

    /**
     * Sets up the RestAdapter, with a JSON response
     * @param context Android context
     * @param gson GSON that will be used to convert the data of the request to JSON
     * @return RestAdapter
     */
    public static RestAdapter setUpRestAdapterWithJsonResponse(Context context, Gson gson)
    {
        RestAdapter.Builder builder = setUpRestAdapterBuilder(context);

        if (gson != null)
            builder.setConverter(new GsonConverter(gson));

        return builder.build();
    }

    /**
     * Sets up the RestAdapter, with a JSON response
     * @param context Android context
     * @param gson GSON that will be used to convert the data of the request to JSON
     * @return RestAdapter
     */
    public static RestAdapter setUpRestAdapterWithoutJsonResponse(Context context, Gson gson)
    {
        RestAdapter.Builder builder = setUpRestAdapterBuilder(context);

        if (gson != null)
            builder.setConverter(new GsonConverter(gson));

        return builder.build();
    }

    /**
     * Sets up the RestAdapter Builder
     * @param context Android Context
     * @return RestAdapter Builder
     */
    private static RestAdapter.Builder setUpRestAdapterBuilder(Context context)
    {
        OkHttpClient client = new OkHttpClient();

        // This setHostnameVerifier line removes hostname verification!
        // Remove when in the production environment!
        /*client.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });*/

        OkClient okClient = new OkClient(client);

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setClient(okClient)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(context.getResources().getString(R.string.api_endpoint))
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String msg) {
                        Log.i("STORIESWITHFRIENDS", msg);
                    }
                });

        return builder;
    }
}
