package com.example.orderbook.server;

import com.example.orderbook.Message;
import com.example.orderbook.client.Response;

public class Request extends Message implements OrderBookService{

	private static final long serialVersionUID = 3321373160778417691L;
	public static final String LIST = "list";
	public static final String BOOK = "bookOrder";
	public static final String CLIENT_EXITS = "clientExits";
	public static final String UPDATE = "updateOrder";
	

	@Override
	public void listOrders() {
		setType(LIST);
	}

	@Override
	public Response bookOrder(String clientId, String securityId, Integer amount,
			Double value, boolean isBuying) {
		setType(BOOK);
		pack(clientId, securityId, amount.toString(), value.toString(), String.valueOf(isBuying) );
		return null;		
	}

	@Override
	public void clientExits(String clientId) {
		setType(CLIENT_EXITS);
		pack(clientId);		
	}

	@Override
	public Response updateOrder(Long orderId, String clientId, String securityId,
			Integer amount, Double value, boolean isBuying) {
		setType(UPDATE);
		pack(orderId.toString(), clientId, securityId, 
					amount.toString(), value.toString(), 
					String.valueOf(isBuying) );
		
		return null;
	}

}
