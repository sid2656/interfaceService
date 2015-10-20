package net.sidland.apesay.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

import net.sidland.apesay.cache.CacheService;
import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;
import net.sidland.apesay.utils.DateUtils;
import net.sidland.apesay.utils.KeegooConfig;
import net.sidland.apesay.utils.MongoDBConnection;

/**
 * 
 * ClassName: MongoDAO 
 * date: 2015年10月17日 下午2:53:06 
 *
 * @author sid
 */
@SuppressWarnings("deprecation")
@Component
public class MongoDAO {
	@Autowired
	private CacheService cacheService;
	
	public static DB db;
	public static MongoDatabase babyrunDatabase;

	protected static Logger logger = LoggerFactory.getLogger(MongoDAO.class);
	
	static {
		db = MongoDBConnection.getMongoClient().getDB(KeegooConfig.mongoDBName);
		babyrunDatabase= MongoDBConnection.getMongoClient().getDatabase(KeegooConfig.mongoDBName);
	}

	/**
	 * 
	 * save(新增)
	 * 
	 * @param model
	 * @param json
	 * @return
	 * @throws Exception
	 *             String
	 * @exception
	 * @since 1.0.0
	 */
	public JSONObject save(String model, String json) throws ServiceException {
		//新增的集合进行按系统主键进行分片
		if(!db.collectionExists(model)){
			shardCollection(KeegooConfig.mongoDBName, model, "_id");
		}
		String date = DateUtils.date24ToString(new Date());
		DBObject dbObject = (DBObject) JSON.parse(json);
		ObjectId objectId = ObjectId.get();
		String id = objectId.toString();
		dbObject.put("_id", objectId);// 系统主键
		dbObject.put(Constant.OBJECTID, id);// 业务主键
		dbObject.put(Constant.CREATED_AT, date);
		dbObject.put(Constant.UPDATED_AT, date);
		DBCollection dbCollection = db.getCollection(model);
		dbCollection.insert(dbObject, WriteConcern.SAFE);
//		if (wr.getError() != null) {
//			throw new ServiceException(wr.getError());
//		}
		try {
			cacheService.set(model+":"+id, dbObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject returnValue = new JSONObject();
		returnValue.put(Constant.OBJECTID, id);// 业务主键
		returnValue.put(Constant.CREATED_AT, date);
		returnValue.put(Constant.UPDATED_AT, date);
		return returnValue;
	}

	public void saveTemp(String model, String json) throws ServiceException {
		//新增的集合进行按系统主键进行分片
		if(!db.collectionExists(model)){
			shardCollection(KeegooConfig.mongoDBName, model, "_id");
		}
		DBCollection dbCollection = db.getCollection(model);
		DBObject dbObject = (DBObject) JSON.parse(json);
		ObjectId objectId = new ObjectId(String.valueOf(dbObject.get(Constant.OBJECTID)));
		dbObject.put("_id", objectId);// 系统主键
		dbCollection.insert(dbObject, WriteConcern.SAFE);
	}

	
	/**
	 * 
	 * delete(删除)
	 * 
	 * @param model
	 * @param id
	 * @return String
	 * @exception
	 * @since 1.0.0
	 */
	public JSONObject delete(String model, String id) throws ServiceException{
		DBCollection dbCollection = db.getCollection(model);
		String date = DateUtils.date24ToString(new Date());
		DBObject dbObject = new BasicDBObject(Constant.OBJECTID, id);
		WriteResult wr = dbCollection.remove(dbObject,WriteConcern.SAFE);
//		if (wr.getError() != null) {
//			throw new ServiceException(wr.getError());
//		}
		if(wr.getN()<=0) {
			return null;
		}
		try {
			cacheService.del(model+":"+id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject returnValue = new JSONObject();
		returnValue.put(Constant.OBJECTID, id);// 业务主键
		returnValue.put(Constant.UPDATED_AT, date);
		return returnValue;
	}

	/**
	 * 
	 * update(更新)
	 * 
	 * @param model
	 * @param json
	 * @param id
	 * @return String
	 * @exception
	 * @since 1.0.0
	 */
	public JSONObject update(String model, String json, String id) throws ServiceException{
		DBCollection dbCollection = db.getCollection(model);
		DBObject updateDBObject = (DBObject) JSON.parse(json);
		String date = DateUtils.date24ToString(new Date());
		updateDBObject.put(Constant.UPDATED_AT, date);
		WriteResult wr = dbCollection.update(new BasicDBObject(Constant.OBJECTID, id), new BasicDBObject("$set", updateDBObject),false,true,WriteConcern.SAFE);
//		if (wr.getError() != null) {
//			throw new ServiceException(wr.getError());
//		}
		if(wr.getN()<=0 || !wr.isUpdateOfExisting()) {
			return null;
		}
		try {
			cacheService.del(model+":"+id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject returnValue = new JSONObject();
		returnValue.put(Constant.OBJECTID, id);// 业务主键
		returnValue.put(Constant.UPDATED_AT, date);
		return returnValue;
	}

	public JSONArray aggregate(String model, String query,int skip, int limit) throws Exception {
		
		DBCollection dbCollection = db.getCollection(model);
		BasicDBObject q = (BasicDBObject)JSON.parse(query);
		BasicDBObject s = new BasicDBObject("$skip", skip);
		BasicDBObject l = new BasicDBObject("$limit", limit);
		List<BasicDBObject> list = new ArrayList<BasicDBObject>();
		list.add(q);
		list.add(s);
		list.add(l);
		AggregationOutput out = dbCollection.aggregate(list);
		Iterator<DBObject> itor = out.results().iterator();
		JSONArray results = new JSONArray();
		while(itor.hasNext()){
			results.add(JSONObject.parseObject(itor.next().toString()));
		}
		return results;
	}
	/**
	 * 
	 * get(按id查找)
	 * 
	 * @param model
	 * @param id
	 * @return String
	 * @exception
	 * @since 1.0.0
	 */
	public String get(String model, String id) throws ServiceException{
		
		String cacheString = cacheService.get(model+":"+id);
		
		if(!DataTypeUtils.isNotEmpty(cacheString)){
			DBCollection dbCollection = db.getCollection(model);
			DBObject dbObject = dbCollection.findOne(new BasicDBObject(Constant.OBJECTID, id));
			if(dbObject!=null){
				cacheString = dbObject.toString();
			}
			
			if(DataTypeUtils.isNotEmpty(cacheString)){
				try {
					cacheService.set(model+":"+id, cacheString);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if(!DataTypeUtils.isNotEmpty(cacheString)){
			return null;
		}
		return cacheString;
	}

	/**
	 * login(用户登录,成功后返回用户信息)
	 * 
	 * @param model
	 * @param id
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	public DBObject login(String model, String json) throws Exception {
		DBCollection dbCollection = db.getCollection(model);
		DBObject query = (DBObject) JSON.parse(json);// 设置查询条件
		BasicDBObject queryCondition = new BasicDBObject();
		BasicDBList values = new BasicDBList();
		// (email=loginIdentifying or username=loginIdentifying or
		// mobilePhoneNumber=loginIdentifying) and password=password
		String loginIdentifying = (String) query.get(Constant.User_loginIdentifying);
		
		values.add(new BasicDBObject(Constant.user_username, loginIdentifying));
		values.add(new BasicDBObject(Constant.user_mobilePhoneNumber, loginIdentifying));
		values.add(new BasicDBObject(Constant.user_email, loginIdentifying));
		values.add(new BasicDBObject(Constant.user_siteUserId, loginIdentifying));
		
		//如果第三方登陆 判断 authData.site.
		String  site = (String) query.get("site");
		
		if(site.equalsIgnoreCase("qq")) {
			values.add(new BasicDBObject("authData.qq.openid", loginIdentifying));
		}else if(site.equalsIgnoreCase("weibo")) {
			values.add(new BasicDBObject("authData.weibo.uid", loginIdentifying));
		}else if(site.equalsIgnoreCase("weixin")){
			values.add(new BasicDBObject("authData.weixin.openid", loginIdentifying));
		}
		
		DBObject dbObject = null;
		queryCondition.put("$or", values);
		DBObject user = dbCollection.findOne(queryCondition);
		if(site.equals("babyrun")){
			if(user==null) return null;
//			String salt = Constant.sha512_salt;
//			if(user.containsField("salt")&&DataTypeUtils.isNotEmpty((String)user.get("salt"))){
//				salt = (String)user.get("salt");
//			}
//			String password = new Sha512Hash((String)query.get(Constant.user_password), salt, 513).toBase64();
			String password = "";
			queryCondition.put(Constant.user_password, password);
			dbObject = dbCollection.findOne(queryCondition);
		}else{
			dbObject = user;
		}
		
		return dbObject;
	}

	/**
	 * 
	 * find(高级查询)
	 * 
	 * @param query
	 *            查询条件
	 * @param fields
	 *            返回字段
	 * @param orderBy
	 *            排序条件
	 * @param model
	 *            查询模型
	 * @param numToSkip
	 *            跳过条数
	 * @param batchSize
	 *            返回条数
	 * @return List<DBObject>
	 * @exception
	 * @since 1.0.0
	 */
	public DBObject find(String where, String fields, String orderBy, String collection, int skip, int size) {
		DBCollection dbCollection = db.getCollection(collection);
		DBObject ref = (DBObject) JSON.parse(where);
		DBObject keys = (DBObject) JSON.parse(fields);
		keys.put("_id", 0);//不返回系统主键
		DBObject order = (DBObject) JSON.parse(orderBy);
		DBCursor dbCursor = dbCollection.find(ref, keys).sort(order).skip(skip).limit(size);
		BasicDBList list = new BasicDBList();
		DBObject result = new BasicDBObject();
		while (dbCursor.hasNext()) {
			list.add(dbCursor.next());
		}
		result.put("count", dbCursor.count());
		result.put(Constant.RESULTS, list);
		return result;
	}

	/**
	 * 
	 * find(高级查询)
	 * 
	 * @param query
	 *            查询条件
	 * @param fields
	 *            返回字段
	 * @param orderBy
	 *            排序条件
	 * @param model
	 *            查询模型
	 * @return List<DBObject>
	 * @exception
	 * @since 1.0.0
	 */
	public DBObject find(String where, String fields, String orderBy, String collection) {
		DBCollection dbCollection = db.getCollection(collection);
		DBObject ref = (DBObject) JSON.parse(where);
		DBObject keys = (DBObject) JSON.parse(fields);
		keys.put("_id", 0);//不返回系统主键
		DBObject order = (DBObject) JSON.parse(orderBy);
		DBCursor dbCursor = dbCollection.find(ref, keys).sort(order);
		BasicDBList list = new BasicDBList();
		DBObject result = new BasicDBObject();
		while (dbCursor.hasNext()) {
			list.add(dbCursor.next());
		}
		result.put("count", dbCursor.count());
		result.put(Constant.RESULTS, list);
		return result;
	}

	/**
	 * 
	 * eval(在mongo Server上执行js函数)
	 * 
	 * @param function
	 * @param args
	 * @return String
	 * @exception
	 * @since 1.0.0
	 */
	public String eval(String function, String json) {
		// 使用javascript最好是预先定义好注册到数据库服务端，客户端传入函数名及参数即可
		String code = "function(obj){return "+function+"(obj);}";
		DBObject obj = (DBObject) JSON.parse(json);
		Object o = db.eval(code, obj);
		String value = null;
		if(o instanceof Boolean){
			value = String.valueOf(((Boolean)o).booleanValue());
		}else if(o instanceof Number){
			value = String.valueOf((Number)o);
		}else if(o instanceof String){
			value = String.valueOf(o);
		}else if(o instanceof BasicDBObject){
			value = ((BasicDBObject)o).toString();
		}else if(o instanceof BasicDBList){
			value = ((BasicDBList)o).toString();
		}
		return value;
	}

	/**
	 * 插入
	 * 
	 * @param collection
	 *            集合名
	 * @param obj
	 *            新文档
	 */
	public WriteResult insert(String collection, DBObject obj) {
		DBCollection dbCollection = db.getCollection(collection);
		return dbCollection.insert(obj);
	}

	/**
	 * 更新
	 * 
	 * @param collection
	 *            集合名
	 * @param dbDoc
	 *            更新目标条件
	 * @param newDoc
	 *            新文档
	 * @param insertAllow
	 *            查找无效后是否可插入
	 * @param multiAlter
	 *            是否可以多条更新
	 */
	public WriteResult update(String collection, DBObject dbDoc, DBObject newDoc, boolean insertAllow, boolean multiAlter) {
		DBCollection dbCollection = db.getCollection(collection);
		return dbCollection.update(dbDoc, newDoc, insertAllow, multiAlter);
	}

	/**
	 * 删除
	 * 
	 * @param collection
	 *            集合名
	 * @param delObj
	 *            删除目标条件
	 */
	public void delete(String collection, DBObject delObj) {
		DBCollection dbCollection = db.getCollection(collection);
		dbCollection.remove(delObj);
	}

	/**
	 * 查询
	 * 
	 * @param collection
	 *            集合名
	 * @param obj
	 *            条件文档
	 * @return DBCursor 游标
	 */
	public DBCursor find(String collection, DBObject obj) {
		DBCollection dbCollection = db.getCollection(collection);
		
		return dbCollection.find(obj);
	}

	/**
	 * 查询unique
	 * 
	 * @param collection
	 *            集合名
	 * @param obj
	 *            条件文档
	 * @return DBObject 文档对象
	 */
	public DBObject findOne(String collection, DBObject obj) {
		DBCollection dbCollection = db.getCollection(collection);
		return dbCollection.findOne(obj);
	}

	/**
	 * 查询
	 * 
	 * @param collection
	 *            集合名
	 * @param key
	 *            查询条件键
	 * @param value
	 *            查询条件值
	 * @return DBCursor 游标
	 */
	public DBCursor find(String collection, String key, String value) {
		DBObject query = new BasicDBObject();
		query.put(key, value);
		DBCollection dbCollection = db.getCollection(collection);
		return dbCollection.find(query);
	}

	/**
	 * 查询unique
	 * 
	 * @param collection
	 *            集合名
	 * @param key
	 *            查询条件键
	 * @param value
	 *            查询条件值
	 * @return DBObject 文档对象
	 */
	public DBObject findOne(String collection, String key, String value) {
		DBObject query = new BasicDBObject();
		query.put(key, value);
		DBCollection dbCollection = db.getCollection(collection);
		return dbCollection.findOne(query);
	}
	
	//对集合启动分片，前提是所在数据库已启动分片
	private boolean shardCollection(String dbname,String collection,String key){
		if (MongoDBConnection.isShard) {
			logger.info("新的集合"+collection+"启动分片");
			MongoDatabase admin = MongoDBConnection.getMongoClient().getDatabase("admin");
			BasicDBObject cmd = new BasicDBObject();
			BasicDBObject shardKeys = new BasicDBObject();
			shardKeys.put("_id",  1);shardKeys.put(Constant.OBJECTID,  1);
			cmd.put("shardcollection", dbname+"."+collection);
			cmd.put("key", shardKeys);
			Document result = admin.runCommand(cmd);
			logger.info("mongodb cmd:"+cmd);
			logger.info("document:"+result.toJson());
			if(result==null||result.isEmpty()){
				return false;
			}else if(result.getDouble("ok")>0){
				return true;
			}
		}
		return false;
	}
	
	//对数据库启动分片
//	private boolean enableSharding(String dbname){
//		MongoDatabase admin = MongoDBConnection.getMongoClient().getDatabase("admin");
//		BasicDBObject cmd = new BasicDBObject();
//		cmd.put("enablesharding", dbname+"."+dbname);
//		Document result = admin.runCommand(cmd);
//		logger.info("mongodb cmd:"+cmd);
//		logger.info("document:"+result.toJson());
//		if(result==null||result.isEmpty()){
//			return false;
//		}else if(result.getDouble("ok")>0){
//			return true;
//		}
//		return false;
//	}
}
