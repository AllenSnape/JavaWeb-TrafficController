package com.allensnape.tc.filter;

/**
 * ��������ģ��
 * @author AllenSnape
 */
public class AccessLimit {
	// ����URI
	private String uri;
	// ʱ��
	private Integer expire;
	// ����
	private Integer count;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Integer getExpire() {
		return expire;
	}
	public void setExpire(Integer expire) {
		this.expire = expire;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
}
