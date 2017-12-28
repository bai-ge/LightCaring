package com.carefor.data.source;


import android.util.Log;

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

public class Repository implements DataSource, ServerAPI {
    private final static String TAG = Repository.class.getCanonicalName();

    private static Repository INSTANCE = null;

    //本地仓库（数据库）
    private LocalRepository mLocalRepository;

    //远程数据仓库（连接远程服务器）
    private RemoteRepository mRemoteRepository;

    private static ExecutorService fixedThreadPool = null;


    private Repository(LocalRepository localRepository) {

        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池
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


    public static void CallBackCode(BaseCallBack callBack, int code) {
        switch (code) {
            case Parm.SUCCESS_CODE:
                callBack.success();
                break;
            case Parm.FAIL_CODE:
                callBack.fail();
                break;
            case Parm.UNKNOWN_CODE:
                callBack.unknown();
                break;
            case Parm.NOTFIND_CODE:
                callBack.notfind();
                break;
            case Parm.TYPE_CONVERT_CODE:
                callBack.typeConvert();
                break;
            case Parm.EXIST_CODE:
                callBack.exist();
                break;
            case Parm.BLANK_CODE:
                callBack.isBlank();
                break;
            case Parm.TIMEOUT_CODE:
                callBack.timeout();
                break;
            case Parm.INVALID_CODE:
                callBack.invalid();
                break;
            default:
                callBack.unknown();
        }
    }

    @Override
    public void login(User user, final BaseCallBack callBack) {
        checkNotNull(user);
        checkNotNull(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.login(user, new CodeNumCallBack(callBack));
            mRemoteRepository.loginMD5(user, new CodeNumCallBack(callBack));
        }
    }

    @Override
    public void afxLogin(final User user, final BaseCallBack callBack) {
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
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.register(user, code, new CodeNumCallBack(callBack));
        }
    }

    @Override
    public void afxRegister(final User user, final String code, final BaseCallBack callBack) {
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
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.verification(tel,  new CodeNumCallBack(callBack));
        }
    }

    @Override
    public void afxVerification(final String tel, final BaseCallBack callBack) {
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
    public void afxQueryByName(final String name, final BaseCallBack callBack) {
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
    public void queryByName(String name, final BaseCallBack callBack) {
        checkNotNull(name);
        checkNotNull(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.queryByName(name, new LoadUsersCallBack(callBack));
        }
    }

    @Override
    public void afxGetAllGuardiansOf(final int id, final BaseCallBack callBack) {
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
    public void getAllGuardiansOf(int id, final BaseCallBack callBack) {
        checkNotNull(id);
        checkNotNull(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.getAllGuardiansOf(id, new LoadUsersCallBack(callBack));
        }
    }

    @Override
    public void afxQueryByTel(final String tel, final BaseCallBack callBack) {
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
    public void queryByTel(String tel, final BaseCallBack callBack) {
        checkNotNull(tel);
        checkNotNull(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.queryByTel(tel, new LoadUsersCallBack(callBack));
        }
    }

    @Override
    public void afxGetAllPupillusOf(final int id, final BaseCallBack callBack) {
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
    public void getAllPupillusOf(int id, BaseCallBack callBack) {
        checkNotNull(id);
        checkNotNull(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.getAllPupillusOf(id, new LoadUsersCallBack(callBack));
        }
    }

    @Override
    public void afxRelative(final User guardian, final User pupils, final BaseCallBack callBack) {
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
    public void relative(User guardian, User pupils, BaseCallBack callBack) {
        checkNotNull(guardian);
        checkNotNull(pupils);
        checkNotNull(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.relative(guardian, pupils, new CodeNumCallBack(callBack));
        }
    }

    @Override
    public void afxUnRelative(final User guardian, final User pupils, final BaseCallBack callBack) {
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
    public void unRelative(User guardian, User pupils, BaseCallBack callBack) {
        checkNotNull(guardian);
        checkNotNull(pupils);
        checkNotNull(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.unRelative(guardian, pupils, new CodeNumCallBack(callBack));
        }
    }

    @Override
    public void afxQuery(final int id, final BaseCallBack callBack) {
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
    public void query(int id, BaseCallBack callBack) {
        checkNotNull(id);
        checkNotNull(callBack);
        if (mRemoteRepository == null) {
            callBack.fail();
        } else {
            mRemoteRepository.query(id, new GetAUserCallBack(callBack));
        }
    }

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
}
