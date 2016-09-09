package com.example.rmi.orderbook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;

import com.example.rmi.orderbook.client.OrderBookClientHandle;
import com.example.rmi.orderbook.server.OrderBookService;
import com.example.rmi.orderbook.util.Analyzer;

public class OrderBookClient {
	/** The handler is how this instance of the client gets messages from the service. **/
	private static OrderBookClientHandleImpl clientHandler;
	/** The unique identifier for this client. **/
	private static String clientId;
	/** The handler is how this instance of the client sends messages to the service. **/
	private static OrderBookService serverHandle;

	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		try{
			Analyzer auxi = new Analyzer(args);
			Object port = auxi.get("PORT");
			Object hostname = auxi.get("HOSTNAME");
			Object service = auxi.get("SERVICE");
			clientId = (String) auxi.get("CLIENT");
			if(clientId == null){
				System.err.println("Please authenticate by passing your clientId through cli arguments: CLIENT=myId");
				System.exit(-1);
			}
			//If we had an auth service, this is where we wouldd use it.

			auxi.dump();

			serverHandle = (OrderBookService) Naming.lookup(String.format("//%s:%s/%s",
					hostname, port, service));

			clientHandler = new OrderBookClientHandleImpl(clientId);

			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					System.out.println("The client had to quit. All pending orders will be cancelled");
					finishSession();
				}
			});

			do{
				System.out.println("Enter your command:");
				String[] input = new BufferedReader(new InputStreamReader(System.in)).readLine().split(" ");
				if(input.length == 1 ){
					if(input[0].equalsIgnoreCase("LIST")){
						listAllOrders(serverHandle, clientHandler);
					}else if(input[0].equalsIgnoreCase("UPDATE")){
						System.out.println("Enter the order to update (add the ID):");
						String[] updateInput = new BufferedReader(new InputStreamReader(System.in)).readLine().split(" ");
						updateOrder(clientId, serverHandle, clientHandler, new Analyzer(updateInput));
					}
				}else{
					parseTransaction(clientId, serverHandle, clientHandler, new Analyzer(input));
				}
			}while(true);
		} catch(Exception e){
			System.err.println("Can't connect now... Try again when trade sessions open");
			System.exit(-1);
		}finally {
			finishSession();
		}


	}


	private static void finishSession(){
		// Unexport any remaining client the callback manager if the server dies.
		try {
			serverHandle.clientExits(clientId);
			clientHandler.unexport();
		} catch (NoSuchObjectException e) {
			System.err.println("Error while unexporting handler");
		}catch (RemoteException e) {
			System.err.println("The server is down, your orders are probably canceled already.");
		}
	}

	private static void parseTransaction(String clientId, OrderBookService serverHandle, OrderBookClientHandle clientHandler, Analyzer command) throws RemoteException{
		if(command.get("ORDERID") != null){
			System.err.println("Must NOT enter an ORDERID");
			System.exit(-1);
		}
		String securityId = Objects.requireNonNull(command.get("SECURITY"), "Must enter a SECURITY").toString();
		Integer amount = Integer.valueOf(Objects.requireNonNull(command.get("AMOUNT"), "Must enter an AMOUNT").toString());
		Double value = Double.valueOf(Objects.requireNonNull(command.get("VALUE"), "Must enter a VALUE").toString());
		boolean isBuying = Objects.requireNonNull(command.get("ISBUYING"), "Must indicate a ISBUYING (yes/no)").toString().equalsIgnoreCase("yes");

		serverHandle.bookOrder(clientId, securityId, amount, value, isBuying, clientHandler);

	}
	
	private static void updateOrder(String clientId, OrderBookService serverHandle, OrderBookClientHandle clientHandler, Analyzer command) throws RemoteException{

		Long orderId = Long.valueOf(Objects.requireNonNull(command.get("ORDERID"), "Must enter an ORDERID").toString());
		String securityId = Objects.requireNonNull(command.get("SECURITY"), "Must enter a SECURITY").toString();
		Integer amount = Integer.valueOf(Objects.requireNonNull(command.get("AMOUNT"), "Must enter an AMOUNT").toString());
		Double value = Double.valueOf(Objects.requireNonNull(command.get("VALUE"), "Must enter a VALUE").toString());
		boolean isBuying = Objects.requireNonNull(command.get("ISBUYING"), "Must indicate a ISBUYING (yes/no)").toString().equalsIgnoreCase("yes");

		serverHandle.updateOrder(orderId, clientId, securityId, amount, value, isBuying, clientHandler);

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
