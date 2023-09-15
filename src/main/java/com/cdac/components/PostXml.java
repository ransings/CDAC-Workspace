package com.cdac.components;

import org.springframework.stereotype.Component;

import lombok.Data;


@Data
public class PostXml {
	private String url="https://es-staging.cdac.in/esignlevel2/2.1/form/signdoc";
	private String eSignRequest;
	private String aspTxnID;
	private String Content_Type="application/xml";
}
