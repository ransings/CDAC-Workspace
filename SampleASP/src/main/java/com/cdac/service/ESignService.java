package com.cdac.service;

import java.io.File;

import java.io.IOException;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Map;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import com.cdac.exception.EsignException;
import com.cdac.exception.InvalidDocSignatureException;
import com.cdac.exception.InvalidResponseException;
import com.cdac.exception.SessionRecoveryException;
import com.cdac.exception.SignatureEmbeddingException;
import com.cdac.models.DocSignature;
import com.cdac.models.Docs;
import com.cdac.models.Esign;
import com.cdac.models.EsignResp;
import com.cdac.models.InputHash;
import com.cdac.models.RequestData;
import com.cdac.sbeans.HashCodeUtils;
import com.cdac.sbeans.KeyLoader;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfString;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

@Service
public class ESignService {
	@Autowired
	private HashCodeUtils utils;
	@Autowired
	private RestTemplate template;
	@Autowired
	private KeyLoader keyLoader;
	@Value("${ip_conf}")
	private String ip;
	@Value("${api_url}")
	private String api_url;
	@Value("${asp_id}")
	private String asp_id;

	public File copyFile(MultipartFile file, HttpServletRequest req) {
		File dest = null;
		if (!file.isEmpty()) {
			try {
				String uploadsDir = "/Files/";
				String realPathtoUploads = req.getServletContext().getRealPath(uploadsDir);
				if (!new File(realPathtoUploads).exists()) {
					new File(realPathtoUploads).mkdir();
				}

				String orgName = file.getOriginalFilename();
				orgName = orgName.replace(" ", "-");
				String filePath = realPathtoUploads + orgName;
				dest = new File(filePath);
				file.transferTo(dest);
				filePath = dest.getAbsolutePath();
				return dest;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}
		return dest;
	}

	public Document generateMarshalledXml(String hash, HttpSession session) throws Exception {
		RequestData requestData = (RequestData) session.getAttribute("requestData");
		InputHash ihash = new InputHash();
		ihash.setDocInfo("Information about document");
		ihash.setDocUrl(requestData.getFilePath());
		ihash.setHashCode(hash);

		Docs docs = new Docs();
		docs.setIHash(ihash);

		Esign esign = new Esign();
		esign.setDoc(docs);
		esign.setTs(utils.getTimestamp());
		requestData.setTxn(utils.generateTxn());
		esign.setTxn(requestData.getTxn());
		esign.setAspId(asp_id);
		esign.setRedirectUrl(ip + "/esign/3.0/redurl");
		esign.setResponseUrl(ip + "/esign/3.0/resurl");
		esign.setSignerid(requestData.getUserName());

		JAXBContext context = JAXBContext.newInstance(Esign.class);
		Marshaller mar = context.createMarshaller();

		// to save xml
//		mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//		mar.marshal(esign, new File(outputFilePath + "App.xml"));
//		
		JAXBContext jaxbContext = JAXBContext.newInstance(Esign.class);
		mar = jaxbContext.createMarshaller();
		mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		DOMResult res = new DOMResult();
		mar.marshal(esign, res);

		Document doc = (Document) res.getNode();

		StringWriter stringWriter = new StringWriter();

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		// Use the transformer to convert the Document to a string
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(stringWriter);
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(source, result);

		// Get the string representation of the Document
		System.out.println("--------------------------------------\n generated-xml\n" + stringWriter.toString() + "\n");

		return doc;

	}

	public String signDocument(Document document) throws Exception {
		XMLSignatureFactory xsf = XMLSignatureFactory.getInstance("DOM");
		PrivateKey privateKey = keyLoader.getPrivateKey();
		DOMSignContext dsc = new DOMSignContext(privateKey, document.getDocumentElement());

		Reference ref = null;
		SignedInfo signedInfo = null;

		try {
			ref = xsf.newReference("", xsf.newDigestMethod(DigestMethod.SHA256, null),
					Collections.singletonList(xsf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
					null, null);

			signedInfo = xsf.newSignedInfo(
					xsf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
					xsf.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));

		} catch (Exception e) {
			e.printStackTrace();
		}

		/* //creating KeyInfo object KeyInfoFactory kif=xsf.getKeyInfoFactory();
		 * KeyValue keyValue=kif.newKeyValue(keyLoader.getPublicKey()); KeyInfo
		 * keyInfo=kif.newKeyInfo(Collections.singletonList(keyValue)); */
		XMLSignature signature = xsf.newXMLSignature(signedInfo, null);

		try {
			signature.sign(dsc);

			// To save signed XML
			// saveDocument(document);

			// saving RequestXml to session Object
			StringWriter stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Use the transformer to convert the Document to a string
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(stringWriter);
			transformer.transform(source, result);
			/* FileOutputStream fos=new FileOutputStream(new
			 * File("C:\\Users\\Shubham Ransing\\Desktop\\2.xml"));
			 * fos.write(stringWriter.toString().getBytes()); fos.close(); */
			System.out.println("\n Signed XML\n" + stringWriter.toString());
			return stringWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public String callEsignApi(String xmlData, HttpSession session) {
		RequestData requestData = (RequestData) session.getAttribute("requestData");
		HttpHeaders headers = new HttpHeaders();
		int statusCode = 400, status = 0;
		String acknowledgement = "", txnref = "";

		// Calling esign API
		headers.setContentType(MediaType.APPLICATION_XML);
		HttpEntity<String> entity = new HttpEntity<String>(xmlData, headers);
		try {
			ResponseEntity<String> res = template.postForEntity(api_url, entity, String.class);
			statusCode = res.getStatusCode().value();
			acknowledgement = res.getBody();
			System.out.println(
					"\n-----------------------------------Response Body----------------------------------------\n"
							+ res.getBody());

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (statusCode >= 200 && statusCode <= 299) {

			if (!acknowledgement.equals("")) {
				status = Integer.parseInt(acknowledgement.substring(acknowledgement.indexOf("status=\"") + 8,
						acknowledgement.indexOf("\" ts=\"")));
				System.out.println("\n-----------------------------------------\nstatus=" + status);
				if (status == 2) {
					String rescode = acknowledgement.substring(acknowledgement.indexOf("resCode=\"") + 9,
							acknowledgement.indexOf("\" status=\""));
					txnref = requestData.getTxn() + "|" + rescode;
					System.out.println("\n-----------------------------------------\ntxnref=" + txnref);
					txnref = new String(Base64.encodeBase64(txnref.getBytes()));
					System.out.println("\n-----------------------------------------\nEncoded txnref=" + txnref);
					return txnref;
				} else {
					System.out.println("------------------------------------------------------------------------");
					System.out.println("Error in acknowledgement XML");
				}

			}

		} else {
			System.out.println("Acknowledgement not received....");
		}
		return txnref;
	}

	public String getFilePath(File file) throws IOException {
		String path = file.getPath().substring(file.getPath().indexOf("\\webapp") + 7, file.getPath().length());
		String fileName = path.replace("\\Files\\", "");

		return "/Files?file=" + fileName;

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
						appearance = ((RequestData) session.getAttribute("requestData")).getAppearance();
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
