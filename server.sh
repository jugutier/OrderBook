#!/bin/bash
#export ROOT="." #Expected to be run at the git clone's location,otherwise adjust.
#export CLASSPATH="$ROOT/orderbook-server/target/classes:$ROOT/orderbook-api/target/classes:~/.m2/repository/com/rabbitmq/amqp-client/3.5.1/*"
#echo $CLASSPATH
#java com.example.orderbook.server.OrderBookServer 
java -jar orderbook-server/target/orderbook-server-1.0-SNAPSHOT.jar
