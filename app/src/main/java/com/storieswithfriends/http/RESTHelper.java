package com.storieswithfriends.http;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.storieswithfriends.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * @author David Horton
 */
public class RESTHelper {

    /**
     * Sets up the RestAdapter, with a JSON response
     * @param context Android context
     * @param gson GSON that will be used to convert the data of the request to JSON
     * @return RestAdapter
     */
    public static RestAdapter setUpRestAdapterWithJsonResponse(Context context, Gson gson) {
        RestAdapter.Builder builder = setUpRestAdapterBuilder(context);

        if (gson != null)
            builder.setConverter(new GsonConverter(gson));

        return builder.build();
    }

    /**
     * Sets up the RestAdapter, with a JSON response
     * @param context Android context
     * @return RestAdapter
     */
    public static RestAdapter setUpRestAdapterWithoutJsonResponse(Context context) {
        RestAdapter.Builder builder = setUpRestAdapterBuilder(context);

        builder.setConverter(new Converter() {
            @Override
            public Object fromBody(TypedInput body, Type type) throws ConversionException {
                try {
                    return convertStreamToString(body.in());
                }
                catch(IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public TypedOutput toBody(Object object) {
                return null;
            }
        });

        return builder.build();
    }

    /**
     * Sets up the RestAdapter, with a JSON response
     * @param context Android context
     * @param gson GSON that will be used to convert the data of the request to JSON
     * @return RestAdapter
     */
    public static RestAdapter setUpRestAdapterWithoutJsonResponseAndGSON(Context context, Gson gson) {
        RestAdapter.Builder builder = setUpRestAdapterBuilder(context);

        GsonConverter converter = new GsonConverter(gson){
            @Override
            public Object fromBody(TypedInput body, Type type) throws ConversionException {
                try {
                    return RESTHelper.convertStreamToString(body.in());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return "COULDN'T PARSE SERVER RESPONSE.";
            }
        };

        builder.setConverter(converter);

        return builder.build();
    }

    /**
     * Sets up the RestAdapter Builder
     * @param context Android Context
     * @return RestAdapter Builder
     */
    private static RestAdapter.Builder setUpRestAdapterBuilder(Context context) {
        OkHttpClient client = new OkHttpClient();

        OkClient okClient = new OkClient(client);

        return new RestAdapter.Builder()
                .setClient(okClient)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(context.getResources().getString(R.string.api_endpoint))
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String msg) {
                        Log.i("STORIESWITHFRIENDS", msg);
                    }
                });
    }

    /**
     * Sets up the RestAdapter Builder for the random word generator
     * @param context Android Context
     * @return RestAdapter Builder
     */
    public static RestAdapter setupRestAdapterBuilderForRandomWord(Context context) {

        OkHttpClient client = new OkHttpClient();
        OkClient okClient = new OkClient(client);

        RestAdapter.Builder builder = new RestAdapter.Builder().setClient(okClient).setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(context.getResources().getString(R.string.api_endpoint_randomword))
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String msg) {
                        Log.i("STORIESWITHFRIENDS", msg);
                    }
                });

        builder.setConverter(new Converter() {
            @Override
            public Object fromBody(TypedInput body, Type type) throws ConversionException {
                try {
                    return convertStreamToString(body.in());
                }
                catch(IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public TypedOutput toBody(Object object) {
                return null;
            }
        });

        return builder.build();
    }

    /**
     * Converts an InputStream to a String
     * @param is - InputStream
     * @return - String response
     */
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
