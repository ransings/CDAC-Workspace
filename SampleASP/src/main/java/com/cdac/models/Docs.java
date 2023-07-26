package com.cdac.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "Docs")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Docs {
	
	@XmlElement(name = "InputHash")
	InputHash iHash;
	
}
