# OrderBook
Simple price-time priority order book

===
##How to run:

The project is setup to use Java 1.8 and Maven, make sure they are present by running

`$> java -version`

`$> mvn -v`

###Maven install
[Download](http://mirror.olnevhost.net/pub/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip) unzip and mv to /opt/, run

`$> export PATH=/opt/apache-maven-3.3.9/bin:$PATH`

validate again:

`$> mvn -v`.

###Compiling
Define the env variable ROOT (for every console window), pointing to where you cloned this repo:

`$> export ROOT="<path_to_repo_clone>"`

Once in "$ROOT" run:
`$> mvn clean install`

###Setting it up to navigate with eclipse (optional)
While in the "$ROOT" folder run:
`$> mvn eclipse:eclipse -DdownloadSources=true`

===

## Showtime
We will need 3 console windows open for this:
### Rmi registry

`$> export CLASSPATH="$ROOT/orderbook-server/target/classes:$ROOT/orderbook-api/target/classes"`

`$> rmiregistry`

### Server

`$> export CLASSPATH="$ROOT/orderbook-server/target/classes:$ROOT/ordebook-api/target/classes"`

`$> java com.example.rmi.orderbook.server.OrderBookServer`

### Client

`$> export CLASSPATH="$ROOT/orderbook-client/target/classes:$ROOT/orderbook-api/target/classes"`

`$> java com.example.rmi.orderbook.OrderBookClient`