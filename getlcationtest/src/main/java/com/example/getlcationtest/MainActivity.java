package com.example.getlcationtest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity {
    TextView tv_show;
    Button btn;
    LocationManager locationManager;
    MyLocationListener myLocationListener = new MyLocationListener();
    public int GET_LOCATION_PERMISSION_REQUEST_CODE = 01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        tv_show = (TextView) findViewById(R.id.tv_show_location);
        btn = (Button) findViewById(R.id.btn_getLocation);
    }

    public void getLocation(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.ACCESS_FINE_LOCATION}
                    , GET_LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 2, 50, new MyLocationListener());
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            setCriteria();
        } else {
            //无法定位：1、提示用户打开定位服务；2、跳转到设置界面
            Toast.makeText(this, "无法定位，请打开定位服务", Toast.LENGTH_SHORT).show();
            Intent i = new Intent();
            i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
        }
    }

    private void setCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
        criteria.setAltitudeRequired(false);//无海拔要求
        criteria.setBearingRequired(false);//无方位要求
        criteria.setCostAllowed(true);//允许产生资费
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗
        // 获取最佳服务对象
        String provider = locationManager.getBestProvider(criteria, true);
        //注册监听
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            //获取当前位置，这里只用到了经纬度
            String string = "纬度为：" + location.getLatitude() + ",经度为："
                    + location.getLongitude();
            parseLocation(location.getLatitude(), location.getLongitude());
        }
        locationManager.requestLocationUpdates(provider, 1000 * 2, 2, myLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_LOCATION_PERMISSION_REQUEST_CODE) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                //        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 2, 50, new MyLocationListener());
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                setCriteria();
            } else {
                //无法定位：1、提示用户打开定位服务；2、跳转到设置界面
                Toast.makeText(this, "无法定位，请打开定位服务", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        }
    }

    //实现监听接口
    private final class MyLocationListener implements LocationListener {
        @Override// 位置的改变
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            double latitude = location.getLatitude();// 维度
            double longitude = location.getLongitude();// 经度
            parseLocation(latitude, longitude);
            //显示当前坐标
            Toast.makeText(MainActivity.this, "location:(" + latitude + "," + longitude + ")", Toast.LENGTH_LONG).show();
        }

        @Override// gps卫星有一个没有找到
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
            if (LocationProvider.OUT_OF_SERVICE == status) {
                Toast.makeText(MainActivity.this, "GPS服务丢失,切换至网络定位",
                        Toast.LENGTH_SHORT).show();

            }
            Toast.makeText(MainActivity.this, "location:(" + "onStatusChanged" + ")", Toast.LENGTH_LONG).show();
        }

        @Override// 某个设置被打开
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
            Toast.makeText(MainActivity.this, "location:(" + "onProviderEnabled" + ")", Toast.LENGTH_LONG).show();
        }

        @Override// 某个设置被关闭
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
            Toast.makeText(MainActivity.this, "location:(" + "onProviderDisabled" + ")", Toast.LENGTH_LONG).show();
        }

    }

    private void parseLocation(double latitude, double longitude) {
        Geocoder gc = new Geocoder(this, Locale.getDefault());
        List<Address> locationList = null;
        try {
            locationList = gc.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (locationList.size()>0){
            Address address = locationList.get(0);//得到Address实例
//Log.i(TAG, "address =" + address);
            String countryName = address.getCountryName();//得到国家名称，比方：中国
            Log.i("MainActivity", "countryName = " + countryName);
            String locality = address.getLocality();//得到城市名称，比方：北京市
            Log.i("MainActivity", "locality = " + locality);
            String addressLine = null;
            for (int i = 0; address.getAddressLine(i) != null; i++) {
                addressLine = address.getAddressLine(i);//得到周边信息。包含街道等。i=0，得到街道名称
                Log.i("MainActivity", "addressLine = " + addressLine);
            }
            tv_show.setText(countryName + locality + addressLine);
        }
    }
}
