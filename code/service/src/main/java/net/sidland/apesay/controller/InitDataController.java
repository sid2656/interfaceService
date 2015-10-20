package net.sidland.apesay.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.service.DocumentService;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;

/**
 * ClassName:InitDataController Reason: 用于初始化数据库 Date: 2015年8月27日 下午1:12:51
 * 
 * @author sid
 * @see
 */
@Controller
public class InitDataController implements ApplicationListener<ApplicationEvent> {

	protected static Logger logger = LoggerFactory.getLogger(InitDataController.class);

	@Autowired
	private DocumentService documentService;

	private static boolean isStart = false;

	private static InitDataController initData;

	private static HashMap<String, Boolean> apis = new HashMap<String, Boolean>();

	public void onApplicationEvent(ApplicationEvent event) {
		initData = this;
		if (!isStart) {
			isStart = true;
			initData.initData();
		}
	}

	public static HashMap<String, Boolean> getApisMap() {
		if (!DataTypeUtils.isNotEmpty(apis)) {
			reloadAuth();
		}
		return apis;
	}

	public static void reloadAuth() {
		initData.initData();
	}

	/**
	 * 
	 * getValue:
	 *
	 * @author sid
	 * @param key
	 * @return
	 */
	public static Boolean getValue(String key) {
		Boolean result = getApisMap().get(key);
		if (DataTypeUtils.isNotEmpty(result)) {
			return result;
		}
		reloadAuth();
		result = getApisMap().get(key);
		return result;
	}

	public static void saveApi(String path, Boolean isPrivate) {
		apis.put(path, isPrivate);
	}

	public static void delApi(String path) {
		apis.remove(path);
	}

	private void initData() {
		try {
			JSONObject results = documentService.listPublished();
			logger.debug(results.toJSONString());
			initAccessList(results);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * initAccessList:(根据查询结果初始化授权列表).
	 *
	 * @author sid
	 * @param results
	 */
	private void initAccessList(JSONObject results) {
		JSONArray accessListAll = results.getJSONArray(Constant.RESULTS);
		// 初始化APIurl为非授权
		apis.put("/api/babyrun/doc/list", false);
		apis.put("/api/babyrun/doc/add", false);
		apis.put("/api/babyrun/doc/one", false);
		for (Object object : accessListAll) {
			JSONObject jo = (JSONObject) object;
			Boolean isPrivate = (Boolean) jo.get("private");
			apis.put(jo.getString("address").trim(),
					(DataTypeUtils.isNotEmpty(isPrivate) && isPrivate == true) ? true : false);
			logger.debug(apis.get(jo.getString("address")) + ":" + jo.getString("address"));
		}
	}
}