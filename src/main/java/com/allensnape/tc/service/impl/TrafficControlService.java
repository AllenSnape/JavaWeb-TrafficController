package com.allensnape.tc.service.impl;

import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.allensnape.tc.service.ITrafficControlService;

@Service
public class TrafficControlService implements ITrafficControlService {
	
	@Autowired
	private DataSource dataSource;
	
	@Override
	public boolean checkExistAndCreate() {
		try {
			ResultSet rs = dataSource
			.getConnection()
			.createStatement()
			.executeQuery("select count(*) from dba_tables where owner = 'pcl' and table_name = 'AS_PCL_TRAFFIC_CONTROL'");
			System.out.println(rs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private Statement getStatement() {
		try {
			return this.dataSource.getConnection().createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}

class TrafficControlServiceSQLs {

	// 创建流量控制表
	public static final String CREATE_TC_TABLE_SQL = 
			"-- Create table" + 
			"create table AS_PCL_TRAFFIC_CONTROL" + 
			"(" + 
			"  URI          VARCHAR2(3000) not null," + 
			"  METHOD       VARCHAR2(10) not null," + 
			"  EXPIRE       NUMBER not null," + 
			"  COUNT        NUMBER not null," + 
			"  CREATED_TIME TIMESTAMP(6) not null," + 
			"  UPDATED_TIME TIMESTAMP(6) not null" + 
			")" + 
			"tablespace PCL" + 
			"  pctfree 10" + 
			"  initrans 1" + 
			"  maxtrans 255;" + 
			"-- Add comments to the table " + 
			"comment on table AS_PCL_TRAFFIC_CONTROL" + 
			"  is '流量控制信息';" + 
			"-- Add comments to the columns " + 
			"comment on column AS_PCL_TRAFFIC_CONTROL.URI" + 
			"  is '请求链接';" + 
			"comment on column AS_PCL_TRAFFIC_CONTROL.METHOD" + 
			"  is '请求方式';" + 
			"comment on column AS_PCL_TRAFFIC_CONTROL.EXPIRE" + 
			"  is '时间限制';" + 
			"comment on column AS_PCL_TRAFFIC_CONTROL.COUNT" + 
			"  is '次数限制';" + 
			"-- Create/Recreate primary, unique and foreign key constraints " + 
			"alter table AS_PCL_TRAFFIC_CONTROL" + 
			"  add constraint PK_AS_PCL_TRAFFIC_CONTROL primary key (METHOD, URI, EXPIRE)" + 
			"  using index " + 
			"  tablespace PCL" + 
			"  pctfree 10" + 
			"  initrans 2" + 
			"  maxtrans 255;";

}
