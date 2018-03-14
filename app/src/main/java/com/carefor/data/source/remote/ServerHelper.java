package com.carefor.data.source.remote;

import com.carefor.callback.BaseCallBack;
import com.carefor.data.entity.Housekeeping;
import com.carefor.data.entity.Location;
import com.carefor.data.entity.User;

import java.util.List;


/**
 * Created by baige on 2017/12/22.
 */

public interface ServerHelper {

   /*基本接口部分*/
    interface PrimaryCallBack{
        void timeout(); //连不上网，或迟迟得不到响应
        void response(String json);
        void error(Exception e); //运行出错
    }

    /*根据json解析后，调用的返回码接口*/
    interface CodeCallBack{

        void success();

        void fail();

        void unknown();

        void notFind();//未找到资源

        void typeConvert();//输入参数类型错误

        void exist();//资源已经存在

        void isBlank();//参数为空

        void timeout();

        void invalid(); //无效
    }

    /*复杂的接口部分*/
    interface ComplexCallBack{

        void meaning(String text); //返回的中文解释

        void onResponse();//服务器有响应，进度条应该停止

        void loadUsers(List<User> list);

        void loadAUser(User user);



        void loadLocation(Location loc);

        void loadLocations(List<Location> locationList);

        void loadHousekeeping(Housekeeping housekeeping);

        void loadHousekeepings(List<Housekeeping> housekeepingList);

        void receiveMessage(String message);

    }

    void login(User user, BaseCallBack callBack);
    void loginMD5(User user, BaseCallBack callBack);

    void register(User user, String code, BaseCallBack callBack);
    void verification(String tel, BaseCallBack callBack);//获取验证码

    void query(int id, BaseCallBack callBack);
    void queryByTel(String tel, BaseCallBack callBack);
    void queryByName(String tel, BaseCallBack callBack);
    void queryByUser(User user, BaseCallBack callBack);
    void getAllUsers(BaseCallBack callBack);

    void relative(User guardian, User pupils, BaseCallBack callBack);

    void unRelative(User guardian, User pupils, BaseCallBack callBack);

    void getAllGuardiansOf(int id, BaseCallBack callBack);

    void getAllPupillusOf(int id, BaseCallBack callBack);

    void assignment(int gid, int bgid, int otherid, BaseCallBack callBack);//转让权限

    void askLocation(int code, String description, int sendUid, int receUid, String content, BaseCallBack callBack);

    void replyLocation(int code, String description, int sendUid, int receUid, String content, BaseCallBack callBack);

    void uploadLocation(int uid, String loc, long time, BaseCallBack callBack);

    void searchLocationByTime(int uid, long time, BaseCallBack callBack);

    void searchLocationByById(int uid, BaseCallBack callBack);

    void getAllHousekeeping(BaseCallBack callBack);

    void searchHousekeepingById(int id, BaseCallBack callBack);

    void searchHousekeepingByKey(String key, BaseCallBack callBack);

    void sendMessageTo(int from, int to, String message, BaseCallBack callBack);



}
