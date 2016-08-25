#!/bin/bash
export ROOT="." #Expected to be run at the git clone's location,otherwise adjust. 
export CLASSPATH="$ROOT/orderbook-server/target/classes:$ROOT/orderbook-api/target/classes"
echo "Running rmiregistry"
rmiregistry
