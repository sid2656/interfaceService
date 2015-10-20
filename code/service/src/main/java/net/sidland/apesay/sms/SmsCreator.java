package net.sidland.apesay.sms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.MessageCreator;

import com.alibaba.fastjson.JSONObject;

/**
 * http get 方式 发送短信的各项参数
 * 
 * @author dixingxing
 * @CreateDate 2011-4-11
 */
public class SmsCreator implements MessageCreator {
	private Sms sms;

	/**
	 * 不用转义
	 * 
	 * @param mobile
	 * @param content
	 * @param typeCode
	 */
	public SmsCreator(String mobile, String content, String typeCode) {
		this.sms = new Sms(mobile, content, typeCode);
	}

	@Override
	public Message createMessage(Session paramSession) throws JMSException {
		return paramSession.createTextMessage(this.toString());
	}

	/**
	 * http get 参数
	 */
	@Override
	public String toString() {
		return JSONObject.toJSONString(this.sms);
	}

}