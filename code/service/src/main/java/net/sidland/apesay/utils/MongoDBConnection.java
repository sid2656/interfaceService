/**
 * 
 */
package net.sidland.apesay.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;


/**
 * 
 * ClassName: MongoDBConnection 
 * Reason: 修改mongodb数据库获取方式 
 * date: 2015年8月27日 上午10:34:56 
 *
 * @author sid
 */
@SuppressWarnings("deprecation")
public class MongoDBConnection {

	protected static Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);

	private static MongoClient instance = null;

	protected static boolean isRenew = false;
	
	public static volatile boolean isShard = true;
	/**
	 * Mongodb连接实例
	 * 
	 * @return
	 */
	public synchronized static MongoClient getMongoClient() {
		if (instance != null) {
			return instance;
		}else{
			try {
				MongoClientOptions.Builder build = new MongoClientOptions.Builder();
				//build.autoConnectRetry(true);//是否连接自动重试
				build.connectionsPerHost(Integer.valueOf(ServiceConfig.mongodbConnectionsCount));//连接数
				build.connectTimeout(30000);//连接超时时间
				build.maxWaitTime(120000);//最大等待时间
				build.socketKeepAlive(true);//保持连接
				build.socketTimeout(0);//0，不限时间
				//build.maxAutoConnectRetryTime(1);//最大重试时间，单位秒
				build.threadsAllowedToBlockForConnectionMultiplier(50);
				MongoClientOptions mongoClientOptions = build.build();

				List<ServerAddress> addresses = new ArrayList<ServerAddress>();
				if (DataTypeUtils.isNotEmpty(ServiceConfig.mongoDBHOSTS)) {
					String[] hosts = ServiceConfig.mongoDBHOSTS.split(",");
					String[] ports = ServiceConfig.momgoDBPORTS.split(",");
					for (int i = 0; i < hosts.length; i++) {
						ServerAddress address = new ServerAddress(hosts[i], Integer.valueOf(ports[i]));
						addresses.add(address);
					}
					instance = new MongoClient(addresses,mongoClientOptions);
					MongoDBConnection.isShard = true;
				}else{
					instance = new MongoClient(ServiceConfig.mongoDBIP, Integer.valueOf(ServiceConfig.mongoDBPORT));
					MongoDBConnection.isShard = false;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				logger.error("Mongon数据库服务器连接失败！", e);
			}
		}
		return instance;
	}

	public static DB getDB(String dbname) {
		return getMongoClient().getDB(dbname);
	}

	/**
	 * 
	 * testConnection(beat heart...) 返回类型：void
	 * 
	 * @exception
	 * @since 1.0.0
	 */
	@SuppressWarnings("unused")
	private static void testConnection() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						List<ServerAddress> serverAddrs = getMongoClient().getServerAddressList();
						for (ServerAddress serverAddr : serverAddrs) {
							logger.warn("mongodb ServerAddress:" + serverAddr.getHost() + ":" + serverAddr.getPort());
						}
						//logger.warn("mongodb debugString:" + getMongoClient().debugString() + ",testMongodbConnection [Cart] recordCount:" + getMongoClient().getDB(KeegooConfig.mongoDBName).getCollection("Cart").getCount() + "...");
						Thread.sleep(60000);
					} catch (Exception e) {
						logger.error("mongodb连接心跳测试失败！,msg:" + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}, "test mongodb connection").start();

	}

	/**
	 * instance
	 * 
	 * @return the instance
	 * @since 1.0.0
	 */

	public static MongoClient getInstance() {
		return instance;
	}

	/**
	 * @param instance
	 *            the instance to set
	 */

	public static void setInstance(MongoClient instance) {
		MongoDBConnection.instance = instance;
	}
}
