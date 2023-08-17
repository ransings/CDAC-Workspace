package com.cdac.esign.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@XmlRootElement
@Data
public class ResponseAck {
	@XmlAttribute
	private String status;
	@XmlAttribute
	private String txn;
	@XmlAttribute
	private String ts;
	@XmlAttribute
	private String errCode;
	@XmlAttribute
	private String errMsg;
}
