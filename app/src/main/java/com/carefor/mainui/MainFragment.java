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
import android.widget.TextView;
import android.widget.Toast;

import com.carefor.data.entity.User;
import com.carefor.dropdetection.DropDetectionActivity;
import com.carefor.location.LocationActivity;
import com.carefor.telephone.PhoneActivity;
import com.carefor.view.ItemCardView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class MainFragment extends Fragment implements MainContract.View {

    private MainContract.Presenter mPresenter;

    private Handler mHandler;

    private Toast mToast;

    private TextView mEmName;

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
        mEmName = (TextView) root.findViewById(R.id.em_name);
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
            }
        });
        root.findViewById(R.id.btn_remind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTip(((ItemCardView)v).getText().toString());
            }
        });
        root.findViewById(R.id.btn_sos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTip(((ItemCardView)v).getText().toString());
            }
        });

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
}
