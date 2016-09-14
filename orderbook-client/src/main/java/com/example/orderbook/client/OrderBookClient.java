package com.example.orderbook.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.SerializationUtils;

import com.example.orderbook.OrderBookClientHandleImpl;
import com.example.orderbook.server.OrderBookService;
import com.example.orderbook.server.Request;
import com.example.orderbook.util.Analyzer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class OrderBookClient {
	private static final String REQUEST_QUEUE_NAME = "com.example.orderbook";
	private static String REPLY_QUEUE_NAME;
	/** The handler is how this instance of the client gets messages from the service. **/
	private static OrderBookClientHandleImpl clientHandler;
	/** The unique identifier for this client. **/
	private static String clientId;
	/** The handler is how this instance of the client sends messages to the service. **/
	private static OrderBookService serverHandle;
	
	private static Connection connection;
	private static Channel channel;
	private static QueueingConsumer consumer;

	public static void main(String[] args) throws MalformedURLException, IOException, NotBoundException {
		try{
			Analyzer auxi = new Analyzer(args);
			Object port = auxi.get("PORT");
			Object hostname = auxi.get("HOSTNAME");
			clientId = (String) auxi.get("CLIENT");
			if(clientId == null ){
				System.err.println("Please authenticate by passing your clientId through cli arguments: CLIENT=myId");
				System.exit(-1);
			}
			//If we had an auth service, this is where we wouldd use it.

			auxi.dump();

			ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost(hostname.toString());
		    factory.setPort(Integer.valueOf(port.toString()));
		    connection = factory.newConnection();
		    channel = connection.createChannel();
		    REPLY_QUEUE_NAME = channel.queueDeclare().getQueue();
		    consumer = new QueueingConsumer(channel);
		    channel.basicConsume(REPLY_QUEUE_NAME, true, consumer);
		    
			clientHandler = new OrderBookClientHandleImpl(clientId);
			serverHandle = new Request();

			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					System.out.println("The client had to quit. All pending orders will be cancelled");
					
				    try {
				    	finishSession();
					} catch (IOException e) {
						System.err.println("RabbitMQ is unavailable. "
								+ "The server couldn't be notified about our abnormal exit");
					}
				}
			});

			do{
				System.out.println("Enter your command(s) separated by a new line (return key):");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String line;
				while( !(line = br.readLine()).equals("") ){
					String[] input = line.split(" ");
					Request request = null;
					if(input.length == 1 && input[0].equalsIgnoreCase("LIST")){
						request = listAllOrders(clientHandler);
					}else{
						try{
							request = parseTransaction(clientId,serverHandle, new Analyzer(input));
							
						}catch(NullPointerException e){
							System.err.println("Error: " + e.getMessage());
							System.err.println("A transaction should look like this: "
									+ "SECURITY=AAPL AMOUNT=20 VALUE=20.19 ISBUYING=YES");
						}
					}
					System.out.println("publish");
					
					BasicProperties props = new BasicProperties
                            .Builder()
                            .correlationId(clientId)
                            .replyTo(REPLY_QUEUE_NAME)
                            .build();
					
					channel.basicPublish("", REQUEST_QUEUE_NAME, props, SerializationUtils.serialize(request)/*c.serialize()*/);
					//Wait for server's response.
					while (true) {
						QueueingConsumer.Delivery delivery = consumer.nextDelivery();
						if (delivery.getProperties().getCorrelationId().equals(clientId)){
							Response response = (Response) SerializationUtils.deserialize(delivery.getBody());
							System.out.println("response: " + response);
							clientHandler.process(response);
							break;
						}
					}
				}
			}while(true);
		} catch(Exception e){
			System.err.println("Can't connect now... Try again when trade sessions open" + e);
			System.exit(-1);
		}finally {
			finishSession();
		}


	}


	private static void finishSession() throws IOException{
		// Unexport any remaining client the callback manager if the server dies.
			//serverHandle.clientExits(clientId);
			//TODO: notify client/server
			//clientHandler.unexport();
			channel.close();
		    connection.close();
	}

	private static Request parseTransaction(String clientId, OrderBookService serverHandle, Analyzer command) throws RemoteException{
		String securityId = Objects.requireNonNull(command.get("SECURITY"), "Must enter a SECURITY").toString();
		Integer amount = Integer.valueOf(Objects.requireNonNull(command.get("AMOUNT"), "Must enter an AMOUNT").toString());
		Double value = Double.valueOf(Objects.requireNonNull(command.get("VALUE"), "Must enter a VALUE").toString());
		boolean isBuying = Objects.requireNonNull(command.get("ISBUYING"), "Must indicate a ISBUYING (yes/no)").toString().equalsIgnoreCase("yes");
		
		Object orderId = command.get("ORDERID");
		if(orderId != null){
			Long orderIdValue = Long.valueOf(orderId.toString());
			serverHandle.updateOrder(orderIdValue, clientId, securityId, amount, value, isBuying);
		}else{
			try{
				serverHandle.bookOrder(clientId, securityId, amount, value, isBuying);
			}catch(IllegalArgumentException e){
				System.err.println(e.getMessage());
			}
		}		
		return (Request)serverHandle;

	}
		
	/* For live testing only. */
	private static Request listAllOrders(OrderBookClientHandleImpl clientHandle) throws RemoteException {
		System.out.println("=============BEGIN==============");
		System.out.println("===  Client state - Transaction Log ===");
		List<String> transactions = clientHandle.getTransactionsLog();
		for (String transaction : transactions) {
			System.out.println(transaction);
		}
		System.out.println("=============END==============");
		Request ret = new Request();
		ret.listOrders();
		return ret;
	}

}
