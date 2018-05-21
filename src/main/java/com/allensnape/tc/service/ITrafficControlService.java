package com.allensnape.tc.service;

public interface ITrafficControlService {
	
	/**
	 * 检查流量控制表是否存在, 如果不存在则创建
	 * @return
	 */
	public boolean checkExistAndCreate();

}
