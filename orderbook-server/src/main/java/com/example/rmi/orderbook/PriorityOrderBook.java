package com.example.rmi.orderbook;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * PriorityOrderBook automatically matches viable transactions, 
 * otherwise stores them in order.
 * 
 * By using heap sort (what Java's PriorityQueue uses under the hood) 
 * our time complexity for ordered insertion is O(n log n).
 * 
 */
public class PriorityOrderBook {
	private PriorityQueue<Order> buy;
	private PriorityQueue<Order> sell;
	
	
	public PriorityOrderBook() {
		this.buy = new PriorityQueue<Order>(new BuyingComparator());
		this.sell = new PriorityQueue<Order>(new SellingComparator());
	}
	
	public void sell(Order sellOrder) throws RemoteException{
		Set<Order> toRemove = new HashSet<Order>();
		for (Order buyOrder : buy) {
			//if we have matched an existing buyOrder
			if(buyOrder.getSecurityId().equals(sellOrder.getSecurityId()) &&
			   buyOrder.getValue() >= sellOrder.getValue()){
				
				//place as many units as we can from buy order
				int buyingUnits = buyOrder.getAmount();
				int sellingUnits = sellOrder.getAmount();
				
				if(buyingUnits > sellingUnits){
					buyOrder.setAmount( buyingUnits - sellingUnits);
					buyOrder.getClientHandle().notifyOrderMatched(buyOrder.getSecurityId(), buyingUnits - sellingUnits);
					sellOrder.setAmount(0);
					sellOrder.getClientHandle().notifyOrderMatched(sellOrder.getSecurityId(), sellingUnits);
				}else if(buyingUnits == sellingUnits){
					//if all units are placed, remove order and notify both parties
					buyOrder.setAmount(0);
					sellOrder.setAmount(0);
					toRemove.add(buyOrder);
					toRemove.add(sellOrder);
					break;
				}else{
					sellOrder.setAmount(sellingUnits - buyingUnits);
				}		
				
				
			}
		}
		if(toRemove.size() > 0){
			for (Order order : toRemove) {
				if(order.isBuying()){
					buy.remove(order);
				}else{
					sell.remove(order);
				}
			}
		}else{
			//If we couldn't match it to any of the existing (or partially fulfilled it), queue it.
			sell.add(sellOrder);
		}
	}
	
	public void buy(Order buyOrder){
		buy.add(buyOrder);		
	}
	
	public Set<Order> getAllOrders(){
		Set<Order> ret = new HashSet<Order>();
		ret.addAll(buy);
		ret.addAll(sell);				
		return ret;
	}
	static class BuyingComparator implements Comparator<Order> {
		 
		public int compare(Order one, Order two) {
			return (int) (two.getValue() - one.getValue());
		}
	}
	
	static class SellingComparator implements Comparator<Order> {
		 
		public int compare(Order one, Order two) {
			return (int) (two.getValue() - one.getValue());
		}
	}

}
