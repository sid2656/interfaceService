package net.sidland.apesay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;

/**
 * 设备服务
 * ClassName: DeviceService 
 * date: 2015年10月17日 下午3:29:53 
 *
 * @author sid
 */
@Component
public class DeviceService {

	protected static Logger logger = LoggerFactory.getLogger(DeviceService.class);
	
	@Autowired
	private APIService apiService;
	
	@Autowired
	private FunctionsService functionsService;

	
	/**
	 * 注册设备
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public JSONObject register(JSONObject device) throws Exception {
        JSONObject result = null;
		JSONObject query = new JSONObject();
		query.put("token", device.getString("token"));
		
		JSONObject deviceByToken = functionsService.find(query.toString(), "{}", "{}", Constant.model_DeviceInstallation, 0, 1);
		if(deviceByToken.getIntValue("count")<=0){
			result = apiService.save(Constant.model_DeviceInstallation, device.toJSONString()); 
		}else{
			result = deviceByToken.getJSONArray(Constant.RESULTS).getJSONObject(0);
			if(DataTypeUtils.isNotEmpty(device.getString("userId"))){
				query.put("userId", device.getString("userId"));
				result = apiService.update(Constant.model_DeviceInstallation, query.toJSONString(), result.getString(Constant.OBJECTID));
			}
		}
		return result;

	}
	
	/**
	 * 更新
	 * @param device
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JSONObject update(JSONObject device,String id) throws Exception {
		return apiService.update(Constant.model_DeviceInstallation, device.toJSONString(), id);
	}
	
	/**
	 * 批量查询用户设备信息
	 * @param userIDS,最多一次支持查询1000个人，多于用户将被抛弃。按设备推送无需处理;按帐号推送需要去重，因为一帐号对多设备
	 * @return
	 * @throws Exception
	 */
	public JSONArray getDeviceInstallationByUserIDS(String[] userIDS) throws Exception {
		if(userIDS!=null && userIDS.length>0){
			JSONArray userIDArray = new JSONArray();
			int size = userIDS.length>1000 ? 1000 : userIDS.length;
			for(int i=0;i<size;i++){
				userIDArray.add(userIDS[i]);
			}
			JSONObject inQuery = new JSONObject();
			inQuery.put("$in", userIDArray);
			JSONObject query = new JSONObject();
			query.put("userId", inQuery);
			JSONObject deviceByToken = functionsService.find(query.toString(), "{}", "{}", Constant.model_DeviceInstallation, 0, userIDS.length);
			return deviceByToken.getJSONArray(Constant.RESULTS);
		}
		return null;
	}
}
