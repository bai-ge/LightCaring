package com.carefor.membermanage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.carefor.BaseActivity;
import com.carefor.adapter.AddMemberAdapter;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.mainui.R;

import java.util.ArrayList;
import java.util.List;

public class AddMemberActivity extends BaseActivity {


    //TODO 已经被搜索SearchActivity 代替，或许还会改回来，打算让SearchActivity 充当应用中的搜索器，可以添加、查询、搜索家政服务等
    private AddMemberAdapter mAdapter;
    private Context mContext;

    private ListView listview;

    private Toolbar mToolbar;

    private Button mSearch;

    private EditText mPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("添加成员");
        //为activity窗口设置活动栏
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        //设置返回图标
        actionBar.setHomeAsUpIndicator(0);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mPhone = (EditText) findViewById(R.id.et_find_phone);
        mSearch = (Button)findViewById(R.id.btn_search);
        mContext = this;

        mSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                listview = (ListView) findViewById(R.id.listview);
                List<MemberInfo> mlist = new ArrayList<>();

                //a
                MemberInfo a = new MemberInfo();
                Drawable drawable = getResources().getDrawable(R.drawable.head_img);
                a.setMember_photo(drawable);
                a.setMember_name("111");
                a.setMember_phone("18378397030");
                mlist.add(a);

                //a
                MemberInfo b = new MemberInfo();
                b.setMember_photo(drawable);
                b.setMember_name("222");
                b.setMember_phone("18378397030");
                mlist.add(b);

                //c
                MemberInfo c = new MemberInfo();
                c.setMember_photo(drawable);
                c.setMember_name("333");
                c.setMember_phone("18378397030");
                mlist.add(c);

                CacheRepository cacheRepository = CacheRepository.getInstance();
                cacheRepository.setmMemberInfoList(mlist);

                mAdapter = new AddMemberAdapter(mContext, mlist);
                listview.setAdapter(mAdapter);

            }
        });




    }

}
