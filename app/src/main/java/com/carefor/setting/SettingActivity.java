package com.carefor.setting;


import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;


import com.carefor.data.source.cache.CacheRepository;
import com.carefor.mainui.R;
import com.carefor.util.JPushTools;
import com.carefor.util.StringValidation;


public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = SettingActivity.class.getCanonicalName();
    public static final String KEY_NAME = "pre_key_name";
    public static final String KEY_SEX = "pre_key_sex";
    public static final String KEY_AGE = "pre_key_age";
    public static final String KEY_ALERT = "pre_key_alert";
    public static final String KEY_VIBRATE = "pre_key_vibrate";
    public static final String KEY_PHONE = "pre_key_phone";
    public static final String KEY_PHONE_SERVER_IP_ARRAY = "pre_key_server_ip_array";
    public static final String KEY_PHONE_SERVER_IP = "pre_key_phone_server_ip";
    public static final String KEY_TCP_PORT = "pre_key_phone_server_tcp_port";
    public static final String KEY_UDP_PORT = "pre_key_phone_server_udp_port";

    public static final String DEFAULT_PHONE_SERVER_IP = "120.78.148.180";
    public static final String DEFAULT_TCP_PORT = "12056";
    public static final String DEFAULT_UDP_PORT = "12059";

    private RingtonePreference mRingtone;

    private EditTextPreference mEpServerIp = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //addPreferencesFromResource(R.xml.preferences);
        // 设置PreferenceActivity保存数据使用的XML文件的名称
        //getPreferenceManager().setSharedPreferencesName("LightCaring_Setting");
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        findPreference(KEY_NAME).setSummary(sp.getString(KEY_NAME, "请输入姓名"));
        findPreference(KEY_SEX).setSummary(sp.getString(KEY_SEX, "请选择性别"));
        findPreference(KEY_AGE).setSummary(sp.getString(KEY_AGE, "请输入年龄"));

        mRingtone = (RingtonePreference) findPreference(KEY_ALERT);
        String uri = sp.getString(KEY_ALERT, null);
        Log.d(TAG, "铃声"+uri);
        String ringtoneName = getRingtoneTitleFromUri(uri);

        mRingtone.setSummary(ringtoneName == null ?"默认铃声" : ringtoneName);

        mRingtone.setOnPreferenceChangeListener(this);



        ((SwitchPreference)findPreference(KEY_VIBRATE)).setChecked(sp.getBoolean(KEY_VIBRATE, true));
        findPreference(KEY_PHONE).setSummary(sp.getString(KEY_PHONE, "紧急联系人电话"));

        findPreference(KEY_PHONE_SERVER_IP).setSummary(sp.getString(KEY_PHONE_SERVER_IP, DEFAULT_PHONE_SERVER_IP));
        findPreference(KEY_TCP_PORT).setSummary(sp.getString(KEY_TCP_PORT, DEFAULT_TCP_PORT));
        findPreference(KEY_UDP_PORT).setSummary(sp.getString(KEY_UDP_PORT, DEFAULT_UDP_PORT));


        mEpServerIp = (EditTextPreference) findPreference(KEY_PHONE_SERVER_IP);
        String serverIp = sp.getString(KEY_PHONE_SERVER_IP_ARRAY, "");
        if(StringValidation.validateRegex(serverIp, StringValidation.RegexIP)){
            mEpServerIp.setEnabled(false);
        }else{
            mEpServerIp.setEnabled(true);
        }
        findPreference(KEY_PHONE_SERVER_IP_ARRAY).setSummary(serverIp);


        String server_ip = sp.getString(KEY_PHONE_SERVER_IP, DEFAULT_PHONE_SERVER_IP);
        if(server_ip.isEmpty()){
            server_ip = "请输入IP";
        }
        findPreference(KEY_PHONE_SERVER_IP).setSummary(server_ip);

        String tcp_port = sp.getString(KEY_TCP_PORT, DEFAULT_TCP_PORT);
        if(tcp_port.isEmpty()){
            tcp_port = "请输入端口号";
        }
        findPreference(KEY_TCP_PORT).setSummary(tcp_port);

        String udp_port = sp.getString(KEY_UDP_PORT, DEFAULT_UDP_PORT);
        if(udp_port.isEmpty()){
            udp_port = "请输入端口号";
        }
        findPreference(KEY_UDP_PORT).setSummary(udp_port);

        //为了在输入框里默认显示
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_PHONE_SERVER_IP, sp.getString(KEY_PHONE_SERVER_IP, DEFAULT_PHONE_SERVER_IP));
        editor.putString(KEY_TCP_PORT, sp.getString(KEY_TCP_PORT, DEFAULT_TCP_PORT));
        editor.putString(KEY_UDP_PORT, sp.getString(KEY_UDP_PORT, DEFAULT_UDP_PORT));
        editor.commit();
        editor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        switch (key) {
            case KEY_NAME:
                Preference namePre = findPreference(key);
                namePre.setSummary(sharedPreferences.getString(key, "请输入姓名"));
                break;
            case KEY_SEX:
                Preference sexPre = findPreference(key);
                sexPre.setSummary(sharedPreferences.getString(key, "请选择性别"));
                break;
            case KEY_AGE:
                Preference agePre = findPreference(key);
                agePre.setSummary(sharedPreferences.getString(key, "请输入年龄"));
                break;
            case KEY_ALERT:
                Preference alertPre = findPreference(key);
                alertPre.setSummary(sharedPreferences.getString(key, ""));
                break;
            case KEY_VIBRATE:
                break;
            case KEY_PHONE:
                Preference phonePre = findPreference(key);
                phonePre.setSummary(sharedPreferences.getString(key, ""));
                break;
            case KEY_PHONE_SERVER_IP_ARRAY:
                Log.d(TAG, KEY_PHONE_SERVER_IP_ARRAY+" ="+sharedPreferences.getString(KEY_PHONE_SERVER_IP_ARRAY, ""));
                String serverIp = sharedPreferences.getString(KEY_PHONE_SERVER_IP_ARRAY, "");
                findPreference(key).setSummary(serverIp);
                if(StringValidation.validateRegex(serverIp, StringValidation.RegexIP)){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_PHONE_SERVER_IP, serverIp);
                    editor.commit();
                    editor.apply();

                    if(mEpServerIp != null){
                        mEpServerIp.setEnabled(false);
                    }
                }else{
                   if(mEpServerIp != null){
                       mEpServerIp.setEnabled(true);
                   }
                }
                break;
            case KEY_PHONE_SERVER_IP:
                Preference ip = findPreference(key);
                String server_ip = sharedPreferences.getString(key, DEFAULT_PHONE_SERVER_IP);

                if(server_ip.isEmpty()){
                    server_ip = "请输入IP";
                }
                ip.setSummary(server_ip);

                break;
            case KEY_TCP_PORT:
                Preference tcp = findPreference(key);
                String tcp_port = sharedPreferences.getString(key, DEFAULT_TCP_PORT);
                Log.d(TAG, "tcp_port="+tcp_port);
                if(tcp_port.isEmpty()){
                    tcp_port = "请输入端口号";
                }
                tcp.setSummary(tcp_port);
                break;
            case KEY_UDP_PORT:
                Preference udp = findPreference(key);
                String udp_port = sharedPreferences.getString(key, DEFAULT_TCP_PORT);
                Log.d(TAG, "udp_port="+udp_port);
                if(udp_port.isEmpty()){
                    udp_port = "请输入端口号";
                }
                udp.setSummary(udp_port);
               // udp.setSummary(sharedPreferences.getInt(key, DEFAULT_UDP_PORT));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CacheRepository.getInstance().readConfig(this);//重新加载配置信息
    }

    /**
     * 将铃声的Uri进行存储
     * @param str
     */
    public  void setRingtonePreference(String str){
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        prefs.edit().putString(KEY_ALERT, str).commit();
    }
    /**
     * 获取铃声名
     * @param uri
     * @return
     */
    public String getRingtoneTitleFromUri(String uri){
        if(JPushTools.isEmpty(uri)){
            return null;
        }
        Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(uri));
        String ringtoneTitle = ringtone.getTitle(this);
        return ringtoneTitle;

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference == mRingtone){
            Toast.makeText(this, "newValue="+newValue, Toast.LENGTH_SHORT).show();
            Log.d(TAG, newValue.toString());
            setRingtonePreference((String)newValue);

            String ringtongTitle = getRingtoneTitleFromUri((String)newValue);
            mRingtone.setSummary(ringtongTitle);
        }
        return true;
    }
}
