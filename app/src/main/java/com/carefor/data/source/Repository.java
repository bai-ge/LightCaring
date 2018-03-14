package com.carefor.data.source;


import android.util.Log;

import com.carefor.callback.BaseCallBack;
import com.carefor.callback.CallbackManager;
import com.carefor.callback.HousekeepingResponseBinder;
import com.carefor.callback.LocationResponseBinder;
import com.carefor.callback.SimpleResponseBinder;
import com.carefor.callback.UserResponseBinder;
import com.carefor.data.entity.Location;
import com.carefor.data.entity.User;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.data.source.remote.Parm;
import com.carefor.data.source.remote.RemoteRepository;
import com.carefor.data.source.remote.ServerHelper;
import com.carefor.util.JPushTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/21.
 */

public class Repository implements DataSource, ServerHelper {
    private final static String TAG = Repository.class.getCanonicalName();

    private static Repository INSTANCE = null;

    //本地仓库（数据库）
    private LocalRepository mLocalRepository;

    //远程数据仓库（连接远程服务器）
    private RemoteRepository mRemoteRepository;

    private static ExecutorService fixedThreadPool = null;

    //根据服务器返回的 Json信息， 和特定的callback 的绑定，实现调用
    private SimpleResponseBinder mSimpleResponseBinder;
    private UserResponseBinder mUserResponseBinder;
    private LocationResponseBinder mLocationResponseBinder;
    private HousekeepingResponseBinder mHousekeepingResponseBinder;

    private Repository(LocalRepository localRepository) {

        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池
        mSimpleResponseBinder = new SimpleResponseBinder();
        mUserResponseBinder = new UserResponseBinder();
        mLocationResponseBinder = new LocationResponseBinder();
        mHousekeepingResponseBinder = new HousekeepingResponseBinder();
        //TODO 新建本地和远程数据获取来源
        mLocalRepository = checkNotNull(localRepository);
        mLocalRepository = localRepository;
        mRemoteRepository = RemoteRepository.getInstance(localRepository);
    }

