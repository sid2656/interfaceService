/**
 * Project Name:keegoo-service
 * File Name:filter.java
 * Package Name:com.keegoo.filter
 * Date:2015年8月31日下午2:10:43
 * Copyright (c) 2015, sid Jenkins All Rights Reserved.
 * 
 *
*/

package net.sidland.apesay.filter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import net.sidland.apesay.utils.Constant;
import net.sidland.apesay.utils.DataTypeUtils;

/**
 * 
 * ClassName: ManageFilter 
 * Function: 登录过滤. 
 * date: 2015年8月31日 下午2:11:22 
 *
 * @author sid
 */
public class ManageFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		List<String> noFilter = new ArrayList<String>();
		noFilter.add("/apis/manager/login");
		noFilter.add("/apis/manager/logout");
		String url = request.getRequestURI();
		// 执行过滤
		// 从session中获取登录者实体
		Object user_roleid = request.getSession().getAttribute(Constant.user_roleid);
		if(!noFilter.contains(url)&&!DataTypeUtils.isNotEmpty(user_roleid)){
			//转发到登录页面
			if (url.startsWith("/api/runbaby")) {
				//TODO 可以做一些其他逻辑处理；待确定
			}else{
				response.sendRedirect("/apis/login.html");
			}
			return;
		}else{
			//TODO 验证user_roleid是超级管理员
		}
		// 如果session中存在登录者实体，则继续
		filterChain.doFilter(request, response);
	}
}


