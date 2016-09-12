package com.example.orderbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Command implements Serializable{
	private static final long serialVersionUID = -1196396812023678389L;
	public static final String LIST = "list";
	public static final String TRADE = "trade";
	
	private String type;
	private Order payload;
	
	public Command(final String type, final Order payload){
		this.type = type;
		this.payload = payload;
	}

	public String getType() {
		return type;
	}

	public Object getPayload() {
		return payload;
	}	
	//http://stackoverflow.com/questions/3736058/java-object-to-byte-and-byte-to-object-converter-for-tokyo-cabinet
	public byte[] serialize() throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(this);
	    return out.toByteArray();
	}
	
	public static Command deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return (Command) is.readObject();
	}

}
