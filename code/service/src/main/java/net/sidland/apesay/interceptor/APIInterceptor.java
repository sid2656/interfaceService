package net.sidland.apesay.interceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.controller.InitDataController;
import net.sidland.apesay.domain.RequestEntity;
import net.sidland.apesay.domain.ResponseEntity;
import net.sidland.apesay.logs.APIRequestLog;
import net.sidland.apesay.service.APIService;
import net.sidland.apesay.service.AuthorizationService;
import net.sidland.apesay.service.UserMessageService;
import net.sidland.apesay.utils.APPUtils;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;

 /**
  * 接口拦截器用来处理登录、会话认证、消息通知
  * ClassName: APIInterceptor 
  * date: 2015年10月17日 下午3:20:02 
  *
  * @author sid
  */
public class APIInterceptor implements HandlerInterceptor {

	protected static Logger logger = LoggerFactory.getLogger(APIInterceptor.class);
	
	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private APIService apiService;
	
	@Autowired
	private UserMessageService userMessageService;
	
	public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object arg2, Exception arg3) throws Exception {
		logger.warn("requestURI:"+req.getRequestURI());
	}

	public void postHandle(HttpServletRequest req, HttpServletResponse res, Object obj, ModelAndView mv) throws Exception {
		//TODO:这里处理执行成功后消息处理
		/**
		 * 1、拦截到URI,分析处理的实体,处理动作post,delete,put
		 * 2、组成id,model的消息
		 * 3、获取getAttribute(app_client_response),如果status成功，执行发送消息。
		 * 4、res.getWriter().print(app_client_response)
		 * 5、结束
		 */
		req.getParameter("");
		ResponseEntity responseEntity = (ResponseEntity)req.getAttribute(Constant.CLIENT_RESPONSE);
		String requestURI = req.getRequestURI();

		if (requestURI.startsWith("/api/babyrun/v3/pay/callback")) {
			printNative(res,responseEntity);
		}else if(requestURI.startsWith("/api/babyrun/")){
			//消息处理
			try{
				if(responseEntity.getStatus().equals("10000")){
					//userMessageService.sendMessage(responseEntity, req);
					userMessageService.friendDynamic(responseEntity, req);//朋友圈
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			print(res,responseEntity);
		}else{
			printKeegooService(res,responseEntity);
		}
	}

	
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object arg2) throws Exception {
		String remoteAddr = req.getRemoteAddr();
		MDC.put("ip", remoteAddr);
		String requestURI = req.getRequestURI();
		
		if(requestURI.startsWith("/api/babyrun/")){
			logger.info("call keegooBabyRunService:"+requestURI);

			/**
			 * 认证接口列表 进行auth认证
			 * 其他接口不进行认证
			 * 1 接口签名较验
			 * 2 认证接口登陆校验
			 * 
			 *  接口签名规则：sign=md5(userId+sessionToken+requestTime+secretKey)
			 * 				公共参数：userId可选，sessionToken可选，requestTime必填，secretKey不传，用来做签名
			 * 				注：签名串中间没有+ ， requestTime请求有效期20秒，公共secretKey:keegoo!@#
			 * 
			 */
//			String sign = ServletRequestUtils.getStringParameter(req, "sign", "");
			String userId = ServletRequestUtils.getStringParameter(req, "userId", "");
			String sessionToken = ServletRequestUtils.getStringParameter(req, "sessionToken", "");
			Long requestTime = ServletRequestUtils.getLongParameter(req, "requestTime", 0L);
			
			ResponseEntity responseEntity = new ResponseEntity();
//			long localTime = (new Date()).getTime();
//			if(( localTime- requestTime)>20*1000){
//				logger.warn("接口方问时间超时:requestTime="+requestTime+",localTime="+localTime);
//				print(res,responseEntity);
//				return false;
//			}
			
//			String str = userId+sessionToken+requestTime;
			/**
			 * 如果是公共接口，那么secretKey = keegoo!@#
			 * 否则取用户私有密钥
			 */
//			String localSign = "",secretKey = "";
			//是否私有接口
			boolean isPrivate = false;
			/**
			 * TODO:判断访问接口isPublic
			 */
//			HashMap<String, Boolean> m = APPUtils.getInterfaceMap();
			HashMap<String, Boolean> m = InitDataController.getApisMap();
			
			//判断是否存在当前的api路径
			if (m.containsKey(requestURI.trim())) {
				isPrivate = m.get(requestURI);
			}else{
				//如果访问接口在map中没有，则更新一下缓存看看数据库中是否存在
				InitDataController.reloadAuth();
				m = InitDataController.getApisMap();
				if (m.containsKey(requestURI.trim())) {
					isPrivate = m.get(requestURI);
				}else{
					//若依然不存在，则判断是否是带有参数的url
					Set<String> mKeySet = m.keySet();
					boolean isParamUrl = false;
					for (String mKey : mKeySet) {
						if(requestURI.startsWith(mKey)){
							isParamUrl=true;
							isPrivate = m.get(mKey);
						}
					}
					if(!isParamUrl){
						logger.warn("私有接口认证失败:requestURI="+requestURI+"，当前接口未开放或不存在");
						print(res,responseEntity);
						return false;
					}
				}
			}
			
			
			if(isPrivate){
				JSONObject user = apiService.get(Constant.model_users, userId);
				if(user==null){
					logger.warn("私有接口认证失败:requestURI="+requestURI+"，用户不存在:userId="+userId);
					print(res,responseEntity);
					return false;
				}
//				secretKey = user.getString(Constant.user_secretKey);
				
			}else{
//				secretKey = "keegoo!@#";
			}
			
			//生成签名
//			localSign = Md5Util.encode(str+secretKey);
//			if (StringUtils.isNotBlank(sign) && sign.equals(localSign)) {
//				return true;
//			}
//			logger.warn("接口签名失败:requestURI="+requestURI+",str="+str+",sign="+sign+",localSign="+localSign);
//			print(res,responseEntity);
			return true;
			
		}else{
			//keegooService auth
			APIRequestLog.logs(req, res);
			RequestEntity requestEntity = new RequestEntity(req);
			if(Constant.method_login.equals(requestEntity.getModel())&&requestEntity.getMethod().equalsIgnoreCase(req.getMethod())){
				logger.info("login......");
				printKeegooService(res,login(req, requestEntity));
				return false;
			}
			
			boolean valid = auth(req, requestEntity);//会话认证
			if(!valid){//认证失败处理
				ResponseEntity responseEntity = new ResponseEntity();
				responseEntity.setStatus("10010");
				responseEntity.setMsg("认证失败");
				printKeegooService(res,responseEntity);
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 
	 * auth(会话认证，通过走后端方法，不通过返回错误信息)
	 * @param req
	 * @return
	 * boolean
	 * @exception
	 * @since  1.0.0
	 */
	protected boolean auth(HttpServletRequest req, RequestEntity requestEntity){
		boolean valid = false;
		//会话认证
		String authorization =req.getHeader(APPUtils.Authorization);
		if(DataTypeUtils.isNotEmpty(authorization)){
			authorization = APPUtils.base64Decode(authorization, APPUtils.CHARSET);
			try {
				if(APPUtils.public_authData.equals(authorization)//这里需要有个公共接口列表，防止非法通过public_authData进行私有接口访问
						||
						//登录时不进行会话认证
						(Constant.method_login.equals(requestEntity.getModel())&&requestEntity.getMethod().equals(req.getMethod()))) {
					return true;
				}
				//会话认证（公共，私有）
				valid = authorizationService.auth(Constant.model_users, authorization);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			//认证信息为空		
		}
		return valid;
	}
	
	/**
	 * 
	 * login(如果是登录请求，在这里拦截做业务处理并返回消息)
	 * @param req
	 * @param requestEntity
	 * @return
	 * ResponseEntity
	 * @exception
	 * @since  1.0.0
	 */
	protected ResponseEntity login(HttpServletRequest req, RequestEntity requestEntity){

		ResponseEntity responseEntity = new ResponseEntity();
		responseEntity.setStatus("10010");
		responseEntity.setMsg("登录失败");
		//会话认证
		String authorization =req.getHeader(APPUtils.Authorization);
		if(DataTypeUtils.isNotEmpty(authorization)){
			authorization = APPUtils.base64Decode(authorization, APPUtils.CHARSET);
		}else{
			//认证信息为空
			return responseEntity;
		}
		try {
			String data = null;//authorizationService.login(Constant.model_users, authorization);
			if(DataTypeUtils.isNotEmpty(data)){
				responseEntity.setData(APPUtils.base64Encoder(data, APPUtils.CHARSET));
				responseEntity.setStatus("10000");
				responseEntity.setMsg("登录成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//这里需要返回给客户端
		return responseEntity;
	}
	
	/**
	 * 
	 * printNative
	 * @param res
	 * @param responseEntity
	 * void
	 * @exception
	 * @since  1.0.0
	 */
	protected void printNative(HttpServletResponse res, ResponseEntity responseEntity) {
		res.setCharacterEncoding(APPUtils.CHARSET);
		res.setHeader("Content-type", "text/xml");
		try {
			logger.info(JSON.toJSON(responseEntity).toString());
			res.getWriter().print(responseEntity.getResult().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * print(客户端输出)
	 * @param res
	 * @param responseEntity
	 * void
	 * @exception
	 * @since  1.0.0
	 */
	protected void print(HttpServletResponse res, ResponseEntity responseEntity) {
		res.setCharacterEncoding(APPUtils.CHARSET);
		res.setHeader("Content-type", "application/json;charset="+APPUtils.CHARSET);
		try {
			logger.info(JSON.toJSON(responseEntity).toString());
			res.getWriter().print(JSON.toJSONString(responseEntity.getResult(), Constant.serializerFeatures));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * print(客户端输出)
	 * @param res
	 * @param responseEntity
	 * void
	 * @exception
	 * @since  1.0.0
	 */
	protected void printKeegooService(HttpServletResponse res, ResponseEntity responseEntity) {
		res.setCharacterEncoding(APPUtils.CHARSET);
		res.setHeader("Content-type", "application/json;charset="+APPUtils.CHARSET);
		try {
			logger.info(JSON.toJSON(responseEntity).toString());
			res.getWriter().print(JSON.toJSONString(responseEntity, Constant.serializerFeatures));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected void apiLogs(HttpServletRequest req, HttpServletResponse res) {
		
	}
	
}
