package net.sidland.apesay.sms;

import javax.jms.Destination;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * 发送短信 jms producer
 * @author dixingxing
 * @CreateDate 2011-5-3
 */
@Component
public class SmsProducer extends Thread {

	private static Logger logger = Logger.getLogger(SmsProducer.class);
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination destination;

	/**
	 * 
	 * @param mobile
	 *            手机号码
	 * @param content
	 *            短信内容
	 * @param typeCode
	 */
	public void sendSms(String mobile, String content, String typeCode) {
		try{
			SmsCreator mc = new SmsCreator(mobile, content, typeCode);
			jmsTemplate.send(destination, mc);
			logger.warn("已经给手机["+mobile+"]发送短信至消息服务器：" + mc);
		}catch(Exception e){
			logger.error("消息服务器异常，errMsg:"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	
}
