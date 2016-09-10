package com.example.orderbook.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class Analyzer {
	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat); 
    private HashMap<String, Object> p = new HashMap<String, Object>();

    public Object get(String key) {
        return p.get(key);
    }

    public void dump() {
        System.out.println("Using: ");
        for (String key : p.keySet()) {
            System.out.println(String.format("%s=%s", key, p.get(key)));
        }
    }

    public Analyzer(String[] args) {
        // default
        p.put("HOSTNAME", "localhost");
        p.put("PORT", 1099);
        p.put("SERVICE", "OrderBookService");
        p.put("START",new SimpleDateFormat(dateFormat).format(new Date()));
        p.put("END", "never");
        
        for (int i = 0; i < args.length; i++) {
            String parameter = args[i];
            Scanner scanner = new Scanner(parameter);
            scanner.useDelimiter("=");
            String vble = scanner.next();
            if (!scanner.hasNext()) {
                System.err.println(String.format("Parameter %s is incorrect", vble));
                continue;
            }
            String value = scanner.next();

            if (vble.trim().equalsIgnoreCase("PORT")) {
                try {
                    int port = Integer.parseInt(value);
                    p.put("PORT", port);
                } catch (NumberFormatException e) {
                    System.err.println(String.format(
                            "Ignoring the incorrect parameter. %s is not a valid port number", value));
                }
            } else {
                p.put(vble.toUpperCase(), value);
            }
            scanner.close();
        }
    }
    
    /**
     * Parses a time stamp following {@link #dateFormat}.
     * @param dateString
     * 			a string formated timestamp
     * @return a date
     * @throws IllegalArgumentException if the date format doesn't match
     */
    public static Date parseTimeStamp(String dateString){
		Date date = null;
		try {
			date = sdf.parse(dateString);
		} catch (ParseException e) {
			System.err.println("Date must be formatted in this way: " + dateFormat);
			new IllegalArgumentException();
		}
		return date;

	}
    
    /**
     * Converts milliseconds from 1970 to a formated time stamp.
     * @param milliseconds
     * @return A time stamp following {@link #dateFormat}
     */
    public static String milliSecondsToTimestamp(long milliseconds) {
        return sdf.format(new Date(milliseconds));
    }
}
