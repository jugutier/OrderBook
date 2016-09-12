package com.example.orderbook.server;

import java.util.List;

import com.example.orderbook.Message;
import com.example.orderbook.Order;
import com.example.orderbook.client.OrderBookClientHandle;

public class Request extends Message implements OrderBookService{

	private static final long serialVersionUID = 3321373160778417691L;
	public static final String LIST = "list";
	public static final String BOOK = "bookOrder";
	public static final String CLIENT_EXITS = "clientExits";
	public static final String UPDATE = "updateOrder";
	
	public Request(String type, Object payload) {
		super(type, payload);
	}

	@Override
	public List<Order> listOrders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bookOrder(String clientId, String securityId, Integer amount,
			Double value, boolean isBuying, OrderBookClientHandle clientHandler) {
		pack(clientId, securityId, amount.toString(), value.toString(), String.valueOf(isBuying) , clientHandler.toString());
		
	}

	@Override
	public void clientExits(String clientId) {
		pack(clientId);		
	}

	@Override
	public void updateOrder(Long orderId, String clientId, String securityId,
			Integer amount, Double value, boolean isBuying,
			OrderBookClientHandle clientHandler) {
		pack(orderId.toString(), clientId, securityId, 
				amount.toString(), value.toString(), 
				String.valueOf(isBuying) , clientHandler.toString());
		
		
	}

}
