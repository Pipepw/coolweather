package com.coolweather.android.util;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    private static final String TAG = "HttpUtil";
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        Log.d(TAG, "sendOkHttpRequest: kkk20");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(address)
                .build();
//        使用newcall进行异步网络请求，callback用于回调（处理服务器响应）
//        callback是专门用于回调的，当其他地方需要用到这个的时候，就在那个地方进行定义，这里只是留下一个位置
//        所以通常将其匿名类作为函数参数
        client.newCall(request).enqueue(callback);
    }
}
