package net.sidland.apesay.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtils {

	private static Logger logger = Logger.getLogger(DateUtils.class);

	/**
	 * yyyy-MM-dd
	 */
	public static SimpleDateFormat SDF1 = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * yyyy/MM/dd
	 */
	public static SimpleDateFormat SDF2 = new SimpleDateFormat("yyyy/MM/dd");
	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
	public static SimpleDateFormat SDF3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * yyMMddHHmmss
	 */
	public static SimpleDateFormat SDF4 = new SimpleDateFormat("yyMMddmmss");

	/**
	 * 仅比较年月日 相等
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static boolean isSameDate(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			logger.debug("isSameDate : 参数有空值，直接返回false");
			return false;
		}
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);

		Calendar c2 = Calendar.getInstance();
		c1.setTime(d2);

		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
				&& c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
				&& c1.get(Calendar.DATE) == c2.get(Calendar.DATE);

	}
	
	/**
	 * 
	 * getDateAddMinutes:(获取指定时间之后多少分钟的时间). 
	 *
	 * @author sid
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date getDateAddMinutes(Date date,int minutes){
		
		Calendar cl = Calendar.getInstance();
		cl.setTime(date);
		cl.add(Calendar.MINUTE	, minutes);
		
		return cl.getTime();
	}

	/**
	 * 保留日期 ，把时间设置为 0 <br>
	 * HOUR_OF_DAY<br>
	 * MINUTE<br>
	 * SECOND<br>
	 * MILLISECOND<br>
	 * 
	 * @param d
	 * @return
	 */
	public static Date clearTime(Date d) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(d);
		ca.set(Calendar.HOUR_OF_DAY, 0);
		ca.set(Calendar.MINUTE, 0);
		ca.set(Calendar.SECOND, 0);
		ca.set(Calendar.MILLISECOND, 0);
		return ca.getTime();
	}

	/**
	 * 增加天数(负值为减)
	 * 
	 * @param d
	 * @param dayToAdd
	 * @return
	 */
	public static Date addDay(Date d, int dayToAdd) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(d);
		ca.add(Calendar.DAY_OF_MONTH, dayToAdd);
		return ca.getTime();
	}

	/**
	 * 是否为"今天"
	 * 
	 * @param d
	 * @return
	 */
	public static boolean isToday(Date d) {
		return isSameDate(d, new Date());
	}

	/**
	 * 日期转字符串
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		if (date == null) {
			return "";
		}
		if (format == null) {
			format = "yyyy-MM-dd hh:mm:ss";
		}
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}

	/**
	 * 日期转字符串
	 *  12小时制
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateToString(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return df.format(date);
	}
	
	/**
	 * add by Bill
	 * 2011-07-07
	 *  格式化为yyyyMMddHHmmss的形式
	 * @param datestr
	 * @return
	 */
	public static String dateToString(){
		return SDF4.format(new Date());
	}
	
	/**
	 *  add by Bill
	 * 日期转字符串
	 *  24小时制
	 * @param date
	 * @param format
	 * @return
	 */
	public static String date24ToString(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);
	}

	/**
	 * 日期转时间戳
	 * 
	 * @param date
	 * @return
	 */
	public static long dateToTimeMillis(Date date) {
		if (date == null) {
			return 0;
		}
		return date.getTime() / 1000;
	}
	
	/**
	 * add by Bill
	 * 2011-07-07
	 * @param datestr
	 * @return
	 */
	public static Date StringToDate(String datestr){
		Date dt=null;
		if(datestr==null || "".equals(datestr)){
			dt = new Date();
		}
		try {
			dt = SDF3.parse(datestr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dt;
	}
	

	public static boolean getIsSaleBegin() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 0);
		Date date = DateUtils.clearTime(calendar.getTime());
		try {
			if (date.compareTo(DateUtils.SDF3.parse("2012-10-24 00:00:00")) == 0) {
				return true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String [] args){
		//System.out.println(clearTime(new Date()));
		
		//System.out.println(StringToDate(dateToString(new Date())));
		//System.out.println(date24ToString(new Date()));
		
		System.out.println(dateToString());
		
		try {
			System.out.println("getIsSaleBegin=="+getIsSaleBegin());

			System.out.println(DateUtils.dateToTimeMillis(new Date())> DateUtils.dateToTimeMillis(DateUtils.SDF3.parse("2012-08-1 23:59:59")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} 
}
