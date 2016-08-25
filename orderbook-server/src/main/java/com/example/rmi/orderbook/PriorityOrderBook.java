package com.example.rmi.orderbook;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * PriorityOrderBook automatically matches viable transactions, 
 * otherwise stores them in order.
 * 
 * By using heap sort (what Java's PriorityBlockingQueue uses under the hood) 
 * our time complexity for ordered insertion is O(n log n).
 * 
 * In terms of concurrent clients, critical operations are:
 * 
 * 1 - Lookup for a given security -> Taken care of by concurrentHashMap
 * 2 - Adding or Removing a new order for a given security -> Taken care of by
 * the individual PriorityBlockingQueues.
 * 3 - Partial fulfillment of an order -> Taken care of by the Order's setAmount method.
 * 
 */
public class PriorityOrderBook {
	private static final int INITIAL_CAPACITY = 10;
	private Map<String,PriorityBlockingQueue<Order>> buyMap;
	private Map<String,PriorityBlockingQueue<Order>> sellMap;

	public PriorityOrderBook() {
		this.buyMap = new ConcurrentHashMap<String, PriorityBlockingQueue<Order>>();
		this.sellMap = new ConcurrentHashMap<String, PriorityBlockingQueue<Order>>();
	}

	public void sell(Order sellOrder) throws RemoteException{
		String security = sellOrder.getSecurityId();
		// 1. Look for buyers of the same security to match with.
		PriorityBlockingQueue<Order> buyQueueForSecurity = buyMap.get(security);
		if(buyQueueForSecurity != null){
			Set<Order> toRemove = new HashSet<Order>();
			for (Order buyOrder : buyQueueForSecurity) {				
				if(buyOrder.getSecurityId().equals(sellOrder.getSecurityId()) &&
						buyOrder.getValue() >= sellOrder.getValue()){
					//1.1 If we have matched an existing buyOrder place as many units as we can.
					int buyingUnits = buyOrder.getAmount();
					int sellingUnits = sellOrder.getAmount();

					if(buyingUnits > sellingUnits){
						//1.1.1 Partially fulfill for a buyer interested in more 
						//securities than the ones in the incoming orders.
						buyOrder.setAmount( buyingUnits - sellingUnits);
						buyOrder.getClientHandle().notifyOrderMatched(buyOrder.getSecurityId(), buyingUnits - sellingUnits);
						sellOrder.setAmount(0);
						sellOrder.getClientHandle().notifyOrderMatched(sellOrder.getSecurityId(), sellingUnits);
					}else if(buyingUnits == sellingUnits){
						//1.1.2 If all units are placed, remove both orders and notify involved parties
						buyOrder.setAmount(0);
						sellOrder.setAmount(0);
						toRemove.add(buyOrder);
						toRemove.add(sellOrder);
						break;
					}else{
						//1.1.3 Partially fulfill for this seller and a buyer willing
						//to take less securities than the ones in the incoming orders.
						sellOrder.setAmount(sellingUnits - buyingUnits);
					}					
				}
			}
			//1.2 If we fulfilled any orders, remove them.
			if(toRemove != null && toRemove.size() > 0){
				for (Order order : toRemove) {
					if(order.isBuying()){
						//Critical section: removing a security.
						buyQueueForSecurity.remove(order);
					}else{
						//1.2.1 The security might not exist if we match at the time of placement
						//at its first appearance for the session.
						if(sellMap.containsKey(security)){
							//Critical section: lookup & remove.
							sellMap.get(security).remove(order);
						}
					}
				}
				if(toRemove.size() == 2){
					//1.2.2 We made a perfect match. Nothing else to do here.
					return;
				}
			}
		}
		//2. If we couldn't match it to any of the existing (or partially fulfilled it), queue it.
		if(sellMap.containsKey(security)){
			sellMap.get(security).offer(sellOrder);
		}else{
			//Critical section: creating and adding a new queue for an unused security.
			PriorityBlockingQueue<Order> pq = new PriorityBlockingQueue<Order>(INITIAL_CAPACITY, new SellingComparator());
			pq.offer(sellOrder);
			sellMap.put(security, pq);

		}

	}
	//just adds, no matching on this side yet.
	public void buy(Order buyOrder){
		String security = buyOrder.getSecurityId();
		PriorityBlockingQueue<Order> buyQueueForSecurity = buyMap.get(security);
		if(buyQueueForSecurity == null){
			buyQueueForSecurity = new PriorityBlockingQueue<Order>(INITIAL_CAPACITY, new BuyingComparator());			
			buyMap.put(security, buyQueueForSecurity);
		}
		buyQueueForSecurity.offer(buyOrder);
	}

	public void clear() {
		buyMap.clear();
		sellMap.clear();		
	}

	public Set<Order> getAllOrders(){
		Set<Order> ret = new HashSet<Order>();
		dumpMap(ret, buyMap);
		dumpMap(ret, sellMap);
		return ret;
	}

	/**
	 * Dumps all orders from map into ret.
	 * @param ret 
	 * 			a set to add all orders contained by the map
	 * @param map
	 * 			buy/sell map to be dumped.
	 */
	private void dumpMap(Collection<Order> ret , Map<String,PriorityBlockingQueue<Order>> map){
		Set<String> keys = map.keySet();
		for (String key : keys) {
			PriorityBlockingQueue<Order> securitiesForKey = map.get(key);
			for (Order order : securitiesForKey) {
				ret.add(order);
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BUYING: \n");
		List<Order> buyingOrders = new LinkedList<Order>();
		dumpMap(buyingOrders, buyMap);
		for (Order order : buyingOrders) {
			sb.append(order.toString()+"\n");
		}
		sb.append("SELLING: \n");
		List<Order> sellingOrders = new LinkedList<Order>();
		dumpMap(sellingOrders, buyMap);
		for (Order order : sellingOrders) {
			sb.append(order.toString()+"\n");
		}
		return sb.toString();
	}

	static class BuyingComparator implements Comparator<Order> {

		public int compare(Order one, Order two) {
			if(!one.getSecurityId().equals(two.getSecurityId())){
				System.err.println("This orders are not comparable, they need to be for the same security");
				new IllegalArgumentException();
			}
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
