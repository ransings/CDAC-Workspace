package com.cdac.components;

import java.io.File;


import java.io.FileOutputStream;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.util.Collections;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import jakarta.servlet.http.HttpSession;

@Component("xmlSigner")
public class XmlSigner {	
	@Autowired
	KeyLoader keyLoader;
	
	public String signDocument(Document document,HttpSession session) throws Exception{
		PostXml postXml=(PostXml)session.getAttribute("postXml");
		XMLSignatureFactory xsf=XMLSignatureFactory.getInstance("DOM");
		PrivateKey privateKey=keyLoader.getPrivateKey();
		DOMSignContext dsc=new DOMSignContext(privateKey, document.getDocumentElement());
		
		Reference ref=null;
		SignedInfo signedInfo=null;
		
		try {
			ref=xsf.newReference("",xsf.newDigestMethod(DigestMethod.SHA256, null),
					Collections.singletonList(xsf.newTransform(Transform.ENVELOPED,(TransformParameterSpec)null)),
					null, null);
			
			signedInfo=xsf.newSignedInfo(xsf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,(C14NMethodParameterSpec) null),
					xsf.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		//creating KeyInfo object
		KeyInfoFactory kif=xsf.getKeyInfoFactory();
		KeyValue keyValue=kif.newKeyValue(keyLoader.getPublicKey());
		KeyInfo keyInfo=kif.newKeyInfo(Collections.singletonList(keyValue));
		
		XMLSignature signature=xsf.newXMLSignature(signedInfo, keyInfo);
		
		try {
			signature.sign(dsc);
			
			//To save signed XML
			//saveDocument(document);
			
			//saving RequestXml to session Object
			StringWriter stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Use the transformer to convert the Document to a string
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(stringWriter);
			transformer.transform(source, result);
			postXml.setESignRequest(stringWriter.toString());
			System.out.println(stringWriter.toString());
			session.setAttribute("postXml", postXml);
			return "XML has signed successfully...";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "XML signing Failed...";
		
	}
	
}