/**
 * 
 */
package net.sidland.apesay.utils;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.alibaba.fastjson.JSONObject;

/**
 * @author xxb
 *
 */
public class VelocityUtils {

	static {
		try{
			Velocity.setProperty("directive.foreach.counter.name", "c");
			Velocity.init();
		} catch(Exception e) {
			throw new RuntimeException("Exception occurs while initialize the velocity", e);
		}
	}
	
	/**
	 * 渲染模版内容
	 * @param template 模板
	 * @param model 变量
	 * @return String
	 */
	public static String render(String template, Map<String, ?> model) {
		try{
			VelocityContext velocityContext = new VelocityContext(model);
			StringWriter  result = new StringWriter();
			Velocity.evaluate(velocityContext, result, "", template);
			return result.toString();
		} catch(Exception e) {
			throw new RuntimeException("Parse template failed.", e);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String template = "$!{codeObj.code}（找回登录密码验证码），请勿向任何人泄露。【奶瓶速印】";
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code","887667");
		map.put("codeObj", jsonObject);
		System.out.println(VelocityUtils.render(template, map));

	}

}
