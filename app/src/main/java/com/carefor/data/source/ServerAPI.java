package com.carefor.data.source;


import com.carefor.data.entity.User;

import java.util.List;

/**
 * Created by baige on 2017/12/23.
 */

public interface ServerAPI {

    interface BaseCallBack{

        //得到服务器的响应， 所有使用到进度条的界面都应该在此表示结束
        void onResponse();

        void success();

        void fail();

        void unknown();

        void notfind();//未找到资源

        void typeConvert();//输入参数类型错误

        void exist();//资源已经存在

        void isBlank();//参数为空

        void timeout();

        void invalid(); //无效

        void meaning(String text);

        void loadUsers(List<User> list);

        void loadAUser(User user);
    }
    class BaseCallBackFactory implements BaseCallBack{

        @Override
        public void onResponse() {

        }

        @Override
        public void success() {

        }

        @Override
        public void fail() {

        }

        @Override
        public void unknown() {

        }

        @Override
        public void notfind() {

        }

        @Override
        public void typeConvert() {

        }

        @Override
        public void exist() {

        }

        @Override
        public void isBlank() {

        }

        @Override
        public void timeout() {

        }

        @Override
        public void invalid() {

        }

        @Override
        public void meaning(String text) {

        }

        @Override
        public void loadUsers(List<User> list) {

        }

        @Override
        public void loadAUser(User user) {

        }
    }
    class BaseCallBackAdapter extends BaseCallBackFactory{

    }

    void login(User user, BaseCallBack callBack);
    void afxLogin(User user, BaseCallBack callBack);

    void register(User user, String code, BaseCallBack callBack);
    void afxRegister(User user, String code, BaseCallBack callBack);


    void verification(String tel, BaseCallBack callBack);
    void afxVerification(String tel, BaseCallBack callBack);

    void afxQuery(int id, BaseCallBack callBack);
    void query(int id, BaseCallBack callBack);


    void afxQueryByTel(String tel, BaseCallBack callBack);
    void queryByTel(String tel, BaseCallBack callBack);

    void afxQueryByName(String name, BaseCallBack callBack);
    void queryByName(String name, BaseCallBack callBack);

    /*
    void queryByUser(User user, ServerCallBack callBack);
    void getAllUsers(ServerCallBack callBack);
    */

    void afxRelative(User guardian, User pupils, BaseCallBack callBack);
    void relative(User guardian, User pupils, BaseCallBack callBack);


    void afxUnRelative(User guardian, User pupils, BaseCallBack callBack);
    void unRelative(User guardian, User pupils, BaseCallBack callBack);


    void afxGetAllGuardiansOf(int id, BaseCallBack callBack);
    void getAllGuardiansOf(int id, BaseCallBack callBack);

    void afxGetAllPupillusOf(int id, BaseCallBack callBack);
    void getAllPupillusOf(int id, BaseCallBack callBack);
    /*
    void assignment(int gid, int bgid, int otherid );//转让权限

    void askLocation(int code, String description, int sendUid, int receUid, String content , ServerCallBack callBack);

    void replyLocation(int code, String description, int sendUid, int receUid, String content, ServerCallBack callBack);
    */
}
