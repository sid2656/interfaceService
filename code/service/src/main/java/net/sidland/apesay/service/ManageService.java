package net.sidland.apesay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;

import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;

@Component
public class ManageService {

	protected static Logger logger = LoggerFactory.getLogger(ManageService.class);
	
	private static final String MODEL="manager";
	@Autowired
	private APIService apiService;
	@Autowired
	private AuthorizationService authorizationService;
	@Autowired
	private FunctionsService functionsService;
	
	/**
	 * 
	 * login:(管理员用户登录).
	 *
	 * @author sid
	 * @param username
	 * @param password
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject login(JSONObject json) throws ServiceException{
		logger.info("login manager data:"+json.toString());
		JSONObject jsonObject = null;
		try {
			jsonObject = authorizationService.login(MODEL, json.toJSONString());
			if(jsonObject == null) {
				return null;
			}
			logger.info("login manager success:"+jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * 
	 * delete:(删除管理员).
	 *
	 * @author sid
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject delete(String id) throws ServiceException{
		return apiService.delete(MODEL, id);
	}
	/**
	 * 
	 * one:(根据id获取指定记录).
	 *
	 * @author sid
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject one(String id) throws ServiceException{
		return apiService.get(MODEL, id);
	}
	/**
	 * 
	 * update:(根据id更新对象).
	 *
	 * @author sid
	 * @param id
	 * @param object
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject update(String id,JSONObject object) throws ServiceException{
		return apiService.update(MODEL, object.toJSONString(), id);
	}

	/**
	 * 
	 * add:(增加管理员用户).
	 *
	 * @author sid
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public JSONObject add(JSONObject user) throws Exception{
		logger.info("add manager data:"+user.toString());
//		String p1 = user.getString(Constant.user_password);
//		String password = new Sha512Hash(p1, Constant.sha512_salt, 513).toBase64();
		String password = "";
		user.put(Constant.user_password, password);
		return apiService.save(MODEL, user.toJSONString());
	}
	
	/**
	 * 列表
	 * @param type 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public JSONArray list(String roleid, int numToSkip, int batchSize) throws Exception {
		JSONObject query = new JSONObject();
		query.put(Constant.user_roleid, roleid);
		JSONObject jsonObject = functionsService.find(query.toJSONString(), "{}", "{'"+Constant.UPDATED_AT+"':-1}", MODEL, numToSkip, batchSize);
		return jsonObject.getJSONArray(Constant.RESULTS);

	}
	
	/**
	 * 用户名是否存在
	 * @param username
	 * @return Boolean true存在 false不存在
	 * @throws ServiceException
	 */
	public Boolean usernameExists(String username) throws ServiceException{
		if(!DataTypeUtils.isNotEmpty(username)){
			return false;
		}
		DBObject dbusername = apiService.findOneByField(MODEL, Constant.user_username, username);
		if (dbusername != null) {
			return true;
		}
		return false;
	}
}

