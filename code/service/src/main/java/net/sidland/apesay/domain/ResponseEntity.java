package net.sidland.apesay.domain;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

import net.sidland.apesay.utils.Constant;

 /**
  * 返回给客户端公共协义
  * ClassName: ResponseEntity 
  * date: 2015年10月17日 下午2:51:30 
  *
  * @author sid
  */
public class ResponseEntity implements Serializable {

	private static final long serialVersionUID = 5296264326011920871L;
	
	@JSONField(name="status")
	private String status = Constant.API_CALL_STATUS_SUCCESS;
	
	@JSONField(name="msg")
	private String msg = Constant.API_CALL_STATUS_SUCCESS_MSG;
	
	private String data;

	@JSONField(name="result")
	private Object result;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setResult(Object result) {
		this.result = result;
	}
	
	public Object getResult() {
		if(this.result == null) return new Object();
		return result;
	}
}
