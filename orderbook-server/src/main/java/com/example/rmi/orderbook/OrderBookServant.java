package com.example.rmi.orderbook;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.example.rmi.orderbook.client.OrderBookClientHandle;
import com.example.rmi.orderbook.server.OrderBookService;

public class OrderBookServant implements OrderBookService{
	private final List<Order> orders;
	
	public OrderBookServant(final String[][] inititalOrders)
            throws RemoteException {
        this.orders = new LinkedList<Order>();
        for (final String[] order : inititalOrders) {
            orders.add(new Order(order));
        }
        UnicastRemoteObject.exportObject(this, 0);
    }

	@Override
	public Set<Order> listOrders() throws RemoteException {
		return new HashSet<Order>(orders);
	}

	@Override
	public void bookOrder(String securityId, int value,
			OrderBookClientHandle clientHandler) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

}
