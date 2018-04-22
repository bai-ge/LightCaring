package com.carefor.drugalarm.alarm;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.carefor.data.entity.AlarmClock;
import com.carefor.data.entity.DrugAlarmConstant;
import com.carefor.data.entity.Medicine;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.mainui.R;
import com.carefor.util.AlarmUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class AlarmClockEditFragment extends Fragment implements AlarmClockContract.View,
        OnClickListener, OnCheckedChangeListener {

    private final static String TAG = AlarmClockEditFragment.class.getCanonicalName();

    private AlarmClockContract.Presenter mPresenter;

    private Toast mToast;

    private Handler mHandler;

    private TextView mTxtTitle;

    private TextView mTxtTag;

    /**
     * 铃声选择按钮的requestCode
     */
    private static final int REQUEST_RING_SELECT = 1;

    /**
     * 小睡按钮的requestCode
     */
    private static final int REQUEST_NAP_EDIT = 2;

    /**
     * 闹钟实例
     */
    private AlarmClock mAlarmClock;

    /**
     * 下次响铃时间提示控件
     */
    private TextView mTimePickerTv;

    /**
     * 周一按钮状态，默认未选中
     */
    private Boolean isMondayChecked = false;

    /**
     * 周二按钮状态，默认未选中
     */
    private Boolean isTuesdayChecked = false;

    /**
     * 周三按钮状态，默认未选中
     */
    private Boolean isWednesdayChecked = false;

    /**
     * 周四按钮状态，默认未选中
     */
    private Boolean isThursdayChecked = false;

    /**
     * 周五按钮状态，默认未选中
     */
    private Boolean isFridayChecked = false;

    /**
     * 周六按钮状态，默认未选中
     */
    private Boolean isSaturdayChecked = false;

    /**
     * 周日按钮状态，默认未选中
     */
    private Boolean isSundayChecked = false;

    /**
     * 保存重复描述信息String
     */
    private StringBuilder mRepeatStr;

    /**
     * 重复描述组件
     */
    private TextView mRepeatDescribe;

    /**
     * 按键值顺序存放重复描述信息
     */
    private TreeMap<Integer, String> mMap;

    /**
     * 铃声描述
     */
    private TextView mRingDescribe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mHandler = new Handler();
        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle.containsKey(DrugAlarmConstant.ALARM_CLOCK)){
            mAlarmClock = bundle.getParcelable(DrugAlarmConstant.ALARM_CLOCK);
        }else{
            mAlarmClock = new AlarmClock();
            Calendar calendar = new GregorianCalendar();
            mAlarmClock.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            mAlarmClock.setMinute(calendar.get(Calendar.MINUTE));
            mAlarmClock.setVolume(8);
        }
        Log.d(TAG, "medicine size ="+mAlarmClock.getMedicineList().size());
        Log.d(TAG, mAlarmClock.toString());
        // 闹钟默认开启
        mAlarmClock.setOnOff(1);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarmclockedit,
                container, false);
        // 设置界面背景
        setBackground(view);
