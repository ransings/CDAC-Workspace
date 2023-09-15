package com.cdac.controller;

import java.util.HashMap;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;

import com.cdac.components.CRLValidator;
import com.cdac.components.PostXml;
import com.cdac.components.SignedPdf;
import com.cdac.model.Signer_User;
import com.cdac.service.ESignService;

import jakarta.servlet.http.HttpSession;

@Controller
public class VarificationController {
	@Autowired
	private ESignService service;
	@Autowired
	private CRLValidator crlValidator;
	public static Map<String, Object> tracker = new HashMap<>();

	@GetMapping("/")
	public String getHome() {
		return "index";
	}

	@GetMapping(value = "/varify")
	public String varifySignaturne1(Map<String, Object> map) throws Exception {
		map.put("vflag", 0);
		map.put("Signer_Name", "Aditya Dharmadhikari");
		map.put("Location", "Selu,Parabhani");
		map.put("error", "Invalid esign response code");
		map.put("Issuer", "C-DAC CA 2014");
		map.put("msg", "Signed hash- 5302625c5736b45c8f4c2e01b3f27919d536d06f7ea172c72d964e67264f3525");
		map.put("reason", "Response has been Tampered");
		return "varify";
	}

	@PostMapping("/validate")
	public String validateString(@RequestParam String data, HttpSession session, Map<String, Object> map)
			throws Exception {
		PostXml postXml = null;
		System.out.println("\nRequest Data\n" + data + "\n");

		// calculate Hash of Data.
		System.out.println("Generated Hash::" + service.calculateHash(data, session));
		// generating requestXML
		Document doc = service.generateMarshalledXml(session);

		System.out.println(service.signDocument(doc, session) + "\n");
		postXml = (PostXml) session.getAttribute("postXml");
		// set hash to session scope
		SignedPdf signedPdf = (SignedPdf) session.getAttribute("signedPdf");
		signedPdf.setTxn(postXml.getAspTxnID());
		tracker.put(postXml.getAspTxnID(), signedPdf);

		map.put("url", postXml.getUrl());
		map.put("esignRequest", postXml.getESignRequest());
		map.put("txnId", postXml.getAspTxnID());
		map.put("content", postXml.getContent_Type());

		return "page1";
	}

	@PostMapping(value = "/varify")
	public String varifySignature(@RequestParam("eSignResponse") String dsc, HttpSession s1, Map<String, Object> map)
			throws Exception {
		String error=service.getErrors(dsc);	
		map.put("dsc", dsc);
		map.put("error",error);
		if(error!=null)
		map.put("vflag",3);
		return "varify";
	}

//To be modified	
	@PostMapping("/varify_dsc")
	public String varifyDsc(Map<String, Object> map, @RequestParam("dsc") String dsc, HttpSession session)
			throws Exception {
		SignedPdf signedPdf = null;
		boolean notTampered = false, isCrlValid = false,isValid=true;
		String reason = null;
		String signedHash = null;
		Signer_User user = null;
		String txn = null;
		String msg = null;

		try {
		// Getting txn id
		txn = service.getTxn(dsc);
		}catch (Exception e) {
			reason="Invalid XML Structure";
			isValid=false;
		}

		if (session.getAttribute("signedPdf") != null) {
			signedPdf = (SignedPdf) session.getAttribute("signedPdf");
		} else if (tracker.containsKey(txn)) {
			signedPdf = (SignedPdf) tracker.get(txn);
			session.setAttribute("signedHash", signedHash);
			session.setAttribute("signedPdf", signedPdf);
		}

		try {
			// checking wheather xml is tampered or not
			notTampered = service.varifyResponse(dsc);

			// checking error code of response
			msg = service.getErrors(dsc);
			
			signedHash=service.getSignedHash(dsc);

			// Get Signer information and validating hash
			user = service.varifyDSC(signedPdf.getDocumentHash(), dsc);

			// Validating CRL Revocation
			isCrlValid = crlValidator.isCrlValid(service.getXmlResponse(dsc));
		} catch (Exception e) {
			user=new Signer_User();
		}
		if(isValid)
		// Setting Signing Failure reason
		reason = !notTampered ? "Response has been tampered": msg != null ? msg: user.getVflag() == 0 ? "Hash mismatched"
								: !isCrlValid ? "CRL revocation failed" : "Unexpected error occured";

		// checking Original response and matched hash
		if ((user.getVflag() == 1) && notTampered && isCrlValid)
			user.setVflag(1);
		else
			user.setVflag(0);

		map.put("vflag", user.getVflag());
		map.put("dsc", dsc);
		map.put("msg", "Signed hash- " + signedHash);
		map.put("Signer_Name", user.getName());
		map.put("Location", user.getLocation());
		map.put("cert_cont", user.getCert_Cont());
		map.put("Issuer", user.getIssuer());
		map.put("reason", reason);

		return "varify";

	}

	@PostMapping("/cert_download")
	public ResponseEntity<byte[]> download_Cert(@RequestParam("dsc") String dsc, HttpSession session) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDisposition(ContentDisposition.attachment().filename("certificates.p7b").build());
		return new ResponseEntity<>(dsc.getBytes(), headers, HttpStatus.OK);
	}

}
