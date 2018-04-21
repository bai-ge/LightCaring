package com.carefor.drugalarm;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.carefor.data.entity.AlarmClock;
import com.carefor.data.entity.DrugAlarmConstant;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.drugalarm.Event.AlarmClockDeleteEvent;
import com.carefor.drugalarm.Event.AlarmClockUpdateEvent;
import com.carefor.drugalarm.alarm.AlarmClockEditActivity;
import com.carefor.mainui.R;
import com.carefor.util.AlarmUtil;
import com.carefor.util.OttoAppConfig;
import com.carefor.util.ToastUtil;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;


/**
 * A simple {@link Fragment} subclass.
 */
public class DrugAlarmFragment extends Fragment implements OnClickListener {

    private final static String TAG = DrugAlarmFragment.class.getCanonicalName();
    /**
     * 新建闹钟的requestCode
     */
    private static final int REQUEST_ALARM_CLOCK_NEW = 1;

    /**
     * 修改闹钟的requestCode
     */
    private static final int REQUEST_ALARM_CLOCK_EDIT = 2;

    /**
     * 闹钟列表
     */
    private RecyclerView mRecyclerView;

    /**
     * 保存闹钟信息的list
     */
    private List<AlarmClock> mAlarmClockList;

    /**
     * 保存闹钟信息的adapter
     */
    private DrugAlarmAdapter mAdapter;

    /**
     * 操作栏编辑按钮
     */
    private ImageView mEditAction;

    /**
     * 操作栏编辑完成按钮
     */
    private ImageView mAcceptAction;

    /**
     * List内容为空时的视图
     */
    private RelativeLayout mEmptyView;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OttoAppConfig.getInstance().register(this);
        mAlarmClockList = new ArrayList<>();
        mAdapter = new DrugAlarmAdapter( getActivity(), mAlarmClockList );
        // 注册Loader
//        getLoaderManager().initLoader(1, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_drugalarm, container, false);

        mEmptyView = (RelativeLayout) view
                .findViewById(R.id.layout_empty);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_view);
        mRecyclerView.setHasFixedSize(true);
        //设置布局管理器
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setLayoutManager(new ErrorCatchLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new ScaleInLeftAnimator(new OvershootInterpolator(1f)));
        mRecyclerView.getItemAnimator().setAddDuration(300);
        mRecyclerView.getItemAnimator().setRemoveDuration(300);
        mRecyclerView.getItemAnimator().setMoveDuration(300);
        mRecyclerView.getItemAnimator().setChangeDuration(300);
        mRecyclerView.setAdapter(mAdapter);

//        OverScrollDecoratorHelper.setUpOverScroll(mRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        // 监听闹铃item点击事件Listener
        OnItemClickListener onItemClickListener = new OnItemClickListenerImpl();
        mAdapter.setOnItemClickListener(onItemClickListener);

        // 操作栏新建按钮
        ImageView newAction = (ImageView) view.findViewById(R.id.action_new);
        newAction.setOnClickListener( this);

        // 编辑闹钟
        mEditAction = (ImageView) view.findViewById(R.id.action_edit);
        mEditAction.setOnClickListener(this);

        // 完成按钮
        mAcceptAction = (ImageView) view.findViewById(R.id.action_accept);
        mAcceptAction.setOnClickListener(this);

        updateList();
        return view;
    }

    class OnItemClickListenerImpl implements OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
            // 不响应重复点击
            if (AlarmUtil.isFastDoubleClick()) {
                return;
            }
            AlarmClock alarmClock = mAlarmClockList.get(position);
            Intent intent = new Intent(getActivity(), AlarmClockEditActivity.class);
            intent.putExtra(DrugAlarmConstant.ALARM_CLOCK, alarmClock);
            intent.putExtra(DrugAlarmConstant.IS_NEW_ALARM_CLOCK, false);
