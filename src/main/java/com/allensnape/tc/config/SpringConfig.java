package com.allensnape.tc.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

import com.allensnape.tc.config.pcl.ReadConfig;

import net.sf.json.JSONObject;

/*
<bean id="readConfigBean" class="com.allensnape.tc.config.SpringConfig"></bean>
*/

/**
 * Spring容器初始化完成事件
 * @author AllenSnape
 */
public class SpringConfig implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// 避免springmvc重复调用
		if (event.getApplicationContext().getParent() != null) return;
		
		// 动态添加web应用
		AbstractUrlHandlerMapping mapper = event.getApplicationContext().getBean(AbstractUrlHandlerMapping.class);
		System.out.println(JSONObject.fromObject(mapper.getHandlerMap()));
		
		// event.getApplicationContext().getAutowireCapableBeanFactory().
		
		// 初始化webservice的controller
		try {
			new ReadConfig(event.getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
