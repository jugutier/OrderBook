package com.example.orderbook;

import java.rmi.RemoteException;
import java.util.List;

import com.example.orderbook.client.OrderBookClientHandle;
import com.example.orderbook.server.OrderBookService;

public class OrderBookServant implements OrderBookService{
	private final PriorityOrderBook orders;

	public OrderBookServant() throws RemoteException{
		System.out.println("Servant init");
		this.orders = new PriorityOrderBook();
	}

	@Override
	public List<Order> listOrders(){
		return orders.getAllOrders();
	}

	@Override
	public void bookOrder(String clientId, String securityId, Integer amount,
			Double value, boolean isBuying,	OrderBookClientHandle clientHandler) 
					{
		Order bookedOrder = new Order(clientId, securityId, amount, value,
				isBuying, System.currentTimeMillis(), clientHandler);
		System.out.println("Booking...");
		if(isBuying){
			orders.buy(bookedOrder);
		}else{
			orders.sell(bookedOrder);
		}
		System.out.println(bookedOrder);


	}

	@Override
	public void updateOrder(Long orderId, String clientId, String securityId,
			Integer amount, Double value, boolean isBuying,
			OrderBookClientHandle clientHandler)  {
		
		System.out.println("Updating...");
		Order orderToUpdate = new Order(orderId, clientId, securityId, amount, value,
				isBuying ,  System.currentTimeMillis(),  clientHandler);
		
		orders.update(orderToUpdate);
		System.out.println(orderToUpdate);

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

	public void processCommand(Command c) {
		String commandType = c.getType();
		if(commandType.equals(Command.LIST)){
			List<Order> l = listOrders();
			System.out.println("orders" + l);
		}else if(commandType.equals(Command.TRADE)){
			Order o = (Order) c.getPayload();
//			if(o.getOrderId() != null){
//				orders.update(o);
//			}
//			else 
			if(o.isBuying()){
				orders.buy(o);
			}
			else{
				orders.sell(o);
			}
		}
		
	}



}
