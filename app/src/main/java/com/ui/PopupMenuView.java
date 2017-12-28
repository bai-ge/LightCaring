package com.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by felix on 16/11/18.
 * http://www.see-source.com/androidwidget/detail.html?wid=1205
 */
/*
此库主要实现了一个类似iOS中的UIMenuController控件的Popup控件。 主要控件如下：
PopLayout继承自FrameLayout，用于实现控件的气泡化。
OptionMenuView继承自LinearLayout，用于实现Menu控件。
PopupView继承自PopupWindow，用于实现控件的指定方位弹出效果。
PopupMenuView是上述三者的集合，实现了弹出气泡菜单的功能。

根据menu资源文件创建 PopupMenuView menuView = new PopupMenuView(this, R.menu.menu_pop, new MenuBuilder(context));
设置点击监听事件 menuView.setOnMenuClickListener(new OptionMenuView.OnOptionMenuClickListener() {     @Override     public boolean onOptionMenuClick(int position, OptionMenu menu) {         Toast.makeText(this, menu.getTitle(), Toast.LENGTH_SHORT).show();         return true;     } });
 显示在mButtom控件的周围 menuView.show(mButtom);
*/


public class PopupMenuView extends PopupView implements OptionMenuView.OnOptionMenuClickListener {

    private PopLayout mPopLayout;

    private OptionMenuView mOptionMenuView;

    private PopVerticalScrollView mVerticalScrollView;

    private PopHorizontalScrollView mHorizontalScrollView;

    private OptionMenuView.OnOptionMenuClickListener mOnOptionMenuClickListener;

    public PopupMenuView(Context context) {
        this(context, 0);
    }

    public PopupMenuView(Context context, int menuRes) {
        super(context);
        mOptionMenuView = new OptionMenuView(context, menuRes);
        mOptionMenuView.setOnOptionMenuClickListener(this);
        mPopLayout = new PopLayout(context);
        ViewGroup scrollView = getScrollView(mOptionMenuView.getOrientation());
        scrollView.addView(mOptionMenuView);
        mPopLayout.addView(scrollView);
        setContentView(mPopLayout);
    }

    public PopupMenuView(Context context, int menuRes, Menu menu) {
        this(context);
        inflate(menuRes, menu);
    }

    public void inflate(int menuRes, Menu menu) {
        mOptionMenuView.inflate(menuRes, menu);
        measureContentView();
    }

    public void setMenuItems(List<OptionMenu> optionMenus) {
        mOptionMenuView.setOptionMenus(optionMenus);
        measureContentView();
    }

    public List<OptionMenu> getMenuItems() {
        return mOptionMenuView.getOptionMenus();
    }

    public void setOrientation(int orientation) {
        mOptionMenuView.setOrientation(orientation);
        measureContentView();
    }

    public int getOrientation() {
        return mOptionMenuView.getOrientation();
    }

    // 暂时暴露出
    @Deprecated
    public PopLayout getPopLayout() {
        return mPopLayout;
    }

    // 暂时暴露出
    @Deprecated
    public OptionMenuView getMenuView() {
        return mOptionMenuView;
    }

    public void setOnMenuClickListener(OptionMenuView.OnOptionMenuClickListener listener) {
        mOnOptionMenuClickListener = listener;
    }

    @Override
    public void show(View anchor, Rect frame, Point origin) {
        mOptionMenuView.notifyMenusChange();
        super.show(anchor, frame, origin);
    }

    @Override
    public void showAtTop(View anchor, Point origin, int xOff, int yOff) {
        mPopLayout.setSiteMode(PopLayout.SITE_BOTTOM);
        mPopLayout.setOffset(origin.x - xOff);
        super.showAtTop(anchor, origin, xOff, yOff);
    }

    @Override
    public void showAtLeft(View anchor, Point origin, int xOff, int yOff) {
        mPopLayout.setSiteMode(PopLayout.SITE_RIGHT);
        mPopLayout.setOffset(-origin.y - yOff);
        super.showAtLeft(anchor, origin, xOff, yOff);
    }

    @Override
    public void showAtRight(View anchor, Point origin, int xOff, int yOff) {
        mPopLayout.setSiteMode(PopLayout.SITE_LEFT);
        mPopLayout.setOffset(-origin.y - yOff);
        super.showAtRight(anchor, origin, xOff, yOff);
    }

    @Override
    public void showAtBottom(View anchor, Point origin, int xOff, int yOff) {
        mPopLayout.setSiteMode(PopLayout.SITE_TOP);
        mPopLayout.setOffset(origin.x - xOff);
        super.showAtBottom(anchor, origin, xOff, yOff);
    }

    @Override
    public boolean onOptionMenuClick(int position, OptionMenu menu) {
        if (mOnOptionMenuClickListener != null) {
            if (mOnOptionMenuClickListener.onOptionMenuClick(position, menu)) {
                dismiss();
                return true;
            }
        }
        return false;
    }

    private ViewGroup getScrollView(int orientation) {
        if (orientation == LinearLayout.HORIZONTAL) {
            if (mHorizontalScrollView == null) {
                mHorizontalScrollView = new PopHorizontalScrollView(getContext());
                mHorizontalScrollView.setHorizontalScrollBarEnabled(false);
                mHorizontalScrollView.setVerticalScrollBarEnabled(false);
            }
            return mHorizontalScrollView;
        } else {
            if (mVerticalScrollView == null) {
                mVerticalScrollView = new PopVerticalScrollView(getContext());
                mVerticalScrollView.setHorizontalScrollBarEnabled(false);
                mVerticalScrollView.setVerticalScrollBarEnabled(false);
            }
            return mVerticalScrollView;
        }
    }
}
