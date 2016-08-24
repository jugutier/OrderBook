package com.example.rmi.orderbook;

import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 8822833371248140397L;
	
	private String securityId;
	private Double value;
	private boolean isBuying;
	//new String[] { "AAPL", "100", "yes", }

	public Order(String[] order) {
		if(order.length != 3){
			throw new IllegalArgumentException("Incorrect Order format, try: Security id, amount, buying?");
		}
		securityId = order[0];
		value = Double.valueOf(order[1]);
		isBuying = order[2].equals("yes");
	}

	public String getSecurityId() {
		return securityId;
	}

	public Double getValue() {
		return value;
	}

	public boolean isBuying() {
		return isBuying;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isBuying ? 1231 : 1237);
		result = prime * result
				+ ((securityId == null) ? 0 : securityId.hashCode());
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
		if (isBuying != other.isBuying)
			return false;
		if (securityId == null) {
			if (other.securityId != null)
				return false;
		} else if (!securityId.equals(other.securityId))
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
		return "Order [securityId=" + securityId + ", value=" + value
				+ ", isBuying=" + isBuying + "]";
	}
	
	

}
