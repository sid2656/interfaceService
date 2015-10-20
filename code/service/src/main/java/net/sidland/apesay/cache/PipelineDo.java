/**
 * 
 */
package net.sidland.apesay.cache;

import redis.clients.jedis.Pipeline;

/**
  *
  * <b>类名称：</b>PipelineDo<br/>
  * <b>类描述：</b><br/>
  * <b>创建人：</b>Jack.sin<br/>
  * <b>修改人：</b><br/>
  * <b>修改时间：</b>2014-9-30 下午05:07:04<br/>
  * <b>修改备注：</b><br/>
  * @version 1.0.0<br/>
  *
 */
public interface PipelineDo<T> {
		public T exec(Pipeline pl, Object ...objects) throws Exception;
}