//        setBounce(view);
        // 初始化操作栏
        initActionBar(view);
        // 初始化时间选择
        initTimeSelect(view);
        // 初始化重复
        initRepeat(view);
        // 初始化标签
        initTag(view);
        // 初始化铃声
        initRing(view);
        // 初始化音量
        initVolume(view);
        // 初始化振动、小睡、天气提示
        initToggleButton(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    private void setBounce(View view) {
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scrollView1);
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
    }

    private void initVolume(View view) {
        // 音量控制seekBar
        SeekBar volumeSkBar = (SeekBar) view.findViewById(R.id.volumn_sk);
        // 设置当前音量显示
        volumeSkBar.setProgress(mAlarmClock.getVolume());
        volumeSkBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 保存设置的音量
                mAlarmClock.setVolume(seekBar.getProgress());

                final SharedPreferences share = getActivity().getSharedPreferences(DrugAlarmConstant.EXTRA_SHARE,
                        Activity.MODE_PRIVATE);
                final SharedPreferences.Editor editor = share.edit();
                editor.putInt(DrugAlarmConstant.AlARM_VOLUME, seekBar.getProgress());
                editor.apply();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

            }
        });

    }

    /**
     * 设置界面背景
     *
     * @param view view
     */
    private void setBackground(View view) {
        // 闹钟修改界面
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.new_alarm_clock);
        viewGroup.setBackgroundResource(R.color.colorPrimaryDark);
        // 设置页面背景
        //AlarmUtil.setBackground(viewGroup, getActivity());
    }

    /**
     * 设置操作栏按钮
     *
     * @param view view
     */
    private void initActionBar(View view) {
        // 操作栏取消按钮
        ImageView cancelAction = (ImageView) view.findViewById(R.id.action_cancel);
        cancelAction.setOnClickListener(this);
        // 操作栏确定按钮
        ImageView acceptAction = (ImageView) view.findViewById(R.id.action_accept);
        acceptAction.setOnClickListener(this);
        // 操作栏标题
         mTxtTitle = (TextView) view.findViewById(R.id.action_title);
    }


    /**
     * 设置时间选择
     *
     * @param view view
     */
    private void initTimeSelect(View view) {
        // 下次响铃提示
        mTimePickerTv = (TextView) view.findViewById(R.id.time_picker_tv);
        // 计算倒计时显示
        displayCountDown();
        // 闹钟时间选择器
        TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        // 初始化时间选择器的小时
        //noinspection deprecation
        timePicker.setCurrentHour(mAlarmClock.getHour());
        // 初始化时间选择器的分钟
        //noinspection deprecation
        timePicker.setCurrentMinute(mAlarmClock.getMinute());

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // 保存闹钟实例的小时
                mAlarmClock.setHour(hourOfDay);
                // 保存闹钟实例的分钟
                mAlarmClock.setMinute(minute);
                // 计算倒计时显示
                displayCountDown();
            }

        });
    }


    /**
     * 设置重复信息
     *
     * @param view view
     */
    private void initRepeat(View view) {
        // 重复描述
        mRepeatDescribe = (TextView) view.findViewById(R.id.repeat_describe);

        // 周选择按钮
        // 周一按钮
        ToggleButton monday = (ToggleButton) view.findViewById(R.id.tog_btn_monday);
        // 周二按钮
        ToggleButton tuesday = (ToggleButton) view.findViewById(R.id.tog_btn_tuesday);
        // 周三按钮
        ToggleButton wednesday = (ToggleButton) view.findViewById(R.id.tog_btn_wednesday);
        // 周四按钮
        ToggleButton thursday = (ToggleButton) view.findViewById(R.id.tog_btn_thursday);
        // 周五按钮
        ToggleButton friday = (ToggleButton) view.findViewById(R.id.tog_btn_friday);
        // 周六按钮
        ToggleButton saturday = (ToggleButton) view.findViewById(R.id.tog_btn_saturday);
        // 周日按钮
        ToggleButton sunday = (ToggleButton) view.findViewById(R.id.tog_btn_sunday);

        monday.setOnCheckedChangeListener(this);
        tuesday.setOnCheckedChangeListener(this);
        wednesday.setOnCheckedChangeListener(this);
        thursday.setOnCheckedChangeListener(this);
        friday.setOnCheckedChangeListener(this);
        saturday.setOnCheckedChangeListener(this);
        sunday.setOnCheckedChangeListener(this);

        mRepeatStr = new StringBuilder();
        mMap = new TreeMap<>();

        String weeks = mAlarmClock.getWeeks();
        // 不是单次响铃时
        if (weeks != null) {
            final String[] weeksValue = weeks.split(",");
            for (String aWeeksValue : weeksValue) {
                int week = Integer.parseInt(aWeeksValue);
                switch (week) {
                    case 1:
                        sunday.setChecked(true);
                        break;
                    case 2:
                        monday.setChecked(true);
                        break;
                    case 3:
                        tuesday.setChecked(true);
                        break;
                    case 4:
                        wednesday.setChecked(true);
                        break;
                    case 5:
                        thursday.setChecked(true);
                        break;
                    case 6:
                        friday.setChecked(true);
                        break;
                    case 7:
                        saturday.setChecked(true);
                        break;
                }

            }
        }
    }


    /**
     * 设置标签
     *
     * @param view view
     */
    private void initTag(View view) {
        // 标签描述控件
        ViewGroup taglayout = (ViewGroup) view.findViewById(R.id.ll_tag);
        taglayout.setOnClickListener(this);
        mTxtTag = (TextView) view.findViewById(R.id.tag_edit_text);
        mTxtTag.setText(mAlarmClock.getTag());
//        mTxtTag.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before,
//                                      int count) {
//                if (!s.toString().equals("")) {
//                    mAlarmClock.setTag(s.toString());
//                } else {
//                    mAlarmClock.setTag(getString(R.string.alarm_clock));
//                }
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count,
//                                          int after) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
    }


    /**
     * 设置铃声
     *
     * @param view view
     */
    private void initRing(View view) {
        // 铃声控件
        ViewGroup ring = (ViewGroup) view.findViewById(R.id.ring_llyt);
        ring.setOnClickListener(this);
        mRingDescribe = (TextView) view.findViewById(R.id.ring_describe);
        mRingDescribe.setText(mAlarmClock.getRingName());
    }

    /**
     * 设置振动、小睡、天气提示
     *
     * @param view view
     */
    private void initToggleButton(View view) {
        // 振动
        ToggleButton vibrateBtn = (ToggleButton) view.findViewById(R.id.vibrate_btn);

        // 小睡
        ToggleButton napBtn = (ToggleButton) view.findViewById(R.id.nap_btn);
        // 小睡组件
        ViewGroup nap = (ViewGroup) view.findViewById(R.id.nap_llyt);
        nap.setOnClickListener(this);
        

        vibrateBtn.setOnCheckedChangeListener(this);
        napBtn.setOnCheckedChangeListener(this);

        vibrateBtn.setChecked(mAlarmClock.isVibrate() == 1 );
        napBtn.setChecked(mAlarmClock.isNap() == 1);

    }
    
    


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 当点击取消按钮
            case R.id.action_cancel:
                drawAnimation();
                break;
            // 当点击确认按钮
            case R.id.action_accept:
