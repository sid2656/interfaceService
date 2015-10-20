package net.sidland.apesay.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.xinge.Message;
import com.tencent.xinge.MessageIOS;
import com.tencent.xinge.XingeApp;

import net.sidland.apesay.cache.CacheService;
import net.sidland.apesay.dao.MongoDAO;
import net.sidland.apesay.domain.ResponseEntity;
import net.sidland.apesay.utils.APPUtils;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;
import net.sidland.apesay.utils.DateUtils;

/**
 *
 * <b>类名称：</b>UserMessageService<br/>
 * <b>类描述：</b>消息<br/>
 * <b>创建人：</b>xxb<br/>
 * <b>修改人：</b>xxb<br/>
 * <b>修改时间：</b>2015年4月10日 下午4:50:42<br/>
 * <b>修改备注：</b><br/>
 * @version 1.0.0<br/>
 *
 */
@Component
public class UserMessageService {
	
	protected static Logger logger = LoggerFactory.getLogger(UserMessageService.class);
	
	@Autowired
	private MongoDAO mongoDAO;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private APIService apiService;
	
	@Autowired
	private UserService userService;

 	 //action 动作 0:评论 1:回复 2:赞 3:关注
	public void sendMessage(ResponseEntity entity, HttpServletRequest request) throws Exception {
		String uri = request.getRequestURI();
		JSONObject message = new JSONObject();
		String userId = ServletRequestUtils.getStringParameter(request, "userId", null);
		
		if(entity==null|| !entity.getStatus().equals("10000")) return;
		
		int action = -1;
		String messageKey = "",messageCountKey = "";
		if(uri.startsWith("/api/service/v2/expcomment/save")) {//评论
			JSONObject entityJson = (JSONObject)entity.getResult();
			if(!DataTypeUtils.isNotEmpty(entityJson.getString(Constant.OBJECTID))) return;
			action = 0;
			String toUserId = ServletRequestUtils.getStringParameter(request, "toUserId", null);
			String comment = ServletRequestUtils.getStringParameter(request, "comment", "");
			if(DataTypeUtils.isNotEmpty(toUserId)){
				action = 1;
			}
			message.put("action", action);
			//用贴子ＩＤ，获取用户ＩＤ
			String expId = ServletRequestUtils.getStringParameter(request, "expId", "");
			JSONObject exp = apiService.get("experience", expId);//贴子
			JSONObject user = apiService.get(Constant.model_users, userId);//评论人
			
			message.put("userId", userId);
			message.put("userIcon", user.getString("iconUrl"));
			message.put("username", user.getString("username"));
			message.put("comment", comment);
			message.put("expId", expId);
			message.put("expUrl", exp.getString("imgUrl"));
			message.put("expContent", exp.getString("content"));
			message.put(Constant.CREATED_AT, entityJson.getString(Constant.CREATED_AT));
			if(userId.equals(exp.getString("userId"))||userId.equals(toUserId)) return;//评论自己的贴子或回复不发消息
			if(action == 0){
				messageKey = Constant.user_message_List_key + exp.getString("userId");
				messageCountKey = Constant.user_message_count_key + exp.getString("userId");
			}else{
				messageKey = Constant.user_message_List_key + toUserId;
				messageCountKey = Constant.user_message_count_key + exp.getString("userId");
			}
			
			logger.info(messageKey+":"+message.toString());
			cacheService.push(messageKey, message.toString());
			cacheService.incrBy(messageCountKey, 1L);
		}else if(uri.startsWith("/api/service/v2/exppraise/save")) {//赞
			JSONObject entityJson = (JSONObject)entity.getResult();
			if(!DataTypeUtils.isNotEmpty(entityJson.getString(Constant.OBJECTID))) return;
			action = 2;
			//用贴子ＩＤ，获取用户ＩＤ
			String expId = ServletRequestUtils.getStringParameter(request, "expId", "");
			JSONObject exp = apiService.get("experience", expId);//贴子ＩＤ
			JSONObject user = apiService.get(Constant.model_users, userId);//赞人
			
			message.put("userId", userId);
			message.put("userIcon", user.getString("iconUrl"));
			message.put("username", user.getString("username"));
			message.put("comment", "");
			message.put("expId", expId);
			message.put("expUrl", exp.getString("imgUrl"));
			message.put("expContent", exp.getString("content"));
			message.put(Constant.CREATED_AT, entityJson.getString(Constant.CREATED_AT));
			message.put("action", action);
			if(userId.equals(exp.getString("userId"))) return;//赞自己的贴子不发消息
			messageKey = Constant.user_message_List_key + exp.getString("userId");
			messageCountKey = Constant.user_message_count_key + exp.getString("userId");
			logger.info(messageKey+":"+message.toString());
			cacheService.push(messageKey, message.toString());
			cacheService.incrBy(messageCountKey, 1L);
		}else if(uri.startsWith("/api/service/v2/user/followee/add")) {//关注
			JSONArray entityArray = (JSONArray)entity.getResult();
			for (int i=0;i<entityArray.size();i++) {
				JSONObject entityJson = entityArray.getJSONObject(i);
				if(!DataTypeUtils.isNotEmpty(entityJson.getString(Constant.OBJECTID))) return;
				action = 3;
				String followee = ServletRequestUtils.getStringParameter(request, "followee", "");
				JSONObject user = apiService.get(Constant.model_users, userId);
				message.put("userId", userId);
				message.put("userIcon", user.getString("iconUrl"));
				message.put("username", user.getString("username"));
				message.put("comment", "");
				message.put("expId", "");
				message.put("expUrl", "");
				message.put("expContent", "");
				message.put(Constant.CREATED_AT, entityJson.getString(Constant.CREATED_AT));
				message.put("action", action);
				messageKey = Constant.user_message_List_key + followee;
				messageCountKey = Constant.user_message_count_key + followee;
				logger.info(messageKey+":"+message.toString());
				cacheService.push(messageKey, message.toString());
				cacheService.incrBy(messageCountKey, 1L);
			}
			
		}

	}
	
	
	public void friendDynamic(ResponseEntity entity, HttpServletRequest request) throws Exception{
		
	/**
	 * 朋友圈数据结构
	 * SortedSet存储
	 * Key:user_friend_dynamic_${userID}
	 * 消息结构
	 * {
	 * 		tid:'目标ID',
	 * 		uid:'发布用户ＩＤ'
	 * 		t:'0 二手，1 活动，2 建群，3 朋友圈，4 发布问答'
	 * 		ct:'时间戳，用来做sortedSet score'
	 * }
	 * 逻辑：
	 * 	生成消息
	 * 		1.获取当前发布消息人的被关注用户列表
	 * 		2.将发布 消息结构 放入每个用户朋友圈中
	 *  获取消息
	 *  	1.当前用户以上一次拉取数据的score为起点，获取新数据，score为0,从最新开始获取
	 * 		2.获取到消息数组后，查询相应类型结构数据及用户信息
	 *  
	 */
		String uri = request.getRequestURI();
		String userId = ServletRequestUtils.getStringParameter(request, "userId", null);
		String messageKey = String.valueOf(new Date().getTime())+String.valueOf((int)((Math.random()*9+1)*1000));
		int expire = 7*24*3600; //保存最近七天的消息
		int t = -1;//0 二手，1 活动，2 建群，3 朋友圈，4 发布问答 5 好友关注
		
		/**
		 * 我的通知列表 改为主动推送，不在服务器端存储
		 */
		int action = -1;//0 评论，1回复，2关注
		JSONObject userMessage = new JSONObject();

		if(uri.startsWith("/api/service/v3/secondHandGoods/comment/add")){
			//retry
			//retryCommentId
			//retryUserId
			int retry = ServletRequestUtils.getIntParameter(request, "retry", 0);
			String retryCommentId = ServletRequestUtils.getStringParameter(request, "retryCommentId", "");
			String retryUserId = ServletRequestUtils.getStringParameter(request, "retryUserId", "");
			String targetId = ServletRequestUtils.getStringParameter(request, "targetId","");
			//通过tid查找 该贴子是谁发的
			JSONObject secondHandGoods = apiService.get("SecondhandGoods", targetId);
			String toUserId = secondHandGoods.getString("userId");
			String mid = toUserId+"_"+messageKey;
			JSONObject entityJson = (JSONObject)entity.getResult();
			userMessage.put("t", 0); //二手
			userMessage.put("tid", targetId);
			userMessage.put("uid", userId);
			long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
			userMessage.put("ct", ct);
			userMessage.put("mid", mid);
			if(retry == 0){
				userMessage.put("action", 0);
				userMessage.put("commentId", entityJson.getString(Constant.OBJECTID));
				push(userMessage, new String[]{toUserId});
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
			}else if(retry == 1){
				userMessage.put("action", 1);
				userMessage.put("commentId", entityJson.getString(Constant.OBJECTID));
				userMessage.put("retryCommentId", retryCommentId);
				userMessage.put("retryUserId", retryUserId);
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+retryUserId, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
				cacheService.incrBy(Constant.user_message_count_key + retryUserId, 1L);
				push(userMessage, new String[]{toUserId,retryUserId});

			}

		}else if(uri.startsWith("/api/service/v3/secondHandGoods/follow/add")){
			String targetId = ServletRequestUtils.getStringParameter(request, "targetId","");
			//通过tid查找 该贴子是谁发的
			JSONObject secondHandGoods = apiService.get("SecondhandGoods", targetId);
			String toUserId = secondHandGoods.getString("userId");
			String mid = toUserId+"_"+messageKey;
			userMessage.put("t", 0); //二手
			userMessage.put("tid", targetId);
			userMessage.put("uid", userId);//发起人
			userMessage.put("action", 2);//关注
			JSONObject entityJson = (JSONObject)entity.getResult();
			long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
			userMessage.put("ct", ct);
			userMessage.put("mid", mid);
			cacheService.setex(mid, expire, userMessage.toJSONString());
			cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
			cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
			push(userMessage, new String[]{toUserId});

			
		}else if(uri.startsWith("/api/service/v3/userActivity/comment/add")){
			//retry
			//retryCommentId
			//retryUserId
			int retry = ServletRequestUtils.getIntParameter(request, "retry", 0);
			String retryCommentId = ServletRequestUtils.getStringParameter(request, "retryCommentId", "");
			String retryUserId = ServletRequestUtils.getStringParameter(request, "retryUserId", "");
			String targetId = ServletRequestUtils.getStringParameter(request, "targetId","");
			//通过tid查找 该贴子是谁发的
			JSONObject secondHandGoods = apiService.get("UserActivity", targetId);
			String toUserId = secondHandGoods.getString("userId");
			String mid = toUserId+"_"+messageKey;
			JSONObject entityJson = (JSONObject)entity.getResult();
			userMessage.put("t", 1); //活动
			userMessage.put("tid", targetId);
			userMessage.put("uid", userId);
			long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
			userMessage.put("ct", ct);
			userMessage.put("mid", mid);
			if(retry == 0){
				userMessage.put("action", 0);
				userMessage.put("commentId", entityJson.getString(Constant.OBJECTID));
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
				push(userMessage, new String[]{toUserId});

			}else if(retry == 1){
				userMessage.put("action", 1);
				userMessage.put("commentId", entityJson.getString(Constant.OBJECTID));
				userMessage.put("retryCommentId", retryCommentId);
				userMessage.put("retryUserId", retryUserId);
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+retryUserId, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
				cacheService.incrBy(Constant.user_message_count_key + retryUserId, 1L);
				push(userMessage, new String[]{toUserId,retryUserId});
			}
		}else if(uri.startsWith("/api/service/v3/userActivity/followee/add")){
			String targetId = ServletRequestUtils.getStringParameter(request, "targetId","");
			//通过tid查找 该贴子是谁发的
			JSONObject secondHandGoods = apiService.get("UserActivity", targetId);
			String toUserId = secondHandGoods.getString("userId");
			String mid = toUserId+"_"+messageKey;
			userMessage.put("t", 1); //活动
			userMessage.put("tid", targetId);
			userMessage.put("uid", userId);//发起人
			userMessage.put("action", 2);//关注
			JSONObject entityJson = (JSONObject)entity.getResult();
			long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
			userMessage.put("ct", ct);
			userMessage.put("mid", mid);
			cacheService.setex(mid, expire, userMessage.toJSONString());
			cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
			cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
			push(userMessage, new String[]{toUserId});
			
		}else if(uri.startsWith("/api/service/v3/experience/comment/add")){
			//retry
			//retryCommentId
			//retryUserId
			int retry = ServletRequestUtils.getIntParameter(request, "retry", 0);
			String retryCommentId = ServletRequestUtils.getStringParameter(request, "retryCommentId", "");
			String retryUserId = ServletRequestUtils.getStringParameter(request, "retryUserId", "");
			String targetId = ServletRequestUtils.getStringParameter(request, "expId","");
			//通过tid查找 该贴子是谁发的
			JSONObject secondHandGoods = apiService.get("experience", targetId);
			String toUserId = secondHandGoods.getString("userId");
			String mid = toUserId+"_"+messageKey;
			JSONObject entityJson = (JSONObject)entity.getResult();
			userMessage.put("t", 3); //朋友圈
			userMessage.put("tid", targetId);
			userMessage.put("uid", userId);
			long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
			userMessage.put("ct", ct);
			userMessage.put("mid", mid);
			if(retry == 0){
				userMessage.put("action", 0);
				userMessage.put("commentId", entityJson.getString(Constant.OBJECTID));
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
				push(userMessage, new String[]{toUserId});

			}else if(retry == 1){
				userMessage.put("action", 1);
				userMessage.put("commentId", entityJson.getString(Constant.OBJECTID));
				userMessage.put("retryCommentId", retryCommentId);
				userMessage.put("retryUserId", retryUserId);
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+retryUserId, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
				cacheService.incrBy(Constant.user_message_count_key + retryUserId, 1L);
				push(userMessage, new String[]{toUserId,retryUserId});

			}
		}else if(uri.startsWith("/api/service/v3/experience/follow/add")){
			String targetId = ServletRequestUtils.getStringParameter(request, "expId","");
			//通过tid查找 该贴子是谁发的
			JSONObject secondHandGoods = apiService.get("experience", targetId);
			String toUserId = secondHandGoods.getString("userId");
			String mid = toUserId+"_"+messageKey;
			userMessage.put("t", 3); //
			userMessage.put("tid", targetId);
			userMessage.put("uid", userId);//发起人
			userMessage.put("action", 2);//关注
			JSONObject entityJson = (JSONObject)entity.getResult();
			long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
			userMessage.put("ct", ct);
			userMessage.put("mid", mid);
			cacheService.setex(mid, expire, userMessage.toJSONString());
			cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
			cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
			push(userMessage, new String[]{toUserId});

		}else if(uri.startsWith("/api/service/v3/userQuestion/comment/add")){
			//retry
			//retryCommentId
			//retryUserId
			int retry = ServletRequestUtils.getIntParameter(request, "retry", 0);
			String retryCommentId = ServletRequestUtils.getStringParameter(request, "retryCommentId", "");
			String retryUserId = ServletRequestUtils.getStringParameter(request, "retryUserId", "");
			String targetId = ServletRequestUtils.getStringParameter(request, "targetId","");
			//通过tid查找 该贴子是谁发的
			JSONObject secondHandGoods = apiService.get("UserQuestion", targetId);
			String toUserId = secondHandGoods.getString("userId");
			String mid = toUserId+"_"+messageKey;
			JSONObject entityJson = (JSONObject)entity.getResult();
			userMessage.put("t", 4); //问答
			userMessage.put("tid", targetId);
			userMessage.put("uid", userId);
			long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
			userMessage.put("ct", ct);
			userMessage.put("mid", mid);
			if(retry == 0){
				userMessage.put("action", 0);
				userMessage.put("commentId", entityJson.getString(Constant.OBJECTID));
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
				push(userMessage, new String[]{toUserId});

			}else if(retry == 1){
				userMessage.put("action", 1);
				userMessage.put("commentId", entityJson.getString(Constant.OBJECTID));
				userMessage.put("retryCommentId", retryCommentId);
				userMessage.put("retryUserId", retryUserId);
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+toUserId, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+retryUserId, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + toUserId, 1L);
				cacheService.incrBy(Constant.user_message_count_key + retryUserId, 1L);
				push(userMessage, new String[]{toUserId,retryUserId});

			}
			
		}else if(uri.startsWith("/api/service/v3/userQuestion/follow/add")){//问答没有关注
			
		}else if(uri.startsWith("/api/service/v2/user/followee/add")){
			JSONArray entityArray = (JSONArray)entity.getResult();
			for (int i=0;i<entityArray.size();i++) {
				JSONObject entityJson = entityArray.getJSONObject(i);
				if(!DataTypeUtils.isNotEmpty(entityJson.getString(Constant.OBJECTID))) return;
				String followee = ServletRequestUtils.getStringParameter(request, "followee", "");
				String mid = followee+"_"+messageKey;
				if(followee.equalsIgnoreCase(userId)) break;// 关注自己不生成消息
				userMessage.put("t", 5); //用户好友关注
				userMessage.put("tid", entityJson.getString(Constant.OBJECTID));
				userMessage.put("uid", userId);//发起人
				userMessage.put("action", 2);//关注
				userMessage.put("mid", mid);
				long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
				userMessage.put("ct", ct);
				cacheService.setex(mid, expire, userMessage.toJSONString());
				cacheService.push(Constant.user_message_List_key+followee, userMessage.toJSONString());
				cacheService.incrBy(Constant.user_message_count_key + followee, 1L);
				push(userMessage, new String[]{followee});
			}
		}
		/**
		 * 1.uri判断t 和 action
		 * {
		 * 		t:
		 * 		uid:
		 * 		ct:
		 * 		action:
		 *  	content:{//评论或回复
		 *  				commentId: 当前评论ＩＤ
		 *  				retryUserId:  回复人评论人Id
		 *  				retryUsername: 回复评论人用户名
		 *  				retryCommentId: 回复评论 ＩＤ
		 *  				content:内容 评论、回复、文字
		 *  			}
		 *  			
		 *  {//
		 *  
		 *  }
		 * 	
		 * }
		 * 2.购造业务消息结构
		 * 3.根据t 和 action类型拼装内容content
		 */
		
