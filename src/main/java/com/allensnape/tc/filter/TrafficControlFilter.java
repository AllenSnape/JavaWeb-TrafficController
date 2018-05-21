package com.allensnape.tc.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.allensnape.tc.filter.tc.TrafficControl;

/*
<!-- 过滤器 -->
<filter>
	<filter-name>trafficController</filter-name>
	<filter-class>com.allensnape.tc.filter.TrafficControlFilter</filter-class>
</filter>
<filter-mapping>
	<filter-name>trafficController</filter-name>
	<url-pattern>/*</url-pattern>
</filter-mapping>
<!-- 只有添加这个之后才能触发实现了ApplicationListener<ContextRefreshedEvent>的类的事件 -->
<listener>
	<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
*/

/**
 * 流量控制过滤器
 * @author AllenSnape
 */
public class TrafficControlFilter implements Filter {
	
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("--流控监听器初始化完成");
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc)
			throws IOException, ServletException {
		try {
			// 流量控制
			if (!new TrafficControl().control((HttpServletRequest)req, (HttpServletResponse)res)) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		fc.doFilter(req, res);
	}

	public void destroy() {
		System.out.println("--限流过滤器销毁完成--");
	}


	
}