//            startActivity(intent);
            // 开启编辑闹钟界面
            startActivityForResult(intent, REQUEST_ALARM_CLOCK_EDIT);
            // 启动移动进入效果动画
            getActivity().overridePendingTransition(R.anim.move_in_bottom, 0);
        }

        @Override
        public void onItemLongClick(View view, int position) {
            // 显示删除，完成按钮，隐藏修改按钮
            displayDeleteAccept();
        }

        @Override
        public void onItemDelete(Object obj) {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_new:
                // 不响应重复点击
                if (AlarmUtil.isFastDoubleClick()) {
                    return;
                }

               Intent intent = new Intent(getActivity(),
                    AlarmClockEditActivity.class);
                intent.putExtra(DrugAlarmConstant.IS_NEW_ALARM_CLOCK, true);
//                startActivity(intent);
                // 开启新建闹钟界面
                startActivityForResult(intent, REQUEST_ALARM_CLOCK_NEW);
                // 启动渐变放大效果动画
                getActivity().overridePendingTransition(R.anim.zoomin, 0);
                break;
            case R.id.action_edit:
                // 当列表内容为空时禁止响应编辑事件
                if (mAlarmClockList.size() == 0) {
                    return;
                }
                // 显示删除，完成按钮，隐藏修改按钮
                displayDeleteAccept();
                break;
            case R.id.action_accept:
                // 隐藏删除，完成按钮,显示修改按钮
                hideDeleteAccept();
                break;
        }

    }

    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    private AlarmClock mDeletedAlarmClock;

    /**
     * 显示删除，完成按钮，隐藏修改按钮
     */
    private void displayDeleteAccept() {
        mAdapter.setIsCanClick(false);
        mAdapter.displayDeleteButton(true);
        mAdapter.notifyDataSetChanged();
        mEditAction.setVisibility(View.GONE);
        mAcceptAction.setVisibility(View.VISIBLE);

        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            mSensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float xValue = Math.abs(event.values[0]);
                    float yValue = Math.abs(event.values[1]);
                    float zValue = Math.abs(event.values[2]);
                    // 认为用户摇动了手机，找回被删除的闹钟
                    if (xValue > 15 || yValue > 15 || zValue > 15) {
                        if (mDeletedAlarmClock != null) {
                            AlarmUtil.vibrate(getActivity());
                            //AlarmClockOperate.getInstance().saveAlarmClock(mDeletedAlarmClock);
                            addList(mDeletedAlarmClock);
                            mDeletedAlarmClock = null;
                            ToastUtil.showLongToast(getActivity(), getString(R.string.retrieve_alarm_clock_success));
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
        }
        mSensorManager.registerListener(mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }


    /**
     * 隐藏删除，完成按钮,显示修改按钮
     */
    private void hideDeleteAccept() {
        mAdapter.setIsCanClick(true);
        mAdapter.displayDeleteButton(false);
        mAdapter.notifyDataSetChanged();
        mAcceptAction.setVisibility(View.GONE);
        mEditAction.setVisibility(View.VISIBLE);

    }


    //闹钟编辑后返回结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        AlarmClock ac = data
                .getParcelableExtra(DrugAlarmConstant.ALARM_CLOCK);
        Log.d(TAG, "medicine size ="+ac.getMedicineList().size());
        switch (requestCode) {
            // 新建闹钟
            case REQUEST_ALARM_CLOCK_NEW:
                // 插入新闹钟数据
//                TabAlarmClockOperate.getInstance(getActivity()).insert(ac);
                List<AlarmClock> list = LocalRepository.getInstance(getActivity()).loadAlarmClocks();

                ac.setId(list.size());

                LocalRepository.getInstance(getActivity()).saveAlarmClock(ac);
                addList(ac);

                showAlarmExplain();
                break;
            // 修改闹钟
            case REQUEST_ALARM_CLOCK_EDIT:
                // 更新闹钟数据
//                TabAlarmClockOperate.getInstance(getActivity()).update(ac);
                LocalRepository.getInstance(getActivity()).updateAlarmClock(ac);
                updateList();
                break;

        }
    }

    private void showAlarmExplain() {
        if (isShow()) {
            new AlertDialogWrapper.Builder(getActivity())
                    .setTitle(getActivity().getString(R.string.warm_tips_title))
                    .setMessage(getActivity().getString(R.string.warm_tips_detail))
                    .setPositiveButton(R.string.roger, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton(R.string.no_tip, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences share = getActivity().getSharedPreferences(
                                    DrugAlarmConstant.EXTRA_SHARE, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = share.edit();
                            editor.putBoolean(DrugAlarmConstant.ALARM_CLOCK_EXPLAIN, false);
                            editor.apply();
                        }
                    })
                    .show();
        }
    }

    private boolean isShow() {
        SharedPreferences share = getActivity().getSharedPreferences(
                DrugAlarmConstant.EXTRA_SHARE, Activity.MODE_PRIVATE);
        return share.getBoolean(DrugAlarmConstant.ALARM_CLOCK_EXPLAIN, true);
    }

    @Subscribe
    public void onAlarmClockUpdate(AlarmClockUpdateEvent event) {
        updateList();
    }

    private boolean isShowingShakeExplain;

    @Subscribe
    public void OnAlarmClockDelete(AlarmClockDeleteEvent event) {
        deleteList(event);

        mDeletedAlarmClock = event.getAlarmClock();

/*        SharedPreferences share = getActivity().getSharedPreferences(
                DrugAlarmConstant.EXTRA_SHARE, Activity.MODE_PRIVATE);
        boolean isACDeleteFirstUse = share.getBoolean(DrugAlarmConstant.SHAKE_RETRIEVE_AC, true);
        if (isACDeleteFirstUse) {
            isShowingShakeExplain = true;
            Intent intent = new Intent(getActivity(), ShakeExplainActivity.class);
            startActivity(intent);
        }*/

    }


    private void addList(AlarmClock ac) {
        mAlarmClockList.clear();

        int id = ac.getId();
        int count = 0;
        int position = 0;
        List<AlarmClock> list = LocalRepository.getInstance(getActivity()).loadAlarmClocks();
        for (AlarmClock alarmClock : list) {
            mAlarmClockList.add(alarmClock);

            mAdapter.getItemCount();
            if (id == alarmClock.getId()) {
                position = count;
                if (alarmClock.isOnOff() == 1) {
                    AlarmUtil.startAlarmClock(getActivity(), alarmClock);
                }
            }
            count++;
        }

        checkIsEmpty(list);

        mAdapter.notifyItemInserted(position);
        mRecyclerView.scrollToPosition(position);
    }


    private void deleteList(AlarmClockDeleteEvent event) {
        mAlarmClockList.clear();

        int position = event.getPosition();
        List<AlarmClock> list = LocalRepository.getInstance(getActivity()).loadAlarmClocks();
        for (AlarmClock alarmClock : list) {
            mAlarmClockList.add(alarmClock);
        }
        // 列表为空时不显示删除，完成按钮
        if (mAlarmClockList.size() == 0) {
            mAcceptAction.setVisibility(View.GONE);
            mEditAction.setVisibility(View.VISIBLE);
            mAdapter.displayDeleteButton(false);
        }

        checkIsEmpty(list);

        mAdapter.notifyItemRemoved(position);
//        mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
    }


    private void updateList() {
        mAlarmClockList.clear();

        //mAdapter = new DrugAlarmAdapter(getActivity(),mAlarmClockList);

        List<AlarmClock> list = LocalRepository.getInstance(getActivity()).loadAlarmClocks();
        for (AlarmClock alarmClock : list) {
            mAlarmClockList.add(alarmClock);

            // 当闹钟为开时刷新开启闹钟
            if (alarmClock.isOnOff() == 1) {
                AlarmUtil.startAlarmClock(getActivity(), alarmClock);
            }
        }

        checkIsEmpty(list);




        mAdapter.notifyDataSetChanged();
    }

    private void checkIsEmpty(List<AlarmClock> list) {
        if (list.size() != 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);

            if (mSensorManager != null) {
                mSensorManager.unregisterListener(mSensorEventListener);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 当没有显示摇一摇找回删除的闹钟操作说明
        if (!isShowingShakeExplain) {
            // 隐藏删除，完成按钮,显示修改按钮
            hideDeleteAccept();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OttoAppConfig.getInstance().unregister(this);
    }



}
