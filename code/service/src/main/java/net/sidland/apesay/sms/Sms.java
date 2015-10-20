package net.sidland.apesay.sms;

public class Sms {
	/**
	 * 手机号码
	 */
	private String mobile;

	/**
	 * 短信内容
	 */
	private String content;

	/**
	 * 短信类型 我们自定义，便于和短信供应商统计
	 */
	private String typeCode;

	public Sms(){
		
	}
	/**
	 * 如果以下参数可能包含中文 或 特殊字符，请先进行 url encode
	 * 
	 * @param mobile
	 * @param content
	 * @param typeCode
	 */
	public Sms(String mobile, String content, String typeCode) {
		this.mobile = mobile;
		this.content = content;
		this.typeCode = typeCode;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}
}
