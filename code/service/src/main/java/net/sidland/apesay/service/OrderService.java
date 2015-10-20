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
import com.mongodb.DBObject;

import net.sidland.apesay.domain.OrderDeleteState;
import net.sidland.apesay.domain.OrderTradeState;
import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;

/**
 * 订单服务
 * @author xxb
 *
 */
@Component
public class OrderService {

	protected static Logger logger = LoggerFactory.getLogger(OrderService.class);
	
	@Autowired
	private APIService apiService;
	
	@Autowired
	private FunctionsService functionsService;

	@Autowired
	private MerchantProductService productService;
	
	/**
	 * 获取订单
	 * @param loginUserId 当前登陆用户id
	 * @param orderId 订单id
	 * @return
	 * @throws Exception
	 */
	public JSONObject get(String loginUserId, String orderId) throws Exception {
		JSONObject order = apiService.get(Constant.model_order, orderId);
		if(order!=null && order.getString("userId").equals(loginUserId)){
			fillOrder(order,true);
			return order;
		}
		return null;
	}

	/**
	 * 填充order的product和Merchant属性
	 * fillOrder:
	 *
	 * @author sid
	 * @param order
	 * @param isDetails 获取更详细信息
	 * @throws ServiceException
	 * @throws Exception
	 */
	private void fillOrder(JSONObject order,boolean isDetails) throws ServiceException, Exception {
//		String productId = order.getString("productId");
//		String merchantId = order.getString("merchantId");
//		JSONObject product = merchantService.getMerchantProductionDetails(productId);
//		JSONObject merchant = merchantService.get(merchantId);
//		if (isDetails) {
//			order.put("product", product);
//			order.put("merchant", merchant);
//			//获取消费码
//			DBObject one = apiService.findOneByField("MerchantTicket", "orderId", order.getString(Constant.OBJECTID));
//			if (DataTypeUtils.isNotEmpty(one)) {
//				order.put("ticketCode", one.get("ticketCode"));
//			}
//		}else{
//			if (DataTypeUtils.isNotEmpty(product)) {
//				product.remove(Constant.CREATED_AT);
//				product.remove(Constant.UPDATED_AT);
//			}
//			if (DataTypeUtils.isNotEmpty(merchant)) {
//				merchant.remove(Constant.CREATED_AT);
//				merchant.remove(Constant.UPDATED_AT);
//			}
//			order.put("product", product);
//			order.put("merchant", merchant);
//		}
	}
	
	/**
	 * 获取订单
	 * @param objectId 订单id
	 * @return
	 * @throws Exception
	 */
	public JSONObject getById(String objectId) throws Exception {
		JSONObject order = apiService.get(Constant.model_order, objectId);
		return order;
	}
	
	/**
	 * 获取订单
	 * @param order_no 订单号
	 * @return
	 * @throws Exception
	 */
	public JSONObject getByOrderNo(String order_no) throws Exception {
		JSONObject query = new JSONObject();
		query.put("order_no", order_no);
		JSONObject jsonObject = functionsService.find(query.toJSONString(), "{}", "{"+Constant.CREATED_AT+":-1}", Constant.model_order, 0, 1);
		JSONArray results = jsonObject.getJSONArray(Constant.RESULTS);
		if(results.size()>0){
			return results.getJSONObject(0);
		}
		return null;
	}
	
