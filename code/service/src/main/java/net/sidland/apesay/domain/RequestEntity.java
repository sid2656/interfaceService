package net.sidland.apesay.domain;

import javax.servlet.http.HttpServletRequest;

 /**
 *
 * <b>类名称：</b>URIEntity<br/>
 * <b>类描述：</b><br/>
 * <b>创建人：</b>Jack.Sin<br/>
 * <b>修改人：</b><br/>
 * <b>修改时间：</b>2014-9-2 上午11:02:46<br/>
 * <b>修改备注：</b><br/>
 * @version 1.0.0<br/>
 *
 */
public class RequestEntity {

	public RequestEntity(HttpServletRequest req){
		String uri[] = req.getRequestURI().split("/");
		this.url = req.getRequestURL().toString();
		// /api/method/model/id
		this.method = uri[2];
		this.model = uri[3];
		if(uri.length==5){
			this.id = uri[4];
		}
		
	}
	
	private String method;
	
	private String model;
	
	private String id;
		
	private String url;

	public String getMethod() {
		return method;
	}

	public String getModel() {
		return model;
	}


	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

}
