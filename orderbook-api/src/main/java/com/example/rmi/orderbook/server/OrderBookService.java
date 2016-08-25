package com.example.rmi.orderbook.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import com.example.rmi.orderbook.Order;
import com.example.rmi.orderbook.client.OrderBookClientHandle;

/**
 * Service to operate in the Order Booking of securities.
 * 
 * Provides a remote environment for client/server execution.
 */
public interface OrderBookService extends Remote {

    /**
     * @return lists current active orders
     * @throws RemoteException
     *             if the client-server connection drops.
     */
    Set<Order> listOrders() throws RemoteException;

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

}
