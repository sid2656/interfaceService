package net.sidland.apesay.logs;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.utils.APPUtils;
import net.sidland.apesay.utils.Constant;

 /**
 *
 * <b>类名称：</b>APIRequestLog<br/>
 * <b>类描述：</b><br/>
 * <b>创建人：</b><br/>
 * <b>修改人：</b><br/>
 * <b>修改时间：</b>2014-9-5 上午10:36:28<br/>
 * <b>修改备注：</b><br/>
 * @version 1.0.0<br/>
 *
 */
public class APIRequestLog {
	
	protected static Logger logger = LoggerFactory.getLogger(APIRequestLog.class);
	
	public static void logs(HttpServletRequest req, HttpServletResponse res){
		try{
			String authorization  = req.getHeader(APPUtils.Authorization);
			String objectId=null,userName=null;
			String url = req.getRequestURL().toString();
			try{
				JSONObject jsonObject = JSONObject.parseObject(APPUtils.base64Decode(authorization, APPUtils.CHARSET));
				objectId = jsonObject.getString(Constant.OBJECTID);
				userName = jsonObject.getString(Constant.user_username);
			}catch(Exception e){
				
			}
			logger.info(url+"||"+objectId+"||"+userName);
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
