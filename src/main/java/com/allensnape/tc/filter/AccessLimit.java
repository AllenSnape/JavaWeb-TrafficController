package com.allensnape.tc.filter;

/**
 * 访问限制模型
 * @author AllenSnape
 */
public class AccessLimit {
	// 访问URI
	private String uri;
	// 时限
	private Integer expire;
	// 次数
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
