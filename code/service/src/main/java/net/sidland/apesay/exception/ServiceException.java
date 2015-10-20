package net.sidland.apesay.exception;
public class ServiceException extends APIException {

	
	/**
	 * serialVersionUID:TODO（用一句话描述这个变量表示什么）
	 *
	 * @since 1.0.0
	 */
	
	private static final long serialVersionUID = -6006955331825052595L;

	/**
	 * 创建一个新的实例 APIServiceException.
	 *
	 */

	public ServiceException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 创建一个新的实例 APIServiceException.
	 *
	 * @param msg
	 */

	public ServiceException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 创建一个新的实例 APIServiceException.
	 *
	 * @param msg
	 * @param cause
	 */

	public ServiceException(String msg, Throwable cause) {
		super(msg, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 创建一个新的实例 APIServiceException.
	 *
	 * @param cause
	 */

	public ServiceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
