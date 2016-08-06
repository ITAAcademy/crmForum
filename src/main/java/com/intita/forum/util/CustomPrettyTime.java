package com.intita.forum.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

public class CustomPrettyTime extends PrettyTime {
	public CustomPrettyTime(Locale loc) {
		super(loc);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String format(Date then) {
		// TODO Auto-generated method stub
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date()); 
		c.add(Calendar.DAY_OF_WEEK, -1);	
		Date d= c.getTime();
		if(c.getTime().before(then))
			return super.format(then);
		
		SimpleDateFormat format = new SimpleDateFormat();
		return format.format(then);
	}
}
