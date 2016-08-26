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

	/**
	 * Attempts to match-sell an order or queues it until a buyer arrives.
	 * 
	 * @param sellOrder
	 * 			the order to be sold
	 * @return
	 * 		The effective transaction value, or null if queued.
	 * @throws RemoteException
	 */
	public Double sell(Order sellOrder) throws RemoteException{
		if(sellOrder.isBuying()){
			throw new IllegalArgumentException("Attempted selling a buying order");
		}
		System.out.println("Trying to sell "+ sellOrder.toString());
		String desiredSecurity = sellOrder.getSecurityId();
		Double transactionValue = null;
		// 1. Look for buyers of the same security to match with.
		PriorityBlockingQueue<Order> buyQueueForSecurity = buyMap.get(desiredSecurity);
		if(buyQueueForSecurity != null){
			for (Order buyOrder : buyQueueForSecurity) {
				if(	!buyOrder.getClientId().equals(sellOrder.getClientId()) &&
						buyOrder.getValue() >= sellOrder.getValue()){
					//1.1 If we have matched an existing buyOrder place as many units as we can.
					int buyingUnits = buyOrder.getAmount();
					int sellingUnits = sellOrder.getAmount();
					if(buyingUnits > sellingUnits){
						//1.1.1 Partially fulfill for a buyer interested in more 
						//securities than the ones in the incoming orders.
						transactionValue = sellOrder.getValue();
						
						buyOrder.setAmount( buyingUnits - sellingUnits);
						buyOrder.getClientHandle().notifyOrderMatched(desiredSecurity, buyingUnits - sellingUnits,
								transactionValue);
						sellOrder.setAmount(0);
						sellOrder.getClientHandle().notifyOrderMatched(desiredSecurity, sellingUnits,
								transactionValue);
						System.out.println("At > "+ (buyingUnits - sellingUnits) +"\n"+ buyOrder.toString() +"\n"+ sellOrder.toString());
					}else if(buyingUnits == sellingUnits){
						//1.1.2 If all units are placed, remove the buying order and notify both parties
						System.out.println("removing");
						buyQueueForSecurity.remove(buyOrder);
						transactionValue = sellOrder.getValue();
						buyOrder.getClientHandle().notifyOrderMatched(desiredSecurity, sellingUnits,
								transactionValue);
						sellOrder.getClientHandle().notifyOrderMatched(desiredSecurity, sellingUnits,
								transactionValue);
						
						// Perfect match (or several partial fulfillments)
						return transactionValue;
					}else{
						//1.1.3 Partially fulfill for this seller and remove the buy order for
						//less securities than the ones in the incoming orders.
						transactionValue = sellOrder.getValue();
						System.out.println("At < "+ (sellingUnits - sellingUnits));
						sellOrder.setAmount(sellingUnits - sellingUnits);						
						buyQueueForSecurity.remove(buyOrder);
						buyOrder.getClientHandle().notifyOrderMatched(desiredSecurity, buyingUnits,
								transactionValue);
						sellOrder.getClientHandle().notifyOrderMatched(desiredSecurity, buyingUnits,
								transactionValue);
					}					
				}
			}
		}
		//2. If we still have sell units (i.e no match or partially fulfilled it), queue it.
		if(sellOrder.getAmount() > 0){
			if(sellMap.containsKey(desiredSecurity)){				
					System.out.println("queuing \n"+ sellOrder.toString());
					sellMap.get(desiredSecurity).offer(sellOrder);
			}else{
				//Critical section: creating and adding a new queue for an non-existing security.
				PriorityBlockingQueue<Order> pq = new PriorityBlockingQueue<Order>(INITIAL_CAPACITY, new SellingComparator());
				System.out.println("queuing 2 \n"+ sellOrder.toString());
				pq.offer(sellOrder);
				sellMap.put(desiredSecurity, pq);
			}
		}
		return transactionValue;
	}
	/**
	 * Attempts to match-buy an order or queues it until a seller arrives.
	 * 
	 * @param buyOrder
	 * 			the order to be bought
	 * @return
	 * 		The effective transaction value, or null if queued.
	 * @throws RemoteException
	 */
	//just adds, no matching on this side yet.
	public Double buy(Order buyOrder){
		if(!buyOrder.isBuying()){
			throw new IllegalArgumentException("Attempted buying a selling order");
		}
		System.out.println("Trying to buy "+ buyOrder.toString());
		String security = buyOrder.getSecurityId();
		PriorityBlockingQueue<Order> buyQueueForSecurity = buyMap.get(security);
		if(buyQueueForSecurity == null){
			buyQueueForSecurity = new PriorityBlockingQueue<Order>(INITIAL_CAPACITY, new BuyingComparator());			
			buyMap.put(security, buyQueueForSecurity);
		}
		buyQueueForSecurity.offer(buyOrder);
		
		return null;
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
		dumpMap(sellingOrders, sellMap);
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
