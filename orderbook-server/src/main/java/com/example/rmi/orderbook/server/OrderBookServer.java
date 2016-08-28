package com.example.rmi.orderbook.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;

import com.example.rmi.orderbook.OrderBookServant;
import com.example.rmi.orderbook.util.Analyzer;

public class OrderBookServer {

	public static void main(final String[] args) throws RemoteException {
		try{
			final Analyzer auxi = new Analyzer(args);
			final int port = Integer.valueOf(auxi.get("PORT").toString());
			final String hostname = auxi.get("HOSTNAME").toString();
			final String service = auxi.get("SERVICE").toString();
			auxi.dump();

			final String start = auxi.get("START").toString();
			sleepUntil(start);


			final Registry registry = LocateRegistry.getRegistry(hostname, port);
			final OrderBookServant servant = new OrderBookServant();

			registry.rebind(service, (OrderBookService)servant);
			System.out.println("Service bound");

			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					System.out.println("The server had to quit. All pending orders will be cancelled");
					servant.finishSession();
				}
			});

			final String end = auxi.get("END").toString();
			System.out.println("Session ends at: " + end);
			Date endDate = Analyzer.parseTimeStamp(end);

			if(endDate != null){
				sleepUntil(end);
				servant.finishSession();
				System.exit(0);
			}
			System.out.println("Running forever");

		}catch(ConnectException e){
			System.err.println("Couldn't find RMIRegistry. Make sure it's up and running.");
			System.exit(-1);
		}
	}

	private static void sleepUntil(String timestamp){
		Date untilDate = Analyzer.parseTimeStamp(timestamp);     

		Date now = new Date ();
		if(untilDate.compareTo(now) > 0){
			System.out.println("Sleeping until " + untilDate);
			try { Thread.sleep(untilDate.getTime()-now.getTime()); }catch (InterruptedException e) {}
		}else{
			System.out.println("Not sleeping");
		}
	}


}
