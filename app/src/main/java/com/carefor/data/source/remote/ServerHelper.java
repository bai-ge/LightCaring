package com.carefor.data.source.remote;

import com.carefor.data.entity.User;


/**
 * Created by baige on 2017/12/22.
 */

public interface ServerHelper {
    interface ServerCallBack{
        void timeout(); //连不上网，或迟迟得不到响应
        void response(String json);
        void error(Exception e); //运行出错
    }

    void login(User user, ServerCallBack callBack);
    void loginMD5(User user, ServerCallBack callBack);

    void register(User user, String code, ServerCallBack callBack);
    void verification(String tel, ServerCallBack callBack);//获取验证码

    void query(int id, ServerCallBack callBack);
    void queryByTel(String tel, ServerCallBack callBack);
    void queryByName(String tel, ServerCallBack callBack);
    void queryByUser(User user, ServerCallBack callBack);
    void getAllUsers(ServerCallBack callBack);

    void relative(User guardian, User pupils, ServerCallBack callBack);

    void unRelative(User guardian, User pupils, ServerCallBack callBack);

    void getAllGuardiansOf(int id, ServerCallBack callBack);

    void getAllPupillusOf(int id, ServerCallBack callBack);

    void assignment(int gid, int bgid, int otherid, ServerCallBack callBack);//转让权限

    void askLocation(int code, String description, int sendUid, int receUid, String content, ServerCallBack callBack);

    void replyLocation(int code, String description, int sendUid, int receUid, String content, ServerCallBack callBack);

    //TODO

}
