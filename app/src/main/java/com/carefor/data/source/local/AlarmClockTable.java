package com.carefor.data.source.local;

import android.provider.BaseColumns;

/**
 * Created by Ryoko on 2018/3/14.
 */

public class AlarmClockTable {
    private AlarmClockTable(){}

    public static abstract class AlarmClockEntry implements BaseColumns{

        public static final String TABLE_NAME = "ALARMCLOCK";
        /**
         * 小时
         */
        public static final String AC_HOUR = "hour";

        /**
         * 分钟
         */
        public static final String AC_MINUTE = "minute";

        /**
         * 周重复信息描述
         */
        public static final String AC_REPEAT = "repeat";

        /**
         * 周重复信息
         */
        public static final String AC_WEEKS = "weeks";

        /**
         * 标签描述信息
         */
        public static final String AC_TAG = "tag";

        /**
         * 铃声名
         */
        public static final String AC_RING_NAME = "ringName";

        /**
         * 铃声地址
         */
        public static final String AC_RING_URL = "ringUrl";

        /**
         * 铃声选择标记界面
         */
        public static final String AC_RING_PAGER = "ringPager";

        /**
         * 音量
         */
        public static final String AC_VOLUME = "volume";

        /**
         * 振动
         */
        public static final String AC_VIBRATE = "vibrate";

        /**
         * 小睡
         */
        public static final String AC_NAP = "nap";

        /**
         * 小睡间隔
         */
        public static final String AC_NAP_INTERVAL = "napInterval";

        /**
         * 小睡次数
         */
        public static final String AC_NAP_TIMES = "napTimes";

        /**
         * 天气提示
         */
        public static final String AC_WEA_PROMPT = "weaPrompt";

        /**
         * 闹钟开关
         */
        public static final String AC_ON_OFF = "onOff";

    }
}
