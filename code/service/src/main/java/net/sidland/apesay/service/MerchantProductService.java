/**
 * 
 */
package net.sidland.apesay.service;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;

/**
 * 单例更改库存量
 * ClassName: MerchantProductService 
 * date: 2015年9月28日 下午12:03:54 
 *
 * @author sid
 */
@Component
@Singleton
public class MerchantProductService {

	protected static Logger logger = LoggerFactory.getLogger(MerchantProductService.class);
	
	@Autowired
	private APIService apiService;

	/**
	 * 修改商品库存量
	 * 订单状态为取消可恢复
	 * 订单为已删除并状态为非支付成功可恢复
	 * changeQuantity:
	 *
	 * @author sid
	 * @param quantity
	 * @param productId
	 * @return
	 * @throws ServiceException
	 */
	public synchronized boolean changeQuantity(int quantity, String productId) throws ServiceException {
		try {
			JSONObject one = apiService.get(Constant.model_production, productId);//findOneByField(table, Constant.OBJECTID, productId);
			int count = Integer.parseInt((one.get("stock")==null?"0":one.get("stock").toString().trim()))+quantity;
			one.put("stock", count);
			apiService.update(Constant.model_production, JSONObject.toJSONString(one), (String)one.get(Constant.OBJECTID));
			
//			JSONArray list = mainPageProductService.listByProductId(productId);
//			for (int i=0,len=list.size();i<len;i++) {
//				JSONObject object = (JSONObject)list.get(i);
//				object.put("count", count);
//				apiService.update("MainpageProductBlock", JSONObject.toJSONString(object), (String)object.get(Constant.OBJECTID));
//			}
			
			return true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
