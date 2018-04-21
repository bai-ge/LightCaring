package com.carefor.mainui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.carefor.data.entity.User;
import com.carefor.dropdetection.DropDetectionActivity;
import com.carefor.drugalarm.DrugAlarmActivity;
import com.carefor.housekeeping.HousekeepingActivity;
import com.carefor.location.LocationActivity;
import com.carefor.more.MoreActivity;
import com.carefor.telephone.PhoneActivity;
import com.carefor.util.Loggerx;
import com.carefor.view.ItemCardView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class MainFragment extends Fragment implements MainContract.View {

    private final static String TAG = MainFragment.class.getCanonicalName();

    private MainContract.Presenter mPresenter;

    private Handler mHandler;

    private Toast mToast;

    private TextView mEmName;

    private ImageButton mBtnLeft;

    private ImageButton mBtnRight;

    private LinearLayout mInformLayout;

    private TextView mInformText;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onResume() {//该函数在界面显示完毕后被调用，用于一开始加载所有数据
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
    }
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_guardian, container, false);
        initView(root);
        return root;
    }
    private void initView(View root){
        //TODO

        mInformLayout = (LinearLayout) root.findViewById(R.id.inform_layout);
        mInformText = (TextView) root.findViewById(R.id.inform_text);
        mEmName = (TextView) root.findViewById(R.id.em_name);
        mBtnLeft = (ImageButton) root.findViewById(R.id.btn_left);
        mBtnRight = (ImageButton) root.findViewById(R.id.btn_right);

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.preUser();
            }
        });

        mBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.nextUser();
            }
        });
        root.findViewById(R.id.btn_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTip(((ItemCardView)v).getText().toString());
                mPresenter.skipToActivity(PhoneActivity.class);
                mPresenter.callTo();
            }
        });
        root.findViewById(R.id.btn_drop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showTip(((ItemCardView)v).getText().toString());
//                Intent intent = new Intent(getActivity(), DropDetectionActivity.class);
//                startActivity(intent);
                mPresenter.skipToActivity(DropDetectionActivity.class);
            }
        });
        root.findViewById(R.id.btn_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTip(((ItemCardView)v).getText().toString());
                mPresenter.skipToActivity(LocationActivity.class);
            }
        });
        root.findViewById(R.id.btn_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showTip(((ItemCardView)v).getText().toString());
                mPresenter.skipToActivity(HousekeepingActivity.class);
            }
        });
        //吃药提醒
        root.findViewById(R.id.btn_remind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTip(((ItemCardView)v).getText().toString());
                mPresenter.skipToActivity(DrugAlarmActivity.class);
            }
        });
        root.findViewById(R.id.btn_sos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTip(((ItemCardView)v).getText().toString());
                Loggerx.d(TAG, ((ItemCardView)v).getText().toString());
                mPresenter.skipToActivity(MoreActivity.class);
            }
        });

        hideInform();

//        root.findViewById(R.id.btn_jpush).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showTip(((Button)v).getText().toString());
//                mPresenter.skipToActivity(com.example.jpushdemo.MainActivity.class);
//            }
//        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tool_bar_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_back:
                showTip("点击"+item.getTitle());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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
    public void showEmUser(final User user) {
        checkNotNull(user);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(user.getName() != null){
                    mEmName.setText(user.getName());
                }

            }
        });
    }

    @Override
    public void setEnableArrow(boolean isEnable) {
        mBtnLeft.setEnabled(isEnable);
        mBtnRight.setEnabled(isEnable);
    }

    private Runnable mHideInformRunnable = new Runnable() {
        @Override
        public void run() {
            if(mInformLayout != null){
                mInformLayout.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    public void showInform(final String text) {
        mHandler.removeCallbacks(mHideInformRunnable);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mInformText.setText(text);
                if(mInformLayout != null){
                    mInformLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void showInform(String text, long time) {
        showInform(text);
        if(time <= 0){
            time = 8000;
        }
        mHandler.postDelayed(mHideInformRunnable, time);
    }

    @Override
    public void hideInform() {
        mHandler.post(mHideInformRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        //mPresenter.stop();
    }
}
