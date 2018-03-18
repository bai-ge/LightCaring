package com.carefor.drugalarm;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.carefor.util.Loggerx;

/**
 * Created by Ryoko on 2018/3/17.
 */

public class ErrorCatchLinearLayoutManager extends LinearLayoutManager {
    private static final String LOG_TAG = "ErrorCatchLinearLayoutManager";

    public ErrorCatchLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Loggerx.e(LOG_TAG, "RecyclerView 错误：" + e.toString());
        }
    }
}
