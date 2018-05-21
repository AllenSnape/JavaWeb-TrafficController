package com.allensnape.tc.config.pcl;

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
import org.springframework.web.bind.annotation.RequestMethod;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ReadConfig {
	
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
	
	public ReadConfig(ApplicationContext ap) {
		// 获取到的地址
		List<RequestHandler> handlers = new ArrayList<>();
		// 获取当前classloader加载的路径
		String folderString = getClass().getResource("/").toString().substring(5);
		
		// 实例化xml读取器
		SAXReader reader = new SAXReader();
		
		// 循环配置路径
		for (String configPathString : configPaths) {
			try {
				File configFile = new File(folderString + configPathString);
				if (!configFile.exists()) continue;
				Document document = reader.read(configFile);
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
					Class bean = this.getClass(beans, ap, beanNameString);
					if (bean == null) continue;
					try {
						// pis = parent interfaces
						Class[] pis = bean.getInterfaces();
						for (Class pi : pis) {
							// 获取接口函数
							Method[] methods = pi.getDeclaredMethods();
							// 遍历函数
							for (Method method : methods) {
								// 判断是否被注解了Path, 没有注解的就不是请求应用的函数
								if (!this.hasPathAnno(method)) continue;
								// 声明实例, 用于保存数据
								RequestHandler rhHandler = new RequestHandler();
								rhHandler.setHandler(method);
								List<RequestMethod> rmethodsList = new ArrayList<>();
								// 获取该函数的其他注解, 获取到请求地址以及请求方式
								Annotation[] annos = method.getAnnotations();
								for (Annotation anno : annos) {
									// 获取请求地址
									if (anno instanceof Path) {
										rhHandler.setUri(addressPrefix + ((Path)anno).value());
										continue;
									}
									
									// 获取请求方式(GET, POST, ...)
									for (Class rmac : AVAILABLE_REQ_METHOD_ANNOS) {
										if (anno.annotationType() == rmac) {
											rmethodsList.add(ANNO_TO_METHOD.get(rmac));
											break;
										}
									}
								}
								
								// 转换List到对应类型的数组
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
		
		// 拼接一个输出内容
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
	
	/**
	 * 根据配置文件以及beanid获取spring的bean容器中对应的bean的Class
	 * @param beansDom		Spring的XML配置文件
	 * @param appCon		ApplicationContext
	 * @param id			bean的id
	 * @return
	 */
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

	/**
	 * 判断函数是否被注解了Path
	 * @param handler
	 * @return
	 */
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
