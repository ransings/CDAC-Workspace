package com.cdac.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "EsignResp")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "esignResp",propOrder = {"userX509Certificate","signatures"})
@Data
public class EsignResp {
	@XmlElement(name ="UserX509Certificate" )
	private String userX509Certificate;

	@XmlElement(name = "Signatures")
	private Signatures signatures;
	
	@XmlAttribute(name ="ver" ,required = true)
	private String ver;
	@XmlAttribute(name ="txn",required = true )
	private String txn;
	@XmlAttribute(name = "ts",required = true)
	private String ts;
	@XmlAttribute(name = "status",required = true)
	private String status;
	@XmlAttribute(name = "resCode",required = true)
	private String resCode;
	@XmlAttribute(name = "Error",required = true )
	private String error;
	
		
}
