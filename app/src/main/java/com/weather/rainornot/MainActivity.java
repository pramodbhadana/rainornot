package com.weather.rainornot;

import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLatitude = 0;
    private double mLongitude = 0;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String weatherKey = "***REMOVED***";
    private static final String googleApiKey = "***REMOVED***";
    private static final String weatherQueryURLPrefix = "https://api.darksky.net/forecast";
    private static final String googleApiURLPrefix = "";
    private String weatherQueryURL = null;
    private ActionBar mActionBar = null;
    private String address = null;

    private Object time;
    private Object summary;
    private Object icon;
    private Object temperature;
    private Object apparentTemperature;

    private TextView mTemperatureTextView;
    private TextView mApparentTemperatureTextView;
    private TextView mSummaryTextView;

    private ImageView mWeatherIconImageView;

    private LinearLayout mainLinearLayout;

    private LinearLayout mWeatherImageLinearLayout;

    //private LinearLayout linearLayoutHeaderProgress;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ImageView mPoweredByDarkSkyImageView;

    private LottieAnimationView lottieAnimationView;

    private static final int LOCATION_REQUEST_ID = 0;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isNetworkAvailable())
        {
            setContentView(R.layout.activity_no_network);
            lottieAnimationView = (LottieAnimationView) findViewById(R.id.animation_view);
            lottieAnimationView.setAnimation("rey_updated!.json");
            lottieAnimationView.playAnimation();
            return;
        }

        setContentView(R.layout.activity_main);

        mLatitudeTextView = (TextView)findViewById(R.id.lattitudeTextView);
        mLongitudeTextView = (TextView)findViewById(R.id.longitudeTextView);

        mLatitudeTextView.setVisibility(View.INVISIBLE);
        mLongitudeTextView.setVisibility(View.INVISIBLE);

        mTemperatureTextView = (TextView)findViewById(R.id.temperatureTextview);
        mApparentTemperatureTextView = (TextView)findViewById(R.id.apparentTemperatureTextView);
        mSummaryTextView = (TextView)findViewById(R.id.summaryTextView);

        mWeatherIconImageView = (ImageView) findViewById(R.id.weatherIconImageView);

        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        LogIt("onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchWeatherInformation();
                    }
                }
        );

        mPoweredByDarkSkyImageView = (ImageView)findViewById(R.id.poweredByDarkSkyImageView);

        mWeatherImageLinearLayout = (LinearLayout)findViewById(R.id.weatherImageLinearLayout);

        if(isNight())
        {
            // set light background for night
        }
        else
        {
            // set dark background for the day
        }

        if(mPoweredByDarkSkyImageView!=null) {
            mPoweredByDarkSkyImageView.setImageResource(R.drawable.powered_by_darksky_light_oneline);
        }



        //linearLayoutHeaderProgress = (LinearLayout) findViewById(R.id.linearLayoutHeaderProgress);

        mainLinearLayout.setBackgroundColor(new BigInteger("4fc3f7",32).intValue());

            if (checkPlayServices()) {
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();
                }
            }

        mWeatherImageLinearLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        YoYo.with(Techniques.Tada)
                                .duration(1000)
                                .playOn(v);
                    }
                }
        );

        mActionBar = getSupportActionBar();
        //hiding app name from the action bar
        if(mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(true);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case LOCATION_REQUEST_ID :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //permission granted
                    showToast("Permission Granted","short");
                    recreate();
                }
                else
                {
                    showToast("Permission Denied","short");

                    //launch application without location support
                    //insert code for selecting location manually by searching through predefined cities
                }
        }
    }
    @Override
    protected void onResume()
    {
        super.onResume();
    }
    @Override
    protected void onStart()
    {
        if(mGoogleApiClient != null)
        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop()
    {
        if(mGoogleApiClient != null)
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onConnected(Bundle connectionHint)
    {
        //showToast("in onConnected","long");
        if(checkPermission("android.permission.ACCESS_COARSE_LOCATION")) {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation != null)
            {
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();
                mLatitudeTextView.setText(String.valueOf(mLatitude));
                mLongitudeTextView.setText(String.valueOf(mLongitude));
                fetchWeatherInformation();
            }
            else
            {
                // location variable is null
                LogIt("Location Variable is NULL");
                showToast(getResources().getString(R.string.locationVariableIsNull),"long");
            }

        }
        else
        {
            LogIt("App does not have location permission");
            showToast(getResources().getString(R.string.noLocationPermission),"short");
            // permission to access location is not available
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,"android.permission.ACCESS_COARSE_LOCATION"))
            {
                //Showing message to user explaining the need for the permission

                MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                        .title("Need for Location Permission")
                        .content("Location Permission required just to show you your local weather Info")
                        .positiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                //ActivityCompat.requestPermissions(getParent(),new String[]{"android.permission.ACCESS_COARSE_LOCATION"},LOCATION_REQUEST_ID);
                                requestPermission(new String[]{"android.permission.ACCESS_COARSE_LOCATION"},LOCATION_REQUEST_ID);
                            }
                        })
                        .show();

            }
            else {
                //ActivityCompat.requestPermissions(this,new String[]{"android.permission.ACCESS_COARSE_LOCATION"},LOCATION_REQUEST_ID);
                requestPermission(new String[]{"android.permission.ACCESS_COARSE_LOCATION"},LOCATION_REQUEST_ID);
            }
        }
    }
    void requestPermission(String permission[], int requestId)
    {
        ActivityCompat.requestPermissions(this,permission,requestId);
    }
    @Override
    public void onConnectionSuspended(int i)
    {
        LogIt("in onConnectionSuspended");
        showToast("in onConnectionSuspended","long");
    }
    @Override
    public void onConnectionFailed(ConnectionResult mConnectionResult)
    {
        LogIt("in onConnectionFailed");
        showToast("in onConnectionFailed","long");
    }
    public Boolean checkPermission(String mPermission)
    {
        return ContextCompat.checkSelfPermission(this,mPermission)== PackageManager.PERMISSION_GRANTED;
    }

    /** showToast method displays the string passed to it, as the Toast on the target device.
     *
     * @param message it is the 'String' which is to be displayed as a Toast
     * @param duration it is a 'String' type. The value of this parameter decides the duration of the Toast
     *                 displayed to the user. It is 'short' and 'long'.
     */
    public void showToast(String message, String duration)
    {
        int messageDuration = Toast.LENGTH_SHORT;
        switch (duration)
        {
            case "short":
                messageDuration = Toast.LENGTH_SHORT;
                break;
            case "long":
                messageDuration = Toast.LENGTH_LONG;
        }
        Toast.makeText(this, message, messageDuration).show();
    }

    /** LogIt function logs the string message passed to it
     * as a parameter in the device logcat
     * @param messageToBeLogged It is the 'String' which is to be logged into the device logcat
     */
    public void LogIt(String messageToBeLogged)
    {

        Log.d(TAG,messageToBeLogged);
    }

    /** This function checks the avaialability of play services
     * on the target device
     * @author Pramod Bhadana
     * @return true if play services are available on the target device, false otherwise
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability mGoogleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = mGoogleApiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(mGoogleApiAvailability.isUserResolvableError(resultCode))
            {
                mGoogleApiAvailability.getErrorDialog(this,resultCode,9000).show();
            }
            else
            {
                // play services not supported on the device
                LogIt("Play services not supported on the device");
                showToast("Play services not supported on the device","long");

                // launch application without location support


                finish();
            }
            return false;
        }
        return true;
    }

    private void fetchWeatherInformation()
    {
        weatherQueryURL = weatherQueryURLPrefix+'/'+weatherKey+'/'+String.valueOf(mLatitude)+','+String.valueOf(mLongitude);
        try
        {
            new fetchWeatherInformationTask().execute(weatherQueryURL);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogIt("Exception cought");
        }
    }

    private class fetchWeatherInformationTask extends AsyncTask<String,Void,Void> {
        @Override
        protected void onPreExecute()
        {
            swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
            //linearLayoutHeaderProgress.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(String... weatherUrl)
        {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            String weatherInfo = connectionHandler.makeServiceCall(weatherUrl[0]); //considering only first Url
            Log.e(TAG,"weatherInfo is extracted : "+weatherInfo);
            if(weatherInfo != null) {
                try {
                    JSONObject weatherInfoObject = new JSONObject(weatherInfo);
                    JSONObject currentWeatherObject = weatherInfoObject.getJSONObject("currently");

                    // getting weather info for the current time

                    time = currentWeatherObject.get("time") ;
                    summary = currentWeatherObject.get("summary") ;
                    icon =currentWeatherObject.get("icon") ;

                    /*
                    nearestStormDistance = currentWeatherObject.get("nearestStormDistance") ;
                    precipIntensity = currentWeatherObject.get("precipIntensity") ;
                    precipIntensity = currentWeatherObject.get("precipIntensity") ;
                    precipProbability = currentWeatherObject.get("precipProbability");
                    precipType = currentWeatherObject.get("precipType");
                    precipType = currentWeatherObject.get("precipType");

                    */
                    temperature = currentWeatherObject.get("temperature");
                    apparentTemperature = currentWeatherObject.get("apparentTemperature");

                    /*
                    dewPoint = currentWeatherObject.get("dewPoint");
                    humidity = currentWeatherObject.get("humidity");
                    windSpeed = currentWeatherObject.get("windSpeed");
                    windBearing = currentWeatherObject.get("windBearing");
                    visibility = currentWeatherObject.get("visibility");
                    cloudCover = currentWeatherObject.get("cloudCover");
                    pressure = currentWeatherObject.get("pressure":);
                    ozone = currentWeatherObject.get("ozone");
                    */

                    String addressInternal = getAddress();
                    //showToast("here is the address "+address,"long");   **don't show toast from Async task. Context value won't be there
                    address = addressInternal;
                    LogIt(address);

                } catch (JSONException e) {
                    Log.e(TAG, "JSONException : ", e.getCause());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            else
            {
                Log.e(TAG,"Could not get Json from server");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Could not get weather Information from server", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            //linearLayoutHeaderProgress.setVisibility(View.INVISIBLE);
            updateUIElements();
            mActionBar.setTitle(address);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    void updateUIElements()
    {
        mTemperatureTextView.setText(temperature.toString());
        mApparentTemperatureTextView.setText(apparentTemperature.toString());
        mSummaryTextView.setText(summary.toString());
        int resId = getResources().getIdentifier(mapIconToDrawable(icon.toString()),"drawable",getPackageName());
        LogIt("Value of Resource Id : "+String.valueOf(resId));
        LogIt("Icon : "+icon.toString()+" drawable converted : "+mapIconToDrawable(icon.toString()));
        mWeatherIconImageView.setImageResource(resId);
        YoYo.with(Techniques.Tada)
                .duration(1000)
                .playOn(mWeatherIconImageView);
        showToast(address,"long");

    }

    String mapIconToDrawable(String icon)
    {
        String drawable = "clear";
        switch(icon) {
            case "clear-day":
            case "clear-night":
                drawable = "clear";
                break;
            case "rain":
                drawable = "rain";
                break;
            case "snow":
                drawable = "snow" + "_scattered";
                break;
            case "cloudy":
                drawable = "clouds";
                break;
        }
        if(isNight())
        {
            drawable = drawable+"_night";
        }
        else
        {
            drawable = drawable+"_day";
        }
        switch(icon)
        {
            case "sleet":
                drawable = "snow"+"_rain";
                break;
            case "wind":
                drawable = "wind";
                break;
            case "fog":
                drawable = "fog";
                break;
            case "partly-cloudy-day":
                drawable = "few_clouds_day";
                break;
            case "partly-cloudy-night":
                drawable = "few_clouds_night";
                break;
            default:
                break;
        }
        drawable = "weather_"+drawable;
        return drawable;
    }
    Boolean isNight()
    {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour < 6 || hour > 18;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.searchcityitem,menu);

        this.menu = menu;

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();

        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String query) {

                //loadHistory(query);

                return true;

            }
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return true;
            }

        });

    return true;

    }
    String getAddress()
    {
        StringBuilder result = new StringBuilder();
        try
        {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(mLatitude,mLongitude,1);
            if(addresses.size() > 0)
            {
                Address address = addresses.get(0);
                result.append(address.getLocality()).append(", ");
                result.append(address.getAdminArea()).append(", ");
                result.append(address.getCountryName());
            }
        }
        catch (IOException e)
        {
            LogIt("IOException :"+e.getMessage());
        }
        catch (IllegalArgumentException e)
        {

        }
        return result.toString();
    }
    boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}
