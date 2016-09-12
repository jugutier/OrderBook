package com.example.orderbook;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = -1196396812023678389L;
	
	private static final String delimiter = ";";
	
	private String type;
	private Object payload;
	
	public Message(final String type, final Object payload){
		this.type = type;
		this.payload = payload;
	}

	public String getType() {
		return type;
	}

	public Object getPayload() {
		return payload;
	}
	
	public void setPayload(Object payload) {
		this.payload = payload;
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
		return ((String)payload).split(delimiter);
	}
//	//http://stackoverflow.com/questions/3736058/java-object-to-byte-and-byte-to-object-converter-for-tokyo-cabinet
//	public byte[] serialize() throws IOException {
//	    ByteArrayOutputStream out = new ByteArrayOutputStream();
//	    ObjectOutputStream os = new ObjectOutputStream(out);
//	    os.writeObject(this);
//	    return out.toByteArray();
//	}
//	
//	public static Command deserialize(byte[] data) throws IOException, ClassNotFoundException {
//	    ByteArrayInputStream in = new ByteArrayInputStream(data);
//	    ObjectInputStream is = new ObjectInputStream(in);
//	    Object o =  is.readObject();
//	    return (Command) o;
//	}

	@Override
	public String toString() {
		return "Command [type=" + type + ", payload=" + payload + "]";
	}
	
}
