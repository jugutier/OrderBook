package com.example.rmi.orderbook.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface that a client passes to the server to be reported about the status of an order.
 */
public interface OrderBookClientHandle extends Remote {
	
	/**
	 * Notifies botch clients when their orders match and how many units where placed
	 * @param securityId
	 * 			The unique identifier for the matched security
	 * @param units
	 * 			The number of securities placed
	 * @param value
	 * 			The actual value for which the matching occurred.
	 * @throws RemoteException
	 * 			If the connection drops
	 */
	void notifyOrderMatched(String securityId, Integer units , Double value) throws RemoteException;
	
	/**
	 * When the trading session ends, any unfulfilled order is cancelled
	 *  and who placed it gets notified here.
	 * @param securityId
	 * 			The unique identifier for the matched security
	 * @throws RemoteException
	 * 			If the connection drops
	 */
	void notifyOrderCancelled(String securityId) throws RemoteException;
	
	
}