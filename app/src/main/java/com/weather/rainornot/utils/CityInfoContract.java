package com.weather.rainornot.utils;

import android.provider.BaseColumns;

/**
 * Created by pramodbhadana on 27/05/17.
 */

public class CityInfoContract {
    private CityInfoContract() {}

    public static class CityEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "Cities";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_ACCESSED = "accessed";
        public static final String COLUMN_NAME_PLACEID = "placeId";
    }
}
