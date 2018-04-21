package com.carefor.mainui;

import android.util.Log;

import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.User;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.login.LoginActivity;
import com.carefor.telephone.TelePhone;
import com.carefor.telephone.TelePhoneAPI;
import com.carefor.util.Tools;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class MainPresenter implements MainContract.Presenter {

    private final static String TAG = MainPresenter.class.getCanonicalName();

    private MainFragment mFragment;

    private Repository mRepository;

    private OnPresenterListener mListener;


    public MainPresenter(Repository repository, MainFragment mainFragment) {
        mRepository = checkNotNull(repository);
        mFragment = checkNotNull(mainFragment);
        mainFragment.setPresenter(this);
    }

    public void setOnPresenterListener(OnPresenterListener listener) {
        mListener = listener;
    }

    @Override
    public void start() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        if (!cacheRepository.isLogin()) {
            if (mListener != null) {
                mListener.startActivity(LoginActivity.class);
            }
        }

        if (cacheRepository.getSelectUser() != null) {
            mFragment.showEmUser(cacheRepository.getSelectUser());
        }

    }

    @Override
    public void stop() {

    }


    @Override
    public void skipToActivity(Class content) {
        if (mListener != null) {
            mListener.startActivity(content);
        }
    }

    @Override
    public void showDrawer() {
        if (mListener != null) {
            mListener.showDrawer();
        }
    }

    @Override
    public void callTo() {
        User user = CacheRepository.getInstance().getSelectUser();
        Log.d(TAG, "打给："+user);
        if (TelePhone.getInstance().isLeisure()) {
            if (user == null) {
                throw new IllegalArgumentException("Don't select the user to call!");
            } else {
                Log.d(TAG, ""+user.toString());
                if (Tools.isEmpty(user.getDeviceId())) {
                    mRepository.asynQuery(user.getUid(),
                            new SeniorCallBack() {
                                @Override
                                public void loadAUser(User user) {
                                    super.loadAUser(user);
                                    Log.d(TAG, ""+user);
                                    if (user != null) {
                                        CacheRepository.getInstance().setSelectUser(user);
                                        TelePhone.getInstance().afxCallTo(user.getDeviceId(), user.getName(), new TelePhoneAPI.BaseCallBackAdapter());
                                    }
                                }

                                @Override
                                public void timeout() {
                                    super.timeout();
                                    mFragment.showTip("查询用户超时");
                                }

                            });
                }else{
                    TelePhone.getInstance().afxCallTo(user.getDeviceId(), user.getName(), new TelePhoneAPI.BaseCallBackAdapter());
                }
            }
        }
    }

    @Override
    public void preUser() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        if (cacheRepository.getRelatedUsers() == null) {
            loadUser(true);
        } else {
            User user = cacheRepository.preRelatedUser();
            if (user != null) {
                mFragment.showEmUser(user);
            }
        }
    }

    @Override
    public void nextUser() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        if (cacheRepository.getRelatedUsers() == null) {
            loadUser(false);
        } else {
            User user = cacheRepository.nextRelatedUser();
            if (user != null) {
                mFragment.showEmUser(user);
            }
        }
    }

    private void loadUser(final boolean isPre) {
        final CacheRepository cacheRepository = CacheRepository.getInstance();
        User user = cacheRepository.who();
        if (cacheRepository.getRelatedUsers() == null) {
            if (user.getType() == 1) {//监护人
                mRepository.asynGetAllPupillusOf(user.getUid(), new SeniorCallBack() {
                    @Override
                    public void onResponse() {
                        super.onResponse();
                    }

                    @Override
                    public void loadUsers(List<User> list) {
                        super.loadUsers(list);
                        Log.d(TAG, list.toString());
                        cacheRepository.setRelatedUsers(list);

                        User user = null;
                        if (isPre) {
                            user = cacheRepository.preRelatedUser();
                            if (user != null) {
                                mFragment.showEmUser(user);
                            }
                        } else {
                            user = cacheRepository.nextRelatedUser();
                            if (user != null) {
                                mFragment.showEmUser(user);
                            }
                        }
                    }

                    @Override
                    public void meaning(String text) {
                        mFragment.showTip("查询被监护人" + text);
                    }
                });
            } else if (user.getType() == 2) { //被监护人
                mRepository.asynGetAllGuardiansOf(user.getUid(), new SeniorCallBack() {
                    @Override
                    public void onResponse() {
                        super.onResponse();
                    }

                    @Override
                    public void loadUsers(List<User> list) {
                        super.loadUsers(list);
                        Log.d(TAG, list.toString());
                        cacheRepository.setRelatedUsers(list);

                        User user = null;
                        if (isPre) {
                            user = cacheRepository.preRelatedUser();
                            if (user != null) {
                                mFragment.showEmUser(user);
                            }
                        } else {
                            user = cacheRepository.nextRelatedUser();
                            if (user != null) {
                                mFragment.showEmUser(user);
                            }
                        }
                    }

                    @Override
                    public void meaning(String text) {
                        mFragment.showTip("查询被监护人" + text);
                    }
                });
            }
        }
    }

    interface OnPresenterListener {
        void startActivity(Class content);

        void startService(Class content);

        void showDrawer();
    }
}
