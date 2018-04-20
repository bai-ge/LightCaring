package com.carefor.drugalarm;


import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.carefor.data.entity.Medicine;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.mainui.R;
import com.carefor.util.Tools;

import java.util.List;

import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class MedicineFragment extends Fragment implements View.OnClickListener {


    private final static String TAG = MedicineFragment.class.getCanonicalName();
    /**
     * 新建药品的requestCode
     */
    private static final int REQUEST_MEDICINE_NEW = 1;

    /**
     * 修改药品的requestCode
     */
    private static final int REQUEST_MEDICINE_EDIT = 2;

    /**
     * 药品列表
     */
    private RecyclerView mRecyclerView;

    /**
     * 保存药品信息的adapter
     */
    private MedicineAdapter mAdapter;

    /**
     * 操作栏编辑按钮
     */
    private ImageView mEditAction;

    /**
     * 操作栏编辑完成按钮
     */
    private ImageView mAcceptAction;

    /**
     * List内容为空时的视图
     */
    private RelativeLayout mEmptyView;

    private List<Medicine> mMedicineList;

    public MedicineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMedicineList = CacheRepository.getInstance().getMedicineList();
        mAdapter = new MedicineAdapter(getActivity(), mMedicineList);
        mAdapter.setOnItemClickListener(mItemClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_medicine, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        mEmptyView = (RelativeLayout) root
                .findViewById(R.id.layout_empty);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.list_view);
        mRecyclerView.setHasFixedSize(true);
        //设置布局管理器
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setLayoutManager(new ErrorCatchLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new ScaleInLeftAnimator(new OvershootInterpolator(1f)));
        mRecyclerView.getItemAnimator().setAddDuration(300);
        mRecyclerView.getItemAnimator().setRemoveDuration(300);
        mRecyclerView.getItemAnimator().setMoveDuration(300);
        mRecyclerView.getItemAnimator().setChangeDuration(300);
        mRecyclerView.setAdapter(mAdapter);

        // 操作栏新建按钮
        root.findViewById(R.id.action_new).setOnClickListener(this);

        // 编辑闹钟
        mEditAction = (ImageView) root.findViewById(R.id.action_edit);
        mEditAction.setOnClickListener(this);

        // 完成按钮
        mAcceptAction = (ImageView) root.findViewById(R.id.action_accept);
        mAcceptAction.setOnClickListener(this);

        checkIsEmpty(mMedicineList);
    }

    private void checkIsEmpty(List<Medicine> list) {
        if (list.size() != 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void showDailog(final Medicine medicine) {
        final AlertDialog ad = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_medicine, null);
        final EditText medicineName = (EditText) view.findViewById(R.id.et_medicine_name);
        final EditText dosage = (EditText) view.findViewById(R.id.et_dosage);

        medicineName.setText("");
        dosage.setText("");

        if (medicine != null) {
            if (!Tools.isEmpty(medicine.getName())) {
                medicineName.setText(medicine.getName());
            }
            if (!Tools.isEmpty(medicine.getDosage())) {
                dosage.setText(medicine.getDosage());
            }
        }
        ad.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // song.setName(editName.getText().toString());
                // song.setSinger(editSinger.getText().toString());
                Log.d(TAG, medicineName.getText() + ", " + dosage.getText());
                String name = medicineName.getText().toString();
                String dosageText = dosage.getText().toString();
                if (!Tools.isEmpty(name) && !Tools.isEmpty(dosageText)) {
                    if (medicine != null) {
                        medicine.setName(name);
                        medicine.setDosage(dosageText);
                    } else {
                        Medicine medicine1 = new Medicine(name, dosageText);
                        mMedicineList.add(medicine1);
                        checkIsEmpty(mMedicineList);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        ad.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        ad.setView(view);
        ad.setTitle("药品信息");
        ad.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_new:
                showDailog(null);
                break;
            case R.id.action_edit:
                // 当列表内容为空时禁止响应编辑事件
                if (mMedicineList.size() == 0) {
                    return;
                }
                // 显示删除，完成按钮，隐藏修改按钮
                displayDeleteAccept();
                break;
            case R.id.action_accept:
                hideDeleteAccept();
                break;
        }
    }

    /**
     * 显示删除，完成按钮，隐藏修改按钮
     */
    private void displayDeleteAccept() {
//        mAdapter.setIsCanClick(false);
        mAdapter.displayDeleteButton(true);
        mAdapter.notifyDataSetChanged();
        mEditAction.setVisibility(View.GONE);
        mAcceptAction.setVisibility(View.VISIBLE);
    }


    /**
     * 隐藏删除，完成按钮,显示修改按钮
     */
    private void hideDeleteAccept() {
//        mAdapter.setIsCanClick(true);
        mAdapter.displayDeleteButton(false);
        mAdapter.notifyDataSetChanged();
        mAcceptAction.setVisibility(View.GONE);
        mEditAction.setVisibility(View.VISIBLE);

    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            showDailog(mMedicineList.get(position));
        }

        @Override
        public void onItemLongClick(View view, int position) {

        }

        @Override
        public void onItemDelete(Object obj) {
            if (obj instanceof Medicine) {
                mMedicineList.remove(obj);
                mAdapter.notifyDataSetChanged();
                checkIsEmpty(mMedicineList);
            }
        }
    };


}
