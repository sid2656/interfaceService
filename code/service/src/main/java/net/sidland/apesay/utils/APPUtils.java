package net.sidland.apesay.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class APPUtils {

	private static Logger logger = Logger.getLogger(APPUtils.class);

	public final static String SIGN = "sign";

	public final static String CHARSET = "UTF-8";
	
	public final static String public_authData = "123456";
	
	public final static String xinge_push_secret_ios = "64e1ddc30e60f9ee46fa3877feda188f";
	
	public final static long xinge_push_accessId_ios = 2200142063L;
	
	public final static String xinge_push_secret_android = "2db154db8dc303f3f957c9a7270699b7";
	
	public final static long xinge_push_accessId_android = 2100142062L;
	
	/**
	 * HTTP Header
	 * APP-Authorization APP用户认证头信息
	 */
	public final static String Authorization = "APP-Authorization";
	
	/**
	 * HTTP Header
	 * APP-functions APP自定义高级查询头信息
	 * <br/>提供原生MongoDB查询命令
	 */
	public final static String FUNCTIONS = "APP-Functions";
	/**
	 * 
	 * verifySign(验证api签名)
	 * 
	 * @param request
	 * @param secretKey
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	public static boolean verifySign(ServletRequest request, String secretKey) {
		Map<String, Object> requestMap = request.getParameterMap();
		String sign = request.getParameter(SIGN);

		String localSign = APPUtils.md5Signature(requestMap, secretKey);
		logger.info("secretKey|sign|localSign=" + secretKey + "|" + sign + "|" + localSign);
		if (DataTypeUtils.isNotEmpty(sign) && sign.equals(localSign)) {
			return true;
		}
		return false;
	}

	/**
	 * 新的md5签名，结尾放secret。
	 * 
	 * @param secretKey
	 *            分配给用户的SECRET
	 */
	public static String md5Signature(Map<String, Object> params, String secretKey) {
		String result = null;
		StringBuffer orgin = getBeforeSign(params, new StringBuffer(""));
		logger.debug("orgin=" + orgin);
		if (orgin == null)
			return result;
		orgin.append(secretKey);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			result = byte2hex(md.digest(orgin.toString().getBytes("utf-8")));
		} catch (Exception e) {
			throw new java.lang.RuntimeException("sign error !");
		}
		return result;
	}

	/**
	 * 添加参数的封装方法
	 */

	private static StringBuffer getBeforeSign(Map<String, Object> params, StringBuffer orgin) {

		try {
			if (params == null)
				return null;
			Map<String, String> treeMap = new TreeMap<String, String>();
			// 对KEY进行升序排序
			treeMap.putAll(getParameterMap(params));
			Iterator<String> iter = treeMap.keySet().iterator();
			while (iter.hasNext()) {
				String name = (String) iter.next();
				if (!SIGN.equals(name)) {// 排除sign参数
					orgin.append(name).append("=").append(treeMap.get(name)).append("&");
				}
			}
			return new StringBuffer(orgin.toString().substring(0, orgin.toString().length() - 1));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 二进制转字符串
	 */
	private static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer();
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs.append("0").append(stmp);
			else
				hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}

	/**
	 * 创建请求业务参数
	 * 
	 * @param map
	 * @return
	 */
	public static String createBusinessParams(Map<String, Object> map) {
		if (map == null)
			return null;
		String params = "";
		Set<String> keySet = map.keySet();
		for (String key : keySet) {
			Object object = (Object) map.get(key);
			String value = "";
			if (object instanceof String[]) {
				value = ((String[]) object)[0];// 兼容 request.getParameterMap()方法
			} else {
				value = (String) object;
			}
			params += key + "=" + value + "&";
		}
		params = params.substring(0, params.length() - 1);
		return params;
	}

	/**
	 * 业务参数Map对象
	 * 
	 * @param s
	 * @return
	 */
	public static HashMap<String, String> getParameterMap(Map<String, Object> map) {
		HashMap<String, String> parameterMap = new HashMap<String, String>();
		if (map == null)
			return null;
		String params = "";
		Set<String> keySet = map.keySet();
		for (String key : keySet) {
			Object object = (Object) map.get(key);
			String value = "";
			if (object instanceof String[]) {
				value = ((String[]) object)[0];// 兼容 request.getParameterMap()方法
			} else {
				value = (String) object;
			}
			parameterMap.put(key, value);
		}
		return parameterMap;
	}

	/**
	 * 
	 * generateAccessToken(生成访问token)
	 * 
	 * @return
	 * @throws Exception
	 *             String
	 * @exception
	 * @since 1.0.0
	 */
	public static String generateAccessToken() throws Exception {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		return uuid;
	}

	/**
	 * 
	 * generateSecretKey(生成用户密钥SecretKey)
	 * 
	 * @return
	 * @throws Exception
	 *             String
	 * @exception
	 * @since 1.0.0
	 */
	public static String generateSecretKey(String userName) throws Exception {
		char[] c = new char[] { '9', 'a', 'b', 'c', '0', '1', '2', '3', '4', '5', '6', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 'x', 't', 'u', 'v', 'w', 's', 'y', 'z', '7', '8', 'd', 'e', 'f', 'g', 'h', 'i', 'j' };
		Random random = new Random();
		int secretLength = 0;
		if (!DataTypeUtils.isNotEmpty(userName)) {
			secretLength = random.nextInt(5);
		} else {
			secretLength = userName.length();
		}
		secretLength = secretLength + random.nextInt(6);
		String secretKey = "";
		for (int i = 0; i < secretLength; i++) {
			secretKey += String.valueOf(c[random.nextInt(c.length)]);

		}
		return secretKey;
	}

	public static String base64Encoder(String s, String charset) {
		try {
			if(!DataTypeUtils.isNotEmpty(charset)) charset = APPUtils.CHARSET;
			return new BASE64Encoder().encode(s.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * 
	 * @param s
	 * @return
	 */
	public static String base64Decode(String s, String charset) {
		if (s == null)
			return null;
		try {
			s = new String(new BASE64Decoder().decodeBuffer(s), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return s;
	}

	/**
	 * 
	 * getDistancesSQL(返回范围在radius半径内的用户(id,user_name,user_icon,distance(
	 * 与当前用户距离米，小数点2位))
	 * 
	 * @param lat
	 *            纬度
	 * @param lng
	 *            经度
	 * @param radius
	 *            半径（米）
	 * @return String
	 * @exception
	 * @since 1.0.0
	 */
	public static String getDistancesSQL(double lat, double lng, int radius) {
		String distanceFormula = "(6371*acos(cos(radians(" + lat + "))*cos(radians(lat))*cos(radians(lng)-radians(" + lng + "))+sin(radians(" + lat + "))*sin(radians(lat))))";
		String sql = "SELECT id, user_name, user_icon, round(" + distanceFormula + ",2) AS distance FROM t_user HAVING distance < " + radius + " ORDER BY distance";
		return sql;
	}

	/**
	 * 
	 * getIdList(切割“,”分隔数字串，返回List<Long>对象)
	 * 
	 * @param str
	 * @return List<Long>
	 * @exception
	 * @since 1.0.0
	 */
	public static List<Long> getIdList(String str) {
		if (!DataTypeUtils.isNotEmpty(str))
			return null;
		List<Long> idList = new ArrayList<Long>();
		String[] str1 = str.split(",");
		for (String string : str1) {
			idList.add(Long.valueOf(string));
		}
		return idList;
	}

	public static String getIdListString(List<Long> list){
		if(list==null || list.size()==0) return null;
		String ids = "";
		for (Long id : list) {
			ids = ids+","+id;
		}
		return ids.substring(1);
	}
	
	/**
	 * 
	 * getRequestBody(获取requestBody体)
	 * @param request
	 * @return
	 * String
	 * @exception
	 * @since  1.0.0
	 */
	public static String getRequestBody(HttpServletRequest request){
		try{
			byte[] bytes = new byte[1024 * 1024];  
	        InputStream is = request.getInputStream();  

	        int nRead = 1;  
	        int nTotalRead = 0;  
	        while (nRead > 0) {  
	            nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);  
	            if (nRead > 0)  
	                nTotalRead = nTotalRead + nRead;  
	        }  
	        return new String(bytes, 0, nTotalRead, "utf-8");  
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * main(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
	 * 
	 * @param args
	 *            void
	 * @exception
	 * @since 1.0.0
	 */
	public static void main(String[] args) {
		try {
			System.out.println(APPUtils.base64Decode("WyB7ICJfaWQiIDogeyAiJG9pZCIgOiAiNTQwOWY5ZDVlNGIwNjYzYmIyMjIwMjE4In0gLCAidXNlcm5hbWUiIDogIui/nuWutuWNjiIgLCAicGFzc3dvcmQiIDogIjEyMzQ1NiIgLCAib2JqZWN0SWQiIDogIjU0MDlmOWQ1ZTRiMDY2M2JiMjIyMDIxOCIgLCAiY3JlYXRlZEF0IiA6ICIyMDE0LTA5LTA2IDAxOjU4OjQ1IiAsICJ1cGRhdGVkQXQiIDogIjIwMTQtMDktMDYgMDE6NTg6NDUifSAsIHsgIl9pZCIgOiB7ICIkb2lkIiA6ICI1NDA4MjA0NDVhOWMxZjJhMDg4NGRmZGMifSAsICJwYXNzd29yZCIgOiAiMTIzNDU2NzgiICwgInVzZXJuYW1lIiA6ICLlrp3otJ3lhL8tMiIgLCAib2JqZWN0SWQiIDogIjU0MDgyMDQ0NWE5YzFmMmEwODg0ZGZkYyIgLCAiY3JlYXRlZEF0IiA6ICIyMDE0LTA5LTA0IDE2OjE4OjEyIiAsICJ1cGRhdGVkQXQiIDogIjIwMTQtMDktMDQgMTY6MTg6MTIifSAsIHsgIl9pZCIgOiB7ICIkb2lkIiA6ICI1NDA4MjMxYTVhOWMxZjJhMDg4NGRmZGQifSAsICJwYXNzd29yZCIgOiAiMTIzNDU2NzgiICwgInVzZXJuYW1lIiA6ICLlrp3otJ3lhL8tMiIgLCAib2JqZWN0SWQiIDogIjU0MDgyMzFhNWE5YzFmMmEwODg0ZGZkZCIgLCAiY3JlYXRlZEF0IiA6ICIyMDE0LTA5LTA0IDE2OjMwOjE4IiAsICJ1cGRhdGVkQXQiIDogIjIwMTQtMDktMDQgMTc6Mjk6MzMifSAsIHsgIl9pZCIgOiB7ICIkb2lkIiA6ICI1NDA4MjVhOTVhOWMxZjJhMDg4NGRmZGUifSAsICJwYXNzd29yZCIgOiAiMTIzNDU2NzgiICwgInVzZXJuYW1lIiA6ICLlrp3otJ3lhL8tMiIgLCAib2JqZWN0SWQiIDogIjU0MDgyNWE5NWE5YzFmMmEwODg0ZGZkZSIgLCAiY3JlYXRlZEF0IiA6ICIyMDE0LTA5LTA0IDE2OjQxOjEzIiAsICJ1cGRhdGVkQXQiIDogIjIwMTQtMDktMDQgMTY6NDE6MTMifV0=", APPUtils.CHARSET));
		} catch (Exception e) {
		}
	}
	private static HashMap<String, Boolean> m = null;
}
