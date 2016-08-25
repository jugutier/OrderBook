package com.example.rmi.orderbook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.example.rmi.orderbook.client.OrderBookClientHandle;
import com.example.rmi.orderbook.server.OrderBookService;
import com.example.rmi.orderbook.util.Analyzer;

public class OrderBookClient {
	private static List<OrderBookClientHandleImpl> handlers;

	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		handlers = new LinkedList<>();
		try{
			Analyzer auxi = new Analyzer(args);
			Object port = auxi.get("PORT");
			Object hostname = auxi.get("HOSTNAME");
			Object service = auxi.get("SERVICE");
			String clientId = (String) auxi.get("CLIENT");
			if(clientId == null){
				System.err.println("Please authenticate by passing your clientId through cli arguments: CLIENT=myId");
				System.exit(-1);
			}

			auxi.dump();

			final OrderBookService serverHandle = (OrderBookService) Naming.lookup(String.format("//%s:%s/%s",
					hostname, port, service));

			final OrderBookClientHandle clientHandler = new OrderBookClientHandleImpl(clientId);

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
				System.out.println("Your command is" + input);
				parseCommand(clientId, serverHandle, clientHandler, new Analyzer(input));
				
			}while(true);
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Can't connect now... Try again when sessions open");
			System.exit(-1);
		}finally {
			finishSession();
		}


	}


	private static void finishSession(){
		for (OrderBookClientHandleImpl handler : handlers) {
			// Unexport any remaining client callbacks if the server dies.
			try {
				handler.unexport();
			} catch (NoSuchObjectException e) {
				System.err.println("Error while unexporting handler");
			}
		}
	}

	private static void parseCommand(String clientId, OrderBookService serverHandle, OrderBookClientHandle clientHandler, Analyzer command) throws RemoteException{
		
		String securityId = Objects.requireNonNull(command.get("SECURITY"), "Must enter a SECURITY").toString();
		Integer amount = Integer.valueOf(Objects.requireNonNull(command.get("AMOUNT"), "Must enter an AMOUNT").toString());
		Double value = Double.valueOf(Objects.requireNonNull(command.get("VALUE"), "Must enter a VALUE").toString());
		boolean isBuying = Objects.requireNonNull(command.get("ISBUYING"), "Must indicate a ISBUYING (yes/no)").toString().equalsIgnoreCase("yes");

		serverHandle.bookOrder(clientId, securityId, amount, value, isBuying, clientHandler);

	}

	private static void testListOrders(OrderBookService serverHandle) throws RemoteException {
		final Set<Order> orders = serverHandle.listOrders();
		for (Order order : orders) {
			System.out.println(order);
		}
	}

}