	/**
	 * 
	 * 取消订单-订单状态改为取消并差恢复库存，只有未支付订单可取消
	 * cancel:
	 * @param loginUserId 当前登陆用户id
	 * @param orderId 订单id
	 * @author sid
	 * @return
	 * @throws Exception
	 */
	public JSONObject cancel(String loginUserId, String orderId) throws Exception {
		JSONObject order = this.get(loginUserId, orderId);
		if(order!=null&&order.getIntValue("status")==OrderTradeState.ORDER_WZF.getVal()){
			JSONObject update = new JSONObject();
			update.put("id", orderId);
			update.put("status", OrderTradeState.ORDER_QXDD.getVal());
			JSONObject result =  updateOrder(orderId, update);
			//订单状态为：待支付时，恢复库存
			int quantity = order.getIntValue("quantity");
			String productId = String.valueOf(order.get("productId"));
			productService.changeQuantity(quantity, productId);
			return result;
		}else{
			logger.info("用户{}所选的订单{}，不存在或者订单状态为交易成功/订单取消",loginUserId,orderId);
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
		update.remove("merchant");
		update.remove("product");
		return apiService.update(Constant.model_order, update.toJSONString(), orderId);
	}

	/**
	 * 更新订单
	 * updateOrder:
	 *
	 * @author sid
	 * @param orderNo
	 * @param update
	 * @return
	 * @throws ServiceException
	 */
	public JSONObject updateOrderByNo(String orderNo, JSONObject update) throws ServiceException {
		DBObject order = apiService.findOneByField(Constant.model_order, "order_no", orderNo);
		if(order!=null&&order.get("status")!=null
				&&OrderTradeState.ORDER_ZFCG.getVal()!=Integer.valueOf(order.get("status").toString()).intValue()){
			String objectId = (String)order.get(Constant.OBJECTID);
			return apiService.update(Constant.model_order, update.toJSONString(), objectId);
		}
		return null;
	}
	
	/**
	 *  
	 * 删除订单-非过渡状态都可删除，如：支付中
	 * delete:
	 * @param loginUserId 当前登陆用户id
	 * @param orderId	订单id
	 * @author sid
	 * @return
	 * @throws Exception
	 */
	public JSONObject delete(String loginUserId, String orderId) throws Exception {
		JSONObject order = this.get(loginUserId, orderId);
		if(order!=null&&order.getIntValue("status")!=OrderTradeState.ORDER_ZFZ.getVal()){
			JSONObject update = new JSONObject();
			update.put("id", orderId);
			update.put("delete", OrderDeleteState.ORDER_SC.getVal());
			JSONObject result =  updateOrder(orderId, update);
			//订单状态为：待支付时，恢复库存
			if(order.getIntValue("status")==OrderTradeState.ORDER_WZF.getVal()){
				int quantity = order.getIntValue("quantity");
				String productId = String.valueOf(order.get("productId"));
				productService.changeQuantity(quantity, productId);
			}
			return result;
		}else{
			logger.info("用户{}所选的订单{}，不存在或者订单状态为支付中",loginUserId,orderId);
		}
		return null;
	}
	

	/**
	 * 更新订单状态
	 * @param orderId 订单ID
	 * @param status 状态
	 * @return
	 * @throws Exception
	 */
	public JSONObject updateOrderStatus(String id, OrderTradeState status) throws Exception {
		JSONObject order = apiService.get(Constant.model_order, id);
		logger.info("订单状态更新:order="+id+",status="+order.getIntValue("status")+">>"+status.getVal());
		JSONObject update = new JSONObject();
		update.put("status", status.getVal());
		return updateOrder(id, update);
	}
	
	 /**
	  * 新增订单
	  * @param question
	  * @return
	  * @throws Exception
	  */
	public JSONObject createOrder(JSONObject order) throws Exception {
		order.put("delete", OrderDeleteState.ORDER_ZC.getVal());
		int quantity = 0-order.getIntValue("quantity");
		String productId = String.valueOf(order.get("productId"));
		productService.changeQuantity(quantity, productId);
		return apiService.save(Constant.model_order, order.toJSONString());
	}
	
	/**
	 * 
	 * getOrdersByUserId:
	 * 根据用户id获取订单列表
	 *
	 * @author sid
	 * @param userId
	 * @param skip
	 * @param size
	 * @return
	 * @throws Exception
	 */
	public JSONArray getOrdersByUserId(String userId,int status,int skip, int size) throws Exception {
		JSONObject query = new JSONObject();
		query.put("userId", userId);
		if (status==OrderTradeState.ORDER_WZF.getVal()) {
			JSONArray array=new JSONArray();
			array.add(OrderTradeState.ORDER_JYCS.getVal());
			array.add(OrderTradeState.ORDER_QXDD.getVal());
			array.add(OrderTradeState.ORDER_WZF.getVal());
			array.add(OrderTradeState.ORDER_ZFSB.getVal());
			array.add(OrderTradeState.ORDER_ZFZ.getVal());
			JSONObject inObject=new JSONObject();
			inObject.put("$in", array);
			query.put("status", inObject);
		}else{
			query.put("status", OrderTradeState.ORDER_ZFCG.getVal());
		}
		query.put("delete", OrderDeleteState.ORDER_ZC.getVal());
		JSONObject jsonObject = functionsService.find(query.toJSONString(), "{}", "{"+Constant.CREATED_AT+":-1}", Constant.model_order, skip, size);//1升序,-1降序。
		JSONArray results = jsonObject.getJSONArray(Constant.RESULTS);
		JSONArray list=new JSONArray();
		for (Object object : results) {
			JSONObject order = (JSONObject)object;
			fillOrder(order,false);
			list.add(order);
		}
		return list;
	}

	
	/**
	 * 订单检查 - 价格数量检查
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public boolean orderCheck(JSONObject order) throws Exception{
//		JSONObject jsonObject = merchantService.getMerchantProductionDetails(order.getString("productId"));
//		if(!DataTypeUtils.isNotEmpty(jsonObject)){
//			throw new Exception("productId指定的商品不存在");
//		}else if(DataTypeUtils.isNotEmpty(jsonObject.getString("endTime"))
//				&&DateUtils.strToDate(jsonObject.getString("endTime")).getTime()<=DateUtils.getTodayMax().getTime()
//				&&DataTypeUtils.isNotEmpty(jsonObject.getString("startTime"))
//				&&DateUtils.strToDate(jsonObject.getString("startTime")).getTime()>=DateUtils.getTodayMin().getTime()){
//			logger.info("当前时间商品活动时间内，无法下单，id："+jsonObject.getString(Constant.OBJECTID));
////			throw new Exception("商品活动时间已过，无法下单");
//		}else if(DataTypeUtils.isNotEmpty(jsonObject.getInteger("saleStatus"))
//				&&jsonObject.getIntValue("saleStatus")!=SaleState.SALE_TG.getVal()){
//			logger.info("当前时间商品未通过审核或者已下架，无法下单，id："+jsonObject.getString(Constant.OBJECTID));
////			throw new Exception("商品活动时间已过，无法下单");
//		}else if(jsonObject.getInteger("type")==ProductType.TYPE_HD.getVal()&&!DataTypeUtils.isNotEmpty(order.getString("nickname"))){
//			logger.info("活动报名必须添加nickname，id："+jsonObject.getString(Constant.OBJECTID));
////			throw new Exception("活动报名必须添加nickname");
//		}else{
//			float pPrice = jsonObject.getFloatValue("currentPrice");
//			float pAmount = order.getInteger("quantity")*pPrice;
//			float pPayAmount = order.getInteger("quantity")*pPrice;
//			int pQuantity = jsonObject.getInteger("stock");
//			if (pQuantity>=order.getInteger("quantity")
//					&&Float.compare(OrderUtils.formatDoubel(order.getFloat("price"), 2), OrderUtils.formatDoubel(pPrice, 2))==0
//					&&Float.compare(OrderUtils.formatDoubel(order.getFloat("amount"), 2), OrderUtils.formatDoubel(pAmount, 2))==0
//					&&Float.compare(OrderUtils.formatDoubel(order.getFloat("payAmount"), 2), OrderUtils.formatDoubel(pPayAmount, 2))==0) {
//				return true;
//			}
//		}
		return false;
	}

	
	/**
	 * 订单支付检查 - 点击支付前价格检查
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public boolean payAmountCheck(JSONObject order) throws Exception{
//		JSONObject jsonObject = merchantService.getMerchantProductionDetails(order.getString("productId"));
//		if(!DataTypeUtils.isNotEmpty(jsonObject)){
//			throw new Exception("productId指定的商品不存在");
//		}
//		float pPrice = jsonObject.getFloatValue("currentPrice");
//		float pAmount = order.getInteger("quantity")*pPrice;
//		float pPayAmount = order.getInteger("quantity")*pPrice;
//		if (Float.compare(OrderUtils.formatDoubel(order.getFloat("price"), 2), OrderUtils.formatDoubel(pPrice, 2))==0
//				&&Float.compare(OrderUtils.formatDoubel(order.getFloat("amount"), 2), OrderUtils.formatDoubel(pAmount, 2))==0
//				&&Float.compare(OrderUtils.formatDoubel(order.getFloat("payAmount"), 2), OrderUtils.formatDoubel(pPayAmount, 2))==0) {
//			return true;
//		}
		return false;
	}
}
