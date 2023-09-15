
package com.cdac.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component("keyLoader")
public class KeyLoader {
	@Value("${key.public}")
	private String publicCert;
	@Value("${key.private}")
	private String privateCert;
	private PrivateKey privateKey;
	private PublicKey publicKey;

	@PostConstruct
	private void setter() {
		try {
			Security.addProvider((Provider) new BouncyCastleProvider());
			this.setPrivateKey();
			this.setPublicKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public void setPublicKey() throws Exception {
//		File file = new File(publicCert);	
		ClassPathResource classPathResource = new ClassPathResource(publicCert);
		
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate cert = cf.generateCertificate(classPathResource.getInputStream());
		PublicKey publicKey = cert.getPublicKey();

		this.publicKey= publicKey;
	}
	
	

	public void setPrivateKey() throws Exception {
		ClassPathResource resource=new ClassPathResource(privateCert);
		StringReader reader=new StringReader(new String(resource.getContentAsByteArray()));
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PemReader pemReader = new PemReader((Reader)reader);
		PemObject pemObject = pemReader.readPemObject();
		byte[] content = pemObject.getContent();
		PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
		this.privateKey = factory.generatePrivate(privKeySpec);
	}
}