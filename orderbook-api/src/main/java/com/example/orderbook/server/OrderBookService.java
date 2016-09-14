package com.example.orderbook.server;

import com.example.orderbook.client.Response;

/**
 * Service to operate in the Order Booking of securities.
 * 
 * Provides a remote environment for client/server execution.
 */
public interface OrderBookService {

	/**
	 * This is to enable a TEST client to inspect the server state.
	 * At a production level we would NOT want to give the user the ability to do this, 
	 * thus it would be implemented outside this interface and grant access through:
	 * a) a secure remote terminal
	 * b) physical access
	 * c) no access at all.
	 * @return an priority - ordered list with current active orders
	 */
	void listOrders();

	/**
	 * Books an order into the value-time priority queue.
	 * 
	 *
	 * @param clientId
	 *            the client's unique identifier
	 * @param securityId
	 *            the security unique identifier
	 * @param amount
	 *            the value offered
	 * @param value
	 *            the value offered
	 * @param clientHandler
	 *            a remote handler for the service to notify clients
	 * @param isBuying
	 * 			boolean value to indicate if its a buying or selling order
	 */
	Response bookOrder(String clientId, String securityId, Integer amount, Double value, boolean isBuying);

	/**
	 * Sent by a client that wants to exit the session, thus canceling all his remaining orders placed.
	 * @param clientId
	 * 		The client's unique identifier
	 */
	void clientExits(String clientId);
	
	
	/**
	 * Updates an existing Booked order into the value-time priority queue.
	 * 
	 * We require many parameters instead of the Order object because marshalling
	 *  becomes more efficient through the net in this way.
	 *
	 * @param orderId
	 * 			the order id to be affected
	 * @param clientId
	 *            the client's unique identifier
	 * @param securityId
	 *            the security unique identifier
	 * @param amount
	 *            the value offered
	 * @param value
	 *            the value offered
	 * @param clientHandler
	 *            a remote handler for the service to notify clients
	 * @param isBuying
	 * 			boolean value to indicate if its a buying or selling order
	 */
	Response updateOrder(Long orderId, String clientId, String securityId, Integer amount, Double value, boolean isBuying);
}
