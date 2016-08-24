package com.example.rmi.orderbook;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.example.rmi.orderbook.client.OrderBookClientHandle;

public class OrderBookClientHandleImpl implements OrderBookClientHandle{

    public OrderBookClientHandleImpl()
            throws RemoteException {
        super();
        UnicastRemoteObject.exportObject(this, 0);
    }
	
    public void unexport() throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(this, true);
    }
}
