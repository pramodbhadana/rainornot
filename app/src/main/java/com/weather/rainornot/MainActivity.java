package com.weather.rainornot;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.weather.rainornot.utils.CityInfoDbHelper;
import com.weather.rainornot.utils.CityInfoHelper;
import com.weather.rainornot.utils.SharedPreferenceUtil;
import com.weather.rainornot.utils.weatherSIUnits;
import com.weather.rainornot.utils.CityInfoContract.CityEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final float DISTANCE_BETWEEN_CITIES = 10000;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLatitude = 0;
    private double mLongitude = 0;
    private LatLng mLatLng;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String weatherKey = BuildConfig.DARKSKY_API_KEY;
    private static final String weatherQueryURLPrefix = "https://api.darksky.net/forecast";
    private String weatherQueryURL = null;
    private ActionBar mActionBar = null;
    private String address = null;
    private boolean jsonError = false;

    private Object time;
    private Object summary;
    private Object icon;
    private Object temperature;
    private Object apparentTemperature;

    private customFontTextView mUserLocationTextView;
    private customFontTextView mTemperatureTextView;
    private customFontTextView mApparentTemperatureTextView;
    private customFontTextView mSummaryTextView;

    private ImageView mWeatherIconImageView;

    //private LinearLayout mainLinearLayout;
    private LinearLayout mWeatherImageLinearLayout;

    private SwipeRefreshLayout swipeRefreshLayout;

    private LottieAnimationView lottieAnimationView;

    private Toolbar toolbar;

    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private static final int LOCATION_REQUEST_ID = 0;

    private Menu menu;

    private int i;

    private NavigationView navigationView;
    private CityInfoDbHelper mCityInfoDbHelper;
    private List<CityInfoHelper> cityInfoHelperList;
    private SharedPreferenceUtil sharedPreferenceUtil;

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

        mUserLocationTextView = (customFontTextView)findViewById(R.id.userLocationTextView);

        mTemperatureTextView = (customFontTextView)findViewById(R.id.temperatureTextView);
        mApparentTemperatureTextView = (customFontTextView)findViewById(R.id.apparentTemperatureTextView);
        mSummaryTextView = (customFontTextView)findViewById(R.id.summaryTextView);

        mWeatherIconImageView = (ImageView) findViewById(R.id.weatherIconImageView);

        //mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.app_name,R.string.app_name);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        if(sharedPreferenceUtil == null)
        {
            sharedPreferenceUtil = SharedPreferenceUtil.getInstance(getApplicationContext());
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                setMenuItemChecked(item.getItemId());
                sharedPreferenceUtil.setLastPlace(item.getItemId());
                drawerLayout.closeDrawers();
                LogIt("Item Id is :"+item.getItemId() + " total items in cityHelperList : "+cityInfoHelperList.size());
                LogIt("Here is the cityInfoList");
                for(int i=0;i<cityInfoHelperList.size();i++)
                {
                    LogIt("Index : "+i+" "+cityInfoHelperList.get(i).getCityName());
                }
                mLatitude = cityInfoHelperList.get(item.getItemId()).getLatitude();
                mLongitude = cityInfoHelperList.get(item.getItemId()).getLongitude();
                address = getAddress(mLatitude,mLongitude);
                fetchWeatherInformation();
                return false;
            }
        });

        mCityInfoDbHelper = new CityInfoDbHelper(getApplicationContext());

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        LogIt("onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        address = getAddress(mLatitude,mLongitude);
                        fetchWeatherInformation();
                    }
                }
        );

        mWeatherImageLinearLayout = (LinearLayout)findViewById(R.id.weatherImageLinearLayout);

        if(isNight())
        {
            // set light background for night
        }
        else
        {
            // set dark background for the day
        }

        //mainLinearLayout.setBackgroundColor(new BigInteger("4fc3f7",32).intValue());

        //mainLinearLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

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

        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();
        //hiding app name from the action bar
        if(mActionBar != null) {
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                    callPlaceSearchIntent();
                }
            });
        }

        cityInfoHelperList = new ArrayList<CityInfoHelper>();

        updateCitiesInDrawer(true);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        int widthFloatingButton = floatingActionButton.getLayoutParams().width;

        ImageView imageViewForDarkSky = (ImageView) findViewById(R.id.powered_by_dark_sky);
        imageViewForDarkSky.getLayoutParams().width = width - widthFloatingButton - 30;

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(actionBarDrawerToggle != null)
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                    showToast("Permission Denied, Add a place manually","short");
                    int lastPlace = sharedPreferenceUtil.getLastPlace();
                    mLatitude = cityInfoHelperList.get(lastPlace).getLatitude();
                    mLongitude = cityInfoHelperList.get(lastPlace).getLongitude();
                    address = getAddress(mLatitude,mLongitude);
                    setMenuItemChecked(lastPlace);
                    fetchWeatherInformation();
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
                address = getAddress(mLatitude,mLongitude);
                if(nearestPlace(mLatitude,mLongitude) == -1) {
                    updateDatabaseAndDrawer();
                }
                fetchWeatherInformation();
                setMenuItemChecked(nearestPlace(mLatitude,mLongitude));
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
        if(messageToBeLogged != null)
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
        }
        @Override
        protected Void doInBackground(String... weatherUrl)
        {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            String weatherInfo = connectionHandler.makeServiceCall(weatherUrl[0]); //considering only first Url
            //Log.e(TAG,"weatherInfo is extracted : "+weatherInfo);
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


                    //cityList[i++] = address;
                    //LogIt(address);

                } catch (JSONException e) {
                    jsonError = true;
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
                jsonError =true;
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
            if(!jsonError) {
                updateUIElements();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    void updateUIElements()
    {
        mTemperatureTextView.setText(temperature.toString()+" "+ weatherSIUnits.temperatureSIUnit);
        mApparentTemperatureTextView.setText("Feels like "+apparentTemperature.toString()+" "+weatherSIUnits.temperatureSIUnit);
        mSummaryTextView.setText(summary.toString());
        int resId = getResources().getIdentifier(mapIconToDrawable(icon.toString()),"drawable",getPackageName());
        LogIt("Value of Resource Id : "+String.valueOf(resId));
        LogIt("Icon : "+icon.toString()+" drawable converted : "+mapIconToDrawable(icon.toString()));
        mWeatherIconImageView.setImageResource(resId);
        mUserLocationTextView.setText(address);
        YoYo.with(Techniques.Tada)
                .duration(1000)
                .playOn(mWeatherIconImageView);

        YoYo.with(Techniques.Swing)
                .duration(1000)
                .playOn(mTemperatureTextView);

        YoYo.with(Techniques.Tada)
                .duration(1000)
                .playOn(mApparentTemperatureTextView);

        YoYo.with(Techniques.Tada)
                .duration(1000)
                .playOn(mSummaryTextView);

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

    String getAddress(Double latitude, Double longitude)
    {
        StringBuilder result = new StringBuilder();
        try
        {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            if(addresses.size() > 0)
            {
                Address address = addresses.get(0);
                if(address.getLocality() != null)
                    result.append(address.getLocality()).append(", ");
                if(address.getAdminArea() != null)
                    result.append(address.getAdminArea()).append(", ");
                if(address.getCountryName() != null)
                    result.append(address.getCountryName());
            }
        }
        catch (IOException e)
        {
            LogIt("Fetching Address, IOException :"+e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            LogIt("Fetching Address, :"+e.getMessage());
        }
        LogIt("Address Fetched :"+result);
        return result.toString();
    }

    boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
    private void callPlaceSearchIntent() {
        try {
            AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(autocompleteFilter)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //autocompleteFragment.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                mLatLng = place.getLatLng();
                Log.i(TAG, "Place:" + place.toString());
                mLatitude = mLatLng.latitude;
                mLongitude = mLatLng.longitude;
                int lastPlace = cityInfoHelperList.size();
                sharedPreferenceUtil.setLastPlace(lastPlace);
                address = getAddress(mLatLng.latitude,mLatLng.longitude);
                fetchWeatherInformation();
                if(nearestPlace(mLatitude,mLongitude) == -1) {
                    updateDatabaseAndDrawer();
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {
                Log.i(TAG, "Request cancelled by User");
            }
        }
    }
    void enterDataIntoDatabase(String place, double latitude, double longitude, int accessed, String placeId)
    {
        SQLiteDatabase db = mCityInfoDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CityEntry.COLUMN_NAME_TITLE,place);
        values.put(CityEntry.COLUMN_NAME_LATITUDE,latitude);
        values.put(CityEntry.COLUMN_NAME_LONGITUDE,longitude);
        values.put(CityEntry.COLUMN_NAME_ACCESSED,accessed);
        values.put(CityEntry.COLUMN_NAME_PLACEID,placeId);

        long newRowId = db.insert(CityEntry.TABLE_NAME,null,values);

        LogIt("Database accessed with newRowId : " + newRowId);
    }

    void updateCitiesInDrawer(boolean init)
    {
        menu = navigationView.getMenu();
//        for(int j=0;j<i;j++) {
//            menu.add(cityList[j]);
//        }


        SQLiteDatabase db = mCityInfoDbHelper.getReadableDatabase();
        String[] projection = {
                CityEntry._ID,
                CityEntry.COLUMN_NAME_TITLE,
                CityEntry.COLUMN_NAME_LATITUDE,
                CityEntry.COLUMN_NAME_LONGITUDE
        };

        String order = init ? "ASC" : "DESC" ;      //if initializing on activity create then demand
        //enteries in ascending order, otherwise just demand the last element from the db and add this
        // element to other list, remember no of elements asked for are different when initializing and
        // adding a new city
        String sortOrder = CityEntry._ID + " "+order;

        String limit = init ? null:"1";

        if(init)
            cityInfoHelperList.clear();

        Cursor cursor = db.query(
                CityEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder,
                limit
        );
        //menu.add(cityList[i - 1]);
        while(cursor.moveToNext()) {
            menu.add(1,cityInfoHelperList.size(),
                    cityInfoHelperList.size(),
                    cursor.getString(cursor.getColumnIndexOrThrow(CityEntry.COLUMN_NAME_TITLE)));
            CityInfoHelper cityInfoHelper = new CityInfoHelper( cursor.getString(cursor.getColumnIndexOrThrow(CityEntry.COLUMN_NAME_TITLE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(CityEntry.COLUMN_NAME_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(CityEntry.COLUMN_NAME_LONGITUDE)));
            cityInfoHelperList.add(cityInfoHelper);
            if(!init)
                setMenuItemChecked(cityInfoHelperList.size()-1);
        }
        cursor.close();

        for (int i = 0, count = navigationView.getChildCount(); i < count; i++) {
            final View child = navigationView.getChildAt(i);
            if (child != null && child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        if(mCityInfoDbHelper != null)
            mCityInfoDbHelper.close();
        super.onDestroy();
    }

    void updateDatabaseAndDrawer()
    {
        enterDataIntoDatabase(address,mLatitude,mLongitude,0,"NA");
        updateCitiesInDrawer(false);
    }

    int nearestPlace(Double latitude, Double longitude)
    {
        int nearestPlaceDistance = Integer.MAX_VALUE;
        int nearestPlace = -1;
        float results[] = new float[4];
        for(int i=0;i<cityInfoHelperList.size();i++)
        {
            String cityName = cityInfoHelperList.get(i).getCityName();
            Double cityLatitude = cityInfoHelperList.get(i).getLatitude();
            Double cityLongitude  = cityInfoHelperList.get(i).getLongitude();

            android.location.Location.distanceBetween(latitude,longitude,cityLatitude,cityLongitude,results);

            float distance = results[0];

            if(distance < DISTANCE_BETWEEN_CITIES)
            {
                LogIt("city "+cityName+" distance "+distance);
                if(nearestPlaceDistance > distance)
                {
                    nearestPlace = i;
                }
            }
        }
        return nearestPlace;
    }

    void setMenuItemChecked(int itemId)
    {
        Menu menu = navigationView.getMenu();
        LogIt("size of menu"+menu.size());
        for(int i = 0;i<menu.size();i++) {
            menu.getItem(i).setChecked(false);
        }
        menu.getItem(itemId+1).setChecked(true); // because there is one item already added in XML
    }

}
