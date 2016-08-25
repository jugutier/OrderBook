package com.example.rmi.orderbook;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	private Map<String,PriorityQueue<Order>> buyMap;
	private Map<String,PriorityQueue<Order>> sellMap;

	
	
	public PriorityOrderBook() {
		this.buyMap = new HashMap<String, PriorityQueue<Order>>();
		this.sellMap = new HashMap<String, PriorityQueue<Order>>();
	}
	
	public void sell(Order sellOrder) throws RemoteException{
		String security = sellOrder.getSecurityId();
		PriorityQueue<Order> buyQueueForSecurity = buyMap.get(security);
		if(buyQueueForSecurity == null){
			buyQueueForSecurity = new PriorityQueue<Order>(new BuyingComparator());
			buyQueueForSecurity.offer(sellOrder);
			buyMap.put(security, buyQueueForSecurity);
		}
		Set<Order> toRemove = new HashSet<Order>();
		for (Order buyOrder : buyQueueForSecurity) {
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
					buyQueueForSecurity.remove(order);
				}else{
					//The security might not exist if we match at the time of placement
					//at its first appearance for the session.
					if(sellMap.containsKey(security)){
						sellMap.get(security).remove(order);
					}
				}
			}
		}else{
			//If we couldn't match it to any of the existing (or partially fulfilled it), queue it.
			if(sellMap.containsKey(security)){
				sellMap.get(security).offer(sellOrder);
			}else{
				PriorityQueue<Order> pq = new PriorityQueue<Order>(new SellingComparator());
				pq.offer(sellOrder);
				sellMap.put(security, pq);
			}
		}
	}
	
	public void buy(Order buyOrder){
		String security = buyOrder.getSecurityId();
		PriorityQueue<Order> sellQueueForSecurity = buyMap.get(security);
		if(sellQueueForSecurity == null){
			sellQueueForSecurity = new PriorityQueue<Order>();			
			buyMap.put(security, sellQueueForSecurity);
		}
		sellQueueForSecurity.offer(buyOrder);
	}
	
	public Set<Order> getAllOrders(){
		Set<Order> ret = new HashSet<Order>();
		dumpMap(ret, buyMap);
		dumpMap(ret, sellMap);
		return ret;
	}
	
	private Set<Order> dumpMap(Set<Order> ret , Map<String,PriorityQueue<Order>> map){
		Set<String> keys = map.keySet();
		for (String key : keys) {
			PriorityQueue<Order> securitiesForKey = map.get(key);
			for (Order order : securitiesForKey) {
				ret.add(order);
			}
		}
		return ret;
	}
	
	static class BuyingComparator implements Comparator<Order> {
		 
		public int compare(Order one, Order two) {
			long deltaTime = one.getTimestamp() - two.getTimestamp();
			
			double deltaValue = one.getValue() - two.getValue();
			if(deltaTime == 0){
				return (int) Math.ceil(deltaValue);
			}
			return (int) deltaTime;
		}
	}
	
	static class SellingComparator implements Comparator<Order> {
		 
		public int compare(Order one, Order two) {
			//The opposite priority
			return - (new BuyingComparator().compare(one,two));
		}
	}

}
