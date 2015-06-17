package com.dxy.android.statistics.util;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.util.Map;

/**
 * brokge@gmail.com
 * Created by chenlw on 2015/6/9.
 */
public class JsonUtil {

    //封装成JSON的格式
    public static String getJsonString(Object object) {
        try {
            Gson gson = new Gson();
            //将对象转换为JSON数据
            return gson.toJson(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //封装成JSON的格式
    public static String getJsonString(Map<String, ?> map) {
        try {
            Gson gson = new Gson();
            //将对象转换为JSON数据
            return gson.toJson(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Object getParseJson(JSONObject jsonObject, Class<?> tClass) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonObject.toString(), tClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json异步解析类
     **/
    public class AsyncParseJson<T> extends Thread {
        JSONObject jsonObject;
        Class<T> tclass;
        Handler mHandler;

        public AsyncParseJson(JSONObject obj, Class<T> tClass, Handler mHandler) {
            // TODO Auto-generated constructor stub
            this.jsonObject = obj;
            this.tclass = tClass;
            this.mHandler = mHandler;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            Gson gson = new Gson();
            T tobj = gson.fromJson(jsonObject.toString(), tclass);
            Message msg = mHandler.obtainMessage();
            msg.obj = tobj;
            mHandler.sendMessage(msg);
        }
    }
}
