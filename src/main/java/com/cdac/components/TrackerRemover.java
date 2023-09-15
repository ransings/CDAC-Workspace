package com.cdac.components;

import org.springframework.stereotype.Component;

import com.cdac.controller.VarificationController;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@Component
public class TrackerRemover implements HttpSessionListener {

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session= se.getSession();
		if(session.getAttribute("signedPdf")!=null) {
			SignedPdf signedPdf=(SignedPdf)session.getAttribute("signedPdf");
			
			if(VarificationController.tracker.containsKey(signedPdf.getDocumentHash())) {
				System.out.println("Removing Session for:"+signedPdf.getDocumentHash());
			VarificationController.tracker.remove(signedPdf.getDocumentHash());
			}
		}
	
	}

}
