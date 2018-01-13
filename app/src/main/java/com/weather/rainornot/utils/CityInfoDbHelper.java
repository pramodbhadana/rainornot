package com.weather.rainornot.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.weather.rainornot.utils.CityInfoContract.CityEntry;

/**
 * Created by pramodbhadana on 07/05/17.
 */

public class CityInfoDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_TABLE_CREATE =
            "CREATE TABLE "+ CityEntry.TABLE_NAME + " ("+
                    CityEntry._ID + " INTEGER PRIMARY KEY," +
                    CityEntry.COLUMN_NAME_TITLE + " TEXT," +
                    CityEntry.COLUMN_NAME_LATITUDE + " REAL,"+
                    CityEntry.COLUMN_NAME_LONGITUDE + " REAL,"+
                    CityEntry.COLUMN_NAME_ACCESSED + " INTEGER,"+
                    CityEntry.COLUMN_NAME_PLACEID + " TEXT)";
    private static final String DATABASE_TABLE_DELETE =
            "DROP TABLE IF EXISTS" + CityInfoContract.CityEntry.TABLE_NAME;
    private static final String DATABASE_NAME = "CityDatabase.db";

    public CityInfoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion)
    {
        db.execSQL("delete");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db,int oldVersion,int newVersion)
    {
        //onUpgrade(db,oldVersion,newVersion);
    }
}
