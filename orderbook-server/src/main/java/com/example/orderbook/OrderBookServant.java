package com.example.orderbook;

import java.rmi.RemoteException;
import java.util.List;

import com.example.orderbook.client.Response;
import com.example.orderbook.server.OrderBookService;
import com.example.orderbook.server.Request;

public class OrderBookServant implements OrderBookService{
	private final PriorityOrderBook orders;

	public OrderBookServant() throws RemoteException{
		System.out.println("Servant init");
		this.orders = new PriorityOrderBook();
	}

	@Override
	public void listOrders(){
		System.out.println("=== Debug: Server state - All current orders ===");
		final List<Order> orders =  this.orders.getAllOrders();
		for (Order order : orders) {
			System.out.println(order);
		}
		System.out.println("=== END === ");
	}

	@Override
	public Response bookOrder(String clientId, String securityId, Integer amount,
			Double value, boolean isBuying) 
					{
		Response response = new Response();
		Order bookedOrder = new Order(clientId, securityId, amount, value,
				isBuying, System.currentTimeMillis(), response);
		System.out.println("Booking...");
		Double retVal;
		if(isBuying){
			retVal = orders.buy(bookedOrder);
		}else{
			retVal = orders.sell(bookedOrder);
		}
		response.setValue(retVal);
		System.out.println(bookedOrder);
		return response;


	}

	@Override
	public Response updateOrder(Long orderId, String clientId, String securityId,
			Integer amount, Double value, boolean isBuying)  {
		
		System.out.println("Updating...");
		Response response = new Response();
		Order orderToUpdate = new Order(orderId, clientId, securityId, amount, value,
				isBuying ,  System.currentTimeMillis(),  new Response());
		
		Double retVal = orders.update(orderToUpdate);
		response.setValue(retVal);
		System.out.println(orderToUpdate);
		return response;

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
			} catch (Exception e) {
				System.out.println("Attempted to notify a client that has probably disconnected");
			}
		}		
		orders.clear();		
	}
	
	public Response process(Request c) {
		if( c == null){
			return null;
		}
		String commandType = c.getType();
		if(commandType.equals(Request.LIST)){
			listOrders();
		}
		Response response = null;
		List<String[]> multiCalls = c.unpack();
		if(multiCalls != null){
			for (String[] arguments : multiCalls) {			
				if(commandType.equals(Request.BOOK)){
					response = bookOrder(arguments[0], arguments[1], Integer.valueOf(arguments[2]), Double.valueOf(arguments[3]), Boolean.valueOf(arguments[4]));
				}else if(commandType.equals(Request.UPDATE)){
					response = updateOrder(Long.valueOf(arguments[0]), arguments[1], arguments[2], Integer.valueOf(arguments[3]), Double.valueOf(arguments[4]), Boolean.valueOf(arguments[5]));
				}else if(commandType.equals(Request.CLIENT_EXITS)){
					clientExits(arguments[0]);
				}
			}
		}
		return response;
		
	}

}
