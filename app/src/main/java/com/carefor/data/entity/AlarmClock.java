/*
 * Copyright (c) 2016 咖枯 <kaku201313@163.com | 3772304@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.carefor.data.entity;

import android.os.Parcel;
import android.os.Parcelable;


public class AlarmClock implements Parcelable {

    /**
     * 闹钟id
     */
    private int id;

    /**
     * 小时
     */
    private int hour;

    /**
     * 分钟
     */
    private int minute;

    /**
     * 重复
     */
    private String repeat;

    /**
     * 周期
     */
    private String weeks;

    /**
     * 标签
     */
    private String tag;

    /**
     * 铃声名
     */
    private String ringName;

    /**
     * 铃声地址
     */
    private String ringUrl;

    /**
     * 铃声选择标记界面
     */
    private int ringPager;

    /**
     * 音量
     */
    private int volume;

    /**
     * 振动
     */
    private int vibrate;

    /**
     * 小睡
     */
    private int nap;

    /**
     * 小睡间隔
     */
    private int napInterval;

    /**
     * 小睡次数
     */
    private int napTimes;

    /**
     * 天气提示
     */
    private int weaPrompt;

    /**
     * 开关
     */
    private int onOff;

    public AlarmClock( ) {
        super();
    }

    /**
     * 闹钟实例构造方法
     *
     * @param hour           小时
     * @param minute         分钟
     * @param repeat         重复
     * @param weeks          周期
     * @param tag            标签
     * @param ringName       铃声名
     * @param ringUrl        铃声地址
     * @param ringPager      铃声界面
     * @param volume         音量
     * @param vibrate        振动
     * @param nap            小睡
     * @param napInterval    小睡间隔
     * @param napTimes       小睡次数
     * @param weaPrompt      天气提示
     * @param onOff          开关
     */
    public AlarmClock(int id, int hour, int minute, String repeat,
                      String weeks, String tag, String ringName, String ringUrl,
                      int ringPager, int volume, int vibrate, int nap,
                      int napInterval, int napTimes, int weaPrompt, int onOff) {
        super();
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.repeat = repeat;
        this.weeks = weeks;
        this.tag = tag;
        this.ringName = ringName;
        this.ringUrl = ringUrl;
        this.ringPager = ringPager;
        this.volume = volume;
        this.vibrate = vibrate;
        this.nap = nap;
        this.napInterval = napInterval;
        this.napTimes = napTimes;
        this.weaPrompt = weaPrompt;
        this.onOff = onOff;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(hour);
        out.writeInt(minute);
        out.writeString(repeat);
        out.writeString(weeks);
        out.writeString(tag);
        out.writeString(ringName);
        out.writeString(ringUrl);
        out.writeInt(ringPager);
        out.writeInt(volume);
        out.writeInt(vibrate);
        out.writeInt(nap);
        out.writeInt(napInterval);
        out.writeInt(napTimes);
        out.writeInt(weaPrompt);
        out.writeInt(onOff);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private AlarmClock(Parcel in) {
        id = in.readInt();
        hour = in.readInt();
        minute = in.readInt();
        repeat = in.readString();
        weeks = in.readString();
        tag = in.readString();
        ringName = in.readString();
        ringUrl = in.readString();
        ringPager = in.readInt();
        volume = in.readInt();
        vibrate = in.readInt();
        nap = in.readInt() ;
        napInterval = in.readInt();
        napTimes = in.readInt();
        weaPrompt = in.readInt();
        onOff = in.readInt();
    }

    public static final Creator<AlarmClock> CREATOR = new Creator<AlarmClock>() {

        @Override
        public AlarmClock createFromParcel(Parcel in) {
            return new AlarmClock(in);
        }

        @Override
        public AlarmClock[] newArray(int size) {

            return new AlarmClock[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id){
        this.id = id;
    }


    public int getNapInterval() {
        return napInterval;
    }

    public void setNapInterval(int napInterval) {
        this.napInterval = napInterval;
    }

    public int getNapTimes() {
        return napTimes;
    }

    public void setNapTimes(int napTimes) {
        this.napTimes = napTimes;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getWeeks() {
        return weeks;
    }

    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRingName() {
        return ringName;
    }

    public void setRingName(String ringName) {
        this.ringName = ringName;
    }

    public String getRingUrl() {
        return ringUrl;
    }

    public int getRingPager() {
        return ringPager;
    }

    public void setRingPager(int ringPager) {
        this.ringPager = ringPager;
    }

    public void setRingUrl(String ringUrl) {
        this.ringUrl = ringUrl;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int isVibrate() {
        return vibrate;
    }

    public void setVibrate(int vibrate) {
        this.vibrate = vibrate;
    }

    public int isNap() {
        return nap;
    }

    public void setNap(int nap) {
        this.nap = nap;
    }

    public int isWeaPrompt() {
        return weaPrompt;
    }

    public void setWeaPrompt(int weaPrompt) {
        this.weaPrompt = weaPrompt;
    }

    public int isOnOff() {
        return onOff;
    }

    public void setOnOff(int onOff) {
        this.onOff = onOff;
    }
}
