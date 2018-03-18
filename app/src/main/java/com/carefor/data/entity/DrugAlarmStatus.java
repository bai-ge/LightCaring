package com.carefor.data.entity;

/**
 * Created by Ryoko on 2018/3/10.
 */

public class DrugAlarmStatus {
    /**
     * 启动的AlarmClockOnTimeActivity个数
     */
    public static int sActivityNumber = 0;

    /**
     * 上一次闹钟响起时间
     */
    public static long sLastStartTime = 0;

    /**
     * 上一次响起级别（1：闹钟，2：小睡，0：无）
     */
    public static int sStrikerLevel = 0;
}
