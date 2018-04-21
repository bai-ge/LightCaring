package com.carefor.drugalarm;

import android.view.View;



/**
 * Created by Ryoko on 2018/3/7.
 */

public interface OnItemClickListener {
    void onItemClick(View view, int position);

    void onItemLongClick(View view, int position);

    void onItemDelete(Object obj);
}
