package com.carefor.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.carefor.data.entity.AlarmClock;
import com.carefor.data.source.DataSource;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.AlarmClockTable.AlarmClockEntry;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/21.
 */

public class LocalRepository implements DataSource {

    private final static String TAG = LocalRepository.class.getCanonicalName();

    private static LocalRepository INSTANCE = null;

    private DBHelper mDbHelper;


    private String[] mAlarmClockProjection = new String[]{
            AlarmClockEntry._ID,
            AlarmClockEntry.AC_HOUR,
            AlarmClockEntry.AC_MINUTE,
            AlarmClockEntry.AC_REPEAT,
            AlarmClockEntry.AC_WEEKS,
            AlarmClockEntry.AC_TAG,
            AlarmClockEntry.AC_RING_NAME,
            AlarmClockEntry.AC_RING_URL,
            AlarmClockEntry.AC_RING_PAGER,
            AlarmClockEntry.AC_VOLUME,
            AlarmClockEntry.AC_VIBRATE,

            AlarmClockEntry.AC_NAP,
            AlarmClockEntry.AC_NAP_INTERVAL,
            AlarmClockEntry.AC_NAP_TIMES,
            AlarmClockEntry.AC_WEA_PROMPT,
            AlarmClockEntry.AC_ON_OFF
    };

    private LocalRepository(@NonNull Context context) {
        checkNotNull(context);
        mDbHelper = new DBHelper(context);
        CacheRepository cacheRepository = CacheRepository.getInstance();
        cacheRepository.readConfig(context);
    }

/*    private LocalRepository(@NonNull Context context){
        checkNotNull(context);
        CacheRepository cacheRepository = CacheRepository.getInstance();
        cacheRepository.readConfig(context);
    }*/

    public static LocalRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (LocalRepository.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new LocalRepository(context);
                }
            }
        }
        return INSTANCE;
    }



    private AlarmClock newAAlarmClockFromCursor(Cursor cursor) {
        AlarmClock ac = null;
        if (cursor != null && cursor.getCount() > 0) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry._ID));
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_HOUR));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_MINUTE));
            String repeat = cursor.getString(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_REPEAT));
            String weeks = cursor.getString(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_WEEKS));
            String tag = cursor.getString(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_TAG));
            String ringName = cursor.getString(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_RING_NAME));
            String ringUrl = cursor.getString(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_RING_URL));
            int ringPager= cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_RING_PAGER));
            int volume = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_VOLUME));
            int vibrate = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_VIBRATE));
            int nap  = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_NAP));
            int napInterval = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_NAP_INTERVAL));
            int napTimes = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_NAP_TIMES));
            int weaPrompt = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_WEA_PROMPT));
            int onOff = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmClockEntry.AC_ON_OFF));



            ac = new AlarmClock(id,hour, minute, repeat,
                    weeks, tag, ringName, ringUrl,
            ringPager, volume,  vibrate, nap,
            napInterval, napTimes, weaPrompt, onOff);
        }
        return ac;
    }

    public void saveAlarmClock(AlarmClock alarmClock) {
        checkNotNull(alarmClock);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        putValue(values, alarmClock);
        db.insert(AlarmClockEntry.TABLE_NAME, null, values);
        db.close();
    }


    public void updateAlarmClock(AlarmClock alarmClock) {
        Preconditions.checkNotNull(alarmClock);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        putValue(values, alarmClock);
        String selection = AlarmClockEntry._ID + "= ?";
        String[] selectionArgs = { String.valueOf(alarmClock.getId()) };
        db.update(AlarmClockEntry.TABLE_NAME, values, selection,selectionArgs);
        db.close();

    }




    public List<AlarmClock> loadAlarmClocks() {
        List<AlarmClock> alarmClockList = new ArrayList<AlarmClock>();;


        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor c = db.query(
                AlarmClockEntry.TABLE_NAME, mAlarmClockProjection, null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                AlarmClock ac = newAAlarmClockFromCursor(c);
                if (ac != null) {
                    alarmClockList.add(ac);
                }
            }
        }
        if (c != null) {
            c.close();
        }
        db.close();

        return alarmClockList;

    }

    public void deleteAlarmClock(AlarmClock alarmClock) {

        Preconditions.checkNotNull(alarmClock.getId());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = AlarmClockEntry._ID + "= ?";
        String[] selectionArgs = {String.valueOf(alarmClock.getId())};

        db.delete(AlarmClockEntry.TABLE_NAME, selection, selectionArgs);
    }


    private void putValue(ContentValues values, AlarmClock alarmClock) {

        if (values != null && alarmClock != null) {
            values.put(AlarmClockEntry._ID, alarmClock.getId());
            values.put(AlarmClockEntry.AC_HOUR, alarmClock.getHour());
            values.put(AlarmClockEntry.AC_MINUTE, alarmClock.getMinute());
            values.put(AlarmClockEntry.AC_REPEAT, alarmClock.getRepeat());
            values.put(AlarmClockEntry.AC_WEEKS, alarmClock.getWeeks());
            values.put(AlarmClockEntry.AC_TAG, alarmClock.getTag());
            values.put(AlarmClockEntry.AC_RING_NAME, alarmClock.getRingName());
            values.put(AlarmClockEntry.AC_RING_URL, alarmClock.getRingUrl());
            values.put(AlarmClockEntry.AC_RING_PAGER, alarmClock.getRingPager());
            values.put(AlarmClockEntry.AC_VOLUME, alarmClock.getVolume());
            values.put(AlarmClockEntry.AC_VIBRATE, alarmClock.isVibrate());
            values.put(AlarmClockEntry.AC_NAP, alarmClock.isNap());
            values.put(AlarmClockEntry.AC_NAP_INTERVAL, alarmClock.getNapInterval());
            values.put(AlarmClockEntry.AC_NAP_TIMES, alarmClock.getNapTimes());
            values.put(AlarmClockEntry.AC_WEA_PROMPT, alarmClock.isWeaPrompt());
            values.put(AlarmClockEntry.AC_ON_OFF, alarmClock.isOnOff());

        }
    }



}
