package com.example.rmi.orderbook.server;

import java.rmi.RemoteException;

import com.example.rmi.orderbook.util.Analyzer;

public class OrderBookServer {
	public static void main(final String[] args) throws RemoteException {

        final Analyzer auxi = new Analyzer(args);
        final int port = Integer.valueOf(auxi.get("PORT").toString());
        final String hostname = auxi.get("HOSTNAME").toString();
        final String service = auxi.get("SERVICE").toString();
        
        System.out.println(String.format("//%s:%s/%s",
                hostname, port, service));
    }
}