    public static Repository getInstance(LocalRepository localRepository) {

        if (INSTANCE == null) {
            synchronized (Repository.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new Repository(localRepository);
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public void login(User user, final BaseCallBack callBack) {
        checkNotNull(user);
        checkNotNull(callBack);
        Log.d(TAG, "登录："+user);
        callBack.setResponseBinder(mSimpleResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
          //  mRemoteRepository.login(user, callBack);
            mRemoteRepository.loginMD5(user, callBack);
        }
    }

    public void asynLogin(final User user, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    login(user, callBack);
                }
            });
        }
    }

    @Override
    public void register(User user, String code, final BaseCallBack callBack) {
        checkNotNull(user);
        checkNotNull(code);
        checkNotNull(callBack);
        callBack.setResponseBinder(mSimpleResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.register(user, code, callBack);
        }
    }

    public void asynRegister(final User user, final String code, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    register(user, code, callBack);
                }
            });
        }
    }

    @Override
    public void verification(String tel, final BaseCallBack callBack) {
        checkNotNull(tel);
        checkNotNull(callBack);
        callBack.setResponseBinder(mSimpleResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.verification(tel,  callBack);
        }
    }

    public void asynVerification(final String tel, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    verification(tel, callBack);
                }
            });
        }
    }

    @Override
    public void queryByName(String name, final BaseCallBack callBack) {
        checkNotNull(name);
        checkNotNull(callBack);
        callBack.setResponseBinder(mUserResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.queryByName(name, callBack);
        }
    }


    public void asynQueryByName(final String name, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    queryByName(name, callBack);
                }
            });
        }
    }


    @Override
    public void getAllGuardiansOf(int id, final BaseCallBack callBack) {
        checkNotNull(id);
        checkNotNull(callBack);
        callBack.setResponseBinder(mUserResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.getAllGuardiansOf(id, callBack);
        }
    }

    public void asynGetAllGuardiansOf(final int id, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    getAllGuardiansOf(id, callBack);
                }
            });
        }
    }


    @Override
    public void queryByTel(String tel, final BaseCallBack callBack) {
        checkNotNull(tel);
        checkNotNull(callBack);
        callBack.setResponseBinder(mUserResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.queryByTel(tel, callBack);
        }
    }

    public void asynQueryByTel(final String tel, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    queryByTel(tel, callBack);
                }
            });
        }
    }


    @Override
    public void getAllPupillusOf(int id, BaseCallBack callBack) {
        checkNotNull(id);
        checkNotNull(callBack);
        callBack.setResponseBinder(mUserResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.getAllPupillusOf(id, callBack);
        }
    }


    public void asynGetAllPupillusOf(final int id, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    getAllPupillusOf(id, callBack);
                }
            });
        }
    }


    @Override
    public void relative(User guardian, User pupils, BaseCallBack callBack) {
        checkNotNull(guardian);
        checkNotNull(pupils);
        checkNotNull(callBack);
        callBack.setResponseBinder(mSimpleResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.relative(guardian, pupils, callBack);
        }
    }


    public void asynRelative(final User guardian, final User pupils, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    relative(guardian, pupils, callBack);
                }
            });
        }
    }


    @Override
    public void unRelative(User guardian, User pupils, BaseCallBack callBack) {
        checkNotNull(guardian);
        checkNotNull(pupils);
        checkNotNull(callBack);
        callBack.setResponseBinder(mSimpleResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.unRelative(guardian, pupils,  callBack);
        }
    }

    public void asynUnRelative(final User guardian, final User pupils, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    unRelative(guardian, pupils, callBack);
                }
            });
        }
    }


    @Override
    public void query(int id, BaseCallBack callBack) {
        checkNotNull(id);
        checkNotNull(callBack);
        callBack.setResponseBinder(mUserResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.query(id, callBack);
        }
    }

    public void asynQuery(final int id, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    query(id, callBack);
                }
            });
        }
    }

    @Override
    public void loginMD5(User user, BaseCallBack callBack) {

    }

    @Override
    public void queryByUser(User user, BaseCallBack callBack) {

    }

    @Override
    public void getAllUsers(BaseCallBack callBack) {

    }

    @Override
    public void assignment(int gid, int bgid, int otherid, BaseCallBack callBack) {

    }

    @Override
    public void askLocation(int code, String description, int sendUid, int receUid, String content, BaseCallBack callBack) {
        checkNotNull(content);
        checkNotNull(callBack);
        callBack.setResponseBinder(mSimpleResponseBinder);
        CallbackManager callbackManager = CallbackManager.getInstance();
        callbackManager.put(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.askLocation(code, description, sendUid, receUid, content, callBack);
        }


    }
    public void asynAskLocation(final int code, final String description, final int sendUid, final int receUid, final String content, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    askLocation(code, description, sendUid, receUid, content, callBack);
                }
            });
        }
    }

    @Override
    public void replyLocation(int code, String description, int sendUid, int receUid, String content, BaseCallBack callBack) {
        checkNotNull(content);
        checkNotNull(callBack);
        callBack.setResponseBinder(mSimpleResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.replyLocation(code, description, sendUid, receUid, content, callBack);
        }

    }
    public void asynReplyLocation(final int code, final String description, final int sendUid, final int receUid, final String content, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    replyLocation(code, description, sendUid, receUid, content, callBack);
                }
            });
        }

    }

    @Override
    public void uploadLocation(int uid, String loc, long time, BaseCallBack callBack) {
        checkNotNull(loc);
        checkNotNull(callBack);
        callBack.setResponseBinder(mSimpleResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.uploadLocation(uid, loc, time, callBack);
        }
    }

    public void asynUploadLocation(final int uid, final String loc, final long time, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    uploadLocation(uid, loc, time, callBack);
                }
            });
        }
    }

    @Override
    public void searchLocationByTime(int uid, long time, BaseCallBack callBack) {
        checkNotNull(callBack);
        callBack.setResponseBinder(mLocationResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.searchLocationByTime(uid, time, callBack);
        }
    }

    public void asynSearchLocationByTime(final int uid, final long time, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    searchLocationByTime(uid, time, callBack);
                }
            });
        }
    }

    @Override
    public void searchLocationByById(int uid, BaseCallBack callBack) {
        checkNotNull(callBack);
        callBack.setResponseBinder(mLocationResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.searchLocationByById(uid, callBack);
        }
    }

    public void asynSearchLocationByById(final int uid, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    searchLocationByById(uid, callBack);
                }
            });
        }
    }

    @Override
    public void getAllHousekeeping(BaseCallBack callBack) {
        checkNotNull(callBack);
        callBack.setResponseBinder(mHousekeepingResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.getAllHousekeeping(callBack);
        }
    }

    public void asynGetAllHousekeeping(final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    getAllHousekeeping(callBack);
                }
            });
        }
    }

    @Override
    public void searchHousekeepingById(int id, BaseCallBack callBack) {
        checkNotNull(callBack);
        callBack.setResponseBinder(mHousekeepingResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.searchHousekeepingById(id, callBack);
        }
    }
    public void asynSearchHousekeepingById(final int id, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    searchHousekeepingById(id, callBack);
                }
            });
        }
    }

    @Override
    public void searchHousekeepingByKey(String key, BaseCallBack callBack) {
        checkNotNull(callBack);
        checkNotNull(key);
        callBack.setResponseBinder(mHousekeepingResponseBinder);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.searchHousekeepingByKey(key, callBack);
        }
    }
    public void asynSearchHousekeepingByKey(final String key, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    searchHousekeepingByKey(key, callBack);
                }
            });
        }
    }

    @Override
    public void sendMessageTo(int from, int to, String message, BaseCallBack callBack) {
        checkNotNull(callBack);
        checkNotNull(message);
        callBack.setResponseBinder(mSimpleResponseBinder);
        CallbackManager callbackManager = CallbackManager.getInstance();
        callbackManager.put(callBack);

        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.sendMessageTo(from, to, message, callBack);
        }
    }

    public void asynSendMessageTo(final int from, final int to, final String message, final BaseCallBack callBack) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    sendMessageTo(from, to, message, callBack);
                }
            });
        }
    }
