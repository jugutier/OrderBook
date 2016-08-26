# OrderBook
Simple price-time priority order book. Supports multiple clients connecting concurrently from different computers (**See:** Adittional parameters - Server). 

Work in progress! :construction_worker:

===
##How to run:

The project is uses Java 1.8 and Maven and rmiregistry, make sure they are present by running:

`$> java -version`

`$> mvn -v`

`$> which rmiregistry #Should Ouput /usr/bin/rmiregistry`



###Install Maven (If missing)
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

or simply import as a maven project through the eclipse-maven's plugin wizard.

===

## Showtime
We will need at least 3 console windows open for this when running local server & clients. For conviniency use the scripts `rmi.sh` `server.sh` `client-joe.sh` `client-pete.sh` all in different console windows will provide a server and 2 valid clients for live testing.

**Steps 1-2-3 can be ignored if you decide to use the scripts.**

### 1 - Rmi registry

`$> export CLASSPATH="$ROOT/orderbook-server/target/classes:$ROOT/orderbook-api/target/classes"`

`$> rmiregistry`

### 2 - Server

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

### 3 - Client

We support many clients running concurrently over the net. If your server is running on a different computer make sure you're passing the optional paramters:


`$> export CLASSPATH="$ROOT/orderbook-client/target/classes:$ROOT/orderbook-api/target/classes"`

`$> java com.example.rmi.orderbook.OrderBookClient CLIENT=myClientId`

===

## Tests

An OrderBook has several rules that it needs to apply, for that reason there are [Unit Tests](/src/test/java/com/example/rmi/orderbook/OrderBookServiceTest.java) the comments should aid to understand the behavior expected for this service.

Given that the project is client-server we only test our PriorityOrderBook where the logic rules are actually enforced. Any remote objects are mocked using the `Mockito` Java tool.

###About RMIRegistry

When working with remote objects through the network client and server need to know about objects that are sitting in a computer in some other location. 

Here is where rmiregistry helps us, when publishing a service it provides a Mocked Skeleton of the shared objects, with the same methods and takes care of the network handling transparently to the user. Client-server are programmed like any regular Java project, with the exception that concurrency now needs to be considered in critical sections.

To get that functionality we have to extend the `Remote` interface indicating that our Methods may through `RemoteException` if the connection drops. Also, all of our shared model objects need to implement `Serializable` to be able to be transported through the network.
