package com.carefor.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Date;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.mindpipe.android.logging.log4j.LogConfigurator;


public class Loggerx {

    //设为false关闭日志
    private static final boolean LOG_ENABLE = true;
    private static boolean isConfigured = false;
    public static boolean bWriteToFile = false;


    public static void v(String tag, String msg){
        if (LOG_ENABLE){
            Log.v(tag, msg);
        }
    }
    public static void d(String tag, String msg){
        if (LOG_ENABLE){
            if(bWriteToFile){
                Logger logger = getLogger(tag);
                logger.debug(msg);
            }else{
                Log.d(tag, msg);
            }

        }
    }

    public static void i(String tag, String msg){
        if (LOG_ENABLE){
            if(bWriteToFile){
                Logger logger = getLogger(tag);
                logger.info(msg);
            }else{
                Log.i(tag, msg);
            }
        }
    }
    public static void w(String tag, String msg){
        if (LOG_ENABLE){
            if(bWriteToFile){
                Logger logger = getLogger(tag);
                logger.warn(msg);
            }else{
                Log.w(tag, msg);
            }
        }
    }
    public static void e(String tag, String msg){
        if (LOG_ENABLE){
            if(bWriteToFile){
                Logger logger = getLogger(tag);
                logger.error(msg);
            }else{
                Log.e(tag, msg);
            }
        }
    }

    /*
    日志级别优先度从高到低:OFF(关闭),FATAL(致命),ERROR(错误),WARN(警告),INFO(信息),DEBUG(调试),ALL(打开所有的日志，我的理解与DEBUG级别好像没有什么区别得)
    Log4j建议只使用FATAL ,ERROR ,WARN ,INFO ,DEBUG这五个级别。
     "yyyy-MM-dd");// 日志的输出格式
     */

    public static void configure() {
        DailyRollingFileAppender dailyRollingFileAppender = new DailyRollingFileAppender();
        final LogConfigurator logConfigurator = new LogConfigurator();
        Date nowtime = new Date();
        // String needWriteMessage = myLogSdf.format(nowtime);
        //日志文件路径地址:SD卡下myc文件夹log文件夹的test文件
        String fileName = Environment.getExternalStorageDirectory()
                + File.separator + "LightCaring" + File.separator + "log"
                + File.separator + "run.log";
        //设置文件名
        logConfigurator.setFileName(fileName);
        //设置root日志输出级别 默认为DEBUG
        logConfigurator.setRootLevel(Level.DEBUG);
        // 设置日志输出级别
        logConfigurator.setLevel("org.apache", Level.INFO);
        //设置 输出到日志文件的文字格式 默认 %d %-5p [%c{2}]-[%L] %m%n
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        //设置输出到控制台的文字格式 默认%m%n
        logConfigurator.setLogCatPattern("%m%n");
        //设置总文件大小
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        //设置最大产生的文件个数
        logConfigurator.setMaxBackupSize(5);
        //设置所有消息是否被立刻输出 默认为true,false 不输出
        logConfigurator.setImmediateFlush(true);
        //是否本地控制台打印输出 默认为true ，false不输出
        logConfigurator.setUseLogCatAppender(true);
        //设置是否启用文件附加,默认为true。false为覆盖文件
        logConfigurator.setUseFileAppender(true);
        //设置是否重置配置文件，默认为true
        logConfigurator.setResetConfiguration(true);
        //是否显示内部初始化日志,默认为false
        logConfigurator.setInternalDebugging(false);

        logConfigurator.configure();

//        Properties property = new Properties();
//        property.put("log4j.rootCategory","INFO,file");
//        property.put("log4j.appender.file","org.apache.log4j.DailyRollingFileAppender");
//        property.put("log4j.appender.file.DatePattern","'.'yyyy-MM-dd");
//        property.put("log4j.appender.file.File","run.log");
//        property.put("log4j.appender.file.Append","true");
//        property.put("log4j.appender.file.Threshold","INFO");
//        property.put("log4j.appender.file.layout","org.apache.log4j.PatternLayout");
//        property.put("log4j.appender.file.layout.ConversionPattern","%c %x - %m%n   ");
//        PropertyConfigurator.configure(property);
        isConfigured = true;
    }

    private static Logger getLogger(String tag) {
        if (!isConfigured) {
            configure();
        }
        Logger logger;
        if (Tools.isEmpty(tag)) {
            logger = Logger.getRootLogger();
        } else {
            logger = Logger.getLogger(tag);
        }
        logger.addAppender(new DailyRollingFileAppender());
        return logger;
    }
}
