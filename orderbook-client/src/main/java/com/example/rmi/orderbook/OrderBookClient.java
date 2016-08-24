package com.example.rmi.orderbook;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.example.rmi.orderbook.client.OrderBookClientHandlerException;
import com.example.rmi.orderbook.server.OrderBookService;
import com.example.rmi.orderbook.util.Analyzer;

public class OrderBookClient {
	private static List<OrderBookClientHandleImpl> handlers;

	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException,
	NoSuchObjectException {
		handlers = new LinkedList<>();
		try{
			Analyzer auxi = new Analyzer(args);
			Object port = auxi.get("PORT");
			Object hostname = auxi.get("HOSTNAME");
			Object service = auxi.get("SERVICE");
			auxi.dump();

			OrderBookService serverHandle = (OrderBookService) Naming.lookup(String.format("//%s:%s/%s",
                    hostname, port, service));
			
			testListOrders(serverHandle,3);
			
		} finally {
			for (OrderBookClientHandleImpl handler : handlers) {
				// Unexport any remaining client callbacks if the server dies.
				handler.unexport();
			}
		}


	}
	
	private static void testListOrders(OrderBookService serverHandle, int expectedOrdersAmount) throws RemoteException {
        final Set<Order> orders = serverHandle.listOrders();
        if (expectedOrdersAmount != orders.size()) {
            throw new OrderBookClientHandlerException("Active orders should be " + expectedOrdersAmount);
        }

        for (Order order : orders) {
            System.out.println(order);
        }
    }

}
