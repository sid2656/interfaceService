package net.sidland.apesay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import net.sidland.apesay.dao.MongoDAO;
import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.APPUtils;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;

 /**
 *
 * <b>类名称：</b>AuthorizationService<br/>
 * <b>类描述：</b>认证服务<br/>
 * <b>创建人：</b><br/>
 * <b>修改人：</b><br/>
 * <b>修改时间：</b>2014-9-20 下午12:51:51<br/>
 * <b>修改备注：</b><br/>
 * @version 1.0.0<br/>
 *
 */
@Component
public class AuthorizationService {
	
	protected static Logger logger = LoggerFactory.getLogger(AuthorizationService.class);
	
	@Autowired
	private MongoDAO mongoDao;
	
	/**
	 * 
	 * auth(ApI调用认证)
	 * 
	 * @param model
	 * @param json {objectId:{userObjectId},sessionToken:{sessionToken}}
	 * @return
	 * @throws Exception
	 *             boolean
	 * @exception
	 * @since 1.0.0
	 */
	public boolean auth(String model, String json) throws ServiceException {
		JSONObject userObject = com.alibaba.fastjson.JSON.parseObject(json);
		String user = mongoDao.get(model, userObject.getString(Constant.OBJECTID));
		JSONObject dbUserObject = com.alibaba.fastjson.JSON.parseObject(user);
		logger.info("userObject:"+json);
		logger.info("dbUserObject:"+user);
		
		String u1_sessionToken = dbUserObject.getString(Constant.user_sessionToken);
		String u2_sessionToken = userObject.getString(Constant.user_sessionToken);
		if (u2_sessionToken.equals(u1_sessionToken)) {
			return true;
		}
		return false;
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
	public JSONObject login(String model, String json) throws Exception {
		JSONObject response = null;
		
		DBObject user = mongoDao.login(model, json);
		if (user != null) {
			DBObject update = new BasicDBObject();// 设置更新内容
			update.put(Constant.user_sessionToken, APPUtils.generateAccessToken());// 生成sessionToken
			update.put(Constant.user_secretKey, APPUtils.generateSecretKey((String) user.get(Constant.user_username)));// 用户分配密钥
			JSONObject loginUser = JSON.parseObject(json);
			//更新用户当前使用版本
			String currentVersion = loginUser.getString(Constant.user_currentVersion);
			String lastVersion = "";
			if(user.containsField(Constant.user_currentVersion)){
				lastVersion = user.get(Constant.user_currentVersion).toString();
			}
			if(DataTypeUtils.isNotEmpty(currentVersion) 
					&& !currentVersion.equalsIgnoreCase(lastVersion) //当前版本有并且有上一个版本
					|| !DataTypeUtils.isNotEmpty(lastVersion)&&DataTypeUtils.isNotEmpty(currentVersion)//之前没有版本信息，当前需要更新
					){
				update.put(Constant.user_currentVersion, loginUser.getString(Constant.user_currentVersion));
			}
			mongoDao.update(model, update.toString(), (String)user.get(Constant.OBJECTID));
			user.put(Constant.user_sessionToken, update.get(Constant.user_sessionToken));
			user.put(Constant.user_secretKey, update.get(Constant.user_secretKey));
			user.removeField("_id");
			user.removeField("password");
			response  = JSON.parseObject(user.toString());
		}
		return response;
	}
}
