package com.cdac.service;

import java.io.File;




import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.cdac.components.SignedPdf;
import com.cdac.components.HashCodeUtils;
import com.cdac.components.KeyLoader;
import com.cdac.components.PostXml;
import com.cdac.components.ResponseError;
import com.cdac.components.XmlSigner;
import com.cdac.components.xml.Docs;
import com.cdac.components.xml.Esign;
import com.cdac.components.xml.InputHash;
import com.cdac.model.Signer_User;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import lombok.Setter;

@Service
public class ESignService {
	@Value("${responseUrl}")
	private String resUr;

	@Autowired
	KeyLoader keyLoader;
	@Autowired
	private HashCodeUtils utils;
	@Autowired
	private XmlSigner signer;
	private PublicKey publicKey;

	@PostConstruct
	private void setValues() {
		publicKey = keyLoader.getPublicKey();
	}
	
	
	public String getSignedHash(String pkcs7Response) throws Exception{
		SignerInformation siinfo = null;
		String hashToMatch=null;
		pkcs7Response=getXmlResponse(pkcs7Response);

		Security.addProvider(new BouncyCastleProvider());
		byte[] respBytes;
		try {
		respBytes = pkcs7Response.getBytes();

		CMSSignedData cms = new CMSSignedData(Base64.decode(respBytes));
		Collection<SignerInformation> si = cms.getSignerInfos()
		.getSigners();
		
		if (si.iterator().hasNext()) {
		siinfo = si.iterator().next();
		AttributeTable attributes = siinfo.getSignedAttributes();
		
		if (attributes != null) {

		Attribute messageDigestAttribute = attributes
		.get(CMSAttributes.messageDigest);
		DEROctetString derocHash = (DEROctetString) messageDigestAttribute
		.getAttrValues().getObjectAt(0);
		hashToMatch = derocHash.toString().replace("#", "");
		}
		else 
			hashToMatch= "";
		}
		
		}
		catch (Exception e) {
			
		}
		return hashToMatch;
	}


	public Document generateMarshalledXml(HttpSession session) throws Exception {
		String resUrl=resUr+("varify");
		PostXml postXml=new PostXml();
		SignedPdf signedPdf=(SignedPdf)session.getAttribute("signedPdf");
		InputHash ihash = new InputHash();
		ihash.setDocInfo("Information about document");
		ihash.setHashCode(signedPdf.getDocumentHash());
				
		Docs docs = new Docs();
		docs.setiHash(ihash);
		Esign esign = new Esign();
		esign.setTs(utils.getTimestamp());
		//esign.setEkycId("01000427V+N/rRjaeS8bsIMI2fd8tPqguxMegPk2xFxeoYLUtRzxWDaJURkuKE53gtlmzIgA");
		esign.setTxn(utils.generateTxn());
		esign.setAuthMode("1");
		postXml.setAspTxnID(esign.getTxn());
		esign.setAspId("TRYI-900");
		esign.setResponseUrl(resUrl);
		esign.setDoc(docs);
		
		
		JAXBContext context = JAXBContext.newInstance(Esign.class);
		Marshaller mar = context.createMarshaller();
		
		//to save xml
//		mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//		mar.marshal(esign, new File(outputFilePath + "App.xml"));
//		
		JAXBContext jaxbContext = JAXBContext.newInstance(Esign.class);
		 mar = jaxbContext.createMarshaller();
		 mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		DOMResult res=new DOMResult();
		mar.marshal(esign, res);
		
		Document doc=(Document)res.getNode();
		
		StringWriter stringWriter = new StringWriter();

		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		// Use the transformer to convert the Document to a string
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(stringWriter);
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(source, result);

		// Get the string representation of the Document
		//System.out.println("--------------------------------------\n generated-xml\n"+ stringWriter.toString()+"\n");
		
		session.setAttribute("postXml", postXml);
		return doc;

	}

	public String signDocument(Document doc,HttpSession session) throws Exception {
		return signer.signDocument(doc,session);
	}

