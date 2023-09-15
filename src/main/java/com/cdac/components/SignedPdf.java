package com.cdac.components;

import java.io.File;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.itextpdf.text.pdf.PdfSignatureAppearance;

import lombok.Data;

@Data
@Component
public class SignedPdf {
    private String responseXml;
    private String txn;
    private String documentHash;

}
