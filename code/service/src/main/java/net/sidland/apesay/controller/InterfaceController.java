package net.sidland.apesay.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import net.sidland.apesay.domain.ResponseEntity;
import net.sidland.apesay.service.InterfaceService;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;

/**
  * 
  * ClassName: InterfaceController 
  * date: 2015年9月22日 下午3:53:23 
  *
  * @author sid
  */
@RestController
@RequestMapping(value = "/api/service/v1/interface")
public class InterfaceController {
	
	protected static Logger logger = LoggerFactory.getLogger(InterfaceController.class);
	
	@Autowired
	private InterfaceService service;
	
	/**
	 * 获取首页推荐商品
	 * @param body
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * String
	 * @exception
	 * @since  1.0.0
	 */
	@RequestMapping(value = "/list")
	public @ResponseBody String categoryList(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			String cityCode = ServletRequestUtils.getStringParameter(request, "cityCode", "");
			if(!DataTypeUtils.isNotEmpty(cityCode)){
				throw new Exception("必填参数为空");
			}
			int skip = ServletRequestUtils.getIntParameter(request, "skip", 0);
			int size = ServletRequestUtils.getIntParameter(request, "size", Integer.MAX_VALUE);
			responseEntity.setResult(service.list(cityCode,skip,size));
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
	
}
