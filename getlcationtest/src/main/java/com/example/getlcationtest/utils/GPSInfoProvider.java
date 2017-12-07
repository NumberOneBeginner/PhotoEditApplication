package com.example.getlcationtest.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by peter on 2017/12/7.
 */

/**
 * 保证这个类只存在一个实例
 * @author zehua
 *
 */
public class GPSInfoProvider {
    LocationManager manager;
    private static GPSInfoProvider mGPSInfoProvider;  //单例
    private static Context context;             //单例
    private static MyLoactionListener listener; //单例
    //1.私有化构造方法

    private GPSInfoProvider(){};

    //2. 提供一个静态的方法 可以返回他的一个实例
    public static synchronized GPSInfoProvider getInstance(Context context){
        if(mGPSInfoProvider==null){
            synchronized (GPSInfoProvider.class) {
                if(mGPSInfoProvider == null){
                    mGPSInfoProvider = new GPSInfoProvider();
                    GPSInfoProvider.context = context;
                }
            }
        }
        return mGPSInfoProvider;
    }


    // 获取gps 信息
    public String getLocation(){
        manager =(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //获取所有的定位方式
        //manager.getAllProviders(); // gps //wifi //
        //获取当前手机最好的位置提供者
        String provider = getProvider(manager);
        // 注册位置的监听器
        //60000每隔一分钟获取当前位置(最大频率)
        //位置每改变50米重新获取位置信息
        //getListener()位置发生改变时的回调方法
        manager.requestLocationUpdates(provider,60000, 50, getListener());
        //拿到最后一次的位置信息
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        String location = sp.getString("location", "");
        return location;
    }



    //停止gps监听
    public void stopGPSListener(){
        manager.removeUpdates(getListener());
    }

    //获取gps监听实例
    private synchronized MyLoactionListener getListener(){
        if(listener==null){
            synchronized (GPSInfoProvider.class) {
                if(listener == null){
                    listener = new MyLoactionListener();
                }
            }

        }
        return listener;
    }

    private class MyLoactionListener implements LocationListener{

        /**
         * 当手机位置发生改变的时候 调用的方法
         */
        @Override
        public void onLocationChanged(Location location) {
            String latitude ="latitude "+ location.getLatitude(); //获取纬度
            String longtitude = "longtitude "+ location.getLongitude(); //获取精度
            //最后一次获取到的位置信息 存放到sharedpreference里面
            SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
            Editor editor = sp.edit();
            editor.putString("location", latitude+" - "+ longtitude);
            editor.commit();
        }

        /**
         * 某一个设备的状态发生改变的时候 调用
         *  可用->不可用
         *  不可用->可用
         *  status 当前状态
         *  extras 额外消息
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        /**
         * 某个设备被打开
         */
        @Override
        public void onProviderEnabled(String provider) {

        }

        /**某个设备被禁用
         *
         */
        @Override
        public void onProviderDisabled(String provider) {

        }

    }

    /**\
     *
     * @param manager 位置管理服务
     * @return 最好的位置提供者
     */
    private String getProvider(LocationManager manager){
        //设置查询条件
        Criteria criteria = new Criteria();
        //定位精准度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //对海拔是否敏感
        criteria.setAltitudeRequired(false);
        //对手机耗电性能要求（获取频率）
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        //对速度变化是否敏感
        criteria.setSpeedRequired(true);
        //是否运行产生开销（费用）
        criteria.setCostAllowed(true);
        //如果置为ture只会返回当前打开的gps设备
        //如果置为false如果设备关闭也会返回
        return  manager.getBestProvider(criteria, true);
    }
}