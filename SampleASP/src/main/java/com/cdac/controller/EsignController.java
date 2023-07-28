package com.cdac.controller;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import com.cdac.models.DocSignature;
import com.cdac.models.EsignResp;
import com.cdac.models.RequestData;
import com.cdac.models.ResponseAck;
import com.cdac.sbeans.StamperGeneration;
import com.cdac.service.ESignService;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/3.0")
public class EsignController {
	@Autowired
	private ESignService service;
	@Autowired
	private StamperGeneration stamperGeneration;
	@Getter
	private Map<String,Object> SessionMapper=new HashMap<>();
	

	
	@Value("${ip_conf}")
	private String ip;
	@Value("${form_url}")
	private String form_url;
		
	@GetMapping("/")
	public String homePage(Map<String,Object> map) {
		System.out.println("EsignController.homePage()");
		map.put("url", ip+"/esign/3.0/upload");
		return "index"; 
	}
	
	
	@PostMapping("/upload")
	public String generateRequest(@RequestParam("file") MultipartFile file,@RequestParam("user") String name, 
			HttpServletRequest request,	Map<String,Object> map) {
		HttpSession session=request.getSession();
		session.setMaxInactiveInterval(300);
	    RequestData requestData;	    
		String str = "",signedXml="";
		File file1 = null;
		Document doc=null;
		
		requestData=new RequestData();
		requestData.setUserName(name);
		session.setAttribute("requestData", requestData);
		//uploading of file
		
		try {
			if (str != null)
				file1 = service.copyFile(file, request);
			str = "File uploaded to:: " + (file1.getAbsolutePath());
			//System.out.println("after copy...::"+session);
		} catch (Exception e) {
			e.printStackTrace();
			str = "Failed to upload...";
		}
		
		//Creating stamper and calculating document hash
		
		try {
			requestData.setHash(stamperGeneration.generateSigner(file1.getAbsolutePath(), session));
			requestData.setFilePath(ip+"/esign/3.0"+service.getFilePath(file1));
			System.out.println("------------------------------------------------------------------------");
			System.out.println("docUrl::"+requestData.getFilePath());
			System.out.println("------------------------------------------------------------------------");
			doc = service.generateMarshalledXml(requestData.getHash(),session);
			System.out.println("------------------------------------------------------------------------");
			str += "\nXml Generation successfull..";
		} catch (Exception e) {
			e.printStackTrace();
			str = "XML Generation Failed...";
		}
		
		try {
			signedXml=service.signDocument(doc);
			System.out.println("------------------------------------------------------------------------");
			str += "\nXml Signing successfull..";
		} catch (Exception e) {
			e.printStackTrace();
			str = "XML Signing Failed...";
		}
		
		try {
			System.out.println("------------------------------------------------------------------------");
			System.out.println(signedXml+"\n");
			requestData.setTxnref(service.callEsignApi(signedXml, session));
		}
		catch (Exception e) {
			e.printStackTrace();
			str="Error in Api call...";
		}
		
		map.put("txnref", requestData.getTxnref());
		map.put("url", form_url);
		
		SessionMapper.put(requestData.getTxn(), session);
		
		return "postRequest";
		
	}
	
	
	@GetMapping("/Files")
	public ResponseEntity<Resource> getFile(@RequestParam("file") String fileName,HttpServletRequest req) throws FileNotFoundException {
	    // Logic to retrieve the file based on the filename
		String filePath=req.getServletContext().getRealPath("/Files/");

	    // Load the file as a resource
	    Resource fileResource = new FileSystemResource(filePath+fileName);
	    InputStream is=new FileInputStream(new File(filePath+fileName));
	    InputStreamResource resource=new InputStreamResource(is,null);

	   
	    // Return the file as a response with appropriate headers
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION, "inline;attachment; filename"+fileName)
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(resource);   
	}
	
	@PostMapping("/resurl")
	@ResponseBody
	public ResponseAck getEsignResponse(@RequestBody String res) throws Exception {
		String result="";
		JAXBContext context= JAXBContext.newInstance(EsignResp.class);
		Unmarshaller unmarshaller=context.createUnmarshaller();
		StringReader reader=new StringReader(res);
		EsignResp response=(EsignResp) unmarshaller.unmarshal(reader);
		System.out.println("---------Response-------------------------------------------------------");
		
		//signing pdf
		result=service.signPdf(response, SessionMapper);
		
		ResponseAck ack= new ResponseAck();
		ack.setErrCode(response.getError());
		ack.setErrMsg("NA");
		ack.setStatus(response.getStatus());
		ack.setTs(response.getTs());
		ack.setTxn(response.getTxn());
		
		return ack;
	}
	
	@PostMapping("failure")
	public String failurePage() {
		return "failure";
	}

	@PostMapping("/redurl")
	public String getEsignResponse1(@RequestParam String txnref,Map<String, Object> map) {
		txnref=new String(Base64.decodeBase64(txnref.getBytes()));
		String txn=txnref.substring(0,txnref.indexOf("|"));
		System.out.println("Decoded txnref::"+txnref);
		System.out.println(txn);
		
		HttpSession session=(HttpSession)SessionMapper.get(txn);
		String filepath=((RequestData)session.getAttribute("requestData")).getServerFile();
		map.put("filePath", filepath);
		
		return "Success";
	}
	
	@PostMapping("/download")
	public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) throws FileNotFoundException{
			File file=new File(filePath);  
		   	Resource fileResource = new FileSystemResource(filePath);
		    InputStream is=new FileInputStream(file);
		    InputStreamResource resource=new InputStreamResource(is,null);
		    
		   
		    // Return the file as a response with appropriate headers
		    return ResponseEntity.ok()
		            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+file.getName())
		            .contentType(MediaType.APPLICATION_PDF)
		            .body(resource);  
	}

}
