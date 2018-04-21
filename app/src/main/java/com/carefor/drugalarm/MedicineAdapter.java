package com.carefor.drugalarm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.carefor.data.entity.Medicine;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.mainui.R;

import java.util.List;

/**
 * Created by Ryoko on 2018/3/7.
 */

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MyViewHolder> {

    private final Context mContext;

    /**
     * 是否显示删除按钮
     */
    private boolean mIsDisplayDeleteBtn = false;

    /**
     * 白色
     */
    private int mWhite;

    /**
     * 淡灰色
     */
    private int mWhiteTrans;

    private List<Medicine> mList;


    public MedicineAdapter(Context context, List<Medicine> objects) {
        mContext = context;
        mList = objects;
        mWhite = mContext.getResources().getColor(android.R.color.white);
        mWhiteTrans = mContext.getResources().getColor(R.color.white_trans30);
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.lv_medicine, parent, false));
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void updateList(List<Medicine> objects){
        mList = objects;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, final int position) {
        final Medicine medicine = mList.get(position);

        if (mOnItemClickListener != null) {
            viewHolder.rippleView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(viewHolder.itemView, viewHolder.getLayoutPosition());
                }
            });

            viewHolder.rippleView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                        mOnItemClickListener.onItemLongClick(viewHolder.itemView, viewHolder.getLayoutPosition());
                        return false;
                }
            });
        }


        // 显示删除按钮
        if (mIsDisplayDeleteBtn) {
            viewHolder.deleteBtn.setVisibility(View.VISIBLE);
            viewHolder.deleteBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(mOnItemClickListener != null){
                        mOnItemClickListener.onItemDelete(medicine);
                    }
                }
            });
        } else {
            viewHolder.deleteBtn.setVisibility(View.GONE);
        }


        viewHolder.medicineName.setText(medicine.getName());

        viewHolder.dosage.setText(medicine.getDosage());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    /**
     * 保存控件实例
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        MaterialRippleLayout rippleView;
        // 时间
        TextView medicineName;
        // 用量
        TextView dosage;
        // 删除
        ImageView deleteBtn;

        public MyViewHolder(View itemView) {
            super(itemView);
            rippleView = (MaterialRippleLayout) itemView.findViewById(R.id.ripple_view);
            medicineName = (TextView) itemView.findViewById(R.id.tv_medicine_name);
            dosage = (TextView) itemView.findViewById(R.id.tv_dosage);
            deleteBtn = (ImageView) itemView.findViewById(R.id.btn_list_delete);
        }
    }

    /**
     *
     *
     * @param isDisplayDeleteBtn 是否显示删除按钮
     */
    public void displayDeleteButton(boolean isDisplayDeleteBtn) {
        mIsDisplayDeleteBtn = isDisplayDeleteBtn;
    }
}
