package com.example.peter.internet;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by peter on 2017/11/21.
 */

public class InternetFactoryImpl implements InternetRequestInterfacr {

    private HttpURLConnection httpUrlConnection;

    public InternetFactoryImpl(Context context) {


    }

    @Override
    public void getRequest(String url) {

        try {
            URL httpUrl = new URL(url);
            httpUrlConnection = (HttpURLConnection) httpUrl.openConnection();
            // 设定请求的方法为"POST"，默认是GET
            httpUrlConnection.setRequestMethod("POST");

// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
// http正文内，因此需要设为true, 默认情况下是false;
            httpUrlConnection.setDoOutput(true);

// 设置是否从httpUrlConnection读入，默认情况下是true;
            httpUrlConnection.setDoInput(true);

// Post 请求不能使用缓存
            httpUrlConnection.setUseCaches(false);

// 设定传送的内容类型是可序列化的java对象
// (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
            httpUrlConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");

// 连接，从上述url.openConnection()至此的配置必须要在connect之前完成，
            httpUrlConnection.connect();
            if (httpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpUrlConnection.getInputStream();

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getRequest(String url, Map<Object, Object> paramsMap) {

    }

    @Override
    public void postRequest(String url, Map<Object, Object> paramsMap) {
        Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {

            }
        }).subscribeOn(Schedulers.io())// 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }


}
