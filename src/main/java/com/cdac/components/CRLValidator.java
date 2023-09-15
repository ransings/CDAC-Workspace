package com.cdac.components;

import java.io.BufferedInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.security.Security;
import java.util.Collection;

import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import java.security.SecureRandom;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.springframework.stereotype.Component;

@Component
public class CRLValidator {
	private  HashMap<String, X509CRL> crl = new HashMap<String, X509CRL>();
	private  HashMap<String, Long> lastModified = new HashMap<String, Long>();
	
	private X509Certificate getX509Certificate(String pkcs7Response) {
		SignerInformation siinfo = null;
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
					Store cs = cms.getCertificates();

					Collection certCollection = cs.getMatches(siinfo.getSID());
					X509CertificateHolder cert = (X509CertificateHolder) certCollection
							.iterator().next();
					X509Certificate certFromSignedData = new JcaX509CertificateConverter()
							.setProvider(new BouncyCastleProvider())
							.getCertificate(cert);
					return certFromSignedData;
				}
			}

		} catch (CMSException e) {
			e.printStackTrace();
		} catch (CertificateException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private String getCRLURLFromPKCS7(X509Certificate certificate) {
		String crlUrl = null;
		try {
			byte[] crlDistributionPointDerEncodedArray = certificate
					.getExtensionValue(Extension.cRLDistributionPoints.getId());

			ASN1InputStream oAsnInStream = new ASN1InputStream(
					new ByteArrayInputStream(
							crlDistributionPointDerEncodedArray));
			ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
			DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;

			oAsnInStream.close();

			byte[] crldpExtOctets = dosCrlDP.getOctets();
			ASN1InputStream oAsnInStream2 = new ASN1InputStream(
					new ByteArrayInputStream(crldpExtOctets));
			ASN1Primitive derObj2 = oAsnInStream2.readObject();
			CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);

			oAsnInStream2.close();

			List<String> crlUrls = new ArrayList<String>();
			for (DistributionPoint dp : distPoint.getDistributionPoints()) {
				DistributionPointName dpn = dp.getDistributionPoint();
				// Look for URIs in fullName
				if (dpn != null) {
					if (dpn.getType() == DistributionPointName.FULL_NAME) {
						GeneralName[] genNames = GeneralNames.getInstance(
								dpn.getName()).getNames();
						// Look for an URI
						for (int j = 0; j < genNames.length; j++) {
							if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
								String url = DERIA5String.getInstance(
										genNames[j].getName()).getString();
								crlUrls.add(url);
							}
						}
					}
				}
			}

			for (String url : crlUrls) {
				crlUrl = url;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return crlUrl;
	}
	
	private class DefaultTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
	
	private X509CRL getCRL(String crlURL) throws MalformedURLException,
	IOException, CRLException, CertificateException {

HttpsURLConnection connection = null;

try {
	/* Start : Code for SSL certificate verfication */
	SSLContext ctx = SSLContext.getInstance("TLS");
	ctx.init(new KeyManager[0],
			new TrustManager[] { new DefaultTrustManager() },
			new SecureRandom());
	SSLContext.setDefault(ctx);
	/* End */
	URL url = new URL(crlURL);

	connection = (HttpsURLConnection) url.openConnection();

	connection.setDoInput(true);
	connection.setConnectTimeout(5000);
	/* Start: Code for SSL certificate verfication */
	connection.setHostnameVerifier(new HostnameVerifier() {
		@Override
		public boolean verify(String arg0, SSLSession arg1) {
			return true;
		}
		/* End */
	});

	long now = connection.getLastModified();

	if (lastModified.get(crlURL) == null
			|| now != lastModified.get(crlURL)) {
		try {
			InputStream is = connection.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			synchronized (crl) {
				synchronized (lastModified) {
					crl.put(crlURL, (X509CRL) CertificateFactory
							.getInstance("X.509").generateCRL(bis));
					lastModified.put(crlURL, now);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
} catch (Exception e) {
	e.printStackTrace();
}
return crl.get(crlURL);
}
	
	private boolean getCertificateStatus(String crlURL,
			X509Certificate certificate) {
		X509CRL crl = null;
		boolean isRevoked=true;
		try {
			crl = getCRL(crlURL);

			if (crl != null) {
				if (crl.isRevoked(certificate)) {
					isRevoked=false;
					 System.out.println("Certificate is revoked");
				} else {
					isRevoked=true;
					System.out.println("CRL is valid");
				}
			}
		} catch (Exception e) {
			System.out.println("Can not download CRL from " + crlURL
					+ ": " + e.getLocalizedMessage());
		}

		return isRevoked;
	}

	public boolean isCrlValid(String pkcs7response) {
		X509Certificate certificate=getX509Certificate(pkcs7response);
		String crlUrl=getCRLURLFromPKCS7(certificate);
		return getCertificateStatus(crlUrl, certificate);
	}

}
