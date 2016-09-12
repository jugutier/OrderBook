package com.example.orderbook.server;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.SerializationUtils;

import com.example.orderbook.Command;
import com.example.orderbook.OrderBookServant;
import com.example.orderbook.util.Analyzer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class OrderBookServer {
	private static final String QUEUE_NAME = "com.example.orderbook";
	
	private static Connection connection;
	private static Channel channel;

	public static void main(final String[] args) {
		try{
			final Analyzer auxi = new Analyzer(args);
			final int port = Integer.valueOf(auxi.get("PORT").toString());
			final String hostname = auxi.get("HOSTNAME").toString();
			auxi.dump();

			final String start = auxi.get("START").toString();
			sleepUntil(start);
			
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(hostname);
		    factory.setPort(port);
			connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			
			channel.basicQos(1);
			final OrderBookServant servant = new OrderBookServant();
			
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
						throws IOException {
					try {
						String message = new String(body, "UTF-8");
				        System.out.println(" [x] Received '" + message + "'");
				        Command c = (Command) SerializationUtils.deserialize(body);
						//Command c = Command.deserialize(body);
						
						servant.processCommand(c);
					} catch (Exception e) {
						System.err.println("Error when processing your command. " + e);
					}finally{
						System.out.println("finally");
						servant.finishSession();
						System.exit(0);
					}
					channel.basicAck(envelope.getDeliveryTag(), false);
				}
			};
			channel.basicConsume(QUEUE_NAME, true, consumer);			

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
				System.out.println("endate");
				servant.finishSession();
				System.exit(0);
			}
			System.out.println("Running forever");

		}catch(IOException e){
			System.err.println("Couldn't find RabbitMQ. Make sure it's up and running.");
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
