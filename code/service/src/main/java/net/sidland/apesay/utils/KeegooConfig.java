package net.sidland.apesay.utils;

import java.util.Properties;

public class KeegooConfig {
	
	public static String mongoDBHOSTS;
	public static String momgoDBPORTS;
	
	public static String mongoDBIP;
	public static String mongoDBPORT;
	
	/** mogoDB 名称 */
	public static String mongoDBName;
	public static String mongoDBCheck;
	/** mongodb连接数 */
	public static String mongodbConnectionsCount;
	
	public static Boolean publishFilter;
	
	public static String uploadFileDirector;
	public static String imgUrl;
	
	public static String messageTitle;
	
	public static String smsChannelTrigger;
	
	public static String notifyUrl;
	
	public static String orderUpdateTime=null;
	
	public static Integer orderOverTime;
	
	static{
		try {
			Properties p = PropertiesUtil.getProperties("application.properties");
			/**
			 * mongodb配置
			 */
			mongoDBName= p.getProperty("mongoDBName");
			mongodbConnectionsCount= p.getProperty("mongodbConnectionsCount");
			mongoDBHOSTS = p.getProperty("mongoDBHOSTS");
			momgoDBPORTS = p.getProperty("mongoDBPORTS");
			mongoDBIP= p.getProperty("mongoDBIP");
			mongoDBPORT= p.getProperty("mongoDBPORT");
			mongoDBCheck= p.getProperty("mongoDBCheck");
			uploadFileDirector = p.getProperty("uploadFileDirector");
			imgUrl = p.getProperty("imgUrl");
			messageTitle = p.getProperty("messageTitle");
			notifyUrl=p.getProperty("notify.url");
			orderOverTime=DataTypeUtils.isNotEmpty(p.getProperty("order.over.time"))?Integer.valueOf(p.getProperty("order.over.time")):30;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
