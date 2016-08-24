package com.example.rmi.orderbook.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.rmi.orderbook.OrderBookServant;
import com.example.rmi.orderbook.util.Analyzer;

public class OrderBookServer {
	//Format: Security id, amount, buying?
	//	private static final String[][] stubbedOrders = new String[][] {
	//        new String[] { "AAPL", "100", "yes", },
	//        new String[] { "BAC", "2500", "no" },
	//        new String[] { "JPM", "300", "yes" } };

	public static void main(final String[] args) throws RemoteException {

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
		Date endDate = parseTimeStamp(end);

		if(endDate != null){
			sleepUntil(end);
			servant.finishSession();
			System.exit(0);
		}
		System.out.println("Running forever");


	}
	
	private static void sleepUntil(String timestamp){
		Date untilDate = parseTimeStamp(timestamp);     

		Date now = new Date ();
		if(untilDate.compareTo(now) > 0){
			System.out.println("Sleeping until " + untilDate);
			try { Thread.sleep(untilDate.getTime()-now.getTime()); }catch (InterruptedException e) {}
		}else{
			System.out.println("Not sleeping");
		}
	}

	private static Date parseTimeStamp(String dateString){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = sdf.parse(dateString);
		} catch (ParseException e) {
			System.err.println("Date must be formatted in this way: yyyy-MM-dd HH:mm:ss");
			new IllegalArgumentException();
		}
		return date;

	}
}
