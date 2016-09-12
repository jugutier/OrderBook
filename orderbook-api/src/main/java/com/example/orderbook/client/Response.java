package com.example.orderbook.client;

import com.example.orderbook.Message;

public class Response extends Message implements OrderBookClientHandle{

	private static final long serialVersionUID = 1228210531241130596L;
	public static final String MATCHED = "notifyOrderMatched";
	public static final String CANCELLED = "notifyOrderCancelled";
	public static final String UPDATED = "notifyOrderUpdated";
	
	public Response(String type, Object payload) {
		super(type, payload);
	}

	@Override
	public void notifyOrderMatched(String securityId, Integer units,
			Double value, boolean isBuying) {
		pack(securityId, units.toString(), value.toString(), String.valueOf(isBuying));

	}

	@Override
	public void notifyOrderCancelled(String securityId) {
		pack(securityId);
	}

	@Override
	public void notifyOrderUpdated(String orderId, boolean success) {
		pack(orderId, String.valueOf(success));
	}

}
