package com.carefor.search;

import android.util.Log;

import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.User;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/26.
 */

public class SearchPresenter implements SearchContract.Presenter {

    private Repository mRepository;
    private SearchFragment mFragment;

    public SearchPresenter(Repository instance, SearchFragment searchFragment) {
        mRepository = checkNotNull(instance);
        mFragment = checkNotNull(searchFragment);
        searchFragment.setPresenter(this);
    }



    @Override
    public void start() {

    }

    @Override
    public void search(String word) {
        //是否是电话号码
        if(word.matches("^[0-9]+$")){
            mRepository.asynQueryByTel(word, new SeniorCallBack() {
                @Override
                public void onResponse() {
                    super.onResponse();
                    mFragment.showTip("查询结果响应");
                    mFragment.setRefreshing(false);
                }

                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mFragment.showTip(text);
                }

                @Override
                public void loadUsers(List<User> list) {
                    mFragment.showUsers(list);
                }
            });
            Log.d("RemoteRepository","以电话搜索");
        }else{
            mRepository.asynQueryByName(word, new SeniorCallBack(){
                @Override
                public void onResponse() {
                    super.onResponse();
                    mFragment.showTip("查询结果响应");
                    mFragment.setRefreshing(false);
                }
                @Override
                public void loadUsers(List<User> list) {
                    mFragment.showUsers(list);
                }

                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mFragment.showTip(text);
                }
            });
            Log.d("RemoteRepository","以姓名搜索");
        }
    }

    @Override
    public void relate(final User user) {
        User own = CacheRepository.getInstance().who();
        if(own.getType() == 1){//有监护他人的权利
            mRepository.asynRelative(own, user, new SeniorCallBack(){
                @Override
                public void success() {
                    super.success();
                    mFragment.showTip("您已成为\""+user.getName()+"\"的监护人");
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("添加被监护人\"" + user.getName() + "\"失败");
                }

                @Override
                public void unknown() {
                    super.unknown();
                    mFragment.showTip("未知错误");
                }
            });
        }else if(own.getType() == 2){//只有被他人监护的权利
            if(user.getType() == 2){
                mFragment.showTip("您和他(她)必须有一人充当监护人");
            }
            if(user.getType() == 1){
                mRepository.asynRelative(user, own, new SeniorCallBack(){
                    @Override
                    public void success() {
                        super.success();
                        mFragment.showTip("\""+user.getName()+"\"已成为您的监护人");
                    }

                    @Override
                    public void fail() {
                        super.fail();
                        mFragment.showTip("添加监护人\"" + user.getName() + "\"失败");
                    }

                    @Override
                    public void unknown() {
                        super.unknown();
                        mFragment.showTip("未知错误");
                    }
                });
            }
        }
    }
}
