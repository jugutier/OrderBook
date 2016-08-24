package com.example.rmi.orderbook;

import java.net.MalformedURLException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.example.rmi.orderbook.util.Analyzer;

public class OrderBookClient {

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException,
            NoSuchObjectException {

            Analyzer auxi = new Analyzer(args);
            Object port = auxi.get("PORT");
            Object hostname = auxi.get("HOSTNAME");
            Object service = auxi.get("SERVICE");
            System.out.println(String.format("//%s:%s/%s",
                    hostname, port, service));
    }
       
}
