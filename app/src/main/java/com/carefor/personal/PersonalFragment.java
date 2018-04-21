package com.carefor.personal;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.carefor.data.entity.User;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.mainui.R;
import com.carefor.util.GlideImageLoader;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

import java.util.ArrayList;

/**
 * Created by Ryoko on 2018/4/14.
 */

public class PersonalFragment extends Fragment {

    private User user;

    //头像
    private ImageView headImg;
    private ImagePicker imagePicker;
    ArrayList<ImageItem> images = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_personal, container, false);


        initHeadImage(view);

        initName();
        initAge();
        initSex();
        initSex();


        return view;
    }

    private void setImageView(){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 100) {
                images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                int size = headImg.getWidth();
                imagePicker.getImageLoader().displayImage(getActivity(), images.get(0).path, headImg, size/4, size/4);
                user = CacheRepository.getInstance().who();
                user.setImgPath(images.get(0).path);
                CacheRepository.getInstance().setYouself(user);
            } else {
                //Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {//该函数在界面显示完毕后被调用，用于一开始加载所有数据
        super.onResume();

        updateView();

    }

    @Override
    public void onPause(){
        super.onPause();
        updateView();
    }

    private void updateView(){
        int size = headImg.getWidth();
        user = CacheRepository.getInstance().who();
        if (user.getImgPath() != null)

            imagePicker.getImageLoader().displayImage(getActivity(), user.getImgPath(), headImg, size/4, size/4);

    }


    //头像
    private void initHeadImage(View view){

        headImg = (ImageView) view.findViewById(R.id.user_photo);

        ViewGroup headImage =(ViewGroup) view.findViewById(R.id.head_image);

        headImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                imagePicker.setMultiMode(false);
                imagePicker.setStyle(CropImageView.Style.CIRCLE);

                int size = headImg.getWidth();
                Integer radius = size/2;
                radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, getResources().getDisplayMetrics());
                imagePicker.setFocusWidth(radius * 2);
                imagePicker.setFocusHeight(radius * 2);
                imagePicker.setOutPutX(radius * 2);
                imagePicker.setOutPutY(radius * 2);

                Intent intent = new Intent(getActivity(),  ImageGridActivity.class);
                intent.putExtra(ImageGridActivity.EXTRAS_IMAGES,images);
                //ImagePicker.getInstance().setSelectedImages(images);
                startActivityForResult(intent, 100);

            }
        });



    }

    //姓名
    private void initName(){

    }

    //年龄
    private void initAge(){

    }

    //性别
    private void initSex(){

    }

    //手机号码
    private void initPhone(){

    }





}
