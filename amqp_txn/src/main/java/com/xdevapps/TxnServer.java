package com.xdevapps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.AMQP.BasicProperties;

public class TxnServer implements AutoCloseable {
    
    private Connection connection;
    private Channel channel;
    private String requestQueue = "txn";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        try (TxnServer txnServer = new TxnServer(connectionInfo)){
            txnServer.start();
        }
    }

    public TxnServer(ConnectionInfo connectionInfo) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(connectionInfo.getHost());
        factory.setUsername(connectionInfo.getUsername());
        factory.setPassword(connectionInfo.getPassword());
        factory.setVirtualHost(connectionInfo.getVirtualHost());
        factory.setPort(connectionInfo.getPort());

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(requestQueue, false, false, false, null);
        channel.queuePurge(requestQueue);
        channel.basicQos(1);
    }

    public void start() throws IOException {
        Object monitor = new Object();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            BasicProperties props = new BasicProperties.Builder()
                                        .correlationId(delivery.getProperties().getCorrelationId())
                                        .build();
            String response = "";

            try {
                String request = new String(delivery.getBody(), StandardCharsets.UTF_8);
                response += getResponse(request);
            } catch (RuntimeException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), props, response.getBytes(StandardCharsets.UTF_8));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        channel.basicConsume(requestQueue, false, deliverCallback, (consumerTag) -> {});

        while (true) {
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getResponse(String request) throws InterruptedException {
        Thread.sleep(1500);
        Gson gson = new GsonBuilder().setDateFormat("MMddHHmmss").create();
        TxnRequestPayload requestPayload = gson.fromJson(request, TxnRequestPayload.class);
        
        /* TODO: Process request */
        TxnResponsePayload responsePayload = new TxnResponsePayload(requestPayload.getTxnId(), requestPayload.getTimestamp(), true, "");
        
        return gson.toJson(responsePayload);
    }

    @Override
    public void close() throws IOException, TimeoutException {
        connection.close();
        channel.close();
    }

}