/*
    class SimpleServerCallBack implements ServerHelper.ServerCallBack{
        ServerAPI.BaseCallBack mCallBack;

        public SimpleServerCallBack(ServerAPI.BaseCallBack callBack){
            mCallBack = checkNotNull(callBack);
        }
        @Override
        public void timeout() {
            mCallBack.onResponse();
            mCallBack.timeout();
        }
        @Override
        public void response(String json) {
            mCallBack.onResponse();
            try {
                //可能报错 Value {"codeNum":"404","meanning":"资源未找到"} of type org.json.JSONObject cannot be converted to JSONArray
                JSONArray jsonArray = new JSONArray(json);
                List<User> userList = new ArrayList<>();
                //遍历数组
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);

                    User user = User.createByJson(jsonObj);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                if (!userList.isEmpty()) {
                    mCallBack.loadUsers(userList);
                    return;
                }
                Log.d(TAG, "是数组,但解析不到数据");
            } catch (JSONException e) {
                e.printStackTrace();
                //Value {"codeNum":"404","meanning":"资源未找到"} of type org.json.JSONObject cannot be converted to JSONArray
                Log.d(TAG, "不是数组"+e.getMessage());
            }
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE_NUM)){//返回码
                    int codeNum = jsonObject.getInt(Parm.CODE_NUM);
                    String text = jsonObject.getString(Parm.MEANING);
                    if(!JPushTools.isEmpty(text)){
                        mCallBack.meaning(text);
                    }
                    CallBackCode(mCallBack, codeNum);
                }else {//返回的单个用户
                    User user = User.createByJson(jsonObject);
                    if (user != null) {//返回的是用户数据，加载成功
                        mCallBack.loadAUser(user);
                        return;
                    }
                    //TODO 返回其他结构
                    mCallBack.fail();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallBack.fail();
            }
        }//服务器返回json数据end

        @Override
        public void error(Exception e) {
            mCallBack.onResponse();
            mCallBack.fail();
        }

    }
    //只需处理返回码的监听器类
    class CodeNumCallBack implements ServerHelper.ServerCallBack{

        ServerAPI.BaseCallBack mCallBack;
        public CodeNumCallBack(ServerAPI.BaseCallBack callBack){
            mCallBack = checkNotNull(callBack);
        }
        @Override
        public void timeout() {
            mCallBack.onResponse();
            mCallBack.timeout();
        }

        @Override
        public void response(String json) {
            mCallBack.onResponse();
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE_NUM)){
                    int codeNum = jsonObject.getInt(Parm.CODE_NUM);
                    String text = jsonObject.getString(Parm.MEANING);
                    if(!JPushTools.isEmpty(text)){
                        mCallBack.meaning(text);
                    }
                    CallBackCode(mCallBack, codeNum);
                    return;
                }
                Log.e(TAG, "解析数据发生错误，数据解析考虑不完全");
            } catch (JSONException e) {
                e.printStackTrace();
                mCallBack.fail();
            }
        }

        @Override
        public void error(Exception e) {
            mCallBack.onResponse();
            mCallBack.fail();
        }
    }

    //加载用户数组的监听器类，可以自动返回解析并返回用户列表
    class LoadUsersCallBack implements ServerHelper.ServerCallBack {

        ServerAPI.BaseCallBack mCallBack;
        public LoadUsersCallBack(ServerAPI.BaseCallBack callBack){
            mCallBack = checkNotNull(callBack);
        }

        @Override
        public void timeout() {
            mCallBack.onResponse();
            mCallBack.timeout();
        }

        @Override
        public void response(String json) {
            mCallBack.onResponse();
            try {
                JSONArray jsonArray = new JSONArray(json);
                List<User> userList = new ArrayList<>();
                //遍历数组
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);

                    User user = User.createByJson(jsonObj);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                if (!userList.isEmpty()) {
                    mCallBack.loadUsers(userList);
                    return;
                }
                Log.d(TAG, "是数组,但解析不到数据");
            } catch (JSONException e) {
                e.printStackTrace();
                //Value {"codeNum":"404","meanning":"资源未找到"} of type org.json.JSONObject cannot be converted to JSONArray
                Log.d(TAG, "不是数组"+e.getMessage());
            }
            try {
                JSONObject jsonObject = new JSONObject(json);
                int codeNum = jsonObject.getInt(Parm.CODE_NUM);
                String text = jsonObject.getString(Parm.MEANING);
                if(!JPushTools.isEmpty(text)){
                    mCallBack.meaning(text);
                }
                CallBackCode(mCallBack, codeNum);
            } catch (JSONException e) {
                e.printStackTrace();
                mCallBack.fail();
            }
        }//服务器返回json数据end

        @Override
        public void error(Exception e) {
            mCallBack.onResponse();
            mCallBack.fail();
        }
    }


    //加载用户的监听器类，可以自动返回解析并返回一名用户
    class GetAUserCallBack implements ServerHelper.ServerCallBack {

        ServerAPI.BaseCallBack mCallBack;

        public GetAUserCallBack(ServerAPI.BaseCallBack callBack) {
            mCallBack = checkNotNull(callBack);
        }

        @Override
        public void timeout() {
            mCallBack.onResponse();
            mCallBack.timeout();
        }

        @Override
        public void response(String json) {
            mCallBack.onResponse();
            try {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.has(Parm.CODE_NUM)) {
                    //返回的是错误类型码
                    int codeNum = jsonObject.getInt(Parm.CODE_NUM);
                    String text = jsonObject.getString(Parm.MEANING);
                    if (!JPushTools.isEmpty(text)) {
                        mCallBack.meaning(text);
                    }
                    CallBackCode(mCallBack, codeNum);
                } else {
                    User user = User.createByJson(jsonObject);
                    if (user != null) {//返回的是用户数据，加载成功
                        mCallBack.loadAUser(user);
                        return;
                    }
                    mCallBack.fail();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallBack.fail();
            }
        }

        @Override
        public void error(Exception e) {
            mCallBack.onResponse();
            mCallBack.fail();
        }
    }
    */
}
