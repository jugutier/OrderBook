package com.example.orderbook.client;

import com.example.orderbook.Message;

public class Response extends Message implements OrderBookClientHandle{

	private static final long serialVersionUID = 1228210531241130596L;
	public static final String MATCHED = "notifyOrderMatched";
	public static final String CANCELLED = "notifyOrderCancelled";
	public static final String UPDATED = "notifyOrderUpdated";
	public static final String QUEUED = "notifyOrderQueued";
	

	@Override
	public void notifyOrderMatched(String securityId, Integer units,
			Double value, boolean isBuying) {
		setType(MATCHED);
		pack(securityId, units.toString(), value.toString(), String.valueOf(isBuying));

	}

	@Override
	public void notifyOrderCancelled(String securityId) {
		setType(CANCELLED);
		pack(securityId);
	}

	@Override
	public void notifyOrderUpdated(String orderId, boolean success) {
		setType(UPDATED);
		pack(orderId, String.valueOf(success));
	}

	@Override
	public void notifyOrderQueued(String orderId) {
		setType(QUEUED);
		pack(orderId);		
	}

}
