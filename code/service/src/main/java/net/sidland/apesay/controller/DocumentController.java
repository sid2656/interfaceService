package net.sidland.apesay.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.domain.ResponseEntity;
import net.sidland.apesay.service.DocumentService;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;
import net.sidland.apesay.utils.ServiceConfig;

@Controller
@RequestMapping("/api/service/doc")
public class DocumentController {
	
	protected static Logger logger = LoggerFactory.getLogger(DocumentController.class);
	@Autowired
	private DocumentService documentService;
	@RequestMapping(value = "/one/{id}")
	public @ResponseBody String one(@PathVariable String id,HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			
			responseEntity.setResult(documentService.one(id));
			//responseEntity.setResult(new JSONObject());
			responseEntity.setMsg(Constant.API_CALL_STATUS_SUCCESS_MSG);
			responseEntity.setStatus(Constant.API_CALL_STATUS_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			responseEntity.setMsg(e.getMessage());
			responseEntity.setStatus(Constant.API_CALL_STATUS_ERROR_EXCEPTION);
		}
		request.setAttribute(Constant.CLIENT_RESPONSE, responseEntity);
		return null;
	}
	@RequestMapping(value = "/list")
	public @ResponseBody String list(HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			
			responseEntity.setResult(documentService.list());
			//responseEntity.setResult(new JSONObject());
			responseEntity.setMsg(Constant.API_CALL_STATUS_SUCCESS_MSG);
			responseEntity.setStatus(Constant.API_CALL_STATUS_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			responseEntity.setMsg(e.getMessage());
			responseEntity.setStatus(Constant.API_CALL_STATUS_ERROR_EXCEPTION);
		}
		request.setAttribute(Constant.CLIENT_RESPONSE, responseEntity);
		return null;
	}

	@RequestMapping(value = "/add")
	public @ResponseBody String add(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			if(StringUtils.isBlank(body)){
				throw new Exception("传入的参数不能为空");
			}
			JSONObject newdoc=JSON.parseObject(body);
			System.out.println(newdoc.toJSONString());
			if(StringUtils.isBlank(newdoc.getString(Constant.OBJECTID))){
				responseEntity.setResult(documentService.save(newdoc));
				
				saveToMap(newdoc);
			}else{
				responseEntity.setResult(documentService.update(newdoc.getString(Constant.OBJECTID), newdoc));

				//删除原有数据
				JSONObject one = documentService.one(newdoc.getString(Constant.OBJECTID));
				InitDataController.delApi(one.getString("address"));
				saveToMap(newdoc);
			}
			
			//responseEntity.setResult(new JSONObject());
			responseEntity.setMsg(Constant.API_CALL_STATUS_SUCCESS_MSG);
			responseEntity.setStatus(Constant.API_CALL_STATUS_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			responseEntity.setMsg(e.getMessage());
			responseEntity.setStatus(Constant.API_CALL_STATUS_ERROR_EXCEPTION);
		}
		request.setAttribute(Constant.CLIENT_RESPONSE, responseEntity);
		return null;
	}
	/**
	 * 
	 * saveToMap:(根据是否发布状态来更新内存信息).
	 *
	 * @author sid
	 * @param newdoc
	 */
	private void saveToMap(JSONObject newdoc) {
		Boolean published = (Boolean)newdoc.get("published");

		//如果不开启过滤则都需要加入到列表中
		if (!ServiceConfig.publishFilter) {
			Boolean isPrivate = (Boolean)newdoc.get("private");
			InitDataController.saveApi(newdoc.getString("address"), (DataTypeUtils.isNotEmpty(isPrivate)&&isPrivate)?true:false);
			logger.debug(InitDataController.getValue(newdoc.getString("address"))+":"+newdoc.getString("address"));
		//若开启过滤，则只有状态为发布的才加入到当前的列表中
		}else if (DataTypeUtils.isNotEmpty(published)&&published) {
			//增加新数据
			Boolean isPrivate = (Boolean)newdoc.get("private");
			InitDataController.saveApi(newdoc.getString("address"), (DataTypeUtils.isNotEmpty(isPrivate)&&isPrivate)?true:false);
			logger.debug(InitDataController.getValue(newdoc.getString("address"))+":"+newdoc.getString("address"));
		}
	}

}
