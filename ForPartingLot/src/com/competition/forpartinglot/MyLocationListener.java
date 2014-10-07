package com.competition.forpartinglot;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

class MyLocationListener  implements BDLocationListener{
	public boolean value=false;
	public String time=null;
	public double latitude;
	public double longitude;
	public float radius;
	
	private int count=0;
	
	@Override
	public void onReceiveLocation(BDLocation location) {
		
		if(location==null)
		{
			value=false;
			return;
		}
		
		time=location.getTime();
		latitude=location.getLatitude();
		longitude=location.getLongitude();
		radius=location.getRadius();
		value=true;
		
		Log.i("百度地图第"+(++count)+"次定位","纬度："+latitude+"/t经度："+longitude);
		
		// TODO Auto-generated method stub
		/*if (location == null)
            return ;
		StringBuffer sb = new StringBuffer(256);
		sb.append("time : ");
		sb.append(location.getTime());
		sb.append("\nerror code : ");
		sb.append(location.getLocType());
		sb.append("\nlatitude : ");
		sb.append(location.getLatitude());
		sb.append("\nlontitude : ");
		sb.append(location.getLongitude());
		sb.append("\nradius : ");
		sb.append(location.getRadius());
		if (location.getLocType() == BDLocation.TypeGpsLocation){
			sb.append("\nspeed : ");
			sb.append(location.getSpeed());
			sb.append("\nsatellite : ");
			sb.append(location.getSatelliteNumber());
		} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
			sb.append("\naddr : ");
			sb.append(location.getAddrStr());
		} */

	//	logMsg(sb.toString());
	}

}
