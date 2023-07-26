package com.cdac;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@SpringBootApplication
public class SampleAspApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleAspApplication.class, args);
	}

	
	@Bean
	public RestTemplate getTemplate() throws Exception {
		HttpComponentsClientHttpRequestFactory requestFactory = null;
        ClassPathResource resource = new ClassPathResource("eSignInterface.crt");
        try (InputStream inputStream = resource.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(inputStream);)
        {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            while (bis.available() > 0) {
                Certificate cert = cf.generateCertificate(bis);
                trustStore.setCertificateEntry("eSign" + bis.available(), cert);
            }
            bis.close();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            context.init(null, trustManagers, null);

            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);

            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

            requestFactory = new HttpComponentsClientHttpRequestFactory();

            requestFactory.setHttpClient(httpClient);   
           
            return new RestTemplate(requestFactory);
    }
	}
	
	
}
