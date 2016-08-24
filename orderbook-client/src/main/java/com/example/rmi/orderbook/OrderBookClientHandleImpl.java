package com.example.rmi.orderbook;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;

import com.example.rmi.orderbook.client.OrderBookClientHandle;

public class OrderBookClientHandleImpl implements OrderBookClientHandle{
	private final String clientId;
	
    public OrderBookClientHandleImpl(final String clientId)
            throws RemoteException {
        super();
        this.clientId = Objects.requireNonNull(clientId, "the clientId can't be null");
        UnicastRemoteObject.exportObject(this, 0);
    }
	
    public void unexport() throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(this, true);
    }

	@Override
	public void notifyOrderMatched(String securityId, Integer amount) throws RemoteException {
		System.out.println(clientId +"-> Order matched! " + securityId +" amount "+ amount);
		
	}

	@Override
	public void notifyOrderCancelled(String securityId) throws RemoteException {
		System.out.println(clientId +"-> Order cancelled :( " + securityId);		
	}
}