	public String calculateHash(String data,HttpSession session) {
		SignedPdf signedPdf=new SignedPdf();
		
		String output = new DigestUtils("SHA-256").digestAsHex(data.getBytes());
		signedPdf.setDocumentHash(output);
		session.setAttribute("signedPdf", signedPdf);
		return output;
	}

	
	
	  //Varification
		public Signer_User varifyDSC(String hash,String dsc) throws Exception {
			return PKCS7Verifier(hash,getXmlResponse(dsc));
		}
	
		public String getXmlResponse(String fileToString) throws Exception{
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder=factory.newDocumentBuilder();
			Document doc=builder.parse(new InputSource(new StringReader(fileToString)));
			
			NodeList signatureNodes = doc.getElementsByTagNameNS("","DocSignature");
			if (signatureNodes.getLength() == 0) {
				throw new Exception("No docSignature found in the XML document.");
			}
			 
			Element signatureElement = (Element) signatureNodes.item(0);
					
			return elementToString(signatureElement);
		}
		
		
	private Signer_User PKCS7Verifier(String documentHash, String pkcs7Response)throws Exception {
			SignerInformation siinfo = null;
			Signer_User signer_User=new Signer_User();
			signer_User.setCert_Cont(pkcs7Response);
			byte[] signatureEcc = null;
			X509Certificate signerCert=null;
			Security.addProvider(new BouncyCastleProvider());
			byte[] respBytes;
			try {
			respBytes = pkcs7Response.getBytes();

			CMSSignedData cms = new CMSSignedData(Base64.decode(respBytes));
			Collection<SignerInformation> si = cms.getSignerInfos()
			.getSigners();
			
			if (si.iterator().hasNext()) {
			siinfo = si.iterator().next();
			AttributeTable attributes = siinfo.getSignedAttributes();
			signatureEcc = siinfo.getSignature();
			
			//creating certificate object
			SignerInfo sinfo=siinfo.toASN1Structure();
			X509CertificateHolder certHolder=(X509CertificateHolder)cms.getCertificates().getMatches(siinfo.getSID()).iterator().next();
		    signerCert = new JcaX509CertificateConverter().getCertificate(certHolder);
			
			if (attributes != null) {

			Attribute messageDigestAttribute = attributes
			.get(CMSAttributes.messageDigest);
			DEROctetString derocHash = (DEROctetString) messageDigestAttribute
			.getAttrValues().getObjectAt(0);
			String hashToMatch = derocHash.toString().replace("#", "");
			
			if (hashToMatch.equals(documentHash)) {
			System.out.println("hash matched");
			Store cs = cms.getCertificates();

			Collection certCollection = cs.getMatches(siinfo
			.getSID());
			X509CertificateHolder cert = (X509CertificateHolder) certCollection
			.iterator().next();
			X509Certificate certFromSignedData = new JcaX509CertificateConverter()
			.setProvider(new BouncyCastleProvider())
			.getCertificate(cert);

			//getDNQualifierFromCertificate(certFromSignedData);
			// get  DNQualifier

			Signature ecdsaVerify = Signature.getInstance(
			"SHA256withECDSA", new BouncyCastleProvider());
			ecdsaVerify.initVerify(certFromSignedData
			.getPublicKey());
			ecdsaVerify.update(siinfo.getEncodedSignedAttributes());
			
			boolean value = ecdsaVerify.verify(signatureEcc);
			if (value == true) {
			System.out.println("verified");
			
			
			//printing Signer Information
			String signer=signerCert.getSubjectX500Principal().getName();
			
			signer_User.setName(signer.substring(signer.indexOf("CN=")+3,signer.indexOf("O=")-1));
			signer_User.setLocation(signer.substring(signer.indexOf("ST=")+3,signer.indexOf("C=")-1)+","+signer.substring(signer.indexOf("C=")+2,signer.length()));
			signer_User.setVflag(1);
			
			String issuer=signerCert.getIssuerDN().getName();
			signer_User.setIssuer(issuer.substring(issuer.indexOf("CN=")+3,issuer.indexOf("OU=")-2));
			System.out.println("UID is::"+getUniqueIdentifierFromCertificate(signerCert));
			return signer_User;
			} 
			
			else {
			System.out.println("verification failed");
			signer_User.setVflag(0);
			return signer_User;
			}
			} else {
			System.out.println("hash verification failed");
			return new Signer_User();
			}

			}
			}

			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			
			
			
			return new Signer_User();
			}


			public String getUniqueIdentifierFromCertificate(
			X509Certificate certFromSignedData)
			throws CertificateEncodingException, IOException {
			X500Name x500name = new JcaX509CertificateHolder(certFromSignedData)
			.getSubject();
			RDN cn1 = x500name.getRDNs(BCStyle.UNIQUE_IDENTIFIER)[0];
			DERBitString derBitString = (DERBitString) cn1.getFirst().getValue();
			byte[] bitstringBytes=derBitString.getBytes();
			String unique_identifier= new String(bitstringBytes);
			return unique_identifier;

			}
		
			
			public String elementToString(Element element) throws Exception {
			        TransformerFactory transformerFactory = TransformerFactory.newInstance();
			        Transformer transformer = transformerFactory.newTransformer();
			        StringWriter writer = new StringWriter();
			        transformer.transform(new DOMSource(element), new StreamResult(writer));
			        String result= writer.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?><DocSignature error=\"NA\" id=\"1\" sigHashAlgorithm=\"SHA256\">", "");
			        return result.replace("</DocSignature>", "");
			    }

