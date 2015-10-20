package net.sidland.apesay.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.sidland.apesay.domain.ResponseEntity;
import net.sidland.apesay.service.ManageService;
import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;

@Controller
@RequestMapping("/apis/manager")
public class ManagerController {
	
	protected static Logger logger = LoggerFactory.getLogger(ManagerController.class);
	@Autowired
	private ManageService manageService;

	/**
	 * 
	 * login:(管理员登录).
	 *
	 * @author sid
	 * @param body
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/login")
	@ResponseBody 
	public String login(@RequestBody String body,HttpServletRequest request, HttpServletResponse response) throws IOException{
		try {
			if(!DataTypeUtils.isNotEmpty(body)){
				throw new Exception("传入的参数不能为空");
			}
			JSONObject newdoc=JSON.parseObject(body);
			logger.info("用户登录："+newdoc);
			String username = newdoc.getString(Constant.user_username);
			String password = newdoc.getString(Constant.user_password);
			String site = Constant.PROJECT_NAME;
			JSONObject json = new JSONObject();
			json.put(Constant.User_loginIdentifying, null);
			json.put(Constant.user_username, username);
			json.put(Constant.user_password, password);
			json.put(Constant.user_site, site);
			if(DataTypeUtils.isNotEmpty(username)&&DataTypeUtils.isNotEmpty(password)){
				JSONObject returnValue = manageService.login(json);
				if (DataTypeUtils.isNotEmpty(returnValue)) {
					request.getSession().setAttribute(Constant.user_sessionToken, returnValue.getString(Constant.user_sessionToken));
					request.getSession().setAttribute(Constant.user_roleid, returnValue.getString(Constant.user_roleid));
				}
			}else{
				throw new Exception("传入的参数不能为空");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/apis/manager/manage.html";
	}

	/**
	 * 
	 * login:(管理员登录).
	 *
	 * @author sid
	 * @param body
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/logout")
	@ResponseBody 
	public String logout(@RequestBody String body,HttpServletRequest request, HttpServletResponse response) throws IOException{
		try {
			request.getSession().setAttribute(Constant.user_roleid, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/apis/login.html";
	}
	
	/**
	 * 
	 * add:(添加管理员).
	 *
	 * @author sid
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/add")
	@ResponseBody 
	public String add(@RequestBody String body,HttpServletRequest request, HttpServletResponse response) throws IOException{
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			//头像iconUrl，用户名username，手机号mobilePhoneNumber，密码password，阶段stage，预产期babyWillBirthday,宝宝出生日期babyBirthday，宝贝性别babyGender ，级别level=1,级别积分levelScore＝0,积分score=0
			if(!DataTypeUtils.isNotEmpty(body)){
				throw new Exception("传入的参数不能为空");
			}
			JSONObject newdoc=JSON.parseObject(body);
			System.out.println(newdoc.toJSONString());
			String username = newdoc.getString(Constant.user_username);
			JSONObject user = fillUser(newdoc, username);
			if(!DataTypeUtils.isNotEmpty(newdoc.getString(Constant.OBJECTID))){
				if (!manageService.usernameExists(username)) {
					JSONObject jsonObject = manageService.add(user);
					responseEntity.setResult(jsonObject);
					responseEntity.setMsg(Constant.API_CALL_STATUS_SUCCESS_MSG);
					responseEntity.setStatus(Constant.API_CALL_STATUS_SUCCESS);
				}
			}else{
				responseEntity.setResult(manageService.update(newdoc.getString(Constant.OBJECTID), user));
			}
		} catch (Exception e) {
			e.printStackTrace();
			responseEntity.setMsg(e.getMessage());
			responseEntity.setStatus(Constant.API_CALL_STATUS_ERROR_EXCEPTION);
		}
		request.setAttribute(Constant.CLIENT_RESPONSE, responseEntity);
		return null;
	}

	/**
	 * 删除群组
	 * @param body
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * String
	 * @exception
	 * @since  1.0.0
	 */
	@RequestMapping(value = "/delete")
	@ResponseBody 
	public String delete(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			if(!DataTypeUtils.isNotEmpty(body)){
				throw new Exception("传入的参数不能为空");
			}
			JSONObject newdoc=JSON.parseObject(body);
			String objectId = newdoc.getString(Constant.OBJECTID);
			responseEntity.setResult(manageService.delete(objectId));
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
	 * 获取群组
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
	@ResponseBody 
	public String list(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			if(!DataTypeUtils.isNotEmpty(body)){
				throw new Exception("传入的参数不能为空");
			}
			JSONObject newdoc=JSON.parseObject(body);
			String roleid = newdoc.getString("roleid");
			int skip = newdoc.getIntValue("skip");
			int size = newdoc.getIntValue("size");
			responseEntity.setResult(manageService.list(roleid, skip, size));
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
	 * 获取群组
	 * @param body
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * String
	 * @exception
	 * @since  1.0.0
	 */
	@RequestMapping(value = "/one")
	public @ResponseBody String one(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			if(!DataTypeUtils.isNotEmpty(body)){
				throw new Exception("传入的参数不能为空");
			}
			JSONObject newdoc=JSON.parseObject(body);
			String id = newdoc.getString(Constant.OBJECTID);
			responseEntity.setResult(manageService.one(id));
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
	 * fillUser:(组装用户信息).
	 *
	 * @author sid
	 * @param newdoc
	 * @param username
	 * @return
	 */
	private JSONObject fillUser(JSONObject newdoc, String username) {
		String password = newdoc.getString(Constant.user_password);
		String email = newdoc.getString(Constant.user_email);
		String roleid = newdoc.getString(Constant.user_roleid);
		String mobile = newdoc.getString(Constant.user_mobilePhoneNumber);
		String site = Constant.PROJECT_NAME;//网站名
		JSONObject user = new JSONObject();
		user.put(Constant.user_username, username);
		user.put(Constant.user_password, password);
		user.put(Constant.user_email, email);
		user.put(Constant.user_roleid, roleid);
		user.put(Constant.user_mobilePhoneNumber, mobile);
		user.put(Constant.user_site, site);
		return user;
	}
	
}
