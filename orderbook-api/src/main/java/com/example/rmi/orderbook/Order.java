package com.example.rmi.orderbook;

import java.io.Serializable;

import com.example.rmi.orderbook.client.OrderBookClientHandle;
import com.example.rmi.orderbook.util.Analyzer;
/**
 * Abstraction for an Order. Supports concurrent users by 
 * synchronizing the amount of securities placed in this
 * order on get and set.
 * 
 * Also supports updating by holding a unique orderId and
 * a priorityTime used for ordering, as well as a displayTime
 * to be the latest update time.
 */
public class Order implements Serializable {
	private static final long serialVersionUID = 8822833371248140397L;

	private Long orderId;
	private String clientId;
	private String securityId;
	private Integer units;
	private Double value;
	private boolean isBuying;
	private Long priorityTime;
	private Long displayTime;
	private OrderBookClientHandle clientHandle;

	/**
	 *  This constructor is to facilitate creations of orders to be updated.
	 */
	public Order (Long orderId, String clientId, String securityId, Integer amount, Double value, boolean isBuying, long timestamp, OrderBookClientHandle clientHandle){
		this.orderId = orderId;
		this.clientId = clientId;
		this.securityId = securityId;
		this.units = amount;
		this.value = value;
		this.isBuying = isBuying;
		this.priorityTime = timestamp;
		this.displayTime = timestamp;
		this.clientHandle = clientHandle;
	}
	
	public Order (String clientId, String securityId, Integer amount, Double value, boolean isBuying, long timestamp, OrderBookClientHandle clientHandle){
		this.orderId = OrderIdService.getInstance().getId();
		this.clientId = clientId;
		this.securityId = securityId;
		this.units = amount;
		this.value = value;
		this.isBuying = isBuying;
		this.priorityTime = timestamp;
		this.displayTime = timestamp;
		this.clientHandle = clientHandle;
	}

	public Long getOrderId() {
		return orderId;
	}
	
	public String getClientId() {
		return clientId;
	}

	public String getSecurityId() {
		return securityId;
	}

	public Integer getUnits() {
		synchronized(units){
			return units;
		}
	}
	
	public void setUnits(Integer units) {
		synchronized(units){
			this.units = units;
		}
	}
	
	public void setDisplayTime(Long milliseconds) {
		synchronized(displayTime){
			this.displayTime = milliseconds;
		}
	}
	
	public Double getValue() {
		return value;
	}

	public boolean isBuying() {
		return isBuying;
	}

	public Long getDisplayTime() {
		synchronized(displayTime){
			return displayTime;
		}
	}
	
	public Long getPriorityTime() {
		synchronized(priorityTime){
			return priorityTime;
		}
	}

	public OrderBookClientHandle getClientHandle() {
		return clientHandle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (orderId == null) {
			if (other.orderId != null)
				return false;
		} else if (!orderId.equals(other.orderId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ORDERID="+ orderId +" CLIENT=" + clientId + " SECURITY=" + securityId
				+ " AMOUNT=" + units + " VALUE=" + value + " ISBUYING="
				+ (isBuying? "YES":"NO") + ", TIMESTAMP=" + Analyzer.milliSecondsToTimestamp(displayTime);
	}

}
