package com.carefor.adapter;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.carefor.data.entity.Housekeeping;
import com.carefor.mainui.R;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/3/13.
 */

public class HousekeepingAdapter extends BaseAdapter {

    private List<Housekeeping> mList;

    private HousekeepingItemListener mItemListener;

    private Handler mHandler;

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public HousekeepingAdapter(List<Housekeeping> housekeepingList, HousekeepingItemListener listener) {
        mItemListener = checkNotNull(listener);
        mHandler = new Handler();
        setList(housekeepingList);
    }

    public void setList(List<Housekeeping> housekeepingList){
        mList = checkNotNull(housekeepingList);
        mHandler.removeCallbacks(mNotifyRunnable);
        mHandler.postDelayed(mNotifyRunnable, 500);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Housekeeping getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HousekeepingAdapter.ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.listview_service, parent, false);
            holder = new HousekeepingAdapter.ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.service_title);
            holder.contentText = (TextView) convertView.findViewById(R.id.service_content);
            holder.name = (TextView) convertView.findViewById(R.id.servant_name);
            holder.phone = (TextView) convertView.findViewById(R.id.servant_phone);
            holder.area = (TextView) convertView.findViewById(R.id.service_area);
            holder.price = (TextView) convertView.findViewById(R.id.service_price);
            holder.img = (ImageView) convertView.findViewById(R.id.service_img);
            convertView.setTag(holder);
        } else {
            holder = (HousekeepingAdapter.ViewHolder) convertView.getTag();
        }
        //导入监护人
        final Housekeeping item =  getItem(position);
        if (item != null) {
            //TODO 显示照片 图片用Bitmap 保存，方便保存，传输
            // holder..setImageDrawable(item.getMember_photo());
            holder.title.setText(item.getServiceTitle());
            holder.contentText.setText(item.getContentText());
            holder.name.setText(item.getServantName());
            holder.phone.setText(item.getPhone());
            holder.area.setText(item.getServiceArea());
            holder.price.setText(item.getPrice());
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemListener.onClickItem(item);
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView title;
        TextView contentText;
        TextView name;
        TextView phone;
        TextView area;
        TextView price;
        ImageView img;
    }

    //TIPS 使用接口的形式比较灵活，当Item 上面多添加某个功能按钮时，只需调用接口即可
    public interface HousekeepingItemListener{
        void onClickItem(Housekeeping housekeeping);
    }
}