//                saveDefaultAlarmTime();

                Intent data = new Intent();
                data.putExtra(DrugAlarmConstant.ALARM_CLOCK, mAlarmClock);
                getActivity().setResult(Activity.RESULT_OK, data);
//                mPresenter.finishEidit(mAlarmClock);

                drawAnimation();
                break;
            case R.id.ll_tag: //药品盒
                final List<Medicine> medicines = CacheRepository.getInstance().getMedicineList();
                if(mAlarmClock.getMedicineList() != null && mAlarmClock.getMedicineList().size() > 0){
                    for (Medicine medicine : mAlarmClock.getMedicineList()) {
                        if(!medicines.contains(medicine)){
                            medicines.add(medicine);
                        }
                        Log.d(TAG, "mAlarm :"+medicine);
                    }
                }
                final String[] items = new String[medicines.size()];
                final boolean[] checks = new boolean[medicines.size()];
                for (int i = 0; i < medicines.size(); i++) {
                    Medicine medicine = medicines.get(i);
                    String item = medicine.getName() + "\t" + medicine.getDosage();
                    items[i] = item;
                }
                if(mAlarmClock.getMedicineList() != null && mAlarmClock.getMedicineList().size() > 0){
                    for (Medicine medicine : mAlarmClock.getMedicineList()) {
                        int i = medicines.indexOf(medicine);
                        if(i >= 0){
                            checks[i] = true;
                            Log.d(TAG, i+"mAlarm checks:"+medicine);
                        }
                    }
                }
                final AlertDialog.Builder multiChoiceDialog = new AlertDialog.Builder(getContext());
                multiChoiceDialog.setTitle("药品盒");

                multiChoiceDialog.setMultiChoiceItems(items, checks, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Log.d(TAG, items[which]+isChecked);
                        checks[which] = isChecked;
                    }
                });

                multiChoiceDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                multiChoiceDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StringBuffer buffer = new StringBuffer();
                                if(mAlarmClock.getMedicineList() == null){
                                    mAlarmClock.setMedicineList(new ArrayList<Medicine>());
                                }
                                mAlarmClock.getMedicineList().clear();
                                for (int i = 0; i < medicines.size(); i++){
                                    if(checks[i]){
                                        mAlarmClock.getMedicineList().add(medicines.get(i));
                                        buffer.append(medicines.get(i).getName() + " x "+medicines.get(i).getDosage()+ " ");
                                    }
                                    Log.d(TAG, "Alarm clock "+mAlarmClock.getMedicineList().size());
                                }
                                mTxtTag.setText(buffer);
                                mAlarmClock.setTag(buffer.toString());
                            }
                        });

                multiChoiceDialog.show();
                break;
            // 当点击铃声
            case R.id.ring_llyt:
                // 不响应重复点击
                if (AlarmUtil.isFastDoubleClick()) {
                    return;
                }
                // 铃声选择界面
