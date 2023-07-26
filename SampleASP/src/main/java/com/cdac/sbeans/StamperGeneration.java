package com.cdac.sbeans;

import java.io.File;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.cdac.models.RequestData;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component("stamperGeneration")
public class StamperGeneration {
	private PdfSignatureAppearance appearance;
	private String destFile;

	public String generateSigner(String source,HttpSession session) throws Exception {
		PdfReader reader = new PdfReader(source);
		  RequestData requestData=(RequestData)session.getAttribute("requestData");
		Rectangle cropBox = reader.getCropBox(1);
		Rectangle rectangle = null;
		File file = new File(source);

		destFile = file.getAbsolutePath();
		destFile = destFile.replace(".pdf", "_signed.pdf");

		rectangle = new Rectangle(cropBox.getLeft(), cropBox.getBottom(), cropBox.getLeft(100), cropBox.getBottom(90));
		FileOutputStream fout = new FileOutputStream(destFile);

		PdfStamper stamper = PdfStamper.createSignature(reader, fout, '\0', null, true);

		appearance = stamper.getSignatureAppearance();
		appearance.setRenderingMode(RenderingMode.DESCRIPTION);
		appearance.setAcro6Layers(false);
		Font font = new Font();
		font.setSize(6);
		font.setFamily("Helvetica");
		font.setStyle("italic");
		appearance.setLayer2Font(font);
		Calendar currentDat = Calendar.getInstance();
		currentDat.add(Calendar.MINUTE, 5);
		appearance.setSignDate(currentDat);
		appearance.setLayer2Text("Signed");

		appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
		appearance.setImage(null);
		appearance.setVisibleSignature(rectangle, reader.getNumberOfPages(), null);

		int contentEstimated = 8192;
		HashMap<PdfName, Integer> exc = new HashMap();
		exc.put(PdfName.CONTENTS, contentEstimated * 2 + 2);

		PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
		dic.setReason(appearance.getReason());
		dic.setLocation(appearance.getLocation());
		dic.setDate(new PdfDate(appearance.getSignDate()));
		appearance.setCryptoDictionary(dic);
		appearance.preClose(exc);
		InputStream is = appearance.getRangeStream();
		requestData.setAppearance(appearance);
		requestData.setAppearance(appearance);
		session.setAttribute("requestData", requestData);

		String hashOfDoc = DigestUtils.sha256Hex(is);
		
		return hashOfDoc;

	}

	/* public String signPdf(HttpSession session) throws Exception { SignedPdf
	 * signedPdf=(SignedPdf)session.getAttribute("signedPdf"); String response =
	 * signedPdf.getResponseXml(); int contentEstimated = 8192; String errorCode =
	 * response.substring(response.indexOf("errCode"), response.indexOf("errMsg"));
	 * errorCode = errorCode.trim(); if (errorCode.contains("NA")) { String
	 * pkcsResponse = parseXml(response.trim()); byte[] sigbytes =
	 * Base64.decodeBase64(pkcsResponse); byte[] paddedSig = new
	 * byte[contentEstimated]; System.arraycopy(sigbytes, 0, paddedSig, 0,
	 * sigbytes.length); PdfDictionary dic2 = new PdfDictionary();
	 * dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
	 * appearance.close(dic2); } else { destFile = "Error"; }
	 * 
	 * return destFile; } */

	public String parseXml(String esignResponse) throws Exception {

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(esignResponse));

		Document doc = db.parse(is);
		NodeList node = doc.getElementsByTagName("DocSignature");
		String sig = node.item(0).getTextContent();
		return sig;
	}

}
