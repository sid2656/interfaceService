package net.sidland.apesay.exception;
public class APIException extends Exception {

	private static final long serialVersionUID = -1720859129158334517L;

	public APIException() {
		super();
	}
	
	public APIException(String msg){
		super(msg);
	}
	
	public APIException(String msg, Throwable cause){
		super(msg,cause);
	}
	
	public APIException(Throwable cause) {
		super(cause);
	}
}
