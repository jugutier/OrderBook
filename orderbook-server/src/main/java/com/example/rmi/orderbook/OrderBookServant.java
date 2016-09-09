package com.example.rmi.orderbook;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.example.rmi.orderbook.client.OrderBookClientHandle;
import com.example.rmi.orderbook.server.OrderBookService;

public class OrderBookServant implements OrderBookService{
	private final PriorityOrderBook orders;

	public OrderBookServant() throws RemoteException{
		System.out.println("Servant init");
		this.orders = new PriorityOrderBook();
		UnicastRemoteObject.exportObject(this, 0);
	}

	@Override
	public List<Order> listOrders() throws RemoteException {
		return orders.getAllOrders();
	}

	@Override
	public void bookOrder(String clientId, String securityId, Integer amount,
			Double value, boolean isBuying,	OrderBookClientHandle clientHandler) 
					throws RemoteException {
		Order bookedOrder = new Order(clientId, securityId, amount, value,
				isBuying , System.currentTimeMillis(), clientHandler);
		System.out.println("Booking...");
		if(isBuying){
			orders.buy(bookedOrder);
		}else{
			orders.sell(bookedOrder);
		}
		System.out.println("Booked: "+ bookedOrder );


	}

	@Override
	public void updateOrder(Long orderId, String clientId, String securityId,
			Integer amount, Double value, boolean isBuying,
			OrderBookClientHandle clientHandler) throws RemoteException {
		// remove and add the order id again.
		
		System.out.println("Updating...");
		Order orderToUpdate = new Order(orderId, clientId, securityId, amount, value,
				isBuying , System.currentTimeMillis(), clientHandler);
		
		orders.update(orderToUpdate);
		System.out.println("Updated: "+ orderToUpdate );

	}

	@Override
	public void clientExits(String clientId) {
		System.out.println("Client "+ clientId +" has exited. We remove his orders.");
		orders.remove(clientId);		
	}

	/**
	 * Notifies all pending orders as cancelled and shuts down the service
	 * Note that this method is not part of the interface because 
	 * clients don't need to know about it.
	 */
	public void finishSession(){
		System.out.println("Finishing: "+ orders.toString());
		List<Order> allOrders = orders.getAllOrders();
		for (Order order : allOrders) {
			try {
				order.getClientHandle().notifyOrderCancelled(order.getSecurityId());
			} catch (RemoteException e) {
				System.out.println("Attempted to notify a client that has probably disconnected");
			}
		}		
		orders.clear();		
	}



}
