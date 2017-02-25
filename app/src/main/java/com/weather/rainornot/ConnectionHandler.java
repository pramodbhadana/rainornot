package com.weather.rainornot;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by pramodbhadana on 11/12/16.
 */

class ConnectionHandler {
    private static final String TAG = ConnectionHandler.class.getSimpleName();
    public String makeServiceCall(String requestedUrl)
    {
        String responseFromWeatherServer = null;
        try {
            URL url = new URL(requestedUrl);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestMethod("GET");
            InputStream inputStream = new BufferedInputStream(httpsURLConnection.getInputStream());
            responseFromWeatherServer = convertStreamToString(inputStream);
        }
        catch (MalformedURLException e)
        {
            Log.e(TAG, "MalformedUrlException : ",e.getCause());
        }
        catch (ProtocolException e)
        {
            Log.e(TAG, "ProtocolException : ",e.getCause());
        }
        catch (IOException e)
        {
            Log.e(TAG, "IOException : ",e.getCause());
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception : ",e.getCause());
        }
        return responseFromWeatherServer;
    }
    private String convertStreamToString(InputStream inputStream)
    {
        String returnResponse = "";
        try {
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            returnResponse = scanner.hasNext() ? scanner.next() : "";
        }
        finally
        {
            try {
                inputStream.close();
            }
            catch (IOException e)
            {
                Log.e(TAG,"IOException : ",e.getCause());
            }
        }
        return returnResponse;
    }
}