			public void getDNQualifierFromCertificate(X509Certificate certFromSignedData)
					throws CertificateEncodingException {
					X500Name x500name = new JcaX509CertificateHolder(certFromSignedData)
					.getSubject();
					RDN cn1 = x500name.getRDNs(BCStyle.DN_QUALIFIER)[0];
					String DNQualifier = IETFUtils.valueToString(cn1.getFirst().getValue());
					System.out.println("DNQualifier ::: " + DNQualifier);

					// System.out.println(IETFUtils.valueToString(cn.getFirst().getValue()));
					// System.out.println(IETFUtils.valueToString(cn1.getFirst().getValue())+" length:: "+IETFUtils.valueToString(cn1.getFirst().getValue()).length());
					}
			
			public boolean varifyResponse(String dsc) throws Exception {
				boolean isValid=false;
				XMLSignature signature=null;
				
		        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		        // Set the namespaceAware property of the DocumentBuilderFactory object to true.
		        documentBuilderFactory.setNamespaceAware(true);

		        // Create a new DocumentBuilder object using the DocumentBuilderFactory object.
		        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

		        // Use the DocumentBuilder object to parse the XML file.
		        Document document = documentBuilder.parse(new InputSource(new StringReader(dsc)));
		        NodeList nl=document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		        if (nl.getLength() == 0) {
		        	throw new Exception("No XML Digital Signature Found, document is discarded");
		        	}
		        
		        DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));
		        valContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);
		        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		        try {
		         signature = fac.unmarshalXMLSignature(valContext);
		         isValid=signature.validate(valContext);
		        }catch (Exception e) {
					isValid=false;
				}
		        System.out.println(isValid);
		        valContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.TRUE);
		        return isValid;
			}
			
			private Element getElement(String resXml,String tag)throws Exception {
				DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder=factory.newDocumentBuilder();
				Document doc=builder.parse(new InputSource(new StringReader(resXml)));
				
				NodeList signatureNodes = doc.getElementsByTagNameNS("",tag);
				if (signatureNodes.getLength() == 0) {
					throw new Exception("No EsignResp found in the XML document.");
				}
				 
				return (Element) signatureNodes.item(0);
			}
			
			
			public String getErrors(String dsc)throws Exception {
				Element element=getElement(dsc,"EsignResp");
				
				if(!element.getAttribute("errCode").equalsIgnoreCase("NA")) {
					return element.getAttribute("errCode")+": "+element.getAttribute("errMsg");	
				}
				return null;
			}		
			
			public String getTxn(String dsc) throws Exception {
				Element element=getElement(dsc,"EsignResp");
				return element.getAttribute("txn");
			}
}
