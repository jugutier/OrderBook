package com.example.rmi.orderbook.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.example.rmi.orderbook.OrderBookServant;
import com.example.rmi.orderbook.util.Analyzer;

public class OrderBookServer {
	//Format: Security id, amount, buying?
	private static final String[][] stubbedOrders = new String[][] {
        new String[] { "AAPL", "100", "yes", },
        new String[] { "BAC", "2500", "no" },
        new String[] { "JPM", "300", "yes" } };
	
	public static void main(final String[] args) throws RemoteException {

        final Analyzer auxi = new Analyzer(args);
        final int port = Integer.valueOf(auxi.get("PORT").toString());
        final String hostname = auxi.get("HOSTNAME").toString();
        final String service = auxi.get("SERVICE").toString();
        auxi.dump();
        
        final Registry registry = LocateRegistry.getRegistry(hostname, port);
        final OrderBookService stub = new OrderBookServant(stubbedOrders);

        registry.rebind(service, stub);
        System.out.println("Service bound");
    }
}
