package com.cdac.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "EsignResp")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@AllArgsConstructor
public class EsignResp {
	@XmlAttribute
	private String ver;
	@XmlAttribute
	private String txn;
	@XmlAttribute
	private String ts;
	@XmlAttribute
	private String status;
	@XmlAttribute
	private String resCode;
	@XmlAttribute(name = "Error" )
	private String error;
	
		
	@XmlElement
	private String UserX509Certificate;

	@XmlElement(name = "Signatures")
	private Signatures signatures;
	
	@XmlElement
	private Signature signature;
}
