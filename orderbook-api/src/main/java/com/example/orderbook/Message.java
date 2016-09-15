package com.example.orderbook;

import java.io.Serializable;
/**
 * Abstraction to represent a message between the client and server.
 */
public abstract class Message implements Serializable{
	private static final long serialVersionUID = -1196396812023678389L;

	private static final String delimiter = ";";

	private String type;
	private Object payload;
	private Double value;

	public Message(){};

	public Message(final String type, final Object payload){
		this.type = type;
		this.payload = payload;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}
	
	public void setValue(Double value) {
		this.value = value;		
	}
	
	public Double getValue(){
		return value;
	}
	
	public void pack(String ... args){
		StringBuffer sb = new StringBuffer(args.length);
		for (String arg : args) {
			sb.append(arg)
			.append(delimiter);
		}
		setPayload(sb.toString());
	}

	public String[] unpack(){
		if(payload == null){
			return null;
		}
		return ((String)payload).split(delimiter);
	}

	@Override
	public String toString() {
		return "Message [type=" + type + ", payload=" + payload + ", value=" + value + "]";
	}

}
