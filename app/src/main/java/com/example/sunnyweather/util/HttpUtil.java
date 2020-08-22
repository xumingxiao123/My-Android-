package com.example.sunnyweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
/**
 * Created by StarDream on 2020/8/21.
 */
//采用OkHttp与服务器进行通信
public class HttpUtil {
    //与服务器进行交互发起一条http请求只需要调用sendOkHttpRequest()即可，
    // 传入要请求的地址，注册一个回调来处理服务器的响应
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        //初始化一个client实例
        OkHttpClient client = new OkHttpClient();
        //初始化一个request实例
        Request request =  new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
