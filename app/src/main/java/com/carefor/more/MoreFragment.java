package com.carefor.more;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.carefor.mainui.R;
import com.carefor.membermanage.MemberManageActivity;
import com.carefor.util.ActivityUtils;

/**
 * Created by Ryoko on 2018/4/14.
 */

public class MoreFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.frag_more, container, false);


        // 更多布局
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.viewstub_more);
        viewStub.inflate();

        // 主题
        view.findViewById(R.id.member).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ActivityUtils.startActivity(getActivity(), MemberManageActivity.class);
            }
        });


        return view;
    }




}
