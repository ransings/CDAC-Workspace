package com.cdac.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.Data;

@XmlRootElement(name = "InputHash")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class InputHash {

	@XmlAttribute
	private String id="1";
	
	@XmlAttribute
	private String hashAlgorithm="SHA256";
	
	@XmlAttribute
	private String docInfo;
	
	@XmlAttribute
	private String responseSigType="pkcs7";
	
	@XmlAttribute
	private String docUrl="";
	
	@XmlValue
	private String hashCode;
		
	

}
