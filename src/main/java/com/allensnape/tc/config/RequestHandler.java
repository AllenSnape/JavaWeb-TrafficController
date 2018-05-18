package com.allensnape.tc.config;

import java.lang.reflect.Method;

import org.springframework.web.bind.annotation.RequestMethod;

public class RequestHandler {
	
	private String uri;
	private RequestMethod[] method;
	private Method handler;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public RequestMethod[] getMethod() {
		return method;
	}
	public void setMethod(RequestMethod[] method) {
		this.method = method;
	}
	public Method getHandler() {
		return handler;
	}
	public void setHandler(Method handler) {
		this.handler = handler;
	}
}