		//朋友圈和个人中心
		t = -1;//重新设置t值
		if(uri.startsWith("/api/service/v3/secondHandGoods/add")){
			t = 0;
		}else if(uri.startsWith("/api/service/v3/userActivity/add")){
			t = 1;
		}else if(uri.startsWith("/api/service/v3/user/group/create")){
			t = 2;
		}else if(uri.startsWith("/api/service/v3/experience/add")){
			t = 3;
		}else if(uri.startsWith("/api/service/v3/userQuestion/add")){
			t = 4;
		}else if(uri.startsWith("/api/service/v3/userQuestion/add")){
			t = 5;
		}
		
		if(t == -1) return;//无指定朋友圈创建消息，返回
		JSONObject entityJson = (JSONObject)entity.getResult();
		JSONObject friendDynamic = new JSONObject();
		friendDynamic.put("tid", entityJson.getString(Constant.OBJECTID));
		friendDynamic.put("uid", userId);
		friendDynamic.put("t", t);
		long ct = DateUtils.SDF3.parse(entityJson.getString(Constant.CREATED_AT)).getTime();
		friendDynamic.put("ct", ct);
		
		//我的用户中心
		cacheService.push(Constant.user_center_dynamic_key+userId, friendDynamic.toJSONString());
		//自己发的朋友圈
		cacheService.zadd(Constant.user_friend_dynamic_key+userId, ct, friendDynamic.toJSONString());
		
	}
	
	@Autowired
	private DeviceService deviceService;
	
	/**
	 * 推送帐户消息到客户端
	 * @param message 业务消息结构
	 * @param userIDS 目标用户ＩＤ 数组
	 * @throws Exception
	 */
	private void push(JSONObject message, String[] userIDS) throws Exception {
		//TODO msgpush
//		ProductorUtils.pushMsgToSole(message.getString("mid"), message.toJSONString());
		
		/**
		 * 1.构建完整业务消息bsMessage
		 * 2.创建自定义消息结构map<String,Object> customer = new HashMap<String, Object>();
		 * 	customer.pu("m",bsMessage)
		 * 2.接用户deviceInstallation构建message或ios推送消息体，并使用相应android或ios sdk 进行消息推送
		 */
		JSONArray userDeviceInstallations = deviceService.getDeviceInstallationByUserIDS(userIDS);
		List<String> pushAccountListForIOS = new ArrayList<String>();//push android accountList
		List<String> pushAccountListForAndroid = new ArrayList<String>();//push ios accoutnList
		Map<String,Integer> existsMap = new HashMap<String, Integer>();
		for(int i=0; i<userDeviceInstallations.size(); i++){
			JSONObject userDeviceInstallation = userDeviceInstallations.getJSONObject(i);
			if(!existsMap.containsKey(userDeviceInstallation.getString("userId"))){
				existsMap.put(userDeviceInstallation.getString("userId"), 1);
			}else{
				continue;
			}
			
			if(userDeviceInstallation.getString("deviceType").equalsIgnoreCase(Constant.DeviceType_Android)){//android
				pushAccountListForAndroid.add(userDeviceInstallation.getString("userId"));
			}else if(userDeviceInstallation.getString("deviceType").equalsIgnoreCase(Constant.DeviceType_IOS)){//ios
				pushAccountListForIOS.add(userDeviceInstallation.getString("userId"));
			}
		}
		
		
		/**
		 * 构建message消息
		 * tid:'目标ID',
		 * uid:'发布用户ＩＤ'
		 * t:'0 二手，1 活动，2 建群，3 朋友圈，4 发布问答'
		 * ct:'时间戳，用来做sortedSet score'
		 */
		//JSONObject customerMessage = userService.userMessageMapper(message);
		
		String content = "你有新消息";//IOS限制为30字
		JSONObject fromUser = apiService.get(Constant.model_users, message.getString("uid"));
		//int action = -1;//0 评论，1回复，2关注
		if(message.getIntValue("action")==0){
			content = fromUser.getString(Constant.user_username)+":评论了您的内容";
		}else if(message.getIntValue("action")==1){
			content = fromUser.getString(Constant.user_username)+":回复了您的评论";
		}if(message.getIntValue("action")==2){
			content = fromUser.getString(Constant.user_username)+":关注了您";;
		}
		
		/**
		 * 推送android消息
		 */
		if(pushAccountListForAndroid.size()>0){
			//信鸽推送android透传消息
			XingeApp xinge = new XingeApp(APPUtils.xinge_push_accessId_android, APPUtils.xinge_push_secret_android);
			Message xgMessage = new Message();
			xgMessage.setExpireTime(86400);
			xgMessage.setTitle("妈妈生活圈通知");
			xgMessage.setContent(content);
			xgMessage.setType(Message.TYPE_NOTIFICATION);//穿透消息
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("m", message.getString("mid"));//自定义消息体
			xgMessage.setCustom(map);
			logger.warn("push android account message:"+xgMessage.toJson());
			org.json.JSONObject ret = xinge.createMultipush(xgMessage);
			if (ret.getInt("ret_code") != 0)
	            System.out.println(ret);
	        else {
	        	org.json.JSONObject result = new org.json.JSONObject();
	            result.append("all", xinge.pushAccountListMultiple(ret.getJSONObject("result").getInt("push_id"), pushAccountListForAndroid));
	        }
		}
		
		/**
		 * 推送ios消息
		 */
		if(pushAccountListForIOS.size()>0){
			/**
			 * 构建messageIOS消息
			 */
			XingeApp xinge = new XingeApp(APPUtils.xinge_push_accessId_ios, APPUtils.xinge_push_secret_ios);
			MessageIOS messageIOS = new MessageIOS();
			messageIOS.setExpireTime(86400);
			messageIOS.setAlert(content);
			messageIOS.setBadge(Integer.valueOf(String.valueOf(cacheService.queueLength(Constant.user_message_List_key+userIDS[0]))));
			messageIOS.setSound("beep.wav");
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("m", message.getString("mid"));//自定义消息体
			messageIOS.setCustom(map);
			org.json.JSONObject ret = xinge.createMultipush(messageIOS, XingeApp.IOSENV_DEV);
			logger.warn("push ios account message:"+messageIOS.toJson());
			if (ret.getInt("ret_code") != 0)
	            System.out.println(ret);
	        else {
	        	org.json.JSONObject result = new org.json.JSONObject();
	            result.append("all", xinge.pushAccountListMultiple(ret.getJSONObject("result").getInt("push_id"), pushAccountListForIOS));
	        }
		}
	}
	
}
