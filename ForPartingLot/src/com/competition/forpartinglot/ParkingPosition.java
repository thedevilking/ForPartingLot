package com.competition.forpartinglot;
/**
 * 存储侧方位停车场信息的类
 * @author thedevilking
 *
 */
class ParkingPosition {
	String county;//区县名称
	String parkingname;//侧方位停车场名
	int largeParking;//大型泊位数量
	int smallParking;//小型泊位数量
	String remark;//备注
	
	String showmesg;

	/*public ParkingPosition(String county,String parkingname,int largeParking,int smallParking)
	{
		this.county=county;
		this.parkingname=parkingname;
		this.largeParking=largeParking;
		this.smallParking=smallParking;
	}*/
	
	public ParkingPosition(String line)
	{
		String[] sbyte=line.split(",");
		this.county=sbyte[1];
		this.parkingname=sbyte[4];
		this.largeParking=Integer.valueOf(sbyte[6]);
		this.smallParking=Integer.valueOf(sbyte[7]);
		this.remark=sbyte[19];
		//总的展示信息
		this.showmesg=county+"区"+parkingname+"侧方位停车场，大型泊位"+largeParking+"个，小型泊位"+smallParking+"个/n+备注："+remark;
	}
}
