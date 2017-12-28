package com.carefor.search;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.carefor.adapter.UserAdapter;
import com.carefor.data.entity.User;
import com.carefor.mainui.R;
import com.carefor.util.JPushTools;
import com.carefor.view.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by baige on 2017/12/26.
 */

public class SearchFragment extends Fragment implements SearchContract.View {

    private SearchContract.Presenter mPresenter;

    private Handler mHandler;

    private Toast mToast;

    private EditText mEtSearchWord;

    private Button mBtnSearch;

    private ListView mUsersListView;

    private UserAdapter mUserAdapter;

    private ScrollChildSwipeRefreshLayout mSwipeRefreshLayout;//下拉刷新组件

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mUserAdapter = new UserAdapter(new ArrayList<User>(0), mUserItemListener, "添加");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_search, container, false);
        initView(root);
        return root;
    }
    private void initView(View root) {
        mEtSearchWord = (EditText) root.findViewById(R.id.et_find_word);
        mBtnSearch = (Button) root.findViewById(R.id.btn_search);
        mUsersListView = (ListView) root.findViewById(R.id.listview);
        mUsersListView.setAdapter(mUserAdapter);
        mSwipeRefreshLayout = (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        mSwipeRefreshLayout.setScrollUpChild(mUsersListView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String word = mEtSearchWord.getText().toString();
                if(!JPushTools.isEmpty(word)){
                    mPresenter.search(word);
                    showTip("搜索"+word);
                }else{
                    setRefreshing(false);
                }
            }
        });
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = mEtSearchWord.getText().toString();
                if(!JPushTools.isEmpty(word)){
                    mPresenter.search(word);
                    showTip("搜索"+word);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void setPresenter(SearchContract.Presenter presenter) {
        mPresenter = presenter;
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
    public void showUsers(final List<User> users) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mUserAdapter.setList(users);
            }
        });
    }

    private UserAdapter.UserItemListener mUserItemListener = new UserAdapter.UserItemListener() {
        @Override
        public void onClickItem(User user) {
            showTip("点击"+user.getName());
            mPresenter.relate(user);
        }
    };

    @Override
    public void setRefreshing(final boolean refresh) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout.isRefreshing() && !refresh) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    mSwipeRefreshLayout.setRefreshing(refresh);
                }
            }
        });
    }
}
