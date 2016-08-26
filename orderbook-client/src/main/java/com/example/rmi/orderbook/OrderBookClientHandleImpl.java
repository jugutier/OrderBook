package com.example.rmi.orderbook;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.example.rmi.orderbook.client.OrderBookClientHandle;
import com.example.rmi.orderbook.util.Analyzer;

/**
 * A sample implementation for {@link OrderBookClientHandle} 
 * that logs all transactions received in a LinkedList. 
 * 
 * The data structure was picked over ArrayList because transactions are always growing, making 
 * it more efficient to add a link at the end of the list than resizing a fixed size array at 
 * the expense of a requiring a bit more memory.
 * 
 * This is to showcase how further extensions could store matched 
 * transactions in a database or do any other actions with it.
 */
public class OrderBookClientHandleImpl implements OrderBookClientHandle{
	private final String clientId;
	private List<String> transactions;
	
    public OrderBookClientHandleImpl(final String clientId)
            throws RemoteException {
        super();
        this.clientId = Objects.requireNonNull(clientId, "the clientId can't be null");
        this.transactions = new LinkedList<String> ();
        UnicastRemoteObject.exportObject(this, 0);
    }
	
    public void unexport() throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(this, true);
    }
    
    public List<String> getTransactionsLog(){
    	return transactions;
    }

	@Override
	public void notifyOrderMatched(String securityId, Integer amount , Double value) throws RemoteException {
		StringBuilder sb = new StringBuilder();
		sb.append(clientId)
		.append("-> Order matched! ")
		.append(securityId)
		.append(" amount ")
		.append(" you payed ")
		.append(value)
		.append(" at ")
		.append(Analyzer.milliSecondsToTimestamp(System.currentTimeMillis()));
		
		String logTransaction = sb.toString();
		transactions.add(logTransaction);
		System.out.println(logTransaction);
		
	}

	@Override
	public void notifyOrderCancelled(String securityId) throws RemoteException {
		StringBuilder sb = new StringBuilder();
		sb.append(clientId)
		.append("-> Order cancelled :( ")
		.append(securityId)
		.append(" at ")
		.append(Analyzer.milliSecondsToTimestamp(System.currentTimeMillis()));
		
		String logTransaction = sb.toString();
		transactions.add(logTransaction);
		System.out.println(logTransaction);	
	}
	
}
