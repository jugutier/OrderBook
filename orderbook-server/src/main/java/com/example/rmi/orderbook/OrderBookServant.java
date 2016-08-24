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
	private final List<OrderBookClientHandle> handles;
	
	public OrderBookServant() throws RemoteException{
		System.out.println("Servant init");
        this.orders = new LinkedList<>();
        this.handles = new LinkedList<>();
        UnicastRemoteObject.exportObject(this, 0);
	}
	
//	public OrderBookServant(final String[][] inititalOrders)
//            throws RemoteException {
//        for (final String[] orderString : inititalOrders) {
//        	String securityId = orderString[0];
//    		Double value = Double.valueOf(orderString[1]);
//    		boolean isBuying = orderString[2].equals("yes");
//            orders.add(new Order(securityId, value, isBuying));
//        }
//        
//    }

	@Override
	public Set<Order> listOrders() throws RemoteException {
		return new HashSet<Order>(orders);
	}

	@Override
	public void bookOrder(String clientId, String securityId, Integer amount, Double value, boolean isBuying,
			OrderBookClientHandle clientHandler) throws RemoteException {
		Order bookedOrder = new Order(clientId, securityId, amount, value, isBuying);
		orders.add(bookedOrder);
		handles.add(clientHandler);
		System.out.println("Booked: "+ bookedOrder );

		
	}
	
	/**
	 * Notifies all pending orders as cancelled and shuts down the service
	 * Note that this method is not part of the interface because 
	 * clients don't need to know about it.
	 */
	public void finishSession(){
		System.out.println("Finishing "+orders.size() +" orders");
		for (int i = 0; i < orders.size(); i++) {
			try {
				handles.get(i).notifyOrderCancelled(orders.get(i).getSecurityId());
			} catch (RemoteException e) {
				System.out.println("Attempted to notify a client that has probably disconnected");
			}
		}
		
		orders.clear();
		handles.clear();
		
	}

}
