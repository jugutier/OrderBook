package com.example.rmi.orderbook.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class Analyzer {
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
        p.put("START",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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
}
