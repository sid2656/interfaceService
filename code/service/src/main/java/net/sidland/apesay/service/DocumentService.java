package net.sidland.apesay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.exception.ServiceException;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.ServiceConfig;

@Component
public class DocumentService {
	private static final String MODEL="document";
	@Autowired
	private APIService apiService;
	@Autowired
	private FunctionsService functionsService;
	public JSONObject save(JSONObject object) throws ServiceException{
		return apiService.save(MODEL, object.toJSONString());
	}
	public JSONObject update(String id,JSONObject object) throws ServiceException{
		return apiService.update(MODEL, object.toJSONString(), id);
	}
	public JSONObject list() throws ServiceException{
		return functionsService.find("{}", "{}", "{}", MODEL, 0, Integer.MAX_VALUE);
	}
	public JSONObject listPublished() throws ServiceException{
		if (ServiceConfig.publishFilter) {
			return functionsService.find("{published:true}", "{}", "{}", MODEL, 0, Integer.MAX_VALUE);
		}
		return functionsService.find("{}", "{}", "{}", MODEL, 0, Integer.MAX_VALUE);
	}
	public JSONObject one(String id) throws ServiceException{
		return apiService.get(MODEL, id);
	}
	public JSONObject oneBykeys(String query) throws ServiceException{
		return functionsService.find(query, "{}", "{}", MODEL, 0, 1);
	}
	public JSONObject last() throws ServiceException{
		return functionsService.find("{}", "{}", "{'"+Constant.UPDATED_AT+"':-1}", MODEL, 0, 1);
	}
}

