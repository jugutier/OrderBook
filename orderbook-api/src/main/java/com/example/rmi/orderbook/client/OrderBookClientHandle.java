package com.example.rmi.orderbook.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface that a client passes to the server to be reported about the status of an order.
 */
public interface OrderBookClientHandle extends Remote {
	
}