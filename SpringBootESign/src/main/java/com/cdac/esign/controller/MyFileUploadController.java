package com.cdac.esign.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.text.DateFormat;
//import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.cdac.esign.encryptor.Encryption;
import com.cdac.esign.form.FormXmlDataAsp;
import com.cdac.esign.form.MyUploadForm;
import com.cdac.esign.form.RequestXmlForm;
import com.cdac.esign.model.EsignResp;
import com.cdac.esign.model.ResponseAck;
import com.cdac.esign.validator.FieldValidator;
import com.cdac.esign.xmlparser.AspXmlGenerator;
import com.cdac.esign.xmlparser.XmlSigning;

 
@Controller
//@Scope("request")
//("session")s
public class MyFileUploadController {
	
	@Autowired
	private PdfEmbedder pdfEmbedder;
	
	@Autowired
	private RestTemplate template;
	
	private Map<String, Object> sessionMapper=new HashMap<>();

 
  /* @RequestMapping(value = "/")
   public String homePage() {
 
      return "index";
   }*/
 
   
	
   @RequestMapping(value = "/")
   public String uploadOneFileHandler(Model model) {
 
      MyUploadForm myUploadForm = new MyUploadForm();
      model.addAttribute("myUploadForm", myUploadForm);
      return "uploadOneFile";
   }
   
 

   @RequestMapping(value = "/uploadMultiFile", method = RequestMethod.POST)
   @ResponseBody
   public RequestXmlForm saveEmployee(@ModelAttribute @Valid MyUploadForm myUploadForm,
         BindingResult result, HttpServletRequest request, Model model, HttpSession session) {
	   System.out.println("**************************************"+session.getId());
	   return this.doProcess(request, model, myUploadForm, session);
   }
   
