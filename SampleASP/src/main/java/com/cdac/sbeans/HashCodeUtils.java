package com.cdac.sbeans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component("utils")
public class HashCodeUtils {
	
	public String getTimestamp() {
		Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS");  
        return dateFormat.format(date); 
	}
	
	public String generateTxn() {
		return UUID.randomUUID().toString();
	}
	

}
