/**
 * 
 */
package net.sidland.apesay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;

/**
 * 订单服务
 * @author xxb
 *
 */
@Component
public class InterfaceService {

	protected static Logger logger = LoggerFactory.getLogger(InterfaceService.class);
	
	private static String model = "document";
	
	@Autowired
	private APIService apiService;
	
	@Autowired
	private FunctionsService functionsService;
	
	/**
	 * 获取订单
	 * @param objectId 订单id
	 * @return
	 * @throws Exception
	 */
	public JSONObject getById(String objectId) throws Exception {
		JSONObject order = apiService.get(Constant.model_interface, objectId);
		return order;
	}
	
	/**
	 * 
	 * list:
	 *
	 * @author sid
	 * @param order_no
	 * @return
	 * @throws Exception
	 */
	public JSONObject list(String userId,int skip,int size) throws Exception {
//		JSONObject query = new JSONObject();
//		query.put("userId", userId);
		JSONObject jsonObject = functionsService.find("{}", "{}", "{"+Constant.CREATED_AT+":-1}", model, skip, size);
//		JSONObject jsonObject = functionsService.find(query.toJSONString(), "{}", "{"+Constant.CREATED_AT+":-1}", Constant.model_interface, skip, size);
		JSONArray results = jsonObject.getJSONArray(Constant.RESULTS);
		if(results.size()>0){
			return results.getJSONObject(0);
		}
		return null;
	}

	/**
	 * 更新订单
	 * updateOrder:
	 *
	 * @author sid
	 * @param orderId
	 * @param update
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject updateOrder(String orderId, JSONObject update) throws ServiceException {
		update.remove("product");
		return apiService.update(Constant.model_interface, update.toJSONString(), orderId);
	}

	/**
	  * 新增订单
	  * @param question
	  * @return
	  * @throws Exception
	  */
	public JSONObject createOrder(JSONObject order) throws Exception {
		return apiService.save(Constant.model_interface, order.toJSONString());
	}
}
