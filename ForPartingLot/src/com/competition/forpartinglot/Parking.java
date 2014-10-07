package com.competition.forpartinglot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

public class Parking extends Activity{
	//百度地图控件
	private MapView mMapView=null;
	//百度地图窗口中添加定位的父类控件
	BaiduMap mBaiduMap=null;
	
	//百度SDK定位
	LocationClient mLocationClient=null;
	//百度定位事件监听
	MyLocationListener myListener=null;
	
	/*//百度地理位置编码
	GeoCoder geocoder=null;*/
	
	//刷新自己位置的线程生命周期
	boolean runtime=true;
	
	//周边停车位的hashmap
	HashMap<Marker,ParkingPosition> mapparking=null;
//	List<ParkingPosition> parkinglist=null;
	
	//驾车路线查询
	RoutePlanSearch mSearch=null;
	//驾车线路覆盖物
	DrivingRouteOverlay overlay=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//初始化百度地图
		SDKInitializer.initialize(getApplicationContext());
		
		setContentView(R.layout.activity_parking);
		
		//获取地图控件引用  
        mMapView = (MapView) findViewById(R.id.bmapView);  
        mBaiduMap=mMapView.getMap();
        
        //驾车线路查询
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
			
			@Override
			public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onGetTransitRouteResult(TransitRouteResult arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onGetDrivingRouteResult(DrivingRouteResult arg0) {
				// TODO Auto-generated method stub
				if(arg0==null)
					return;
				
				if(overlay!=null)
					overlay.removeFromMap();
				else
					overlay=new DrivingRouteOverlay(mBaiduMap);
//				mBaiduMap.setOnMarkerClickListener(overlay);
				overlay.setData(arg0.getRouteLines().get(0));
				overlay.addToMap();
				overlay.zoomToSpan();
				
			}
		});
        
        //对周边节点的事件响应
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker arg0) {
				// TODO Auto-generated method stub
				 
				PlanNode enNode = PlanNode.withLocation(arg0.getPosition());
				PlanNode stNode = PlanNode.withLocation(new LatLng(myListener.latitude, myListener.longitude));
				
				mSearch.drivingSearch((new DrivingRoutePlanOption())  
					    .from(stNode)  
					    .to(enNode));
				
				return false;
			}
		});
        
        //初始化定位SDK
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        
        
        //定位参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
        option.setOpenGps(true);//打开GPS
        option.setProdName("BaiduLocation");//线程名称
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(1200);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
//        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        
        myListener=new MyLocationListener();
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        
        //启动定位服务
        mLocationClient.start();
        mLocationClient.requestLocation();
		
		    
        //启动显示自己位置线程
        LocatingMe locating=new LocatingMe();
        locating.start();
        
        
        //初始化停车位的hashmap,以及进行周边停车位定位
		mapparking=new HashMap<Marker, ParkingPosition>();
		try
		{
			ShowPosition showposition=new ShowPosition(getResources().getAssets().open("ParkingPosition.txt"));
			showposition.start();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
        
	}

	//菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override  
    protected void onDestroy() {  
		runtime=false;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();
        //销毁位置监听服务
        
//        mLocationClient.unRegisterLocationListener(myListener);
        if(mLocationClient!=null && mLocationClient.isStarted())
        {
        	mLocationClient.stop();
        	mLocationClient=null;
        }
        //销毁公交查询
        mSearch.destroy();
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume();  
        
        }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();  
        
        }

    //显示周边停车位的节点
    private class ShowPosition extends Thread
    {
    	GetData getdata=null;
    	GeoCoder geocoder=null;
    	
    	String showmesg="";
    	
    	int sequence=1;
    	
    	//定位周边节点的坐标
//		LatLng point=null;
    	private boolean isRunOfTime()
    	{
    		if(!runtime)
    			return true;
    		else
    			return false;
    	}
		
    	public ShowPosition(InputStream in)
    	{
    		try {
    			getdata=new GetData(getResources().getAssets().open("ParkingPosition.txt"));
//    			parkinglist=getdata.parkinglist;
    			
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    		//根据车位的地理位置定位
			geocoder=GeoCoder.newInstance();
			OnGetGeoCoderResultListener geoCoderListener=new OnGetGeoCoderResultListener() 
			{
				//地理位置编码的监听事件
				//反向地址编码，坐标->地址
				@Override
				public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) 
				{
					// TODO Auto-generated method stub
					
				}
	    			
				//正向地址编码，地址->坐标
				@Override
				public void onGetGeoCodeResult(GeoCodeResult result) 
				{
					// TODO Auto-generated method stub
					if(result==null || result.getAddress()==null)
					{
						Log.i("正向地址编码失败","地址：“"+result.getAddress()+"”不能被初始化");
						sequence=1;
						return;
					}
					Log.i("正向地址编码成功","地址：“"+result.getAddress()+"”");
//					LatLng location=result.getLocation();
//					point =new LatLng(location.latitude	, location.longitude);
					//定义周边停车场坐标点 
//					LatLng point = new LatLng(point.latitude, point.longitude);  
					//构建Marker图标  
					BitmapDescriptor bitmap = BitmapDescriptorFactory  
					    .fromResource(R.drawable.parking);  
					//构建MarkerOption，用于在地图上添加Marker  
					OverlayOptions option = new MarkerOptions()  
					    .position(result.getLocation())  
					    .icon(bitmap)
					    .draggable(false)
					    .title(showmesg);  
					//在地图上添加Marker，并显示  
	//				BaiduMap mBaiduMap=mMapView.getMap();
					Marker marker = (Marker)mBaiduMap.addOverlay(option);
					sequence=1;
				}
    		};
    		geocoder.setOnGetGeoCodeResultListener(geoCoderListener);//注册监听
    		
    	}
    	
    	@Override
    	public void run() 
    	{
    		
    		//测试正向编码
//    		geocoder.geocode(new GeoCodeOption().city("北京").address("光明路停车场"));
    		
    		/*try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
    		
    		
    		int i=0;
    		for(ParkingPosition parking:getdata.parkinglist)
    		{
    			//主线程退出时，结束线程
    			if(isRunOfTime())
    				break;
    			
    			Log.i("添加周边停车场","第"+(++i)+"个");
    			
    			while(sequence==0)
        		{
    				//主线程退出时，结束线程
        			if(isRunOfTime())
        				break;
    				
        			try 
        			{
    					Thread.sleep(100);
    				} 
        			catch (InterruptedException e) 
        			{
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
        		}
    			
    			if(sequence==1)
    			{
    				geocoder.geocode(new GeoCodeOption().city("北京").address(parking.parkingname));
    				showmesg=parking.showmesg;
    				sequence=0;
    			}
    			
    			
    		}
    		
    		while(sequence==0)
    		{
    			//主线程退出时，结束线程
    			if(isRunOfTime())
    				break;
    			
    			try 
    			{
					Thread.sleep(100);
				} 
    			catch (InterruptedException e) 
    			{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		
			geocoder.destroy();
			
    		/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
    		
    	}
    }
    
    //负责刷新显示自己位置的线程
	private class LocatingMe extends Thread
	{
		
		@Override
		public void run() 
		{
			while(runtime)
			{
				Marker marker=null;
				
				if(myListener.value)
				{
					
					double latitude=myListener.latitude;
					double longitude=myListener.longitude;
					
					
					//在地图上显示自己的位置
					//定义Maker坐标点  
					LatLng point = new LatLng(latitude, longitude);  
					//构建Marker图标  
					BitmapDescriptor bitmap = BitmapDescriptorFactory  
					    .fromResource(R.drawable.vehicle);  
					//构建MarkerOption，用于在地图上添加Marker  
					OverlayOptions option = new MarkerOptions()  
					    .position(point)  
					    .icon(bitmap);  
					//在地图上添加Marker，并显示  
//					BaiduMap mBaiduMap=mMapView.getMap();
					marker = (Marker)mBaiduMap.addOverlay(option); 
					//marker.remove(); //用来消除添加的覆盖物
					//设置监听事件
					/*mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
						
						@Override
						public boolean onMarkerClick(Marker arg0) {
							// TODO Auto-generated method stub
							
							return false;
						}
					});*/
					
				}//结束if判断
				
				//线程睡眠500ms
				try {
					Thread.sleep(1400);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(marker!=null)
					marker.remove();
				
			}//结束while循环
		}
	}
	
	
}
