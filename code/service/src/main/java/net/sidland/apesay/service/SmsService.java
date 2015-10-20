/**
 * 
 */
package net.sidland.apesay.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.sms.SmsProducer;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.VelocityUtils;

/**
 * @author xxb
 *
 */
@Component
public class SmsService {

	@Autowired
	private FunctionsService functionService;
	
	@Autowired
	private SmsProducer smsProducer;
	
	public String getTemplate(String typeCode) throws Exception {
		JSONObject jsonObject = functionService.find("{typeCode:'"+typeCode+"'}", "{}", "{}", Constant.model_smsTemplate, 0, 1);
		JSONArray jsonArray = jsonObject.getJSONArray(Constant.RESULTS);
		return jsonArray.getJSONObject(0).getString("template");
	}
	
	/**
	 * 发送短信
	 * @param mobile 手机号
	 * @throws Exception
	 */
	public void smsSend(JSONObject sms) throws Exception {
		String mobile = sms.getString("mobile");
		String typeCode = sms.getString("typeCode");
//		String production = sms.getString("production");
		String template = getTemplate(typeCode);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("data", sms.getJSONObject("data"));
		smsProducer.sendSms(mobile, VelocityUtils.render(template, model), typeCode);
	}
}
