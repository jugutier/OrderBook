package com.example.rmi.orderbook;

import java.io.Serializable;

import com.example.rmi.orderbook.client.OrderBookClientHandle;
import com.example.rmi.orderbook.util.Analyzer;
/**
 * Abstraction for an Order. Supports concurrent users by 
 * synchronizing the amount of securities placed in this
 * order on get and set.
 */
public class Order implements Serializable {
	private static final long serialVersionUID = 8822833371248140397L;

	private String clientId;
	private String securityId;
	private Integer amount;
	private Double value;
	private boolean isBuying;
	private long timestamp;
	private OrderBookClientHandle clientHandle;

	public Order (String clientId, String securityId, Integer amount, Double value, boolean isBuying, long timestamp, OrderBookClientHandle clientHandle){
		this.clientId = clientId;
		this.securityId = securityId;
		this.amount = amount;
		this.value = value;
		this.isBuying = isBuying;
		this.timestamp = timestamp;
		this.clientHandle = clientHandle;
	}

	public String getClientId() {
		return clientId;
	}

	public String getSecurityId() {
		return securityId;
	}

	public Integer getAmount() {
		synchronized(amount){
			return amount;
		}
	}
	
	public void setAmount(Integer amount) {
		synchronized(amount){
			this.amount = amount;
		}
	}
	
	public Double getValue() {
		return value;
	}

	public boolean isBuying() {
		return isBuying;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public OrderBookClientHandle getClientHandle() {
		return clientHandle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result
				+ ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + (isBuying ? 1231 : 1237);
		result = prime * result
				+ ((securityId == null) ? 0 : securityId.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (isBuying != other.isBuying)
			return false;
		if (securityId == null) {
			if (other.securityId != null)
				return false;
		} else if (!securityId.equals(other.securityId))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Order [clientId=" + clientId + ", securityId=" + securityId
				+ ", amount=" + amount + ", value=" + value + ", isBuying="
				+ isBuying + ", timestamp=" + Analyzer.milliSecondsToTimestamp(timestamp) + "]";
	}

}
