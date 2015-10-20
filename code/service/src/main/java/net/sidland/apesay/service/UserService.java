package net.sidland.apesay.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.easemob.server.example.jersey.apidemo.EasemobIMUsers;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBObject;

import net.sidland.apesay.cache.CacheService;
import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;
import net.sidland.apesay.utils.DateUtils;

/**
 * @author xxb
 *
 */
@Component
public class UserService {

	protected static Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private APIService apiService;
	
	@Autowired
	private FunctionsService functionsService;
	
	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private VerifyCodeService verifyCodeService;
	
	public JSONObject register(JSONObject user) throws Exception{
		logger.info("register data:"+user.toString());
		String reqmobilePhoneNumber = user.getString(Constant.user_mobilePhoneNumber);
		String requsername = user.getString(Constant.user_username);
		if("babyrun".equals(user.getString(Constant.user_site))){
			logger.info(reqmobilePhoneNumber);
			if (!DataTypeUtils.isNotEmpty(reqmobilePhoneNumber) || mobilePhoneNumberExists(reqmobilePhoneNumber)) return null;
			//if (StringUtils.isBlank(requsername) || usernameExists(requsername)) return null;
		}else{

		}
		String p1 = user.getString(Constant.user_password);
//		String password = new Sha512Hash(p1, Constant.sha512_salt, 513).toBase64();
		String password = "";
		user.put(Constant.user_password, password);
		user.put("easemob", 0);
		
		JSONObject dbUser = apiService.save(Constant.model_users, user.toJSONString());
		
		if(syncEasemobUser(dbUser.getString(Constant.OBJECTID), user.getString(Constant.user_username), p1)){//注册环信用户
			apiService.update(Constant.model_users, "{easemob:1}", dbUser.getString(Constant.OBJECTID));
		}
		JSONObject login = new JSONObject();
		if(!"babyrun".equals(user.getString(Constant.user_site))){
			login.put(Constant.User_loginIdentifying, requsername);
		}else{
			login.put(Constant.User_loginIdentifying, user.getString(Constant.user_mobilePhoneNumber));
		}
		
		login.put(Constant.user_password, p1);
		login.put(Constant.user_site, user.getString("site"));
		JSONObject loginUser = login(login);
		
		return loginUser;
	}
	
