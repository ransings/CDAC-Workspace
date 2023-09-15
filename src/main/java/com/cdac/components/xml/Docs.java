package com.cdac.components.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Docs")
@XmlAccessorType(XmlAccessType.FIELD)
public class Docs {

	@XmlElement(name = "InputHash")
	InputHash iHash;

	public InputHash getiHash() {
		return iHash;
	}

	public void setiHash(InputHash iHash) {
		this.iHash = iHash;
	}

}
