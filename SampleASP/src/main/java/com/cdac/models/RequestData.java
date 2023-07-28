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
	//for displaying file on e-hastakshar page(endpoint with file name)
	private String filePath;
	private String serverFile;

}
