package com.example.rmi.orderbook;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

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
		if(sellOrder.isBuying() || sellOrder.getUnits() <= 0){
			throw new IllegalArgumentException("Attempted selling a buying order");
		}
		System.out.println("Trying to sell "+ sellOrder.toString());
		String desiredSecurity = sellOrder.getSecurityId();		
		Double transactionValue = 0.0;
		PriorityBlockingQueue<Order> buyQueueForSecurity = buyMap.get(desiredSecurity);
		if(buyQueueForSecurity != null){
			System.out.println("require for "+ sellOrder.getClientId());
			requireClientDoesntExist(buyQueueForSecurity, sellOrder);
			System.out.println("match pq. Dump: \n");
			System.out.println(this);
			System.out.println("====================");
			transactionValue = match(buyQueueForSecurity, sellOrder);
		}
		else{
			System.out.println( "sell - Pq no Match for "+ desiredSecurity +".Dumping: \n"+ this.toString() );
			System.out.println(" =========END dump =========");
		}
		//2. If we still have sell units (i.e no match or partially fulfilled it), queue it.
		if(sellOrder.getUnits() > 0){
			if(sellMap.containsKey(desiredSecurity)){				
				System.out.println("sell - queuing "+ sellOrder.toString());
				sellMap.get(desiredSecurity).offer(sellOrder);
			}else{
				//Critical section: creating and adding a new queue for an non-existing security.
				PriorityBlockingQueue<Order> pq = new PriorityBlockingQueue<Order>(INITIAL_CAPACITY, new SellingComparator());
				System.out.println("sell - queuing with new queue:  "+ sellOrder.toString());
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
	 * 		The effective transaction value, or 0 if queued.
	 * @throws RemoteException
	 */
	public Double buy(Order buyOrder) throws RemoteException{
		if(!buyOrder.isBuying() || buyOrder.getUnits() <= 0){
			throw new IllegalArgumentException("Attempted buying a selling order");
		}

//		System.out.println("Trying to buy "+ buyOrder.toString());
		String desiredSecurity = buyOrder.getSecurityId();
		Double transactionValue = 0.0;
		PriorityBlockingQueue<Order> sellQueueForSecurity = sellMap.get(desiredSecurity);
		if(sellQueueForSecurity != null){
			requireClientDoesntExist(sellQueueForSecurity, buyOrder);
			transactionValue = match(sellQueueForSecurity, buyOrder);
		}
//		else{
//			System.out.println("buy pq No match " + desiredSecurity);
//		}
		if(buyOrder.getUnits() > 0){
			if(buyMap.containsKey(desiredSecurity)){				
//				System.out.println("buy - queuing \n"+ buyOrder.toString());
				buyMap.get(desiredSecurity).offer(buyOrder);
			}else{
				//Critical section: creating and adding a new queue for an non-existing security.
				PriorityBlockingQueue<Order> pq = new PriorityBlockingQueue<Order>(INITIAL_CAPACITY, new BuyingComparator());
//				System.out.println("buy - queuing with new queue: "+ buyOrder.toString());
				pq.offer(buyOrder);
				buyMap.put(desiredSecurity, pq);
			}
		}
		return transactionValue;
	}
	
	// we expect that buyer & seller can't be the same user for the same security.
	// otherwise we would have to iterate the pq in case the bestcandidate 
	// is one of the same user's orders.
	private void requireClientDoesntExist(PriorityBlockingQueue<Order> pq, Order order){
		List<Order> st = pq.stream()
			    .filter(o -> o.getClientId().equals(order.getClientId())).collect(Collectors.toList());
		if(st.size() >0){
			throw new IllegalArgumentException(order.getClientId()+
			" is Trying to buy and Sell the same security, which we don't allow");
		}
	}
	
	private Double match(PriorityBlockingQueue<Order> pq, Order o) throws RemoteException{
		Order bestCandidate = pq.peek();
		if(bestCandidate == null || o.getUnits() == 0){
			return 0.0;
		}
		String security = o.getSecurityId();
		Double transactionValue = bestCandidate.getValue();	
		int placedUnits = 0;
//		System.out.println("o -> " + o.getValue() + " best -> " + bestCandidate.getValue());
		boolean shouldMakeTransaction = o.isBuying()?
				(o.getValue() >= transactionValue):
					(o.getValue() <= transactionValue);
		if(	shouldMakeTransaction){			
			int oUnits = o.getUnits();
			int bestCandidateUnits = bestCandidate.getUnits();

			if(oUnits > bestCandidateUnits){
				placedUnits = bestCandidateUnits;
				o.setUnits(oUnits - bestCandidateUnits);
				transactionValue = bestCandidate.getValue();
				o.getClientHandle().
				notifyOrderMatched(security, 
						placedUnits, 
						transactionValue,
						o.isBuying());

				bestCandidate.setUnits(0);				
				bestCandidate.getClientHandle().
				notifyOrderMatched(security, 
						placedUnits, 
						transactionValue,
						bestCandidate.isBuying());
				pq.remove(bestCandidate);
			}else if(oUnits < bestCandidateUnits){
				placedUnits = oUnits;
				o.setUnits(0);
				o.getClientHandle().
				notifyOrderMatched(security, 
						placedUnits, 
						transactionValue,
						o.isBuying());

				bestCandidate.setUnits(bestCandidateUnits - oUnits);				
				bestCandidate.getClientHandle().
				notifyOrderMatched(security, 
						placedUnits, 
						transactionValue,
						bestCandidate.isBuying());
			}else{
				placedUnits = oUnits;//either one... 
				o.setUnits(0);				
				o.getClientHandle().
				notifyOrderMatched(security, 
						placedUnits, 
						transactionValue,
						o.isBuying());
				bestCandidate.setUnits(0);
				bestCandidate.getClientHandle().
				notifyOrderMatched(security, 
						placedUnits, 
						transactionValue,
						bestCandidate.isBuying());
				pq.remove(bestCandidate);
			}
			//If we still have units, attempt to match recursively
			return transactionValue * placedUnits + match(pq,o);
		}
		return transactionValue;
	}

	public void clear() {
		buyMap.clear();
		sellMap.clear();		
	}
	
	public void remove(String clientId) {
		removeFromMap(clientId, buyMap);
		removeFromMap(clientId, sellMap);
	}
	
	/**
	 * Removes from a given map, all orders that where placed by a client.
	 * @param clientId
	 * 			The clients unique identifier.
	 * @param map
	 * 			The map for which we want to remove.
	 */
	private void removeFromMap(String clientId, Map<String,PriorityBlockingQueue<Order>> map){
		Set<String> keys = map.keySet();
		for (String key : keys) {
			PriorityBlockingQueue<Order> securitiesForKey = map.get(key);
			securitiesForKey.removeIf(o -> o.getClientId().equals(clientId));
		}	
	}

	public List<Order> getAllOrders(){
		List<Order> ret = new LinkedList<Order>();
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
		sb.append("========toString==========\n");
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
		sb.append("========END-toString==========\n");
		return sb.toString();
	}

	static class BuyingComparator implements Comparator<Order> {

		public int compare(Order one, Order two) {
			if(!one.getSecurityId().equals(two.getSecurityId())){
				System.err.println("These orders are not comparable, they need to be for the same security");
				new IllegalArgumentException();
			}
			int naturalOrder = Double.compare(one.getValue() , two.getValue());
			if(naturalOrder == 0){
				return  - Long.compare(one.getTimestamp(), two.getTimestamp());
			}
			//The orders are listed Highest to Lowest on the Buy Side, natural sorting on value
			return naturalOrder;
		}
	}

	static class SellingComparator implements Comparator<Order> {

		public int compare(Order one, Order two) {
			if(!one.getSecurityId().equals(two.getSecurityId())){
				System.err.println("These orders are not comparable, they need to be for the same security");
				new IllegalArgumentException();
			}
			int naturalOrder = Double.compare(one.getValue() , two.getValue());
			if(naturalOrder == 0){
				return  - Long.compare(one.getTimestamp(), two.getTimestamp());
			}
			//The orders are listed Lowest to Highest on the Sell Side, we do the opposite of natural order on the value
			return -(naturalOrder);
		}
	}

}
