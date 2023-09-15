package com.cdac.model;

import lombok.Data;

@Data
public class Signer_User {

	private String name;
	private String location;
	private int vflag=0;
	private String cert_Cont;
	private String issuer;
}
