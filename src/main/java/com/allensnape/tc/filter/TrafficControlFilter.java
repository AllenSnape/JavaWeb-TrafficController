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

	// ��ŷ��ʴ�����key
	private static final String REDIS_KEY_PREFIX = "pcl_web_tc_";
	// ��ŷ��ʴ����������ݵ�key, ����������д��redis��
	private static final String TABLE_REDIS_KEY_PREFIX = "table_pcl_web_tc_";

	public void init(FilterConfig arg0) throws ServletException {
		System.out.println("--������������ʼ�����--");
	}

	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		try {
			Jedis j = RedisPool.getJedis();
			try {
				// ��ȡURI
				String URI = ((HttpServletRequest) arg0).getRequestURI();
				// ��ȡ�����б�
				List<AccessLimit> list = this.getAccessLimits(URI);
				// ѭ����ѯ�Ƿ���
				for (AccessLimit al : list) {
					// ��ȡʱ�޴���ͳ��
					String key = this.getRedisKey(al.getUri(), al.getExpire());
					String count = j.get(key);
					// ������
					if (count == null) {
						Transaction transaction = j.multi();
			            transaction.incr(key);
			            transaction.expire(key, al.getExpire());
			            transaction.exec();
					} else {
						// ��������ֱ�ӷ��ش�����Ϣ
			            if(Integer.parseInt(count) >= al.getCount()){  
			                // TODO ���÷�������
			            	HttpServletResponse res = ((HttpServletResponse)arg1);
			            	res.setCharacterEncoding("utf-8");
			            	res.setContentType("Content-Type: application/json");
			            	res.getWriter().write("{\"msg\": \"���ʹ���Ƶ��!\"}");
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
		System.out.println("--�����������������--");
	}

	/**
	 * ��ȡredis�е�key
	 * @param URI			��ȡkey��URI
	 * @param expire		��ʱʱ��(��)
	 * @return
	 */
	private String getRedisKey(String URI, int expire) {
		return REDIS_KEY_PREFIX + URI + "_" + expire;
	}

	/**
	 * ��ȡuri�����б�
	 * @param URI
	 * @return
	 */
	private List<AccessLimit> getAccessLimits(String URI) {
		Jedis j = RedisPool.getJedis();
		List<AccessLimit> list = new ArrayList<AccessLimit>();
		// ��redis��ȡ�����б�, ��ŵ���json��ʽ������: {"expire": 86400, "count": 10000}
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
