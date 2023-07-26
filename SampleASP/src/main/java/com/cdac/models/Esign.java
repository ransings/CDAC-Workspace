package com.cdac.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;

@XmlRootElement(name = "Esign")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Esign {
	@XmlAttribute
	private String ver = "3.0";
	
	@XmlAttribute
	private String maxWaitPeriod="1440";
	
	@XmlAttribute
	private String redirectUrl="";
	
	@XmlAttribute
	private String responseUrl="";
	
	@XmlAttribute
	private String signerid="";
	
	@XmlAttribute
	private String signingAlgorithm="ECDSA";
	
	@XmlAttribute
	private String aspId = "TRYI-900";
	
	@XmlAttribute
	private String ts="";

	@XmlAttribute
	private String txn="";

	@XmlElement(name = "Docs") 
	private Docs doc;


}
