package com.carefor.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.carefor.util.Loggerx;

/**
 * Created by Ryoko on 2018/3/14.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;

    public static final String DATABASE_NAME = "CareforApp.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String INTEGER_TYPE = " INTEGER";

    private static final String DATETIME_TYPE = "DATETIME";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ALARMCOLCK_ENTRIES =
            "CREATE TABLE " + AlarmClockTable.AlarmClockEntry.TABLE_NAME + " (" +
                    AlarmClockTable.AlarmClockEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +

                    AlarmClockTable.AlarmClockEntry.AC_HOUR + INTEGER_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_MINUTE + INTEGER_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_REPEAT + TEXT_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_WEEKS + TEXT_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_TAG + TEXT_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_RING_NAME + TEXT_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_RING_URL + TEXT_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_RING_PAGER + INTEGER_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_VOLUME + INTEGER_TYPE +COMMA_SEP +

                    AlarmClockTable.AlarmClockEntry.AC_VIBRATE + INTEGER_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_NAP + INTEGER_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_NAP_INTERVAL + INTEGER_TYPE + COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_NAP_TIMES + INTEGER_TYPE +COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_ON_OFF + INTEGER_TYPE +COMMA_SEP +
                    AlarmClockTable.AlarmClockEntry.AC_WEA_PROMPT + INTEGER_TYPE +
                    " )";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Loggerx.d("DBHelper", "onCreate()");
        db.execSQL(SQL_CREATE_ALARMCOLCK_ENTRIES); //创建数据库
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop table if exists "+ AlarmClockTable.AlarmClockEntry.TABLE_NAME);
        db.execSQL(SQL_CREATE_ALARMCOLCK_ENTRIES); //创建数据库

    }
}
