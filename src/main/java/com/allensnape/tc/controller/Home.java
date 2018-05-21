package com.allensnape.tc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.allensnape.tc.service.ITrafficControlService;

/**
 * 流量控制配置器
 * @author AllenSnape
 */
@Controller
@RequestMapping("/tc")
public class Home {
	
	// TODO 完成控制
	
	@Autowired
	private ITrafficControlService trafficControlService;
	
	/**
	 * Hello World!
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = {"/hello", "/"}, method = RequestMethod.GET)
	public String hello() {
		// trafficControlService.checkExistAndCreate();
		return "HelloWorld!";
	}

}
