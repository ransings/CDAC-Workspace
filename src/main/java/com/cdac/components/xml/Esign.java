package com.cdac.components.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Esign")
@XmlAccessorType(XmlAccessType.FIELD)
public class Esign {
	@XmlAttribute
	String ver = "2.1";

	@XmlAttribute
	String sc = "Y";

	@XmlAttribute
	String ts="";

	@XmlAttribute
	String txn="";

	@XmlAttribute
	String ekycId="";

	@XmlAttribute
	String ekycIdType = "A";

	@XmlAttribute
	String aspId = "TRYI-900";

	@XmlAttribute(name = "AuthMode")
	String AuthMode = "1";

	@XmlAttribute
	String responseSigType = "pkcs7";

	@XmlAttribute
	String responseUrl = "http://10.208.53.47:8082/app/";
	@XmlElement(name = "Docs") 
	Docs doc;

	public Docs getDoc() {
		return doc;
	}

	public String getVer() {
		return ver;
	}

	public String getSc() {
		return sc;
	}

	public String getTs() {
		return ts;
	}

	public String getTxn() {
		return txn;
	}

	public String getEkycId() {
		return ekycId;
	}

	public String getEkycIdType() {
		return ekycIdType;
	}

	public String getAspId() {
		return aspId;
	}

	public String getAuthMode() {
		return AuthMode;
	}

	public String getResponseSigType() {
		return responseSigType;
	}

	public String getResponseUrl() {
		return responseUrl;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}

	public void setSc(String sc) {
		this.sc = sc;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public void setTxn(String txn) {
		this.txn = txn;
	}

	public void setEkycId(String ekycId) {
		this.ekycId = ekycId;
	}

	public void setEkycIdType(String ekycIdType) {
		this.ekycIdType = ekycIdType;
	}

	public void setAspId(String aspId) {
		this.aspId = aspId;
	}

	public void setAuthMode(String authMode) {
		AuthMode = authMode;
	}

	public void setResponseSigType(String responseSigType) {
		this.responseSigType = responseSigType;
	}

	public void setResponseUrl(String responseUrl) {
		this.responseUrl = responseUrl;
	}

	public void setDoc(Docs doc) {
		this.doc = doc;
	}

}
