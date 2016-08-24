package com.example.rmi.orderbook.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface that a client passes to the server to be reported about the status of an order.
 */
public interface OrderBookClientHandle extends Remote {
	
	/** notifies botch clients when their orders match and how many units where placed**/
	void notifyOrderMatched(String securityId, Integer amount) throws RemoteException;
	/** when the trading session ends, any unfulfilled order is cancelled
	 *  and who placed it gets notified here.**/
	void notifyOrderCancelled(String securityId) throws RemoteException;
	
	
}