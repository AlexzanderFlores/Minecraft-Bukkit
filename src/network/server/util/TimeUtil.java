package network.server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	public static String getTime() {
		return new SimpleDateFormat("yyyy/MM/dd/HH:mm").format(new Date()) + " EST";
	}
	
	public static String addDate(int hours) {
		return addDate(null, hours);
	}
	
	public static String addDate(String time, int hours) {
		SimpleDateFormat simpleDateFormat = null;
		if(time == null) {
			simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
		} else {
			simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
			try {
				Date date = simpleDateFormat.parse(time);
				simpleDateFormat.format(date);
			} catch(ParseException e) {
				e.printStackTrace();
			}
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.HOUR, hours);
		return simpleDateFormat.format(calendar.getTime()) + " EST";
	}
	
	public static String addDate(int days, int hours) {
		return addDate(null, days, hours);
	}
	
	public static String addDate(String time, int days, int hours) {
		SimpleDateFormat simpleDateFormat = null;
		if(time == null) {
			simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
		} else {
			simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
			try {
				Date date = simpleDateFormat.parse(time);
				simpleDateFormat.format(date);
			} catch(ParseException e) {
				e.printStackTrace();
			}
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, days);
		calendar.add(Calendar.HOUR, hours);
		return simpleDateFormat.format(calendar.getTime()) + " EST";
	}
}
