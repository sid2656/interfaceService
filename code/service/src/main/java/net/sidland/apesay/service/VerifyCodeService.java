package net.sidland.apesay.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.cache.CacheService;
import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.sms.SmsProducer;
import net.sidland.apesay.utils.VelocityUtils;

@Component
public class VerifyCodeService {

	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private SmsProducer smsProducer;
	
	@Autowired
	private SmsService smsService;
	
	
	/**
	 * 手机验证码校验
	 * @param mobile 手机号
	 * @param code 用户传入验证码
	 * @param production app产品名称
	 * @return true 成功 false失败
	 * @throws ServiceException
	 */
	public boolean verifyMobileCode(String mobile, String code, String production) throws ServiceException {
		String code1 = cacheService.get(production+"_"+mobile);
		if(StringUtils.isBlank(code)|| code.length()!=6){
			return false;
		}
		
		if(code.equals(code1)){
			cacheService.del(production+"_"+mobile);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 给指定手机发送验证码(6位随机数)
	 * @param mobile 手机
	 * @param expire 过期时间 单位：秒
	 * @param typeCode 短信模板编号
	 * @param production app名称
	 * @return 
	 * @throws ServiceException
	 */
	public void sendMobileCode(String mobile, int expire, String typeCode, String production) throws Exception {
		/**
		 * 1 生成6位数字验证码
		 * 2 放入缓存过期时间expire秒
		 * 3 短信模板，发送短信给mobile
		 */
		expire = expire<=0 ? 60 : expire;
		String code = String.valueOf((int)((Math.random()*9+1)*100000));
		cacheService.setex(production+"_"+mobile, expire, code);
		String template = smsService.getTemplate(typeCode);
		Map<String, Object> model = new HashMap<String, Object>();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", code);
		model.put("data", jsonObject);
		smsProducer.sendSms(mobile, VelocityUtils.render(template, model), typeCode);
	}
	
	
}
