package com.carefor.telephone;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.carefor.connect.Connector;
import com.carefor.mainui.R;
import com.carefor.view.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by baige on 2017/10/29.
 */

public class PhoneFragment extends Fragment implements PhoneContract.View {
    private PhoneContract.Presenter mPresenter;
    private Handler mHandler;
    private Toast mToast;

    private TextView mTextDelayTime;
    private CircleImageView mImg;
    private TextView mTextUserName;
    private TextView mTextAddress;
    private TextView mTextStatus;
    private EditText mEditLog;
    private Button mBtnHangUp;
    private Button mBtnPickUp;
    private SimpleDateFormat mSimpleDateFormat;

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_phone, container, false);
        mTextDelayTime = (TextView) root.findViewById(R.id.network_speed);
        mImg = (CircleImageView) root.findViewById(R.id.user_img);
        mTextUserName = (TextView) root.findViewById(R.id.user_name);
        mTextAddress = (TextView) root.findViewById(R.id.address);
        mTextStatus = (TextView) root.findViewById(R.id.status);
        mEditLog = (EditText) root.findViewById(R.id.log);
        mBtnHangUp = (Button) root.findViewById(R.id.btn_hang_up);
        mBtnPickUp = (Button) root.findViewById(R.id.btn_pick_up);
        mBtnHangUp.setOnClickListener(new View.OnClickListener() {//挂断电话
            @Override
            public void onClick(View v) {
                mPresenter.onHangUp();
            }
        });
        mBtnPickUp.setOnClickListener(new View.OnClickListener() {//接听电话
            @Override
            public void onClick(View v) {
                mBtnPickUp.setVisibility(View.GONE);
                mPresenter.onPickUp();
            }
        });
        return root;
    }

    @Override
    public void setPresenter(PhoneContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public static PhoneFragment newInstance() {
        return new PhoneFragment();
    }





    @Override
    public void showAddress(final String address) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextAddress.setText(address);
            }
        });
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
    public void showStatus(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextStatus.setText(text);
            }
        });
    }

    @Override
    public void showName(final String name) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextUserName.setText(name);
            }
        });
    }

    @Override
    public void showLog(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEditLog.append(mSimpleDateFormat.format(new Date()) + " " + text + "\n");
            }
        });
    }

    @Override
    public void showDelayTime(final long delay) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
              mTextDelayTime.setText(String.valueOf(delay)+"ms");
            }
        });
    }

    @Override
    public void clearLog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEditLog.setText("");
            }
        });
    }

    @Override
    public void hidePickUpBtn() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBtnPickUp.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void close() {
        Connector.getInstance().unRegistConnectorListener("presenter");
        getActivity().finish();
    }
}
