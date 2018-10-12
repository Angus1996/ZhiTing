package com.example.android.zhiting;

import android.Manifest;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/*import com.baidu.location.Address;*/
import android.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.search.route.*;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.core.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy.ECAR_AVOID_JAM;
import static com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy.ECAR_TIME_FIRST;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    private DrawerLayout mDrawerLayout;
    private double longitude;
    private double latitude;
    private FloatingActionButton fab;
    private BDLocation mCurrentLocation;
    private Snackbar snackbar;
    private LatLng loc_start;
    private LatLng loc_end;
    private RoutePlanSearch routePlanSearch;
    private String loc_address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        /*baiduMap.setMyLocationEnabled(true);*/
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20f));
        //创建路径搜索工具
        routePlanSearch = RoutePlanSearch.newInstance();

        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMapOverlay(latLng);
                LatLng mCurrentlatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                if (latLng != mCurrentlatLng) {
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fabGray)));
                }
                /*getInfoFromLAL(latLng);*/
            }

            @Override
            public boolean onMapPoiClick(final MapPoi mapPoi) {

                setMapOverlay(mapPoi.getPosition());
                /*driverOnClick(mapView);*/
                loc_end=mapPoi.getPosition();
                //路线规划
                PlanNode stNode = PlanNode.withLocation(loc_start);
                PlanNode enNode = PlanNode.withLocation(loc_end);
                routePlanSearch.drivingSearch((new DrivingRoutePlanOption())
                        .policy(ECAR_AVOID_JAM)
                        .policy(ECAR_TIME_FIRST)
                        .from(stNode)
                        .to(enNode));

                // 反地理编码查询
                GeoCoder geoCoder = GeoCoder.newInstance();

                OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
                    // 反地理编码查询结果回调函数
                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                        if (result == null
                                || result.error != SearchResult.ERRORNO.NO_ERROR) {
                            // 没有检测到结果
                            Toast.makeText(MainActivity.this, "抱歉，未能找到结果",
                                    Toast.LENGTH_LONG).show();
                        }
                        /*Toast.makeText(MainActivity.this,
                                "位置：" + result.getAddress(), Toast.LENGTH_LONG)
                                .show();*/
                        loc_address = result.getAddress();
                    }
                    // 地理编码查询结果回调函数
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult result) {
                        if (result == null
                                || result.error != SearchResult.ERRORNO.NO_ERROR) {
                            // 没有检测到结果
                        }
                    }
                };
                // 设置地理编码检索监听者
                geoCoder.setOnGetGeoCodeResultListener(listener);
                //  latLang为坐标点
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(loc_end));

                //Snackbar弹出
                snackbar.make(mapView,mapPoi.getName()+"\n"+loc_address, Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(Color.WHITE)
                        .setAction("Reserve and Pay", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(MainActivity.this, PayActivity.class);
                                startActivity(intent);
                                Toast.makeText(MainActivity.this, mapPoi.getName(),Toast.LENGTH_SHORT).show();
                                if ((mapPoi.getPosition().latitude != mCurrentLocation.getLatitude()) && (mapPoi.getPosition().longitude
                                        != mCurrentLocation.getLongitude())) {
                                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fabGray)));
                                }

                            }
                        })
                        .show();


                return false;
            }
        });

        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {

            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                List<DrivingRouteLine> routeLines = drivingRouteResult.getRouteLines();
                if (routeLines != null) {
                    List<DrivingRouteLine.DrivingStep> allStep = routeLines.get(0).getAllStep();
                    for (DrivingRouteLine.DrivingStep drivingStep : allStep) {
                        List<LatLng> wayPoints = drivingStep.getWayPoints();
                        if (wayPoints.size() >= 2) {
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .color(Color.GREEN)
                                    .width(15);
                            polylineOptions.points(wayPoints);
                            baiduMap.addOverlay(polylineOptions);
                        }
                    }
                    /*for (int i=0; i<routeLines.size(); i++) {
                        List<DrivingRouteLine.DrivingStep> allStep = routeLines.get(i).getAllStep();
                        for (DrivingRouteLine.DrivingStep drivingStep : allStep) {
                            List<LatLng> wayPoints = drivingStep.getWayPoints();
                            if (wayPoints.size() >= 2) {
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .color(Color.rgb(50*i,50*i, 50*i))
                                        .width(15);
                                polylineOptions.points(wayPoints);
                                baiduMap.addOverlay(polylineOptions);
                            }
                        }
                    }*/

                    /*for (DrivingRouteLine routeLine : routeLines) {
                        List<DrivingRouteLine.DrivingStep> allStep = routeLine.getAllStep();
                        for (DrivingRouteLine.DrivingStep drivingStep : allStep) {
                            List<LatLng> wayPoints = drivingStep.getWayPoints();
                            if (wayPoints.size() >= 2) {
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .color(Color.GREEN)
                                        .width(15);
                                polylineOptions.points(wayPoints);
                                baiduMap.addOverlay(polylineOptions);
                            }
                        }
                    }*/
                }

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });



        positionText = (TextView) findViewById(R.id.position_text_view);

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        /*navView.setCheckedItem(R.id.nav_account);*/
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_account:
                        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_history:
                        Intent intent2 = new Intent(MainActivity.this, AccountActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_setting:
                        Intent intent3 = new Intent(MainActivity.this, AccountActivity.class);
                        startActivity(intent3);
                        break;
                    case R.id.nav_logout:
                        finish();
                        break;
                    default:
                }
                return true;
            }
        });

        /*Button fab = (Button) findViewById(R.id.fab);*/
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentLocation != null) {
                    LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    setMapOverlay(latLng);
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
                    baiduMap.animateMapStatus(mapStatusUpdate, 1000);
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                }
            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }



    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setLocationNotify(true);
        option.setIsNeedLocationDescribe(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            setMapOverlay(point);
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
            /*MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);*/
            isFirstLocate = false;
        }
        /*MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);*/
    }


    // 在地图上添加标注
    private void setMapOverlay(LatLng point) {
        latitude = point.latitude;
        longitude = point.longitude;

        baiduMap.clear();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmarker);
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        baiduMap.addOverlay(option);
    }

    public class MyLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && (bdLocation.getLocType() == 161 || bdLocation.getLocType() == 66)) {
                if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() ==
                        BDLocation.TypeNetWorkLocation) {
                    navigateTo(bdLocation);
                }
            } else if (bdLocation == null) {
                Toast.makeText(MainActivity.this, "定位失败，请检查手机网络或设置！", Toast.LENGTH_LONG).show();
            }
            mCurrentLocation = bdLocation;
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            loc_start = latLng;
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
        routePlanSearch.destroy();
    }
}
