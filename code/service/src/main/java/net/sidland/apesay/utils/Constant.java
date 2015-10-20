package net.sidland.apesay.utils;

import com.alibaba.fastjson.serializer.SerializerFeature;


public class Constant {
	
	/**
	 * 客户端输出
	 */
	public static final String CLIENT_RESPONSE = "app_client_response";	
	
	/******************************特殊模型****************************************/
	public static final String model_interface = "interface";
	public static final String model_users="users";
	public static final String model_order="order";
	public static final String model_production = "MerchantProduction";
	
	/**
	 * 安装设备表
	 */
	public static final String model_DeviceInstallation = "DeviceInstallation";
	
	/**
	 * 短信模板模型
	 */
	public static final String model_smsTemplate = "smsTemplate";
	
	/******************************特殊模型****************************************/
	
	
	/******************************所有模型都有字段****************************************/
	/**
	 * 创建时间
	 */
	public static final String CREATED_AT = "createdAt";
	
	/**
	 * 更新时间
	 */
	public static final String UPDATED_AT = "updatedAt";
	
	/**
	 * 主键key
	 */
	public static final String OBJECTID = "objectId";
	
	/******************************所有模型必有字段****************************************/

	/******************************用户users模型必有字段****************************************/
	
	public static final String user_sessionToken = "sessionToken";
	
	public static final String user_importFromParse = "importFromParse";
	
	public static final String user_secretKey = "secretKey";
	
	public static final String user_mobilePhoneNumber = "mobilePhoneNumber";
	
	public static final String user_mobilePhoneVerified = "mobilePhoneVerified";
	
	public static final String user_username = "username";
	
	public static final String user_password = "password";
	
	public static final String user_email = "email";
	
	public static final String user_site = "site";
	
	public static final String user_siteUserId = "siteUserId";
	
	public static final String user_roleid = "roleid";//指定用户身份：超级管理员、商户管理员等
	
	public static final String user_currentVersion = "currentVersion";//用户当前使用版本
	
	public static final String user_regVersion = "regVersion";//用户注册版本
	
	/******************************用户users模型必有字段****************************************/
	
	/******************************文件files模型必有字段****************************************/
	public static final String files_content = "content";
	public static final String files_path = "path";
	public static final String files_host = "host";
	public static final String files_filetype = "fileType";
	/******************************文件files模型必有字段****************************************/
	
	/******************************验证码模型必有字段****************************************/
	public static final String codes_mobile = "mobile";
	public static final String codes_code = "code";
	public static final String codes_due_time = "duetime";
	/******************************验证码模型必有字段****************************************/
	
	/******************************特定动作处理********************************************/
	
	/**
	 * 登录
	 * GET
	 */
	public static final String method_login = "login";
	
	public static final String sha512_salt = "keegoo!@#";
	
	/**
	 * 重置密码
	 * POST
	 */
	public static final String method_requestPasswordReset = "requestPasswordReset";
	
	/**
	 * 请求发送用户手机号码验证短信
	 * POST
	 */
	public static final String method_requestMobilePhoneVerify = "requestMobilePhoneVerify";
	
	/**
	 * 使用验证码验证用户手机号
	 * POST
	 */
	public static final String method_verifyMobilePhone  = "verifyMobilePhone";
	
	/**
	 * 请求验证用户邮箱
	 * POST
	 */
	public static final String method_requestEmailVerify = "requestEmailVerify";
	
	/**
	 * 更新密码, 要求输入旧密码
	 * PUT
	 */
	public static final String method_updatePassword = "updatePassword";
	
	/******************************特定动作处理********************************************/
	
	public static final SerializerFeature[] serializerFeatures = { 
		SerializerFeature.WriteMapNullValue, // 输出空置字段
        SerializerFeature.WriteNullListAsEmpty,//list字段如果为null，输出为[]，而不是null
        SerializerFeature.WriteNullNumberAsZero,// 数值字段如果为null，输出为0，而不是null
        SerializerFeature.WriteNullBooleanAsFalse,//Boolean字段如果为null，输出为false，而不是null
        SerializerFeature.WriteNullStringAsEmpty,// 字符类型字段如果为null，输出为""，而不是null
	};
	
	public static final String API_CALL_STATUS_SUCCESS = "10000";
	public static final String API_CALL_STATUS_SUCCESS_MSG = "调用成功";
	
	public static final String API_CALL_STATUS_ERROR_INVALID_ACCESS_TOKEN = "10001";
	public static final String API_CALL_STATUS_ERROR_INVALID_ACCESS_TOKEN_MSG = "access_token无效";

	public static final String API_CALL_STATUS_ERROR_INVALID_SIGN = "10002";
	public static final String API_CALL_STATUS_ERROR_INVALID_SIGN_MSG = "签名失败";

	public static final String API_CALL_STATUS_ERROR_EXCEPTION = "11000";
	public static final String API_CALL_STATUS_ERROR_EXCEPTION_MSG = "系统异常";
	
	/** 用户图片上传目录 */
	public static final String USER_UPLOAD_DIRECTORY = "userfiles";
	
	/**用户登录标识键*/
	public static final String User_loginIdentifying = "loginIdentifying";
	
	
	/**
	 * 品牌搜索字段
	 */
	public static final String BRAND_SEARCH_KEY="key";
	
	/**
	 * 产品名称 速印
	 */
	public static final String keegoo_babyprinting_app = "babyprinting";
	
	/**
	 * 产品名称 快跑
	 */
	public static final String keegoo_babyrun_app = "babyrun";
	
	public static final String user_message_List_key = "user_message_list_";
	
	public static final String user_message_count_key = "user_message_count_";
	
	public static final String user_friend_dynamic_key = "user_friend_dynamic_";
	
	public static final String user_center_dynamic_key = "user_center_dynamic_";
	/**
	 * 高级检索接口中的数据
	 */
	public static final String RESULTS="results";
	
	public static final String DeviceType_IOS = "ios";
	
	public static final String DeviceType_Android = "android";
	
	/**
	 * 支付平台-微信支付
	 */
	public static final String PAY_PLATFORM_WEIXIN = "weixin";
	
	/**
	 * 支付平台-支付宝
	 */
	public static final String PAY_PLATFORM_ALIPAY = "alipay";

}