	/**
	 * 注册环信用户
	 * @param userId
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public boolean syncEasemobUser(String userId,String username,String password) throws Exception {
        ObjectNode datanode = JsonNodeFactory.instance.objectNode();
        datanode.put("username",userId);
        datanode.put("password", password);
        datanode.put("nickname", username);
        ObjectNode createNewIMUserSingleNode = EasemobIMUsers.createNewIMUserSingle(datanode);
        if (null != createNewIMUserSingleNode) {
            logger.info("注册环信IM用户[单个]: " + createNewIMUserSingleNode.toString());
            if(createNewIMUserSingleNode.get("error")!=null){
            	logger.error("同步环信用户失败："+createNewIMUserSingleNode.get("error_description"));
            	return false;
            }
            return true;
        }
        return false;
	}
	
	public JSONObject userAccountBind(String userId, String site, JSONObject data) throws Exception {
		JSONObject user = this.getUser(userId);
		JSONObject authData = user.getJSONObject("authData");
		if(authData==null) authData = new JSONObject();
		authData.put(site, data);
		JSONObject updateObject = new JSONObject();
		updateObject.put("authData", authData);
		return apiService.update(Constant.model_users, updateObject.toJSONString(), userId);
	}
	
	public JSONObject userAccountUnbind(String userId, String site) throws Exception {
		JSONObject user = this.getUser(userId);
		JSONObject authData = user.getJSONObject("authData");
		if(authData==null) return null;
		authData.remove(site);
		JSONObject updateObject = new JSONObject();
		updateObject.put("authData", authData);
		return apiService.update(Constant.model_users, updateObject.toJSONString(), userId);
	}
	/**
	 * 用户名是否存在
	 * @param username
	 * @return Boolean true存在 false不存在
	 * @throws ServiceException
	 */
	public Boolean usernameExists(String username) throws ServiceException{
		if(StringUtils.isBlank(username)){
			return false;
		}
		DBObject dbusername = apiService.findOneByField(Constant.model_users, "username", username);
		if (dbusername != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * 用户手机号是否存在
	 * @param mobile
	 * @return Boolean true存在 false不存在
	 * @throws ServiceException
	 */
	public boolean mobilePhoneNumberExists(String mobile) throws ServiceException{
		if(DataTypeUtils.isNotEmpty(mobile)){
			return false;
		}
		DBObject dbusername = apiService.findOneByField(Constant.model_users, Constant.user_mobilePhoneNumber, mobile);
		if (dbusername != null) {
			return true;
		}
		return false;
	}
	/**
	 * 修改用户信息
	 * @param json
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject modify(JSONObject json) throws ServiceException{
		return apiService.update(Constant.model_users, json.toJSONString(), json.getString(Constant.OBJECTID));
	}
	
	/**
	 * 用户详情
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject getUser(String userId,String viewUserId) throws ServiceException{
		JSONObject user = apiService.get(Constant.model_users, viewUserId);
		if (user!=null && !user.isEmpty()){
			user.remove(Constant.user_password);
			user.remove(Constant.user_sessionToken);
			user.remove(Constant.user_secretKey);
			//user.put("userMerchantCount", this.getMerchantCount(userId));
			//user.put("userBrandCount", this.getBrandCount(userId));
		}else{
			user = new JSONObject();
		}
		return user;
	}
	/**
	 * 用户详情
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject getUser( String viewUserId) throws ServiceException{
		return getUser(null, viewUserId);
	}

	/**
	 * 用户基本信息
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public JSONArray getUsernameAndIcon(String[] userIds) throws ServiceException{
		JSONArray ja = new JSONArray();
		for (String userId : userIds) {
			JSONObject jsonObject = apiService.get(Constant.model_users, userId);
			JSONObject user = new JSONObject();
			if (jsonObject!=null && !jsonObject.isEmpty()){
				user.put("username", jsonObject.getString("username"));
				user.put("iconUrl", jsonObject.getString("iconUrl"));
				user.put(Constant.OBJECTID, jsonObject.getString(Constant.OBJECTID));
				user.put("stage", jsonObject.getIntValue("stage"));
				user.put("babyBirthday", jsonObject.getString("babyBirthday"));
				user.put("babyGender", jsonObject.getIntValue("babyGender"));
			}else{
				jsonObject = new JSONObject();
			}
			ja.add(user);
		}
		
		return ja;
	}
	
	/**
	 * 用户密码修改
	 * @param userId 用户ＩＤ
	 * @param newPassword 新密码
	 * @return json {objectId//修改成功对象ＩＤ,updatedAt//修改时间}
	 * @throws ServiceException
	 */
	public JSONObject modifyPassword(String userId,String newPassword) throws ServiceException {
		return apiService.update(Constant.model_users, "{"+Constant.user_password+":'"+newPassword+"',"+Constant.user_secretKey+":'',"+Constant.user_sessionToken+":''}", userId);
	}
	
	/**
	 * 修改密码，需要登陆及旧密码
	 * @param userId 用户ＩＤ
	 * @param sessionToken 会话
	 * @param oldPassword 旧密码
	 * @param newPassword 新密码
	 * @return json {objectId//修改成功对象ＩＤ,updatedAt//修改时间}
	 * @throws ServiceException
	 */
	public JSONObject modifyPassword(String userId, String sessionToken, String oldPassword, String newPassword) throws ServiceException {
		boolean isLogin = authorizationService.auth(Constant.model_users, "{"+Constant.OBJECTID+":'"+userId+"',"+Constant.user_sessionToken+":'"+sessionToken+"'}");
		if(isLogin){
			JSONObject user = apiService.get(Constant.model_users, userId);
			String salt = user.getString("salt");
			if(StringUtils.isBlank(salt)){
				salt = Constant.sha512_salt;
			}
			oldPassword = new Sha512Hash(oldPassword, salt, 513).toBase64();
			
			if(user!=null && user.getString(Constant.user_password).equals(oldPassword) && StringUtils.isNotBlank(oldPassword)){
				
				try {
						 ObjectNode datanode = JsonNodeFactory.instance.objectNode();
					        datanode.put("newpassword",newPassword);
						ObjectNode node=EasemobIMUsers.modifyIMUserPasswordWithAdminToken(userId, datanode);
						logger.info(node.toString());
				} catch (Exception e) {
					return null;
				}
				newPassword = new Sha512Hash(newPassword, salt, 513).toBase64();
				return modifyPassword(userId, newPassword);
			}
			
		}
		return null;
	}
	
	/**
	 * 登陆
	 * @param json
	 * @return 用户基本信息
	 * @throws Exception
	 */
	public JSONObject login(JSONObject json) throws Exception {
		JSONObject jsonObject = authorizationService.login(Constant.model_users, json.toJSONString());
		if(jsonObject == null) {
			return null;
		}
		JSONObject user = this.getUser(jsonObject.getString(Constant.OBJECTID));
		if(user.getIntValue("easemob") == 0){
			if(this.syncEasemobUser(user.getString(Constant.OBJECTID), user.getString(Constant.user_username), json.getString(Constant.user_password))){
				apiService.update(Constant.model_users, "{easemob:1}", user.getString(Constant.OBJECTID));
			}
		}
		user.put(Constant.user_secretKey, jsonObject.get(Constant.user_secretKey));
		user.put(Constant.user_sessionToken, jsonObject.get(Constant.user_sessionToken));
		return user;
	}
	
	
	/**
	 * 
	 * followCancel(取消关注)
	 * @param userId 用户ID
	 * @param follow 取消关注用户ID
	 * @return
	 * @throws Exception
	 * JSONObject
	 * @exception
	 * @since  1.0.0
	 */
	public JSONObject followCancel(String userId, String follow) throws Exception {
		JSONObject jsonObject = functionsService.find("{userId:'"+userId+"',followee:'"+follow+"'}", "{}", "{"+Constant.CREATED_AT+":-1}", "followee", 0, 1);
		
		JSONObject returnValue = apiService.delete("followee", jsonObject.getJSONArray(Constant.RESULTS).getJSONObject(0).getString(Constant.OBJECTID));
		jsonObject = functionsService.find("{userId:'"+follow+"',follower:'"+userId+"'}", "{}", "{"+Constant.CREATED_AT+":-1}", "follower", 0, 1);
		apiService.delete("follower", jsonObject.getJSONArray(Constant.RESULTS).getJSONObject(0).getString(Constant.OBJECTID));
		return returnValue;
	}
	
	public JSONArray getUserMessage(String userId, int skip, int size) throws Exception {
		String messageKey = Constant.user_message_List_key+userId;
		String messageCountKey = Constant.user_message_count_key+userId;
		List<String> list = cacheService.lrange(messageKey, skip, skip+size);
		/**
		 * comment
{
	t: 类型 //0 二手，1 活动，2 建群，3 朋友圈，4 发布问答 5 好友关注
	tid: 业务ＩＤ
	uid: 发起用户ＩＤ
	action: 动作类型
	commentId:
	retryCommentId:
	retryUserId:
 }

{
		t:类型
		target:{业务数据}
		user:{发起人信息}
		action: 动作类型
		comment:{评论数据}
		retryComment:{被评论数据}
		retryUser:{被评论用户数据}
 }

		 */
		JSONArray ja = new JSONArray();
		for (String string : list) {
			JSONObject m = JSON.parseObject(string);
			JSONObject target = friendDynamicMapper(m);
			if(target != null){
				m.put("target", target);
				m.put("user", getUserBase(new String[]{m.getString("uid")}).get(0));
				String commentModel = "";
				int t = m.getIntValue("t");
				int action = m.getIntValue("action");
				if(action==0 || action == 1){//评论
					if(t == 0) {//二手
						commentModel = "SecondhandGoodsComment";
					}else if(t == 1){//活动
						commentModel = "UserActivityComment";
					}else if(t == 2){//建群
						//commentModel = "UserChatGroup";
					}else if(t == 3){//朋友圈
						commentModel = "expcomment";
					}else if(t == 4){//发布回答
						commentModel = "UserQuestionAnwser";
					}
					JSONObject comment = apiService.get(commentModel, m.getString("commentId"));
					if(comment!=null){
						if(action == 1){
							String retryUserId = comment.getString("retryUserId");
							if(StringUtils.isNotBlank(retryUserId)){
								comment.put("retryUsername", this.getUserBase(new String[]{retryUserId}).getJSONObject(0).getString(Constant.user_username));
							}
						}
						m.put("comment", comment);
					}
				}
				
				ja.add(m);
			}
		}
			
		//清空消息列表
		cacheService.del(messageKey);
		//清空未读数量
		cacheService.del(messageCountKey);
		return ja;
	}
	
	public JSONObject messageDetail(String m) throws Exception {
		String message = cacheService.get(m);
		if(StringUtils.isNotBlank(message)){
			//cacheService.del(m);
			return userMessageMapper(JSON.parseObject(message));
		}
		return null;
	}
	public JSONObject userMessageMapper(JSONObject m) throws Exception {
		JSONObject target = friendDynamicMapper(m);
		if(target != null){
			m.put("target", target);
			m.put("user", getUsernameAndIcon(new String[]{m.getString("uid")}).get(0));
			String commentModel = "";
			int t = m.getIntValue("t");
			int action = m.getIntValue("action");
			if(action==0 || action == 1){//评论
				if(t == 0) {//二手
					commentModel = "SecondhandGoodsComment";
				}else if(t == 1){//活动
					commentModel = "UserActivityComment";
				}else if(t == 2){//建群
					//commentModel = "UserChatGroup";
				}else if(t == 3){//朋友圈
					commentModel = "expcomment";
				}else if(t == 4){//发布回答
					commentModel = "UserQuestionAnwser";
				}
				JSONObject comment = apiService.get(commentModel, m.getString("commentId"));
				if(comment!=null){
					if(action == 1){
						String retryUserId = comment.getString("retryUserId");
						if(StringUtils.isNotBlank(retryUserId)){
							comment.put("retryUsername", this.getUserBase(new String[]{retryUserId}).getJSONObject(0).getString(Constant.user_username));
						}
					}
					m.put("comment", comment);
				}
			}
		}
		return m;
	}
	
	public int getUserMessageUnreadCount(String userId) throws Exception {
		String count = cacheService.get(Constant.user_message_count_key+userId);
		if(StringUtils.isNotBlank(count)){
			return Integer.valueOf(count);
		}
		return 0;
	}
	
	public JSONArray userCenterDynamic(String userId, String viewUserId, int skip, int size) throws Exception {
		List<String> list = cacheService.lrange(Constant.user_center_dynamic_key+viewUserId, skip, skip+size);
		JSONArray results = new JSONArray();
		int newSize = 0;
		if(list==null || list.size()==0 || list.size()<size){
			newSize = list == null ? 0:list.size();
		}
		if(list==null||list.size()==0) list = new ArrayList<String>();
		
		if(newSize<size){
			size = size-newSize;
		}
		String lastExpCreatedAt = null;
		for (String string : list) {
			//0 二手，1 活动，2 建群，3 朋友圈，4 发布问答
			try{
				JSONObject jo = JSON.parseObject(string);
				if(jo.getIntValue("t")==3){
					lastExpCreatedAt = DateUtils.SDF3.format(new Date(jo.getLong("ct")));
					//break;
				}
			}catch(Exception e){
				logger.warn("userCenterDynamic record deleted:"+string+",err:"+e.getMessage());
			}

		}
		if(size > 0){//处理旧数据
			JSONObject query = new JSONObject();
			query.put("userId", viewUserId);
			if(StringUtils.isNotBlank(lastExpCreatedAt)){
				JSONObject lt = new JSONObject();
				lt.put("$lt", lastExpCreatedAt);
				query.put(Constant.CREATED_AT, lt);
			}
			JSONObject result = functionsService.find(query.toJSONString(), "{objectId:1,"+Constant.CREATED_AT+":1}", "{"+Constant.CREATED_AT+":-1}", "experience", skip, size);
			if(result.getIntValue("count")>0){
				
				JSONArray ja = result.getJSONArray(Constant.RESULTS);
				for(int i=0 ;i<ja.size();i++){
					JSONObject row = new JSONObject();
					row.put("t", 3);
					row.put("tid", ja.getJSONObject(i).getString(Constant.OBJECTID));
					row.put("uid", viewUserId);
					row.put("ct", DateUtils.SDF3.parse(ja.getJSONObject(i).getString(Constant.CREATED_AT)).getTime());
					list.add(row.toJSONString());
				}
			}
		}
		for (String string : list) {
			//0 二手，1 活动，2 建群，3 朋友圈，4 发布问答
			try{
				JSONObject jo = JSON.parseObject(string);
				JSONObject result = friendDynamicMapper(jo);
				if(result!=null){
					jo.put("result", result);
					results.add(jo);
				}
			}catch(Exception e){
				logger.warn("userCenterDynamic record deleted:"+string);
			}

		}
		
		
		return results;
	}
	
	/**
	 * 批量获取第三方用户信息
	 * @param userId
	 * @param String[] siteUserIds
	 * @return
	 * @throws Exception
	 */
	public JSONArray getSiteUser(String userId,String site, String[] siteUserIds) throws Exception {
		JSONArray jsonArray = new JSONArray();
		for (String siteUserId : siteUserIds) {
			String key = "authData.qq.openid";
			if(site.equalsIgnoreCase("weibo")){
				key = "authData.weibo.uid";
			}else if(site.equalsIgnoreCase("weixin")){
				key = "authData.weixin.openid";
			}
			DBObject user = apiService.findOneByField(Constant.model_users, key, siteUserId);
			JSONObject jo = new JSONObject();
			if(user==null){
				
			}else{
				String viewUserId = (String)user.get(Constant.OBJECTID);
				jo.put("userId", viewUserId);
				jo.put("username", (String)user.get(Constant.user_username));
				jo.put("userIcon", (String)user.get("iconUrl"));
			}
			jo.put("siteUserId", siteUserId);
			jsonArray.add(jo);
			
		}
		return jsonArray;
	}
	
	/**
	 * 批量获取手机号注册用户信息
	 * @param userId
	 * @param String[] mobiles
	 * @return
	 * @throws Exception
	 */
	public JSONArray getMobileContact(String userId,String[] mobiles) throws Exception {
		JSONArray jsonArray = new JSONArray();
		for (String mobile : mobiles) {
			DBObject user = apiService.findOneByField(Constant.model_users, Constant.user_mobilePhoneNumber, mobile);
			JSONObject jo = new JSONObject();
			if(user==null){
				
			}else{
				String viewUserId = (String)user.get(Constant.OBJECTID);
				jo.put("userId", viewUserId);
				jo.put("username", (String)user.get(Constant.user_username));
				jo.put("userIcon", (String)user.get("iconUrl"));
			}
			jo.put("mobile", mobile);
			jsonArray.add(jo);
			
		}
		return jsonArray;
	}
	
	/**
	 * 发送手机验证码
	 * @param mobile
	 * @throws Exception
	 */
	public void sendMobleVerifyCode(String mobile) throws Exception {
		verifyCodeService.sendMobileCode(mobile, 120, "105", Constant.keegoo_babyrun_app);
	}
	
	/**
	 * 手机验证码较验
	 * @param mobile
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public boolean verifyCode(String mobile, String code) throws Exception {
		boolean b = verifyCodeService.verifyMobileCode(mobile, code, Constant.keegoo_babyrun_app);
		return b;
	}
	
	/**
	 * 绑定手机号
	 * @param mobile 要绑定手机
	 * @param code 手机验证码
	 * @return
	 * @throws Exception
	 */
	public JSONObject userMobileBind(String userId, String mobile, String code) throws Exception {
		if(this.verifyCode(mobile, code)){
			//绑定该手机号
			if(!this.mobilePhoneNumberExists(mobile)){
				return apiService.update(Constant.model_users, "{mobilePhoneNumber:'"+mobile+"'}", userId);
			}
		}
		return null;
	}
	
	public JSONObject resetPwd(String mobile, String code,String pwd) throws Exception {
		if(this.verifyCode(mobile, code)&&DataTypeUtils.isNotEmpty(pwd)){
			DBObject user = apiService.findOneByField(Constant.model_users, "mobilePhoneNumber", mobile);
			if(user!=null){
				String salt = user.get("salt") == null ? null : (String)user.get("salt");
				String userId = user.get(Constant.OBJECTID) == null ? null : (String)user.get(Constant.OBJECTID);
				if(StringUtils.isBlank(salt)){
					salt = Constant.sha512_salt;
				}
				pwd = new Sha512Hash(pwd, salt, 513).toBase64();
				return modifyPassword(userId, pwd);
			}

		}
		return null;
	}

	private JSONObject friendDynamicMapper(JSONObject jo) throws Exception{
		int t = jo.getIntValue("t");
		String uid = jo.getString("uid");
		String tid = jo.getString("tid");
		JSONObject user = this.getUsernameAndIcon(new String[]{uid}).getJSONObject(0);
		JSONObject result = new JSONObject();
		if(t == 0) {//二手
			result = apiService.get("SecondhandGoods", tid);
		}else if(t == 1){//活动
			result = apiService.get("UserActivity", tid);
		}else if(t == 2){//建群
			result = apiService.get("UserChatGroup", tid);
		}else if(t == 3){//朋友圈
			result = apiService.get("experience", tid);
		}else if(t == 4){//发布回答
			result = apiService.get("UserQuestion", tid);
		}
		if(result != null){
			result.put("user", user);
		}
		return result;
	}

	/**
	 * 用户基本信息
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public JSONArray getUserBase(String[] userIds) throws ServiceException{
		JSONArray ja = new JSONArray();
		for (String userId : userIds) {
			JSONObject jsonObject = apiService.get(Constant.model_users, userId);
			if (jsonObject!=null && !jsonObject.isEmpty()){
				jsonObject.remove(Constant.user_password);
				jsonObject.remove(Constant.user_sessionToken);
				jsonObject.remove(Constant.user_secretKey);
				jsonObject.remove("salt");
				jsonObject.remove("mobilePhoneNumber");
				jsonObject.remove("authData");
			}else{
				jsonObject = new JSONObject();
			}
			ja.add(jsonObject);
		}
		
		return ja;
	}
}
