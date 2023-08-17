package com.cdac.esign.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.wss4j.dom.handler.RequestData;
import org.springframework.stereotype.Component;

import com.cdac.esign.exceptions.InvalidDocSignatureException;
import com.cdac.esign.exceptions.InvalidResponseException;
import com.cdac.esign.exceptions.SessionRecoveryException;
import com.cdac.esign.exceptions.SignatureEmbeddingException;
import com.cdac.esign.model.DocSignature;
import com.cdac.esign.model.EsignResp;
import com.cdac.esign.xmlparser.XmlSigning;
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

@Component
//@Scope("session")
public class PdfEmbedder {
    String    destFile=null;
   // HttpSession session = null;
    FileOutputStream fout;
   
     PdfSignatureAppearance appearance;
     
   public String pdfSigner(File file, HttpServletRequest request, HttpSession session) {
         
  
       String hashDocument =null;
       PdfReader reader;
       try {
           String sourcefile=file.getAbsolutePath();
           String filename=null;
           System.out.println("Path--->"+sourcefile);
           filename=file.getName().substring(0, (file.getName().length())-4)+"_Signed.pdf";
         destFile=sourcefile.replace(file.getName(), filename);
         session.setAttribute("fileName",filename);
//     destFile=sourcefile.replace(file.getName(), "/Signed_Pdf.pdf");
//     request.getSession().setAttribute("fileName","/Signed_Pdf.pdf");
           reader =  new PdfReader(sourcefile);
           
           Rectangle cropBox = reader.getCropBox(1);
           Rectangle rectangle = null;
           String user=null;
           rectangle = new Rectangle(cropBox.getLeft(),cropBox.getBottom(), cropBox.getLeft(100),cropBox.getBottom(90));
            fout = new FileOutputStream(destFile);
           PdfStamper stamper = PdfStamper.createSignature(reader, fout, '\0', null, true);
     
           appearance= stamper.getSignatureAppearance();
           appearance.setRenderingMode(RenderingMode.DESCRIPTION);
           appearance.setAcro6Layers(false);
           Font font = new Font();
           font.setSize(6);
           font.setFamily("Helvetica");
           font.setStyle("italic");
           appearance.setLayer2Font(font);
           Calendar currentDat = Calendar.getInstance();
           currentDat.add(currentDat.MINUTE, 5);
           appearance.setSignDate(currentDat);
           
           if(user == null || user == "null" || user.equals(null) || user.equals("null") ){
           appearance.setLayer2Text("Signed");
           }else{
               appearance.setLayer2Text("Signed by "+user);
             }
           //appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
           appearance.setImage(null);
           appearance.setVisibleSignature(rectangle,
                   reader.getNumberOfPages(), null);

           int contentEstimated = 8192;
           HashMap<PdfName, Integer> exc = new HashMap();
           exc.put(PdfName.CONTENTS, contentEstimated * 2 + 2);

           PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE,
                   PdfName.ADBE_PKCS7_DETACHED);
           dic.setReason(appearance.getReason());
           dic.setLocation(appearance.getLocation());
           dic.setDate(new PdfDate(currentDat));

           appearance.setCryptoDictionary(dic);
         //  request.getSession().setAttribute("pdfHash",appearance);
           appearance.preClose(exc);
          // fout.close();
           session.setAttribute("appearance",appearance);
         // System.gc();
           // getting bytes of file
           InputStream is = appearance.getRangeStream();

           hashDocument = DigestUtils.sha256Hex(is);
           //session=request.getSession();
           //session.setAttribute("appearance1",appearance);
             System.out.println("hex:    " + is.toString());
       } catch (Exception e) {
           System.out.println("Error in signing doc.");
           
       }
       return hashDocument;
      
   }

public String signPdfwithDS(String response,HttpServletRequest request, HttpSession session) {
	session = request.getSession(false);
	//PdfSignatureAppearance appearance = (PdfSignatureAppearance)request.getSession().getAttribute("appearance");
	   int contentEstimated = 8192; 
	   try {
		   if(request.getSession() == null) {
			   System.out.println("=================session===========");
		   }
	//   PdfSignatureAppearance appearance = (PdfSignatureAppearance)request.getSession().getAttribute("pdfHash");
	   
	   //String esignRespResult = DocSignature;
       String errorCode = response.substring(response.indexOf("errCode"),response.indexOf("errMsg"));
       errorCode = errorCode.trim();
       if(errorCode.contains("NA")) {
		   String pkcsResponse = new XmlSigning().parseXml(response.trim());
		   byte[] sigbytes = Base64.decodeBase64(pkcsResponse);
		   byte[] paddedSig = new byte[contentEstimated];
		   System.arraycopy(sigbytes, 0, paddedSig, 0, sigbytes.length);
		   PdfDictionary dic2 = new PdfDictionary();
		   dic2.put(PdfName.CONTENTS,
		           new PdfString(paddedSig).setHexWriting(true));
		   //fout.close();
		   appearance.close(dic2); 
       }
       else {
    	   destFile = "Error";
       }
	   }
	   catch(Exception e) {
		   e.printStackTrace();
	   }
	   return destFile;
	}

public String signPdf(EsignResp response, Map<String, Object> sessionMapper) throws Exception {
	PdfSignatureAppearance appearance = null;
	PdfDictionary dic2 = null;
	HttpSession session = null;

	// checking if response is not error response
	if (response.getError() == null || response.getError().contains("NA")) {

		// Retrieving session object.
		try {
			session = (HttpSession) sessionMapper.get(response.getTxn());
			if (session == null)
				throw new SessionRecoveryException("Could Not Recover session for txn:" + response.getTxn());

		} catch (Exception e) {
			e.printStackTrace();
			throw new SessionRecoveryException("Could Not Recover session for txn:" + response.getTxn());
		}

		// checking if signature is null or not
		if (response.getSignatures() != null) {
			// Retieving docsignature and pdf appearence object for signing
			DocSignature dsc = response.getSignatures().getList().get(0);

			if (dsc.getError().contains("NA")) {
				try {
					byte[] sigbytes = Base64.decodeBase64(dsc.getDsc().getBytes());
					byte[] paddedSig = new byte[8192];
					System.arraycopy(sigbytes, 0, paddedSig, 0, sigbytes.length);
					dic2 = new PdfDictionary();
					dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
					appearance = (PdfSignatureAppearance) session.getAttribute("appearance");
					appearance.close(dic2);
				} catch (Exception e) {
					e.printStackTrace();
					throw new SignatureEmbeddingException("Signature Embedding Failed..");
				}
			} else {
				System.out.println("------------------------------------------------------------------------");
				System.out.println("Error in Docsignature...");
				System.out.println("------------------------------------------------------------------------");
				throw new InvalidDocSignatureException("Error In DocSignature,Error code:" + dsc.getError());
			}

		} else {
			System.out.println("------------------------------------------------------------------------");
			System.out.println("DocSignature is null..");
			System.out.println("------------------------------------------------------------------------");
			throw new InvalidDocSignatureException("Cannot Use Empty DocSignature");
		}

	} else {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Error in ESP Response..");
		System.out.println("------------------------------------------------------------------------");
		throw new InvalidResponseException("Error in eSign Response, Error code:" + response.getError());
	}
	return "Signed Successfully";

}
}