package com.xdevapps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

public class TxnClient implements AutoCloseable {
    
    private Connection connection;
    private Channel channel;
    private String requestQueue = "txn";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        Random random = new Random();
        Gson gson = new GsonBuilder().setDateFormat("MMddHHmmss").create();

        while (true) {
            try(TxnClient txnClient = new TxnClient(connectionInfo)) {
                TxnRequestPayload payload = new TxnRequestPayload(UUID.randomUUID().toString(), new Date(), 42, 1000);
                String payloadString = gson.toJson(payload);

                System.out.println(payloadString);

                String response = txnClient.requestTxn(payloadString);
                System.out.println(" [.] Got `" + response + "`");
            }
            Thread.sleep(random.nextInt(4000)+1000);
        }
    }

    public TxnClient(ConnectionInfo connectionInfo) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(connectionInfo.getHost());
        factory.setUsername(connectionInfo.getUsername());
        factory.setPassword(connectionInfo.getPassword());
        factory.setVirtualHost(connectionInfo.getVirtualHost());
        factory.setPort(connectionInfo.getPort());

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public String requestTxn(String message) throws IOException, InterruptedException {
        final String correlationId = UUID.randomUUID().toString();

        String responseQueue = channel.queueDeclare().getQueue();
        BasicProperties props = new BasicProperties.Builder()
                                    .correlationId(correlationId)
                                    .replyTo(responseQueue)
                                    .build();
        channel.basicPublish("", requestQueue, props, message.getBytes(StandardCharsets.UTF_8));

        final BlockingQueue<String> resultQueue = new ArrayBlockingQueue<>(1);

        String consumerTag = channel.basicConsume(responseQueue, true, /* delivery callback */(consumer_tag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                resultQueue.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        }, /* cancel callback */(consumer_tag) -> {});

        String response = resultQueue.take();
        channel.basicCancel(consumerTag);
        return response;
    }

    @Override
    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

}
