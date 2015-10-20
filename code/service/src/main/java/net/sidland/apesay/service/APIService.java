package net.sidland.apesay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;

import net.sidland.apesay.dao.MongoDAO;
import net.sidland.apesay.exception.ServiceException;

/**
 * 
 * ClassName: APIService 
 * date: 2015年10月17日 下午2:52:39 
 *
 * @author sid
 */
@Component
public class APIService {

	@Autowired
	private MongoDAO mongoDao;

	/**
	 * 
	 * get(按id获取对象)
	 * 
	 * @param model
	 * @param id
	 * @return
	 * @throws Exception
	 *             String
	 * @exception
	 * @since 1.0.0
	 */
	public JSONObject get(String model, String id) throws ServiceException {
		String entity = mongoDao.get(model, id);
		if(entity!=null){
			JSONObject jsonObj = JSON.parseObject(entity);
			jsonObj.remove("_id");
			return jsonObj;
		}
		return null;
	}

	/**
	 * 
	 * save(保存对象)
	 * 
	 * @param model
	 * @param json
	 * @return JSONObject
	 * @throws Exception
	 *             String
	 * @exception
	 * @since 1.0.0
	 */
	public JSONObject save(String model, String json) throws ServiceException {
		JSONObject returnVaue = mongoDao.save(model, json);
		return returnVaue;
	}

	/**
	 * 
	 * update(更新某对象)
	 * 
	 * @param model
	 * @param json
	 * @param id
	 * @return
	 * @throws Exception
	 *             String
	 * @exception
	 * @since 1.0.0
	 */
	public JSONObject update(String model, String json, String id) throws ServiceException {
		return mongoDao.update(model, json, id);
	}

	/**
	 * 
	 * delete(删除某对象)
	 * 
	 * @param model
	 * @param id
	 * @return JSONObject
	 * @throws Exception
	 * @exception
	 * @since 1.0.0
	 */
	public JSONObject delete(String model, String id) throws ServiceException {
		return mongoDao.delete(model, id);
	}

	/**
	 * 按(多条)字段获取一条记录
	 * 
	 * @param String
	 *            [] field(s)
	 * @return
	 * 
	 *         public boolean findOneByFields(String ...fields) { return true; }
	 */

	/**
	 * 按字段查询一条记录(用于非严谨地注册)
	 * 
	 * @param String
	 *            value String collection String key
	 * @return
	 */

	public DBObject findOneByField(String collection, String key, String value) {
		return mongoDao.findOne(collection, key, value);
	}

}
