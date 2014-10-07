package com.competition.forpartinglot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

class GetData {
		
	List<ParkingPosition> parkinglist=null;
	
	public GetData(InputStream in)//穿入读取assets的数据流
	{

		parkinglist=new ArrayList<ParkingPosition>();
		
		try { 
			InputStreamReader inputReader = new InputStreamReader(in,Charset.forName("GBK")); 
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line=bufReader.readLine();//读取第一行表头
			while((line = bufReader.readLine()) != null)
			{
				parkinglist.add(new ParkingPosition(line));//读取每行的数据并存储
			}
			
       } catch (Exception e) { 
           e.printStackTrace(); 
       }
		
		/*for(ParkingPosition temp:parkinglist)
		{
			Log.i("读取的侧方位停车数据",temp.showmesg);
		}*/
	}
}