   private RequestXmlForm doProcess(HttpServletRequest request, Model model, //
	         MyUploadForm myUploadForm, HttpSession session) {
	   int statusCode=400,status=0;
	   String rescode="";
	   HttpHeaders headers=new HttpHeaders();
	   File serverFile=null;
	   String acknowledgement="";
	   System.out.println("**************************************"+session.getId());
		   FieldValidator fieldValidator = new FieldValidator();
		  // PdfEmbedder pdfEmbedder = new PdfEmbedder();
		   if(fieldValidator.validateFields(myUploadForm.getAadhar(), "Y", myUploadForm.getAuthType(), myUploadForm.getFileDatas())) {
		   
			  // Root Directory.
		      String uploadRootPath = request.getServletContext().getRealPath("upload");
		      System.out.println("uploadRootPath=" + uploadRootPath);
		 
		      File uploadRootDir = new File(uploadRootPath);
		      // Create directory if it not exists.
		      if (!uploadRootDir.exists()) {
		         uploadRootDir.mkdirs();
		      }
		      MultipartFile[] fileDatas = myUploadForm.getFileDatas();
		      //
		      List<File> uploadedFiles = new ArrayList<File>();
		      List<String> failedFiles = new ArrayList<String>();
		
		 	  String fileHash = "";
		      for (MultipartFile fileData : fileDatas) {
		 
		         // Client File Name
		         String name = fileData.getOriginalFilename();
		         System.out.println("Client File Name = " + name);
		 
		         if (name != null && name.length() > 0) {
		            try {
		               // Create the file at server
		               serverFile = new File(uploadRootDir.getAbsolutePath() + File.separator + name);
		 
		               BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
		               stream.write(fileData.getBytes());
		               stream.close();
		               //
		               uploadedFiles.add(serverFile);
		               //fileHash = calculateFileHash(uploadRootDir.getAbsolutePath() + File.separator + name);
		               fileHash = pdfEmbedder.pdfSigner(serverFile,request, session);
		               request.getSession().setAttribute("pdfEmbedder", pdfEmbedder);
		               System.out.println("Write file: " + serverFile);
		            } catch (Exception e) {
		               System.out.println("Error Write file: " + name);
		               failedFiles.add(name);
		            }
		         }
		      }
			   
			  		  
			  //get data from Form
		      String authType = myUploadForm.getAuthType();
		      System.out.println("Description: " + authType);
		      System.out.println("Aadhar: " + myUploadForm.getAadhar());
		      System.out.println("Consent: " + myUploadForm.getConsent());
		 	            
		      Date now = new Date();
		      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
			   //try xml generation
			  AspXmlGenerator aspXmlGenerator = new AspXmlGenerator();
			  FormXmlDataAsp formXmalDataAsp = new FormXmlDataAsp();
			  Random randNum = new Random();
			  int randInt = randNum.nextInt();
			  formXmalDataAsp.setVer("3.0");
			  formXmalDataAsp.setMaxWaitPeriod("1440");
			  formXmalDataAsp.setTs(dateFormat.format(now));
			  //formXmalDataAsp.setTxn((myUploadForm.getAadhar() + randInt).replace("-", ""));
			  formXmalDataAsp.setTxn(("" + randInt).replace("-", ""));
			  formXmalDataAsp.setSignerid(myUploadForm.getSignerId());
			  formXmalDataAsp.setSigningAlgorithm("ECDSA");
			  formXmalDataAsp.setResponseUrl("http://10.208.52.194:8080/SpringBootESign/finalResponse");
			  formXmalDataAsp.setRedirectUrl("https://google.com/");
			  formXmalDataAsp.setDocUrl("http://10.208.52.194:8080/SpringBootESign/files?file="+serverFile.getName());
			  //formXmalDataAsp.setAspId("CDAC-901");
			  formXmalDataAsp.setAspId("CDAC-009");
			  
			  formXmalDataAsp.setId("1");
			  formXmalDataAsp.setHashAlgorithm("SHA256");
			  formXmalDataAsp.setResponseSigType("pkcs7");
			  formXmalDataAsp.setDocInfo("My Document");
			  //formXmalDataAsp.setResponseUrl("url");
			  formXmalDataAsp.setDocHashHex(fileHash);
			  
			  //setting txn for session
			  session.setAttribute("txn", formXmalDataAsp.getTxn());

			  //Get encrypted string/ signed data for xml signature tag
		      String strToEncrypt = aspXmlGenerator.generateAspXml(formXmalDataAsp,request);
		      String encryptedText = "";
		      String xmlData = "";
			  try {
		      Encryption encryption = new Encryption();
			  PrivateKey rsaPrivateKey =  encryption.getPrivateKey("testasp.pem");
			  File encrFile = new File(uploadRootDir.getAbsolutePath() + File.separator + "Excrypted.xml");
			  String encryptedFile = uploadRootDir.getAbsolutePath() + File.separator + "Excrypted.xml";
			  xmlData = new XmlSigning().signXmlStringNew(uploadRootDir.getAbsolutePath() + File.separator + "Testing.xml", rsaPrivateKey);
			  System.out.println("\n\n\nRequestXml\n\n\n"+xmlData+"\n\n\n");
			  aspXmlGenerator.writeToXmlFile(xmlData,uploadRootDir.getAbsolutePath() + File.separator + "Testing.xml");
			  
			  
		      }
		      catch(Exception e) {
		    	  System.out.println("Error in Encryption.");
		    	  e.printStackTrace();
		    	  return new RequestXmlForm();
		      }
			  
			  
			  //Calling esign API
			  headers.setContentType(MediaType.APPLICATION_XML);
			  HttpEntity<String> entity=new HttpEntity<String>(xmlData,headers);
			  try {
			  ResponseEntity<String> res=template.postForEntity("https://esign-rel:11443/esign/3.0/signdoc", entity, String.class);
			  statusCode=res.getStatusCodeValue();
			  acknowledgement=res.getBody();
			  System.out.println("\n--------------------------------------------\n"+"Status code:"+res.getStatusCode());
			  System.out.println("\n-----------------------------------Response Body----------------------------------------\n"+res.getBody());
			  
			  }
			  catch (Exception e) {
				e.printStackTrace();
			}
			 String txnref="";
			  if(statusCode>=200&&statusCode<=299) {
				  
				  if(!acknowledgement.equals("")) {
					  status=Integer.parseInt(acknowledgement.substring(acknowledgement.indexOf("status=\"")+8, acknowledgement.indexOf("\" ts=\"")));
					  System.out.println("\n-----------------------------------------\nstatus="+status);
					  if(status==2) {
						 rescode=acknowledgement.substring(acknowledgement.indexOf("resCode=\"")+9, acknowledgement.indexOf("\" status=\""));
						  txnref=formXmalDataAsp.getTxn()+"|"+rescode;
						  System.out.println("\n-----------------------------------------\ntxnref="+txnref);
						  txnref=new String(Base64.encodeBase64(txnref.getBytes()));
						  System.out.println("\n-----------------------------------------\nEncoded txnref="+txnref);
						  formXmalDataAsp.setTxnRef(txnref);
						  
						  headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
						  entity=new HttpEntity("txnref="+txnref,headers);
						   
					 }
					 
					  
				  }
				  else {
					  System.out.println("Acknowledgement not received....");
				  }
				  
			  }
			
			  //for session Management
			  sessionMapper.put(formXmalDataAsp.getTxn(), session);
			  
			  
			  RequestXmlForm myRequestXmlForm = new RequestXmlForm();
			  myRequestXmlForm.setId("");
			  myRequestXmlForm.setType(myUploadForm.getAuthType());
			  myRequestXmlForm.setDescription("Y");
			  myRequestXmlForm.seteSignRequest(txnref);
			  myUploadForm.setXml(xmlData);
		      return myRequestXmlForm;
		   }
		   else {
			   return new RequestXmlForm();
		   }
	   }   
   
 

   
   @PostMapping("/finalResponse")
   @ResponseBody
   public ResponseAck ReadEspResponse(@RequestBody String resp,HttpServletRequest req) throws Exception {
    HttpSession session=req.getSession();
    String result="";
	JAXBContext context= JAXBContext.newInstance(EsignResp.class);
	Unmarshaller unmarshaller=context.createUnmarshaller();
	StringReader reader=new StringReader(resp);
	EsignResp response=(EsignResp) unmarshaller.unmarshal(reader);
	System.out.println("---------Response-------------------------------------------------------");
	
	//signing pdf
	result=pdfEmbedder.signPdf(response, sessionMapper);
	System.out.println(result);
	
	ResponseAck ack= new ResponseAck();
	ack.setErrCode(response.getError());
	ack.setErrMsg("NA");
	ack.setStatus(response.getStatus());
	ack.setTs(response.getTs());
	ack.setTxn(response.getTxn());
	
	return ack;
    
   }


   
   @GetMapping("/files")
	public ResponseEntity<Resource> getFile(@RequestParam("file") String fileName,HttpServletRequest req) throws FileNotFoundException {
	    // Logic to retrieve the file based on the filename
		String filePath=req.getServletContext().getRealPath("/upload/");

	    // Load the file as a resource
	    InputStream is=new FileInputStream(new File(filePath+fileName));
	    InputStreamResource resource=new InputStreamResource(is,null);

	   
	    // Return the file as a response with appropriate headers
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION, "inline;attachment; filename"+fileName)
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(resource);   
	}
   
   @RequestMapping("/downloadPdfLocally")
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//If user is not authorized - he should be thrown out from here itself
			
			//Authorized user will download the file
			String dataDirectory = request.getServletContext().getRealPath("upload");
			Path file = Paths.get(dataDirectory, "Signed_Pdf.pdf");
			if (Files.exists(file))
				{
					response.setContentType("application/pdf");
					response.addHeader("Content-Disposition", "attachment; filename="+"Signed_Pdf.pdf");
				try
				{
					Files.copy(file, response.getOutputStream());
					response.getOutputStream().flush();
				}
					catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
}