package net.sidland.apesay.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;

import net.sidland.apesay.dao.MongoDAO;
import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;
import net.sidland.apesay.utils.ServiceConfig;

/**
 * 
 * <b>类名称：</b>APIService<br/>
 * <b>类描述：</b><br/>
 * <b>创建人：</b><br/>
 * <b>修改人：</b><br/>
 * <b>修改时间：</b>2014-9-2 下午04:08:16<br/>
 * <b>修改备注：</b><br/>
 * 
 * @version 1.0.0<br/>
 * 
 */
@Component
public class FunctionsService {
	protected static Logger logger = LoggerFactory.getLogger(FunctionsService.class);
	@Autowired
	private MongoDAO mongoDao;

	/**
	 * 
	 * find(高级查询)
	 * @param query 查询条件
	 * @param fields 返回字段
	 * @param orderBy 排序条件
	 * @param model 查询模型
	 * @param numToSkip 跳过条数
	 * @param batchSize	返回条数
	 * @return
	 * String
	 * @exception
	 * @since  1.0.0
	 */
	public JSONObject find(String query, String fields, String orderBy, String model, int numToSkip, int batchSize) throws ServiceException{
		/**
		 * {
		 * 	"operation":"query",
		 *  "command":{
		 *  			"query":"$query",
		 *  			"fields":"$fields",
		 *  			"orderBy":"$orderBy",
		 *  			"model":"$model",
		 *  			"skip":"$skip",
		 *  			"size":"$size"	
		 *  			}
		 * }
		 * 此结构需建立bean实体转换
		 */
		
		numToSkip = Math.abs(numToSkip);
		batchSize = Math.abs(batchSize);
		if(batchSize>1000){
			batchSize = 1000;
		}
		logger.info("query:"+query+",fields:"+fields+",orderby:"+orderBy+",model:"+model+",numToSkip:"+numToSkip+",batchSize:"+batchSize);
		DBObject dbos = mongoDao.find(query, fields, orderBy, model, numToSkip, batchSize);
		
		if(dbos==null){
			return JSON.parseObject("{}");
		}
		return JSON.parseObject(dbos.toString());
	}

	
	public JSONObject functions(String functions) throws ServiceException{
		System.out.println(functions);
		JSONObject jo = JSONObject.parseObject(functions);
		
		//高级查询
		if(jo.getString("operation").equals("query")){
			jo = jo.getJSONObject("command");
			return this.find(jo.getString("query"), jo.getString("fields"), jo.getString("orderBy"), jo.getString("model"), Integer.parseInt(jo.getString("skip")), Integer.parseInt(jo.getString("size")));
		}else if(jo.getString("operation").equals("functions")){
			//执行函数查询
			/**
			 * {
			 * 	"operation":"functions",
			 *  "command":{
			 *  			"function":"$function",
			 *  			"args":"[1,2,3,4,5]",
			 *  			}
			 * }
			 */

			JSONObject jo2 = jo.getJSONObject("command");
			String function = jo2.getString("function");
			String argsObj = jo2.getString("argsObj");

			if(function.toLowerCase().startsWith("function")){
				//非法请求
				throw new ServiceException("非法请求");
			}
			//验证 function 和 args 是否含非法关键字 drop remove kill rename
			if(check(function)){
				//非法请求
				throw new ServiceException("function 中含非法关键字");
			}
			String returnValue = mongoDao.eval(function, argsObj);
			if(!DataTypeUtils.isNotEmpty(returnValue)){
				return JSON.parseObject("{}");
			}
			return JSON.parseObject(returnValue);
		}
		throw new ServiceException("非法请求");
	}
	/**
	 * 验证非法关键字 防js语句注入
	 * @param t
	 * @return
	 */
	private boolean check(String t){
		if(t != null){
			Pattern pattern = Pattern.compile(ServiceConfig.mongoDBCheck);  
	        Matcher mat= pattern.matcher(t.toLowerCase());  
	        return mat.find();
		}else{
			return false;
		}
	}
	public static void main(String[] args) {
		System.out.println(new FunctionsService().check("drfopakillfdsa"));
	}
	
	/**
	 * 
	 * findByNear(距离查询)
	 * @param query 查询条件
	 * @param model 查询模型
	 * @param numToSkip 跳过条数
	 * @param batchSize	返回条数
	 * @return
	 * String
	 * @exception
	 * @since  1.0.0
	 */
	public JSONObject findByNear(String query, String model, int numToSkip, int batchSize) throws Exception{
		
		numToSkip = Math.abs(numToSkip);
		batchSize = Math.abs(batchSize);
		if(batchSize>1000){
			batchSize = 1000;
		}
		JSONArray ja = mongoDao.aggregate(model, query, numToSkip, batchSize);
		JSONObject jo = new JSONObject();
		jo.put(Constant.RESULTS, ja);
		return jo;
	}
}
