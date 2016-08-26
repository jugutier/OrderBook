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
		Order saleOrder1 = new Order(SELLER1, SECURITY, 1, 10.0,
				false , System.currentTimeMillis(), clientHandler);
		Order saleOrder2 = new Order(SELLER2, SECURITY, 1, 10.0,
				false , System.currentTimeMillis(), clientHandler);

		book.sell(saleOrder1);
		book.sell(saleOrder2);

		Order buyOrder = new Order(BUYER1, SECURITY, 1, 10.0,
				true , System.currentTimeMillis(), clientHandler);
		book.buy(buyOrder);

		Set<Order> remainingOrders = book.getAllOrders();
		//Only one order should be in the book
		assertEquals(1, remainingOrders.size());
		//and it should be order2 (because we favored the first one).
		assertEquals(saleOrder2 , remainingOrders.iterator().next());

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
		//Only one order should be in the book
		assertEquals(remainingOrders.size(),1);		
		//and it should be order2 (because we favored the first one).
		assertEquals(remainingOrders.iterator().next(), buyOrder2);
	}

	/**
	 * A buyer places an order for more than the highest offer queued.
	 * 
	 * Expected: The transaction should occur at the highest offer 
	 * (i.e less than what the buyer actually placed.)
	 * @throws RemoteException
	 */
	@Test
	public void testBuyerPaysLessThanExpected() throws RemoteException {
		//BUYS and pays less than expected (20.10)
//		ISBUYING=NO SECURITY=AAPL AMOUNT=1 VALUE=20.10  AT="2016-08-24 11:36:00"
//		ISBUYING=YES SECURITY=AAPL AMOUNT=1 VALUE=40  AT="2016-08-24 11:36:01"
		Order sellOrder = new Order(SELLER1, SECURITY, 1, 20.10,
				false , System.currentTimeMillis(), clientHandler);

		assertEquals(null, book.sell(sellOrder));
		
		Order buyOrder = new Order(BUYER1, SECURITY, 1, 40.0,
				true , System.currentTimeMillis(), clientHandler);

		book.buy(buyOrder);
		
		Double transactionValue = book.sell(sellOrder);		
		assertEquals(new Double(20.10), transactionValue);		
	}

	/**
	 * A seller places an order for less than the lowest bid queued.
	 * 
	 * Expected: The transaction should occur for the HIGHEST bidder
	 * at the sellers price (LOWEST)
	 * @throws RemoteException
	 */
	@Test
	public void testSellerGetsMoreThanExpected() throws RemoteException {
		//Sells and gets more than expected (20.21)
//		ISBUYING=YES SECURITY=AAPL AMOUNT=1 VALUE=20.21  AT="2016-08-24 11:36:00"
//		ISBUYING=NO SECURITY=AAPL AMOUNT=1 VALUE=20.10  AT="2016-08-24 11:36:01"
					
		Order buyOrder = new Order(BUYER1, SECURITY, 1, 20.21,
				true , System.currentTimeMillis(), clientHandler);

		assertEquals(null, book.buy(buyOrder));
		
		Order sellOrder = new Order(SELLER1, SECURITY, 1, 20.10,
				false , System.currentTimeMillis(), clientHandler);

		Double transactionValue = book.sell(sellOrder);		
		assertEquals(new Double(20.21), transactionValue);
	}

	/**
	 * A sell order arrives for less units than a queued applicable buy order.
	 * 
	 * Expected: We partially fulfill the order thus satisfying the buyer 
	 * and partially completing the sellers order.
	 * @throws RemoteException
	 */
	@Test
	public void partialSale() throws RemoteException {
		Order buyOrder = new Order(BUYER1, SECURITY, 2, 10.0,
				true , System.currentTimeMillis(), clientHandler);

		book.buy(buyOrder);

		Order sellOrder = new Order(SELLER1, SECURITY, 1, 9.0,
				false , System.currentTimeMillis(), clientHandler);
		book.sell(sellOrder);
		
		Set<Order> remainingOrders = book.getAllOrders();
		//The buy order remains
		assertEquals(1,remainingOrders.size());
		
		Order remainingOrder = remainingOrders.iterator().next();
		// The same one we have.		
		assertEquals(buyOrder, remainingOrder);
		
		//but with less units
		assertEquals(new Integer(1), remainingOrder.getAmount());
	}

	/**
	 * A buy order arrives for less units than a queued applicable sale order.
	 * 
	 * Expected: We partially fulfill the order thus satisfying the buyer 
	 * and partially completing the sellers order.
	 * @throws RemoteException
	 */
	@Test
	public void partialBuy() throws RemoteException {
		Order sellOrder = new Order(SELLER1, SECURITY, 2, 9.0,
				false , System.currentTimeMillis(), clientHandler);

		book.sell(sellOrder);

		Order buyOrder = new Order(BUYER1, SECURITY, 1, 10.0,
				true , System.currentTimeMillis(), clientHandler);
		book.buy(buyOrder);
		Set<Order> remainingOrders = book.getAllOrders();
		System.out.println(book.toString());
		//The sale order remains
		assertEquals(1, remainingOrders.size());
		
		Order remainingOrder = remainingOrders.iterator().next();
		// The same one we have.		
		assertEquals(sellOrder, remainingOrder);
		
		//but with less units
		assertEquals(new Integer(1), remainingOrder.getAmount());
	}
	
	/**
	 * Users aren't allowed to make transactions with themselves.
	 * They could mess up the market!
	 * @throws RemoteException
	 * 			if the connection drops
	 */
	@Test
	public void cantBuyToYourself() throws RemoteException {
		Order sellOrder = new Order(SELLER1, SECURITY, 1, 10.0,
				false , System.currentTimeMillis(), clientHandler);
		book.sell(sellOrder);

		Order buyOrder = new Order(SELLER1, SECURITY, 1, 10.0,
				true , System.currentTimeMillis(), clientHandler);
		book.buy(buyOrder);
		
		Set<Order> remainingOrders = book.getAllOrders();
		//No match occurred and both orders are placed.
		assertEquals(2, remainingOrders.size());
	}
	
	/**
	 * Users aren't allowed to make transactions with themselves.
	 * They could mess up the market!
	 * @throws RemoteException
	 * 			if the connection drops
	 */
	@Test
	public void cantSellToYourself() throws RemoteException {
		Order buyOrder = new Order(SELLER1, SECURITY, 1, 10.0,
				true , System.currentTimeMillis(), clientHandler);
		book.buy(buyOrder);
		
		Order sellOrder = new Order(SELLER1, SECURITY, 1, 10.0,
				false , System.currentTimeMillis(), clientHandler);
		book.sell(sellOrder);
		
		Set<Order> remainingOrders = book.getAllOrders();
		//No match occurred and both orders are placed.
		assertEquals(2, remainingOrders.size());
	}

}