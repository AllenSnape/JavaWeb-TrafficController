package com.allensnape.tc.config;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

/* <bean id="readConfigBean" class="config.ReadConfig"></bean> */

@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class ReadConfig implements ApplicationListener<ContextRefreshedEvent> {
	
	private static String[] configPaths = new String[]{
		"/config/webservice/loanCenter-servlet.xml"
	};
	
	private static Class[] AVAILABLE_REQ_METHOD_ANNOS = {
		POST.class,
		GET.class,
		OPTIONS.class,
		DELETE.class,
		PUT.class,
		HEAD.class,
	};
	
	private static Map<Class, RequestMethod> ANNO_TO_METHOD = new HashMap<Class, RequestMethod>();
	static {
		ANNO_TO_METHOD.put(AVAILABLE_REQ_METHOD_ANNOS[0], RequestMethod.POST);
		ANNO_TO_METHOD.put(AVAILABLE_REQ_METHOD_ANNOS[1], RequestMethod.GET);
		ANNO_TO_METHOD.put(AVAILABLE_REQ_METHOD_ANNOS[2], RequestMethod.OPTIONS);
		ANNO_TO_METHOD.put(AVAILABLE_REQ_METHOD_ANNOS[3], RequestMethod.DELETE);
		ANNO_TO_METHOD.put(AVAILABLE_REQ_METHOD_ANNOS[4], RequestMethod.PUT);
		ANNO_TO_METHOD.put(AVAILABLE_REQ_METHOD_ANNOS[5], RequestMethod.HEAD);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// 获取到的地址
		List<RequestHandler> handlers = new ArrayList<>();
		
		System.out.println(getClass().getResource("/"));
		
		String folderString = getClass().getResource("/").toString().substring(5);
		
		// 实例化xml读取器
		SAXReader reader = new SAXReader();
		
		// 循环配置路径
		for (String configPathString : configPaths) {
			try {
				Document document = reader.read(new File(folderString + configPathString));
			    Element beans = document.getRootElement();
				List<Element> servers = beans.elements("server");
				
				for (Element server : servers) {
					// 获取地址前缀
					String addressPrefix = server.attributeValue("address");
					System.out.println(addressPrefix);
					// 获取引用的bean
					String beanNameString = server.element("serviceBeans").element("ref").attributeValue("bean");
					System.out.println(beanNameString);
					
					// 根据beanid获取bean的class
					Class bean = this.getClass(beans, event.getApplicationContext(), beanNameString);
					if (bean == null) continue;
					try {
						// pis = parent interfaces
						Class[] pis = bean.getInterfaces();
						for (Class pi : pis) {
							Method[] methods = pi.getDeclaredMethods();
							for (Method method : methods) {
								if (!this.hasPathAnno(method)) continue;
								RequestHandler rhHandler = new RequestHandler();
								rhHandler.setHandler(method);
								List<RequestMethod> rmethodsList = new ArrayList<>();
								Annotation[] annos = method.getAnnotations();
								for (Annotation anno : annos) {
									if (anno instanceof Path) {
										rhHandler.setUri(addressPrefix + ((Path)anno).value());
										continue;
									}
									
									for (Class rmac : AVAILABLE_REQ_METHOD_ANNOS) {
										if (anno.annotationType() == rmac) {
											rmethodsList.add(ANNO_TO_METHOD.get(rmac));
											break;
										}
									}
								}
								RequestMethod[] rmsMethods = new RequestMethod[rmethodsList.size()];
								for (int i = 0; i < rmethodsList.size(); i++) {
									rmsMethods[i] = rmethodsList.get(i);
								}
								rhHandler.setMethod(rmsMethods);
								handlers.add(rhHandler);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		StringBuffer handlersJSON = new StringBuffer("[");
		for (RequestHandler rh : handlers) {
			StringBuffer s = new StringBuffer();
			s.append("{\"URI\": \"" + rh.getUri() +"\", \"methods\": [");
			StringBuffer rmS = new StringBuffer();
			for (RequestMethod rm : rh.getMethod()) {
				rmS.append("\"" + rm.name() + "\",");
			}
			s.append(rmS.length() > 0 ? rmS.substring(0, rmS.length() - 1) : "");
			s.append("]},");
			
			handlersJSON.append(s);
		}
		if (handlersJSON.length() > 0) ;
		
		System.out.println(handlersJSON.length() > 0 ? handlersJSON.substring(0, handlersJSON.length() - 1) + "]" : "");
	}
	
	private Class getClass(Element beansDom, ApplicationContext appCon, String id) {
		List<Element> beans = beansDom.elements("bean");
		for (Element bean : beans) {
			if (bean.attributeValue("id").equals(id)) {
				try {
					return Class.forName(bean.attributeValue("class"));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private boolean hasPathAnno(Method handler) {
		Annotation[] annos = handler.getAnnotations();
		for (Annotation anno : annos) {
			if (anno instanceof Path) {
				return true;
			}
		}
		return false;
	}
	
}
