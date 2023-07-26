package com.cdac.models;

import com.itextpdf.text.pdf.PdfSignatureAppearance;

import lombok.Data;

@Data
public class RequestData {
	private String userName;
	private String txn;
	private String hash;
	private String txnref;
	private PdfSignatureAppearance appearance;
	private String filePath;

}
