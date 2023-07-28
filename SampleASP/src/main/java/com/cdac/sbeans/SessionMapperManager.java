package com.cdac.sbeans;

import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HttpSessionMutexListener;

import com.cdac.controller.EsignController;
import com.cdac.models.RequestData;

@Component
public class SessionMapperManager extends HttpSessionMutexListener {
	@Autowired
	private EsignController controller;
	
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		super.sessionDestroyed(event);
		
		HttpSession session=event.getSession();
		
		if(session!=null) {
		String txn=((RequestData)session.getAttribute("requestData")).getTxn();
		Map<String, Object> map=controller.getSessionMapper();
		if(map.containsKey(txn))
		controller.getSessionMapper().remove(txn);
		System.out.println("session removed from mapper for txn:"+txn);
		}
		
	}

}
