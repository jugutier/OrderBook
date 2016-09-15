package com.example.orderbook.server;

import java.util.Date;

import org.apache.commons.lang.SerializationUtils;

import com.example.orderbook.OrderBookServant;
import com.example.orderbook.client.Response;
import com.example.orderbook.util.Analyzer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

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
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(QUEUE_NAME, false, consumer);

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
			
			System.out.println("Service bound");

			while (true) {
				Response response = null;

				QueueingConsumer.Delivery delivery = consumer.nextDelivery();

				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties
						.Builder()
				.correlationId(props.getCorrelationId())
				.build();

				try {
					 Request request = (Request) SerializationUtils.deserialize(delivery.getBody());
					 //System.out.println("request: "+ request);
					response = servant.process(request);
				}
				catch (Exception e){
					System.err.println("Error when processing your request. " + e.toString());
				}
				finally {  
					channel.basicPublish( "", props.getReplyTo(), replyProps, SerializationUtils.serialize(response));
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			}

		}catch(Exception e){
			System.err.println("Couldn't find RabbitMQ. Make sure it's up and running.");
			System.exit(-1);
		}finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (Exception ignore) {}
			}
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
