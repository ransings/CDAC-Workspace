package com.cdac.esign.form;



/*
<Esign ver ="" sc ="" ts="" txn="" ekycId="" ekycIdType="" aspId=""
AuthMode="" responseSigType="" responseUrl="">
<Docs>
<InputHash id=""
hashAlgorithm="" docInfo="">Document
Hash in Hex</InputHash>
</Docs>
<Signature>Digital signature of ASP</Signature>
</Esign>
*/
public class FormXmlDataAsp {
	public String ver;
	public String maxWaitPeriod;
	public String ts;
	public String txn;
	public String signerid;
	public String signingAlgorithm;
	public String aspId;
	public String redirectUrl;
	public String responseSigType;
	public String responseUrl;
	public String id;
	public String hashAlgorithm;
	public String docInfo;
	public String docUrl;
	public String docHashHex;
	public String digSigAsp;
	public String txnRef;
	
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
	}
	public String getTs() {
		return ts;
	}
	public void setTs(String ts) {
		this.ts = ts;
	}
	public String getTxn() {
		return txn;
	}
	public void setTxn(String txn) {
		this.txn = txn;
	}
	public String getAspId() {
		return aspId;
	}
	public void setAspId(String aspId) {
		this.aspId = aspId;
	}
	public String getResponseSigType() {
		return responseSigType;
	}
	public void setResponseSigType(String responseSigType) {
		this.responseSigType = responseSigType;
	}
	public String getResponseUrl() {
		return responseUrl;
	}
	public void setResponseUrl(String responseUrl) {
		this.responseUrl = responseUrl;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}
	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}
	public String getDocInfo() {
		return docInfo;
	}
	public void setDocInfo(String docInfo) {
		this.docInfo = docInfo;
	}
	public String getDocHashHex() {
		return docHashHex;
	}
	public void setDocHashHex(String docHashHex) {
		this.docHashHex = docHashHex;
	}
	public String getDigSigAsp() {
		return digSigAsp;
	}
	public void setDigSigAsp(String digSigAsp) {
		this.digSigAsp = digSigAsp;
	}
	public String getMaxWaitPeriod() {
		return maxWaitPeriod;
	}
	public void setMaxWaitPeriod(String maxWaitPeriod) {
		this.maxWaitPeriod = maxWaitPeriod;
	}
	public String getSignerid() {
		return signerid;
	}
	public void setSignerid(String signerid) {
		this.signerid = signerid;
	}
	public String getSigningAlgorithm() {
		return signingAlgorithm;
	}
	public void setSigningAlgorithm(String signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public String getDocUrl() {
		return docUrl;
	}
	public void setDocUrl(String docUrl) {
		this.docUrl = docUrl;
	}
	public String getTxnRef() {
		return txnRef;
	}
	public void setTxnRef(String txnRef) {
		this.txnRef = txnRef;
	}
}
