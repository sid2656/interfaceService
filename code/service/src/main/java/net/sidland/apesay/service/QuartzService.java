/**
 * 
 */
package net.sidland.apesay.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.cache.CacheService;
import net.sidland.apesay.domain.OrderTradeState;
import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;
import net.sidland.apesay.utils.DateUtils;
import net.sidland.apesay.utils.ServiceConfig;

/**
 * 
 * ClassName: QuartzService 
 * date: 2015年9月29日 下午6:19:31 
 *
 * @author sid
 */
@Component
public class QuartzService {

	protected static Logger logger = LoggerFactory.getLogger(QuartzService.class);
	
	@Autowired
	private APIService apiService;
	
	@Autowired
	private FunctionsService functionsService;
	
	@Autowired
	private CacheService cacheService;

	@Autowired
	private MerchantProductService productService;

    //调用的方法
    public void updateStatus(){
		//需要做的事情
    	int overtime = ServiceConfig.orderOverTime;
    	Date date = DateUtils.getDateAddMinutes(new Date(), -overtime);
    	String queryDate = DateUtils.dateToString(date, "yyyy-MM-dd HH:mm:ss");
    	String dateQuery = "";
    	ServiceConfig.orderUpdateTime = cacheService.get(Constant.model_order+":orderUpdateTime");
    	//第一次获取30分钟前的所有数据；之后只需要获取时间间隔的数据
    	if(DataTypeUtils.isNotEmpty(ServiceConfig.orderUpdateTime)){
    		dateQuery = Constant.CREATED_AT+":{$lte:'"+queryDate+"',$gt:'"+ServiceConfig.orderUpdateTime+"'},";
    	}else{
    		dateQuery = Constant.CREATED_AT+":{$lte:'"+queryDate+"'},";
    	}
    	try {
    		ServiceConfig.orderUpdateTime = queryDate;
        	//获取未支付和支付中(主要是防止用户支付途中退出的垃圾订单)
        	String query = "{"+dateQuery + "status:{$in:["+OrderTradeState.ORDER_WZF.getVal()+","+OrderTradeState.ORDER_ZFZ.getVal()+"]}}";
			JSONObject find = functionsService.find(query, "{}", "{"+Constant.CREATED_AT+":-1}", Constant.model_order, 0, Integer.MAX_VALUE);
			JSONArray subAreaArray=find.getJSONArray(Constant.RESULTS);
			for(int i=0;i<subAreaArray.size();i++){
				JSONObject tempObject=subAreaArray.getJSONObject(i);
				tempObject.put("status", OrderTradeState.ORDER_JYCS.getVal());
				apiService.update(Constant.model_order, tempObject.toJSONString(), tempObject.getString(Constant.OBJECTID));
				int quantity = tempObject.getIntValue("quantity");
				String productId = String.valueOf(tempObject.get("productId"));
				productService.changeQuantity(quantity, productId);
				logger.info(tempObject.getString(Constant.CREATED_AT));
			}
        	cacheService.set(Constant.model_order+":orderUpdateTime", queryDate);
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	logger.info("---quartz-end---"+overtime+"间隔");
	}
}
