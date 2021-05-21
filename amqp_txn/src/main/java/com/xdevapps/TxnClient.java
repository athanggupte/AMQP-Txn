package com.xdevapps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;

public class TxnClient implements AutoCloseable {
    
    private Connection connection;
    private Channel channel;
    private String requestQueue = "txn";
    public static String getElementRandom(Set<? extends Object> set) {
        int size = set.size();
        int item = new Random().nextInt(size);
        int i = 0;
        for (Object entry:set) {
            if (i == item) return (String) entry;
            i++;
        }
        return "??";
    }
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

        ConnectionInfo connectionInfo = new ConnectionInfo();
        Random random = new Random();
        HashSet<String> typeOfTran = new HashSet<String>();
        typeOfTran.add("42 Cash Withdrawal");
        typeOfTran.add("50 Cash Deposit ONUS");
        typeOfTran.add("32 Cash Deposit OFFUS");
        typeOfTran.add("41 Balance Enquiry");
        String string_type_of_trans = getElementRandom(typeOfTran);
        while (true) {
            try(TxnClient txnClient = new TxnClient(connectionInfo)) {
                String jsonPayload = "{ 'ts':'" + new SimpleDateFormat("MMddHHmmss").format(new Date()) +
                        "' , 'Type of Transaction':'" + string_type_of_trans+"'";
                if(!string_type_of_trans.equals("41 Balance Enquiry")){
                    Random random1 = new Random();
                    jsonPayload += ", 'Amount':'" + String.format("%06d" , random1.nextInt(10000)) +"'";
                }
                jsonPayload += "}";
                String response = txnClient.requestTxn(jsonPayload);
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
