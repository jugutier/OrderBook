package com.example.orderbook;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.example.orderbook.client.OrderBookClientHandle;
import com.example.orderbook.client.Response;
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
public class OrderBookClientHandleImpl implements OrderBookClientHandle{
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
	
	@Override
	public void notifyOrderQueued(String orderId) {
		StringBuilder sb = new StringBuilder();
		sb.append(clientId)
		.append("-> Order queued, id: ")
		.append(orderId)
		.append(" at ")
		.append(Analyzer.milliSecondsToTimestamp(System.currentTimeMillis()));
		
		String logTransaction = sb.toString();
		transactions.add(logTransaction);
		System.out.println(logTransaction);	
		
	}
	
	public void process(Response r) {
		if(r == null){
			return;
		}
		String commandType = r.getType();
		String[] arguments = r.unpack();
		if(commandType.equals(Response.CANCELLED)){
			notifyOrderCancelled(arguments[0]);
		}else if (commandType.equals(Response.MATCHED)){
			notifyOrderMatched(arguments[0], Integer.valueOf(arguments[1]), Double.valueOf(arguments[2]), Boolean.valueOf(arguments[3]));
		}else if (commandType.equals(Response.UPDATED)){
			notifyOrderUpdated(arguments[0], Boolean.valueOf(arguments[1]));
		}else if (commandType.equals(Response.QUEUED)){
			notifyOrderQueued(arguments[0]);
		}
	}	
	
}
