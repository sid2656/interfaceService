package net.sidland.apesay.cache;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sidland.apesay.utils.APPUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

@Component
public class CacheService {

	@Autowired
	private JedisPool jedisPool;

	/**
	 * 
	 * setex(写入缓存)
	 * @param key 
	 * @param seconds 过期时间 单位秒
	 * @param value
	 * void
	 * @exception
	 * @since  1.0.0
	 */
	public void setex(String key, int seconds,String value){
		Jedis jedis = null;
		try{
			jedis = getResource();
			jedis.setex(key.getBytes(APPUtils.CHARSET), seconds, value.getBytes(APPUtils.CHARSET));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
	}
	
	/**
	 * 
	 * set(key,string)
	 * @param key
	 * @param value
	 * @throws Exception
	 * void
	 * @exception
	 * @since  1.0.0
	 */
	public void set(String key, String value) throws Exception{
		Jedis jedis = null;
		try{
			jedis = getResource();
			
			jedis.set(key, value);
		}finally{
			returnResource(jedis);
		}
	}
	
	/**
	 * 
	 * get(返回指定key值)
	 * @param key
	 * @return
	 * String
	 * @exception
	 * @since  1.0.0
	 */
	public String get(String key){
		String value = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			value = jedis.get(key);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return value;
	}
	/**
	 * 
	 * get(返回指定key值)
	 * @param key
	 * @return
	 * String
	 * @exception
	 * @since  1.0.0
	 */
	public void del(String key){
		Jedis jedis = null;
		try{
			jedis = getResource();
			jedis.del(key.getBytes());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
	}
	/**
	 * 
	 * push(在指定链表头部增加一个或多个值，链表不存在则新建)
	 * @param queueName 队列名
	 * @param values 值顺序列表，如：a,b,c.先进先出
	 * @return Long 队列元素个数
	 * @exception
	 * @since  1.0.0
	 */
	public Long push(String queueName,String... values){
		Long queueLength = 0L;
		Jedis jedis = null;
		try{
			jedis = getResource();
			queueLength = jedis.lpush(queueName, values);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);
			
		}
		return queueLength;
	}
	
	/**
	 * 
	 * pop(从指定队列表尾部取出一个值)
	 * @param queueName 队列名
	 * @return String 元素值
	 * @exception
	 * @since  1.0.0
	 */
	public String pop(String queueName){
		String value = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			value =jedis.rpop(queueName);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return value;
	}
	
	/**
	 * 
	 * lrange(从指定队列取起始位置到结束位置元素)
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * List<String>
	 * @exception
	 * @since  1.0.0
	 */
	public List<String> lrange(String key,long start, long end){
		List<String> value = new ArrayList<String>();
		Jedis jedis = null;
		try{
			jedis = getResource();
			value = jedis.lrange(key, start, end);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return value;
	}
	
	public void hset(String key,String field, String value){
		Jedis jedis = null;
		try{
			jedis = getResource();
			jedis.hset(key, field, value);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
	}
	
	public void hmset(String key, Map<String,String> value){
		Jedis jedis = null;
		try{
			jedis = getResource();
			jedis.hmset(key, value);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
	}
	
	public String hget(String key,String field){
		String value = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			value = jedis.hget(key, field);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return value;
	}
	
	public Map<String,String> hget(String key){
		Map<String,String> value = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			 value = jedis.hgetAll(key);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return value;
	}
	
	public List<String> hmget(String key,String...fields){
		List<String> value = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			value = jedis.hmget(key, fields);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return value;
	} 
	
	public Long hdel(String key,String...fields){
		Long value = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			 value = jedis.hdel(key, fields);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return value;
	} 
	/**
	 * 
	 * queueLength(返回指定队列元素个数)
	 * @param queueName
	 * @return Long
	 * @exception
	 * @since  1.0.0
	 */
	public Long queueLength(String queueName){
		Long queueLength = 0L;
		Jedis jedis = null;
		try{
			jedis = getResource();
			queueLength = jedis.llen(queueName);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return queueLength;
	}
	
	
	/**
	 * 
	 * zadd(向有序sorts-set里添加值)
	 * @param key 指定key
	 * @param score 分数，排序用
	 * @param member 成员(要存的值)
	 * void
	 * @exception
	 * @since  1.0.0
	 */
	public void zadd(String key,double score,String member){
		Jedis jedis = null;
		try{
			jedis = getResource();
			jedis.zadd(key, score, member);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
	}
	
	/**
	 * 
	 * zcard(返回key Sets数量)
	 * @param key
	 * @return
	 * Long
	 * @exception
	 * @since  1.0.0
	 */
	public Long zcard(String key){
		Long c = 0L;
		Jedis jedis = null;
		try{
			jedis = getResource();
			c = jedis.zcard(key);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return c;
	}
	
	/**
	 * 
	 * zrange(返回有序list，以score正序排，1，2，3，4等 )
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * List<String>
	 * @exception
	 * @since  1.0.0
	 */
	public List<String> zrange(String key,final long start,final long end){
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		Jedis jedis = null;
		try{
			jedis = getResource();
			set = (LinkedHashSet<String>)jedis.zrange(key, start, end);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return  new ArrayList<String>(set);
	}
	
	
	/**
	 * 
	 * zrevrangeByScore(返回有续集key中score<=max并且score>=min 的元素，返回结果根据score从大到小顺序排列 )
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * List<String>
	 * @exception
	 * @since  1.0.0
	 */
	public List<String> zrevrangeByScore(String key,final double min,final double max, int skip, int size){
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		Jedis jedis = null;
		try{
			jedis = getResource();
			set = (LinkedHashSet<String>)jedis.zrevrangeByScore(key, max, min, skip, size);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return  new ArrayList<String>(set);
	}
	
	
	public void zrem(String key,String... members){
		Jedis jedis = null;
		try{
			jedis = getResource();
			jedis.zrem(key, members);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
	}
	
	/**
	 * 
	 * zscore(返回某个成员的分数，如果不存在返回null)
	 * @param key
	 * @param member
	 * @return
	 * Double
	 * @exception
	 * @since  1.0.0
	 */
	public Double zscore(String key,String member){
		Jedis jedis = null;
		try{
			jedis = getResource();
			Double v = jedis.zscore(key, member);
			return v;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return null;
	}
	
	/**
	 * incr(按key自增(+1))
	 * @param key
	 * @return
	 * Long
	 * @exception
	 * @since  1.0.0
	 */
	public Long incr(String key){
		Long i = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			i = jedis.incr(key);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return i;
	}
	
	public Long incrBy(String key, long increament){
		Long i = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			i = jedis.incrBy(key, increament);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			returnResource(jedis);
		}
		return i;
	}
	
	public void returnResource(Jedis jedis){
		jedisPool.returnResource(jedis);
	}
	
	public Jedis getResource(){
		Jedis jedis = jedisPool.getResource();
		return jedis;
	}
	public  <T> T pipeline(PipelineDo<T> pipelineDo,Object ...objects) throws Exception{
		T value = null;
		Jedis jedis = null;
		try{
			jedis = getResource();
			Pipeline pl = jedis.pipelined();
			value = pipelineDo.exec(pl,objects);
		}finally{
			returnResource(jedis);
		}
		return value;
	} 
}
