package com.carefor.housekeeping;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.carefor.adapter.HousekeepingAdapter;
import com.carefor.data.entity.Housekeeping;
import com.carefor.mainui.R;
import com.carefor.util.Tools;
import com.carefor.view.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baige on 2018/3/13.
 */

public class HousekeepingFragment extends Fragment implements HousekeepingContract.View {

    private HousekeepingContract.Presenter mPresenter;

    private Handler mHandler;

    private Toast mToast;

    private EditText mEtSearchWord;

    private Button mBtnSearch;

    private HousekeepingAdapter mHousekeepingAdapter;

    private ListView mHousekeepingListView;

    private ScrollChildSwipeRefreshLayout mSwipeRefreshLayout;//下拉刷新组件



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mHousekeepingAdapter = new HousekeepingAdapter(new ArrayList<Housekeeping>(0), mItemListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_housekeeping, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        mEtSearchWord = (EditText) root.findViewById(R.id.et_find_word);
        mBtnSearch = (Button) root.findViewById(R.id.btn_search);
        mHousekeepingListView = (ListView) root.findViewById(R.id.listview);
        mHousekeepingListView.setAdapter(mHousekeepingAdapter);

        mSwipeRefreshLayout = (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        mSwipeRefreshLayout.setScrollUpChild(mHousekeepingListView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String word = mEtSearchWord.getText().toString();
                if(!Tools.isEmpty(word)){
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
                if(!Tools.isEmpty(word)){
                    setRefreshing(true);
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

    @Override
    public void setPresenter(HousekeepingContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public static HousekeepingFragment newInstance() {
        return new HousekeepingFragment();
    }



    @Override
    public void showHousekeepings(final List<Housekeeping> housekeepingList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mHousekeepingAdapter.setList(housekeepingList);
            }
        });
    }

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

    private HousekeepingAdapter.HousekeepingItemListener mItemListener = new HousekeepingAdapter.HousekeepingItemListener() {
        @Override
        public void onClickItem(Housekeeping housekeeping) {
            showTip(housekeeping.getServiceTitle());
        }
    };
}
