package com.allensnape.tc.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class TrafficControlFilter implements Filter {

	// 存放访问次数的key
	private static final String REDIS_KEY_PREFIX = "pcl_web_tc_";
	// 存放访问次数设置数据的key, 由其他程序写入redis的
	private static final String TABLE_REDIS_KEY_PREFIX = "table_pcl_web_tc_";

	public void init(FilterConfig arg0) throws ServletException {
		System.out.println("--限流过滤器初始化完成--");
	}

	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		try {
			Jedis j = RedisPool.getJedis();
			try {
				// 获取URI
				String URI = ((HttpServletRequest) arg0).getRequestURI();
				// 获取限制列表
				List<AccessLimit> list = this.getAccessLimits(URI);
				// 循环查询是否超限
				for (AccessLimit al : list) {
					// 获取时限次数统计
					String key = this.getRedisKey(al.getUri(), al.getExpire());
					String count = j.get(key);
					// 检查次数
					if (count == null) {
						Transaction transaction = j.multi();
			            transaction.incr(key);
			            transaction.expire(key, al.getExpire());
			            transaction.exec();
					} else {
						// 超出次数直接返回错误信息
			            if(Integer.parseInt(count) >= al.getCount()){  
			                // TODO 设置返回数据
			            	HttpServletResponse res = ((HttpServletResponse)arg1);
			            	res.setCharacterEncoding("utf-8");
			            	res.setContentType("Content-Type: application/json");
			            	res.getWriter().write("{\"msg\": \"访问过于频繁!\"}");
			            	return;
			            } else {
				            j.incr(key);
			            }
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				j.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		arg2.doFilter(arg0, arg1);
	}

	public void destroy() {
		System.out.println("--限流过滤器销毁完成--");
	}

	/**
	 * 获取redis中的key
	 * @param URI			获取key的URI
	 * @param expire		超时时间(秒)
	 * @return
	 */
	private String getRedisKey(String URI, int expire) {
		return REDIS_KEY_PREFIX + URI + "_" + expire;
	}

	/**
	 * 获取uri限制列表
	 * @param URI
	 * @return
	 */
	private List<AccessLimit> getAccessLimits(String URI) {
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
