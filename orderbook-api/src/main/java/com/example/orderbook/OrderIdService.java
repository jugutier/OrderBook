package com.example.orderbook;

public class OrderIdService {

	/** Simulates an auto incremented value from a database somewhere. **/
	private static Long currentOrderId = 0L;
	private static OrderIdService instance;

	private OrderIdService(){};/** So no one can instantiate a second copy **/

	public static OrderIdService getInstance() {
		if (instance == null) {
			synchronized (OrderIdService.class){
				if (instance == null) {
					instance = new OrderIdService();
				}
			}
		}
		return instance ;
	}
	
	/** Generates a system-wide unique order id **/
	public Long getId() {
		synchronized(currentOrderId){
			return currentOrderId++;
		}
	}

}
