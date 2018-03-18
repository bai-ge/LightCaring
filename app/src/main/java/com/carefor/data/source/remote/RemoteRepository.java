package com.carefor.data.source.remote;

import android.util.Log;

import com.carefor.callback.BaseCallBack;
import com.carefor.data.entity.User;
import com.carefor.data.source.DataSource;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/21.
 */

public class RemoteRepository implements DataSource, ServerHelper {

    private final static String TAG = RemoteRepository.class.getName();

    private static RemoteRepository INSTANCE = null;

    private String serverAddress = "http://120.78.148.180";

    //这里本地数据库只用来获取本地用户信息
    private LocalRepository mLocalRepository;

    private RemoteRepository(LocalRepository localRepository) {
        mLocalRepository = localRepository;
    }

    public static RemoteRepository getInstance(LocalRepository localRepository) {
        if (INSTANCE == null) {
            synchronized (RemoteRepository.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new RemoteRepository(localRepository);
                }
            }
        }
        return INSTANCE;
    }

    private void HttpURLPost(String url, String json, PrimaryCallBack callBack) {
        checkNotNull(url);
        checkNotNull(json);
        checkNotNull(callBack);
        HttpURLConnection connection;
        OutputStreamWriter out = null;
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        Log.d(TAG, url+" "+json);
        try {
            URL httpUrl = new URL(url);
            connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setConnectTimeout(60 * 1000);
            connection.setReadTimeout(60 * 1000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);//设置不要缓存
            connection.setInstanceFollowRedirects(true);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            //POST请求
            out = new OutputStreamWriter(connection.getOutputStream());
            out.write(json);
            out.flush();

            //读取相应
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                response.append(lines);
            }
            reader.close();
            connection.disconnect();
            Log.d(TAG, "服务器反馈信息" + response.toString());
            callBack.response(response.toString());
        } catch (IOException e) {
            callBack.error(e);
        }  finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void HttpURLGet(String url, PrimaryCallBack callBack) {
        checkNotNull(url);
        checkNotNull(callBack);
        HttpURLConnection connection;
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        Log.d(TAG, url);
        try {
            URL httpUrl = new URL(url);
            connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setConnectTimeout(60 * 1000);
            connection.setReadTimeout(60 * 1000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);//设置不要缓存
            connection.setInstanceFollowRedirects(true);//设置本次连接是否自动处理重定向
            //connection.setDoInput(true);设置这句话会变成POST
            //connection.setDoOutput(true);
            connection.connect();
            //读取相应
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                response.append(lines);
            }
            reader.close();
            connection.disconnect();
            Log.d(TAG, "服务器反馈信息" + response.toString());
            callBack.response(response.toString());
        } catch (IOException e) {
            callBack.error(e);

        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void login(User user, BaseCallBack callBack) {
        Log.d(TAG, "登录："+user);
        String url = serverAddress + "/api/user/login";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.USER_NAME, user.getName());
            jsonObject.put(Parm.PASSWORD, user.getPsw());
            jsonObject.put(Parm.DEVICE_ID, user.getDeviceId());
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void loginMD5(User user, BaseCallBack callBack) {
        String url = serverAddress + "/api/user/login";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.USER_NAME, user.getName());
            jsonObject.put(Parm.PASSWORD, Tools.MD5(user.getPsw()));
            jsonObject.put(Parm.DEVICE_ID, user.getDeviceId());
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void register(User user, String code, BaseCallBack callBack) {
        String url = serverAddress + "/api/user/register";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.USER_NAME, user.getName());
            jsonObject.put(Parm.TEL, user.getTel());
            jsonObject.put(Parm.PASSWORD, user.getPsw());
            jsonObject.put(Parm.CODE, code);
            jsonObject.put(Parm.TYPE, user.getType());
            jsonObject.put(Parm.DEVICE_ID, user.getDeviceId());
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }


    //获取验证码
    @Override
    public void verification(String tel, BaseCallBack callBack) {
        String url = serverAddress + "/api/user/verification";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.TEL, tel);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void query(int id, BaseCallBack callBack) {
        String url = serverAddress + "/api/user/"+id;
        HttpURLGet(url, callBack);
    }

    @Override
    public void queryByTel(String tel, BaseCallBack callBack) {
        String url = serverAddress + "/api/user/search";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.TELEPHONE, tel);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void queryByName(String name, BaseCallBack callBack) {
        String url = serverAddress + "/api/user/search";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.NAME, name);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void queryByUser(User user, BaseCallBack callBack) {

    }

    @Override
    public void getAllUsers(BaseCallBack callBack) {
        String url = serverAddress + "/api/user/all";
        HttpURLGet(url, callBack);
    }

    @Override
    public void relative(User guardian, User pupils, BaseCallBack callBack) {
        String url = serverAddress + "/api/contacts/build";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.GUARD_ID, guardian.getUid());
            jsonObject.put(Parm.PUP_ID, pupils.getUid());
            jsonObject.put(Parm.GRANT, 1);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void unRelative(User guardian, User pupils, BaseCallBack callBack) {
        String url = serverAddress + "/api/contacts/unbinding";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.GUARD_ID, guardian.getUid());
            jsonObject.put(Parm.PUP_ID, pupils.getUid());
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }


    /**
     * @param pid 被监护人
     * @param callBack 返回所有监护人
     */
    @Override
    public void getAllGuardiansOf(int pid, BaseCallBack callBack) {
        String url = serverAddress + "/api/contacts/g";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.PUP_ID, pid);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    /**
     * @param gid 监护人
     * @param callBack 返回所有被监护人
     */
    @Override
    public void getAllPupillusOf(int gid, BaseCallBack callBack) {
        String url = serverAddress + "/api/contacts/ug";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.GUARD_ID, gid);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void assignment(int gid, int bgid, int otherid, BaseCallBack callBack) {
        String url = serverAddress + "/api/contacts/assignment";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.GUARD_ID, gid);
            jsonObject.put(Parm.PUP_ID, bgid);
            jsonObject.put(Parm.OTHER_ID, otherid);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void askLocation(int code, String description, int sendUid, int receUid, String content, BaseCallBack callBack) {
        String url = serverAddress + "/api/services/location/ug";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.INUM, code);
            jsonObject.put(Parm.DESCRIPTION, description);
            jsonObject.put(Parm.SENDU_ID, sendUid);
            jsonObject.put(Parm.RECEIVEU_ID, receUid);
            JSONObject conJson = new JSONObject(content);
            jsonObject.put(Parm.CONTENT, conJson);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void replyLocation(int code, String description, int sendUid, int receUid, String content, BaseCallBack callBack) {
        String url = serverAddress + "/api/services/location/g";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.INUM, code);
            jsonObject.put(Parm.DESCRIPTION, description);
            jsonObject.put(Parm.SENDU_ID, String.valueOf(sendUid));
            jsonObject.put(Parm.RECEIVEU_ID, String.valueOf(receUid));
            JSONObject conJson = new JSONObject(content);
            jsonObject.put(Parm.CONTENT, conJson);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void uploadLocation(int uid, String loc, long time, BaseCallBack callBack) {
        String url = serverAddress + "/api/services/location/custom";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(Parm.POS, loc);
            jsonObject.put(Parm.TIME, time);
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void searchLocationByTime(int uid, long time, BaseCallBack callBack) {
        String url = serverAddress + "/api/services/location/historyByTime?u_id="+uid+"&time="+time;
        HttpURLGet(url, callBack);
    }

    @Override
    public void searchLocationByById(int uid, BaseCallBack callBack) {
        String url = serverAddress + "/api/services/location/history?u_id="+uid;
        HttpURLGet(url, callBack);
    }

    @Override
    public void getAllHousekeeping(BaseCallBack callBack) {
        String url = serverAddress + "/api/service/hourse_keeping/list";
        HttpURLGet(url, callBack);
    }

    @Override
    public void searchHousekeepingById(int id, BaseCallBack callBack) {
        String url = serverAddress + "/api/service/hourse_keeping/"+id;
        HttpURLGet(url, callBack);
    }

    @Override
    public void searchHousekeepingByKey(String key, BaseCallBack callBack) {
        String url = serverAddress + "/api/service/hourse_keeping/list";
        JSONObject json = new JSONObject();
        try {
            json.put(Parm.KEYWORD, key);
            HttpURLPost(url, json.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void sendMessageTo(int from, int to, String message, BaseCallBack callBack) {
        String url = serverAddress + "/api/services/transmission/custom";
        JSONObject json = new JSONObject();
        try {
            json.put(Parm.FROM, String.valueOf(from));
            json.put(Parm.TO, String.valueOf(to));
            json.put(Parm.MESSAGE, message);
            HttpURLPost(url, json.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void informTumble(int bgid, BaseCallBack callBack) {
        String url = serverAddress + "/api/services/transmission/tumble";
        JSONObject json = new JSONObject();
        try {
            json.put(Parm.PUP_ID, String.valueOf(bgid));
            HttpURLPost(url, json.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }
}
