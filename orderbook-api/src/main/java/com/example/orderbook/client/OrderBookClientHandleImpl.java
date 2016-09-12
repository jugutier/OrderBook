package com.example.orderbook.client;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.example.orderbook.util.Analyzer;

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
public class OrderBookClientHandleImpl implements OrderBookClientHandle, Serializable{
	private static final long serialVersionUID = 6505362435620566841L;
	private final String clientId;
	private List<String> transactions;
	
    public OrderBookClientHandleImpl(final String clientId){
        super();
        this.clientId = Objects.requireNonNull(clientId, "the clientId can't be null");
        this.transactions = new LinkedList<String> ();
    }
    
    public List<String> getTransactionsLog(){
    	return transactions;
    }

	@Override
	public void notifyOrderMatched(String securityId, Integer amount , Double value, boolean isBuying){
		StringBuilder sb = new StringBuilder();
		sb.append(clientId)
		.append("-> Order matched! ")
		.append(" security: ")
		.append(securityId)		
		.append(" you ")
		.append(isBuying?"paid ": "got ")		
		.append("$" + value)
		.append(" each unit, for a total amount of: ")
		.append(amount)
		.append(". At ")
		.append(Analyzer.milliSecondsToTimestamp(System.currentTimeMillis()));
		
		String logTransaction = sb.toString();
		transactions.add(logTransaction);
		System.out.println(logTransaction);
		
	}

	@Override
	public void notifyOrderCancelled(String securityId){
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

	@Override
	public void notifyOrderUpdated(String orderId, boolean success){
		StringBuilder sb = new StringBuilder();
		sb.append(clientId)
		.append("-> Order update ")
		.append(success?"succesful ": "failed ")
		.append(orderId)
		.append(" at ")
		.append(Analyzer.milliSecondsToTimestamp(System.currentTimeMillis()));
		
		String logTransaction = sb.toString();
		transactions.add(logTransaction);
		System.out.println(logTransaction);	
		
	}
	
}
