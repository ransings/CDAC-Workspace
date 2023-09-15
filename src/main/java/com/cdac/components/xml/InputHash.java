package com.cdac.components.xml;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "InputHash")
//@XmlType(propOrder = {"id","hashAlgorithm","docInfo"})
@XmlAccessorType(XmlAccessType.FIELD)
public class InputHash {

	@XmlAttribute
	private String id="1";
	
	@XmlAttribute
	String hashAlgorithm="SHA256";
	
	@XmlAttribute
	private String docInfo;
	
	@XmlValue
	private String hashCode;
	
	
	public String getHashCode() {
		return hashCode;
	}
	public String getId() {
		return id;
	}
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}
	public String getDocInfo() {
		return docInfo;
	}
	
	
	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}
	
	
	public void setId(String id) {
		this.id = id;
	}
	
	
	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}
	
	
	public void setDocInfo(String docInfo) {
		this.docInfo = docInfo;
	}
	
	
	

}
