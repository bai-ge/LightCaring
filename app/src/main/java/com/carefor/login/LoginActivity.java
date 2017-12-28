package com.carefor.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.carefor.BaseActivity;
import com.carefor.data.source.Repository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.util.ActivityUtils;

/**
 * Created by baige on 2017/12/22.
 */

public class LoginActivity extends BaseActivity {
    private Toast mToast;


    private LoginPresenter.OnPresenterListener mOnPresenterListener = new LoginPresenter.OnPresenterListener() {
        @Override
        public void startActivity(Class content) {
            Intent intent = new Intent(LoginActivity.this, content);
            LoginActivity.this.startActivity(intent);
        }

        @Override
        public void closeActivity() {
            LoginActivity.this.finish();
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(loginFragment == null){
            loginFragment = LoginFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), loginFragment, R.id.contentFrame);
        }
        Repository repository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));
        LoginPresenter loginPresenter = new LoginPresenter(repository, loginFragment);
        loginPresenter.setOnPresenterListener(mOnPresenterListener);
        Log.d("guide_page", "启动登录界面");
    }

    private long clickBackTime = 0;
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK){
            long now = System.currentTimeMillis();
            if(now - clickBackTime < 500){
               finishAll();
            }else{
                clickBackTime = now;
                showTip("再按一次返回键退出");
            }
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
    private void showTip(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }


}