/*                Intent i = new Intent(getActivity(), RingSelectActivity.class);
                i.putExtra(DrugAlarmConstant.RING_NAME, mAlarmClock.getRingName());
                i.putExtra(DrugAlarmConstant.RING_URL, mAlarmClock.getRingUrl());
                i.putExtra(DrugAlarmConstant.RING_PAGER, mAlarmClock.getRingPager());
                i.putExtra(DrugAlarmConstant.RING_REQUEST_TYPE, 0);
                startActivityForResult(i, REQUEST_RING_SELECT);*/
                break;
            // 当点击小睡
            case R.id.nap_llyt:
                // 不响应重复点击
                if (AlarmUtil.isFastDoubleClick()) {
                    return;
                }
                // 小睡界面
/*                Intent nap = new Intent(getActivity(), NapEditActivity.class);
                nap.putExtra(DrugAlarmConstant.NAP_INTERVAL,
                        mAlarmClock.getNapInterval());
                nap.putExtra(DrugAlarmConstant.NAP_TIMES, mAlarmClock.getNapTimes());
                startActivityForResult(nap, REQUEST_NAP_EDIT);*/
                break;
        }
    }

    private void saveDefaultAlarmTime() {
        SharedPreferences share = getActivity().getSharedPreferences(
                DrugAlarmConstant.EXTRA_SHARE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putInt(DrugAlarmConstant.DEFAULT_ALARM_HOUR, mAlarmClock.getHour());
        editor.putInt(DrugAlarmConstant.DEFAULT_ALARM_MINUTE, mAlarmClock.getMinute());
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            // 铃声选择界面返回
            case REQUEST_RING_SELECT:
                // 铃声名
                String name = data.getStringExtra(DrugAlarmConstant.RING_NAME);
                // 铃声地址
                String url = data.getStringExtra(DrugAlarmConstant.RING_URL);
                // 铃声界面
                int ringPager = data.getIntExtra(DrugAlarmConstant.RING_PAGER, 0);

                mRingDescribe.setText(name);

                mAlarmClock.setRingName(name);
                mAlarmClock.setRingUrl(url);
                mAlarmClock.setRingPager(ringPager);
                break;
            // 小睡编辑界面返回
            case REQUEST_NAP_EDIT:
                // 小睡间隔
                int napInterval = data.getIntExtra(DrugAlarmConstant.NAP_INTERVAL, 10);
                // 小睡次数
                int napTimes = data.getIntExtra(DrugAlarmConstant.NAP_TIMES, 3);
                mAlarmClock.setNapInterval(napInterval);
                mAlarmClock.setNapTimes(napTimes);
                break;
        }
    }
    

    /**
     * 结束新建闹钟界面时开启移动退出效果动画
     */
    private void drawAnimation() {
        getActivity().finish();
        getActivity().overridePendingTransition(0, R.anim.move_out_bottom);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            // 选中周一
            case R.id.tog_btn_monday:
                if (isChecked) {
                    isMondayChecked = true;
                    mMap.put(1, getString(R.string.one_h));
                    setRepeatDescribe();
                    displayCountDown();
                } else {
                    isMondayChecked = false;
                    mMap.remove(1);
                    setRepeatDescribe();
                    displayCountDown();
                }
                break;
            // 选中周二
            case R.id.tog_btn_tuesday:
                if (isChecked) {
                    isTuesdayChecked = true;
                    mMap.put(2, getString(R.string.two_h));
                    setRepeatDescribe();
                    displayCountDown();
                } else {
                    isTuesdayChecked = false;
                    mMap.remove(2);
                    setRepeatDescribe();
                    displayCountDown();
                }
                break;
            // 选中周三
            case R.id.tog_btn_wednesday:
                if (isChecked) {
                    isWednesdayChecked = true;
                    mMap.put(3, getString(R.string.three_h));
                    setRepeatDescribe();
                    displayCountDown();
                } else {
                    isWednesdayChecked = false;
                    mMap.remove(3);
                    setRepeatDescribe();
                    displayCountDown();
                }
                break;
            // 选中周四
            case R.id.tog_btn_thursday:
                if (isChecked) {
                    isThursdayChecked = true;
                    mMap.put(4, getString(R.string.four_h));
                    setRepeatDescribe();
                    displayCountDown();
                } else {
                    isThursdayChecked = false;
                    mMap.remove(4);
                    setRepeatDescribe();
                    displayCountDown();
                }
                break;
            // 选中周五
            case R.id.tog_btn_friday:
                if (isChecked) {
                    isFridayChecked = true;
                    mMap.put(5, getString(R.string.five_h));
                    setRepeatDescribe();
                    displayCountDown();
                } else {
                    isFridayChecked = false;
                    mMap.remove(5);
                    setRepeatDescribe();
                    displayCountDown();
                }
                break;
            // 选中周六
            case R.id.tog_btn_saturday:
                if (isChecked) {
                    isSaturdayChecked = true;
                    mMap.put(6, getString(R.string.six_h));
                    setRepeatDescribe();
                    displayCountDown();
                } else {
                    isSaturdayChecked = false;
                    mMap.remove(6);
                    setRepeatDescribe();
                    displayCountDown();
                }
                break;
            // 选中周日
            case R.id.tog_btn_sunday:
                if (isChecked) {
                    isSundayChecked = true;
                    mMap.put(7, getString(R.string.day));
                    setRepeatDescribe();
                    displayCountDown();
                } else {
                    isSundayChecked = false;
                    mMap.remove(7);
                    setRepeatDescribe();
                    displayCountDown();
                }
                break;
            // 振动
            case R.id.vibrate_btn:
                if (isChecked) {
                    AlarmUtil.vibrate(getActivity());
                    mAlarmClock.setVibrate(1);
                } else {
                    mAlarmClock.setVibrate(0);
                }
                break;
            // 小睡
            case R.id.nap_btn:
                if (isChecked) {
                    mAlarmClock.setNap(1);
                } else {
                    mAlarmClock.setNap(0);
                }
                break;
            
        }
    }

    /**
     * 设置重复描述的内容
     */
    private void setRepeatDescribe() {
        // 全部选中
        if (isMondayChecked & isTuesdayChecked & isWednesdayChecked
                & isThursdayChecked & isFridayChecked & isSaturdayChecked
                & isSundayChecked) {
            mRepeatDescribe.setText(getResources()
                    .getString(R.string.every_day));
            mAlarmClock.setRepeat(getString(R.string.every_day));
            // 响铃周期
            mAlarmClock.setWeeks("2,3,4,5,6,7,1");
            // 周一到周五全部选中
        } else if (isMondayChecked & isTuesdayChecked & isWednesdayChecked
                & isThursdayChecked & isFridayChecked & !isSaturdayChecked
                & !isSundayChecked) {
            mRepeatDescribe.setText(getString(R.string.week_day));
            mAlarmClock.setRepeat(getString(R.string.week_day));
            mAlarmClock.setWeeks("2,3,4,5,6");
            // 周六、日全部选中
        } else if (!isMondayChecked & !isTuesdayChecked & !isWednesdayChecked
                & !isThursdayChecked & !isFridayChecked & isSaturdayChecked
                & isSundayChecked) {
            mRepeatDescribe.setText(getString(R.string.week_end));
            mAlarmClock.setRepeat(getString(R.string.week_end));
            mAlarmClock.setWeeks("7,1");
            // 没有选中任何一个
        } else if (!isMondayChecked & !isTuesdayChecked & !isWednesdayChecked
                & !isThursdayChecked & !isFridayChecked & !isSaturdayChecked
                & !isSundayChecked) {
            mRepeatDescribe.setText(getString(R.string.repeat_once));
            mAlarmClock.setRepeat(getResources()
                    .getString(R.string.repeat_once));
            mAlarmClock.setWeeks(null);

        } else {
            mRepeatStr.setLength(0);
            mRepeatStr.append(getString(R.string.week));
            Collection<String> col = mMap.values();
            for (String aCol : col) {
                mRepeatStr.append(aCol).append(getResources().getString(R.string.caesura));
            }
            // 去掉最后一个"、"
            mRepeatStr.setLength(mRepeatStr.length() - 1);
            mRepeatDescribe.setText(mRepeatStr.toString());
            mAlarmClock.setRepeat(mRepeatStr.toString());

            mRepeatStr.setLength(0);
            if (isMondayChecked) {
                mRepeatStr.append("2,");
            }
            if (isTuesdayChecked) {
                mRepeatStr.append("3,");
            }
            if (isWednesdayChecked) {
                mRepeatStr.append("4,");
            }
            if (isThursdayChecked) {
                mRepeatStr.append("5,");
            }
            if (isFridayChecked) {
                mRepeatStr.append("6,");
            }
            if (isSaturdayChecked) {
                mRepeatStr.append("7,");
            }
            if (isSundayChecked) {
                mRepeatStr.append("1,");
            }
            mAlarmClock.setWeeks(mRepeatStr.toString());
        }

    }


    /**
     * 计算显示倒计时信息
     */
    private void displayCountDown() {
        // 取得下次响铃时间
        long nextTime = AlarmUtil.calculateNextTime(mAlarmClock.getHour(),
                mAlarmClock.getMinute(), mAlarmClock.getWeeks());
        // 系统时间
        long now = System.currentTimeMillis();
        // 距离下次响铃间隔毫秒数
        long ms = nextTime - now;

        // 单位秒
        int ss = 1000;
        // 单位分
        int mm = ss * 60;
        // 单位小时
        int hh = mm * 60;
        // 单位天
        int dd = hh * 24;

        // 不计算秒，故响铃间隔加一分钟
        ms += mm;
        // 剩余天数
        long remainDay = ms / dd;
        // 剩余小时
        long remainHour = (ms - remainDay * dd) / hh;
        // 剩余分钟
        long remainMinute = (ms - remainDay * dd - remainHour * hh) / mm;

        // 响铃倒计时
        String countDown;
        // 当剩余天数大于0时显示【X天X小时X分】格式
        if (remainDay > 0) {
            countDown = getString(R.string.countdown_day_hour_minute);
            mTimePickerTv.setText(String.format(countDown, remainDay,
                    remainHour, remainMinute));
            // 当剩余小时大于0时显示【X小时X分】格式
        } else if (remainHour > 0) {
            countDown = getResources()
                    .getString(R.string.countdown_hour_minute);
            mTimePickerTv.setText(String.format(countDown, remainHour,
                    remainMinute));
        } else {
            // 当剩余分钟不等于0时显示【X分钟】格式
            if (remainMinute != 0) {
                countDown = getString(R.string.countdown_minute);
                mTimePickerTv.setText(String.format(countDown, remainMinute));
                // 当剩余分钟等于0时，显示【1天0小时0分】
            } else {
                countDown = getString(R.string.countdown_day_hour_minute);
                mTimePickerTv.setText(String.format(countDown, 1, 0, 0));
            }

        }
    }

    public static AlarmClockEditFragment newInstance() {
        return new AlarmClockEditFragment();
    }

    @Override
    public void showTip(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }

    @Override
    public void setPresenter(AlarmClockContract.Presenter presenter) {
            this.mPresenter = presenter;
    }

    @Override
    public void showTitle(final String title) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTxtTitle.setText(title);
            }
        });
    }
}
