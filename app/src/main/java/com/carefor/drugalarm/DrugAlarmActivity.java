package com.carefor.drugalarm;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carefor.mainui.R;

import java.util.ArrayList;
import java.util.List;


public class DrugAlarmActivity extends AppCompatActivity implements OnClickListener {

    //吃药提醒控件
    private TextView tv_drug_alarm;

    //药品库控件
    private TextView tv_medicine;

    //Tab页面集合
    private List<Fragment> mFragmentList;

    //当前Tab的index
    private int mCurrentIndex = -1;

    //用于对Fragment进行管理
    private FragmentManager mFm;

    //Tab未选中文字颜色
    private int mUnSelectColor;

    //Tab选中时文字颜色
    private int mSelectColor;

    //滑动菜单视图
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drugalarm);

        // 设置主题壁纸
        setThemeWallpaper();

        mFm = getSupportFragmentManager();
        // Tab选中文字颜色
        mSelectColor = getResources().getColor(R.color.white);
        // Tab未选中文字颜色
        mUnSelectColor = getResources().getColor(R.color.white_trans50);
        // 初始化布局元素
        initViews();
        // 启动程序后选中Tab为闹钟
        setTabSelection(0);
    }

    /**
     * 设置背景颜色
     */
    private void setThemeWallpaper() {
        ViewGroup vg = (ViewGroup) findViewById(R.id.activity_drugalarm);
        vg.setBackgroundResource(R.color.colorPrimaryDark);
    }

    /**
     * 获取布局元素，并设置事件
     */
    private void initViews() {
        // 取得Tab布局

        // 吃药提醒Tab界面布局
        ViewGroup tab_durg_alarm = (ViewGroup) findViewById(R.id.tab_durg_alarm);
        // 药品盒Tab界面布局
        ViewGroup tab_medicine = (ViewGroup) findViewById(R.id.tab_medicine);

        // 取得Tab控件
        tv_drug_alarm = (TextView) findViewById(R.id.tv_durg_alarm);
        tv_medicine = (TextView) findViewById(R.id.tv_medicine);

        // 设置Tab点击事件
        tab_durg_alarm.setOnClickListener(this);
        tab_medicine.setOnClickListener(this);

        // 设置Tab页面集合
        mFragmentList = new ArrayList<>();
        // 展示吃药提醒的Fragment
        DrugAlarmFragment mDrugAlarmFragment = new DrugAlarmFragment();
        // 展示药品盒的Fragment
        MedicineFragment mMedicineFragment = new MedicineFragment();

        mFragmentList.add(mDrugAlarmFragment);
        mFragmentList.add(mMedicineFragment);


        // 设置ViewPager
        mViewPager = (ViewPager) findViewById(R.id.fragment_container);
        mViewPager.setAdapter(new MyFragmentPagerAdapter(mFm));
        // 设置一边加载的page数
        mViewPager.setOffscreenPageLimit(3);
        // TODO：切换渐变
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int index) {
                setTabSelection(index);

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

    }

    /**
     * ViewPager适配器
     */
    class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

    }


    @Override
    public void onClick(View v) {
        // 判断选中的Tab
        switch (v.getId()) {
            // 当选中闹钟Tab时
            case R.id.tab_durg_alarm:
                // 切换闹钟视图
                setTabSelection(0);
                break;
            // 当选中天气Tab时
            case R.id.tab_medicine:
                // 切换天气视图
                setTabSelection(1);
                break;
            default:
                break;
        }
    }

    /**
     * 设置选中的Tab
     *
     * @param index 每个tab对应的下标。0表示闹钟，1表示天气，2表示计时，3表示更多。
     */
    private void setTabSelection(int index) {
        // 当重复选中相同Tab时不进行任何处理
        if (mCurrentIndex == index) {
            return;
        }

        // 设置当前Tab的Index值为传入的Index值
        mCurrentIndex = index;
        // 改变ViewPager视图
        mViewPager.setCurrentItem(index, false);
        // 清除掉上次的选中状态
        clearSelection();
        // 判断传入的Index
        switch (index) {
            // 吃药提醒
            case 0:
                // 改变吃药提醒控件的图片和文字颜色
                setTextView(R.drawable.ic_drug_alarm_select, tv_drug_alarm, mSelectColor);
                break;
            // 药品盒
            case 1:
                // 改变药品盒控件的图片和文字颜色
                setTextView(R.drawable.ic_medicine_select, tv_medicine, mSelectColor);
                break;
        }

    }

    /**
     * 清除掉所有的选中状态。
     */
    private void clearSelection() {
        // 设置吃药提醒Tab为未选中状态
        setTextView(R.drawable.ic_drug_alarm_unselect, tv_drug_alarm, mUnSelectColor);
        // 设置药品盒Tab为未选中状态
        setTextView(R.drawable.ic_medicine_unselect, tv_medicine, mUnSelectColor);

    }

    /**
     * 设置Tab布局
     *
     * @param iconId   Tab图标
     * @param textView Tab文字
     * @param color    Tab文字颜色
     */
    private void setTextView(int iconId, TextView textView, int color) {
        @SuppressWarnings("deprecation") Drawable drawable = getResources().getDrawable(iconId);
        if (drawable != null) {
            drawable.setBounds(0, 0, 100, 100);
            // 设置图标
            textView.setCompoundDrawables(null, drawable, null, null);
        }
        // 设置文字颜色
        textView.setTextColor(color);
    }

    @Override
    protected void onDestroy() {
       // LogUtil.d(LOG_TAG, "onDestroy()");
//        Process.killProcess(Process.myPid());
        super.onDestroy();
    }


}
