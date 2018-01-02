package com.carefor.location;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.carefor.mainui.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/1/2.
 */


public class LocationFragment extends Fragment implements LocationContract.View {

    private LocationContract.Presenter mPresenter;

    private Toast mToast;

    private Handler mHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mHandler = new Handler();
    }

    public static LocationFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public void setPresenter(LocationContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_location, container, false);
        initView(root);
        return root;
    }

    private void initView(View root){

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
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







}

