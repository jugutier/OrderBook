package com.example.orderbook.client;

/**
 * Remote interface that a client passes to the server to be reported about the status of an order.
 */
public interface OrderBookClientHandle{
	
	/**
	 * Notifies botch clients when their orders match and how many units where placed
	 * @param securityId
	 * 			The unique identifier for the matched security
	 * @param units
	 * 			The number of securities placed
	 * @param value
	 * 			The actual value for which the matching occurred.
	 * @param isBuying
	 * 			true if it matched a buying order, false otherwise.
	 */
	void notifyOrderMatched(String securityId, Integer units , Double value, boolean isBuying);
	
	/**
	 * When the trading session ends, any unfulfilled order is cancelled
	 *  and who placed it gets notified here.
	 * @param securityId
	 * 			The unique identifier for the matched security
	 */
	void notifyOrderCancelled(String securityId);
	
	/**
	 * Notifies the client when an order gets successfully updated.
	 * @param orderId
	 * 			the unique orderId that was successfully updated.
	 * @param success
	 * 			true if the update was successful, false if the id was bogus.
	 */
	void notifyOrderUpdated(String orderId, boolean success);
	
	/**
	 * Notifies the client when an order gets successfully queued.
	 * @param orderId
	 * 			the unique orderId that was generated for the requested Order by the Server..
	 */
	void notifyOrderQueued(String orderId);
	
	
}