# OrderBook
Simple price-time priority order book. Supports multiple clients connecting concurrently from different computers (**See:** Adittional parameters - Server). 

Work in progress! :construction_worker:

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

###Setting it up to browse with eclipse (optional)
While in the "$ROOT" folder run:
`$> mvn eclipse:eclipse -DdownloadSources=true`

===

## Showtime
We will need at least 3 console windows open for this when running local server & clients. For conviniency use the scripts `rmi.sh` `server.sh` `client-joe.sh` `client-pete.sh` all in different console windows will provide a server and 2 valid clients for live testing.


### Rmi registry

`$> export CLASSPATH="$ROOT/orderbook-server/target/classes:$ROOT/orderbook-api/target/classes"`

`$> rmiregistry`

### Server

`$> export CLASSPATH="$ROOT/orderbook-server/target/classes:$ROOT/ordebook-api/target/classes"`

`$> java com.example.rmi.orderbook.server.OrderBookServer`

####Aditional parameters (Server)

Aditional parameters to host for clients on other computers should be set (optional). 

**Current defaults are:**

`HOSTNAME=localhost PORT=1099 SERVICE=OrderBookService`

change as you please and pass as command line argument.

The server supports a session start & end time (i.e Market operates during a given time interval). 

**To set that use:**

* START = A delayed start date in the format yyyy-MM-dd HH:mm:ss, default is current time.

* END = A session expiration date in the format yyyy-MM-dd HH:mm:ss, default is never expire.

for example:

`START="2016-08-24 01:34:30" END="2016-08-24 01:36:00"`

### Client

We support many clients running concurrently over the net. If your server is running on a different computer make sure you're passing the optional paramters:


`$> export CLASSPATH="$ROOT/orderbook-client/target/classes:$ROOT/orderbook-api/target/classes"`

`$> java com.example.rmi.orderbook.OrderBookClient CLIENT=myClientId`