package com.carefor.adapter;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.carefor.data.entity.User;
import com.carefor.mainui.R;
import com.carefor.view.CircleImageView;
import com.carefor.view.RippleView;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserAdapter extends BaseAdapter {

    private List<User> mUsers;
    private UserItemListener mItemListener;
    private String mBtnText;

    /*避免频繁刷新列表，导致被忽略刷新*/
    private Handler mHandler;
    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public UserAdapter(List<User> users, UserItemListener listener, String btnText) {
        mItemListener = checkNotNull(listener);
        mBtnText = checkNotNull(btnText);
        mHandler = new Handler();
        setList(users);
    }


    public void clear(){
        for (int i = 0; i < mUsers.size() ; i++) {
            mUsers.clear();
            mHandler.removeCallbacks(mNotifyRunnable);
            mHandler.postDelayed(mNotifyRunnable, 500);
        }
    }
    public void add(User user){
        if(user != null && !mUsers.contains(user)){
            mUsers.add(user);
            mHandler.removeCallbacks(mNotifyRunnable);
            mHandler.postDelayed(mNotifyRunnable, 500);
        }
    }
    public void addUsers(List<User> list){
        if(list != null && list.size() > 0){
            for (int i = 0; i < list.size() ; i++) {
                if(!mUsers.contains(list.get(i))){
                    mUsers.add(list.get(i));
                }
            }
            mHandler.removeCallbacks(mNotifyRunnable);
            mHandler.postDelayed(mNotifyRunnable, 500);
        }
    }
    public void setList(List<User> users){
        mUsers = checkNotNull(users);
        mHandler.removeCallbacks(mNotifyRunnable);
        mHandler.postDelayed(mNotifyRunnable, 500);
    }
    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public User getItem(int position) {
        return mUsers.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.listview_user, parent, false);
            holder = new ViewHolder();
            holder.imgView = (CircleImageView) convertView.findViewById(R.id.user_img);
            holder.nameView = (TextView) convertView.findViewById(R.id.user_name);
            holder.phoneView = (TextView) convertView.findViewById(R.id.user_phone);
            holder.btnView = (RippleView) convertView.findViewById(R.id.btn_item);
            holder.btnTextView = (TextView) convertView.findViewById(R.id.btn_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //导入监护人
        final User  item =  getItem(position);
        if (item != null) {
            //TODO 显示照片 图片用Bitmap 保存，方便保存，传输
           // holder..setImageDrawable(item.getMember_photo());
            holder.nameView.setText(item.getName());
            holder.phoneView.setText(item.getTel());
            holder.btnTextView.setText(mBtnText);
        }
        //点击操作
        holder.btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*设为当前主页监护人*/
                if(mItemListener != null){
                    mItemListener.onClickItem(item);
                }
            }
        });


        return convertView;
    }


    class ViewHolder {
        CircleImageView imgView;
        TextView nameView;
        TextView phoneView;
        RippleView btnView;
        TextView btnTextView;
    }
    //TIPS 使用接口的形式比较灵活，当Item 上面多添加某个功能按钮时，只需调用接口即可
    public interface UserItemListener{
        void onClickItem(User user);
    }

}
