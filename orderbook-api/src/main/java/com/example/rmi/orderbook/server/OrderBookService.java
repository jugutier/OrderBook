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
     * Books an order into the value-time priority queue
     *
     * @param securityId
     *            the securities identifier
     * @param value
     *            the value offered
     * @param clientHandler
     *            a remote handler for the service to notify clients
     * @throws RemoteException
     *             if the client-server connection drops.
     */
    void bookOrder(String securityId, int value, OrderBookClientHandle clientHandler) throws RemoteException;

}
