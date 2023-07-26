package com.cdac.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class DocSignature {
	@XmlAttribute(name = "id")
	private String id;
	@XmlAttribute(name = "error")
	private String error;
	@XmlAttribute(name = "sigHashAlgorithm")
	private String sigHashAlgorithm;
	
	@XmlValue
	private String dsc;
		

}
