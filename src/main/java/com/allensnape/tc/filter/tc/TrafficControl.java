package com.allensnape.tc.filter.tc;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.allensnape.tc.utils.RedisPool;

import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class TrafficControl {

	// 存放访问次数的key
	private static final String REDIS_KEY_PREFIX = "pcl_web_tc_";
	// 存放访问次数设置数据的key, 由其他程序写入redis的
	private static final String TABLE_REDIS_KEY_PREFIX = "table_pcl_web_tc_";
	
	/**
	 * 开始流量控制
	 * @param req		过滤器进入的request
	 * @param res		过滤器响应的response
	 * @return true: 可以通过; false: 进行拦截了
	 */
	public boolean control(HttpServletRequest req, HttpServletResponse res) {
		Jedis j = RedisPool.getJedis();
		try {
			// 获取URI
			String URI = req.getRequestURI();
			// 获取限制列表
			List<AccessLimit> list = getAccessLimits(URI);
			// 循环查询是否超限
			for (AccessLimit al : list) {
				// -1表示无限制, 直接放行
				if (al.getCount() == -1) {
					break;
				}
				
				// 获取时限次数统计
				String key = getRedisKey(al.getUri(), al.getExpire());
				String count = j.get(key);
				// 检查次数
				if (count == null) {
					Transaction transaction = j.multi();
		            transaction.incr(key);
		            transaction.expire(key, al.getExpire());
		            transaction.exec();
		            count = "0";
				}
				
				// 超出次数直接返回错误信息
	            if(Integer.parseInt(count) > al.getCount()){  
	                // TODO 设置返回数据
	            	res.setCharacterEncoding("utf-8");
	            	res.setContentType("Content-Type: application/json");
	            	res.getWriter().write("{\"msg\": \"访问过于频繁!\"}");
	            	return false;
	            } else {
		            j.incr(key);
	            }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			j.close();
		}
		return true;
	}
	
	/**
	 * 获取redis中的key
	 * @param URI			获取key的URI
	 * @param expire		超时时间(秒)
	 * @return
	 */
	private static String getRedisKey(String URI, int expire) {
		return REDIS_KEY_PREFIX + URI + "_" + expire;
	}

	/**
	 * 获取uri限制列表
	 * @param URI
	 * @return
	 */
	private static List<AccessLimit> getAccessLimits(String URI) {
		Jedis j = RedisPool.getJedis();
		List<AccessLimit> list = new ArrayList<AccessLimit>();
		// 从redis获取限制列表, 存放的是json格式的内容: {"expire": 86400, "count": 10000}
		List<String> jsons = j.lrange(TABLE_REDIS_KEY_PREFIX + URI, 0, -1);
		for (String json : jsons) {
			try {
				AccessLimit al = (AccessLimit) JSONObject.toBean(JSONObject.fromObject(json), AccessLimit.class);
				al.setUri(URI);
				list.add(al);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		RedisPool.returnResource(j);
		return list;
	}
	
}
