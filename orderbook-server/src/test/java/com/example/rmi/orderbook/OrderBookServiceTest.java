package com.example.rmi.orderbook;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.rmi.RemoteException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.example.rmi.orderbook.client.OrderBookClientHandle;

/**
 * Tests for edge cases in the PriorityOrderBook.
 * 
 * Some sources consulted:
 * http://money.stackexchange.com/questions/45983/price-time-priority-order-matching-limit-order-starvation
 * http://money.stackexchange.com/questions/15156/how-do-exchanges-match-limit-orders
 */
 public class OrderBookServiceTest {
	 
	private static final String SELLER1 = "seller1";
	private static final String SELLER2 = "seller2";
	private static final String BUYER1 = "buyer1";
	private static final String BUYER2 = "buyer2";
	
	private static final String SECURITY = "AAPL";
	private OrderBookClientHandle clientHandler;
	private PriorityOrderBook book;
	
 	@Before
    public final void before() {
 		clientHandler = mock(OrderBookClientHandle.class);
 		book = new PriorityOrderBook();
    }
 	/**
 	 * Two identical sales are queued while a valid buyer arrives.
 	 * 
 	 * Expected: The first sale order placed should be favored (via timestamp).
 	 * @throws RemoteException
 	 */
	@Test
    public void testTwoIdenticalSales() throws RemoteException {
//		Order saleOrder1 = new Order(SELLER1, SECURITY, 1, 10.0,
//				false , System.currentTimeMillis(), clientHandler);
//		Order saleOrder2 = new Order(SELLER2, SECURITY, 1, 10.0,
//				false , System.currentTimeMillis(), clientHandler);
//		
//		book.sell(saleOrder1);
//		book.sell(saleOrder2);
//		
//		Order buyOrder = new Order(BUYER1, SECURITY, 1, 10.0,
//				true , System.currentTimeMillis(), clientHandler);
//		book.buy(buyOrder);
		
    }
	
	/**
	 * Two identical purchase orders are queued while a valid seller arrives.
	 * 
	 * Expected: The first buy order placed should be favored (via time stamp).
	 * @throws RemoteException
	 */
	@Test
    public void testTwoIdenticalPurchases() throws RemoteException {
		Order buyOrder1 = new Order(BUYER1, SECURITY, 1, 10.0,
				true , System.currentTimeMillis(), clientHandler);
		Order buyOrder2 = new Order(BUYER2, SECURITY, 1, 10.0,
				true , System.currentTimeMillis(), clientHandler);
		
		book.buy(buyOrder1);
		book.buy(buyOrder2);
		
		Order sellOrder = new Order(SELLER1, SECURITY, 1, 10.0,
				false , System.currentTimeMillis(), clientHandler);
		book.sell(sellOrder);
		Set<Order> remainingOrders = book.getAllOrders();
		assertEquals(remainingOrders.size(),1);		
		assertEquals(remainingOrders.iterator().next().getClientId(),BUYER2);
    }
	
	/**
	 * A buyer places an order for more than the highest offer queued.
	 * 
	 * Expected: The transaction should occur at the highest offer 
	 * (i.e less than what the buyer actually placed.)
	 * @throws RemoteException
	 */
	@Test
    public void testBuyerForHighestOffer() throws RemoteException {
    }
	
	/**
	 * A seller places an order for less than the lowest bid queued.
	 * 
	 * Expected: The transaction should occur for the HIGHEST bidder
	 * at the sellers price (LOWEST)
	 * @throws RemoteException
	 */
	@Test
    public void testSellerForLowestBid() throws RemoteException {
    }

}