package com.example.orderbook.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;

import com.example.orderbook.Command;
import com.example.orderbook.Order;
import com.example.orderbook.OrderBookClientHandleImpl;
import com.example.orderbook.server.OrderBookService;
import com.example.orderbook.util.Analyzer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class OrderBookClient {
	private static final String QUEUE_NAME = "com.example.orderbook";
	/** The handler is how this instance of the client gets messages from the service. **/
	private static OrderBookClientHandleImpl clientHandler;
	/** The unique identifier for this client. **/
	private static String clientId;
	/** The handler is how this instance of the client sends messages to the service. **/
	private static OrderBookService serverHandle;
	
	private static Connection connection;
	private static Channel channel;

	public static void main(String[] args) throws MalformedURLException, IOException, NotBoundException {
		try{
			Analyzer auxi = new Analyzer(args);
			Object port = auxi.get("PORT");
			Object hostname = auxi.get("HOSTNAME");
			clientId = (String) auxi.get("CLIENT");
			if(clientId == null){
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
		    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

			clientHandler = new OrderBookClientHandleImpl(clientId);

			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					System.out.println("The client had to quit. All pending orders will be cancelled");
					
				    try {
				    	finishSession();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			do{
				System.out.println("Enter your command(s) separated by a new line (return key):");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String line;
				while( !(line = br.readLine()).equals("") ){
					String[] input = line.split(" ");
					Command c = null;
					if(input.length == 1 && input[0].equalsIgnoreCase("LIST")){
						//listAllOrders(serverHandle, clientHandler);
						c = new Command(Command.LIST, null);
					}else{
						try{
							Order o = parseTransaction(clientId, serverHandle, clientHandler, new Analyzer(input));
							c = new Command(Command.TRADE, o);
						}catch(NullPointerException e){
							System.err.println("Error: " + e.getMessage());
							System.err.println("A transaction should look like this: "
									+ "SECURITY=AAPL AMOUNT=20 VALUE=20.19 ISBUYING=YES");
						}
					}
					System.out.println("publish");
					channel.basicPublish("", QUEUE_NAME, null, c.serialize());					
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
			serverHandle.clientExits(clientId);
			//TODO: notify client/server
			//clientHandler.unexport();
			channel.close();
		    connection.close();
	}

	private static Order parseTransaction(String clientId, OrderBookService serverHandle, OrderBookClientHandle clientHandler, Analyzer command) throws RemoteException{
		String securityId = Objects.requireNonNull(command.get("SECURITY"), "Must enter a SECURITY").toString();
		Integer amount = Integer.valueOf(Objects.requireNonNull(command.get("AMOUNT"), "Must enter an AMOUNT").toString());
		Double value = Double.valueOf(Objects.requireNonNull(command.get("VALUE"), "Must enter a VALUE").toString());
		boolean isBuying = Objects.requireNonNull(command.get("ISBUYING"), "Must indicate a ISBUYING (yes/no)").toString().equalsIgnoreCase("yes");
		
		Object orderId = command.get("ORDERID");
		Order ret;
		if(orderId != null){
			Long orderIdValue = Long.valueOf(orderId.toString());
			ret = new Order(orderIdValue, clientId, securityId, amount, value, isBuying, System.currentTimeMillis(), clientHandler);
		}else{
			ret = new Order( clientId, securityId, amount, value, isBuying, System.currentTimeMillis(), clientHandler);
		}
		
		return ret;

	}
		
	/* For live testing only. */
	private static void listAllOrders(OrderBookService serverHandle, OrderBookClientHandleImpl clientHandle) throws RemoteException {
		System.out.println("=============BEGIN==============");
		System.out.println("=== Debug: Server state - All current orders ===");
		final List<Order> orders =  serverHandle.listOrders();
		for (Order order : orders) {
			System.out.println(order);
		}
		System.out.println("===  Client state - Transaction Log ===");
		List<String> transactions = clientHandle.getTransactionsLog();
		for (String transaction : transactions) {
			System.out.println(transaction);
		}
		System.out.println("=============END==============");
	}

}
