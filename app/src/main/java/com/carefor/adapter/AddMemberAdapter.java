package com.carefor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.carefor.mainui.R;
import com.carefor.membermanage.MemberInfo;
import com.carefor.view.RippleView;

import java.util.List;

/**
 * Created by Ryoko on 2017/12/25.
 */

public class AddMemberAdapter extends BaseAdapter {
    public List<MemberInfo> mlistMemberInfo;
    LayoutInflater infater = null;
    private Context mContext;


    public AddMemberAdapter(Context context, List<MemberInfo> memberlist) {
        infater = LayoutInflater.from(context);
        mContext = context;

        this.mlistMemberInfo = memberlist;

    }

    @Override
    public int getCount() {
        return mlistMemberInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mlistMemberInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AddMemberAdapter.ViewHolder holder = null;
        if (convertView == null) {
            convertView = infater.inflate(R.layout.listview_add_member, null);

            holder = new AddMemberAdapter.ViewHolder();
            holder.member_photo = (ImageView) convertView.findViewById(R.id.add_member_photo);
            holder.member_name = (TextView) convertView.findViewById(R.id.add_member_name);
            holder.member_phone = (TextView) convertView.findViewById(R.id.add_member_phone);

            holder.ripleMemberSeleted = (RippleView) convertView.findViewById(R.id.riple_add_memberseleted);
            convertView.setTag(holder);
        } else {
            holder = (AddMemberAdapter.ViewHolder) convertView.getTag();
        }
        //导入监护人
        final MemberInfo item = (MemberInfo) getItem(position);
        if (item != null) {

            holder.member_photo.setImageDrawable(item.getMember_photo());
            holder.member_name.setText(item.getMember_name());
            holder.member_phone.setText(item.getMember_phone());
        }
        //点击操作
        holder.ripleMemberSeleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*添加监护关系*/
            }
        });


        return convertView;
    }


    class ViewHolder {
        ImageView member_photo;
        TextView member_name;
        TextView member_phone;
        RippleView ripleMemberSeleted;

    }
}
