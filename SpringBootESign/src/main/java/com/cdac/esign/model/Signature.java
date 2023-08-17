package com.cdac.esign.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Signature {
	@XmlValue
	private String value;

}
