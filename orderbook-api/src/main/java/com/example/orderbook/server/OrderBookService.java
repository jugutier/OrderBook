package com.example.orderbook.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.example.orderbook.Order;
import com.example.orderbook.client.OrderBookClientHandle;

/**
 * Service to operate in the Order Booking of securities.
 * 
 * Provides a remote environment for client/server execution.
 */
public interface OrderBookService extends Remote {

	/**
	 * This is to enable a TEST client to inspect the server state.
	 * At a production level we would NOT want to give the user the ability to do this, 
	 * thus it would be implemented outside this interface and grant access through:
	 * a) a secure remote terminal
	 * b) physical access
	 * c) no access at all.
	 * @return an priority - ordered list with current active orders
	 * @throws RemoteException
	 *             if the client-server connection drops.
	 */
	List<Order> listOrders() throws RemoteException;

	/**
	 * Books an order into the value-time priority queue.
	 * 
	 * We require many parameters instead of the Order object because marshalling
	 *  becomes more efficient through the net in this way.
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
	 * @throws RemoteException
	 *             if the client-server connection drops.
	 */
	void bookOrder(String clientId, String securityId, Integer amount, Double value, boolean isBuying, OrderBookClientHandle clientHandler) throws RemoteException;

	/**
	 * Sent by a client that wants to exit the session, thus canceling all his remaining orders placed.
	 * @param clientId
	 * 		The client's unique identifier
	 * @throws RemoteException
	 *             if the client-server connection drops.
	 */
	void clientExits(String clientId) throws RemoteException;
	
	
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
	 * @throws RemoteException
	 *             if the client-server connection drops.
	 */
	void updateOrder(Long orderId, String clientId, String securityId, Integer amount, Double value, boolean isBuying, OrderBookClientHandle clientHandler) throws RemoteException;
}
