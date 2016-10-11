#!/bin/bash
#export ROOT="." #Expected to be run at the git clone's location,otherwise adjust. 
#export CLASSPATH="$ROOT/orderbook-client/target/classes:$ROOT/orderbook-api/target/classes"
#java com.example.orderbook.client.OrderBookClient CLIENT=PETE
java -jar orderbook-client/target/orderbook-client-1.0-SNAPSHOT.jar CLIENT=PETE
