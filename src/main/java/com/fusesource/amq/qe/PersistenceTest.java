package com.fusesource.amq.qe;

import com.fusesource.amq.qe.util.AsyncConsumer;
import com.fusesource.amq.qe.util.Producer;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import java.util.concurrent.TimeUnit;


/**
 * Created by kearls on 07/08/14.
 */
public class PersistenceTest {
    protected static final String TARGET_QUEUE_NAME = "queue://JOBS.suspend";
    protected static String brokerURL = "tcp://localhost:61616";
    protected static String amqUser ="admin";
    protected static String amqPassword ="admin";
    private static String ACTION;

    private static Integer totalMessages = 1000;
    public static String jobs[] = new String[]{"delete", "suspend"};

    public void receiveMessages() throws Exception {
        AsyncConsumer consumer = new AsyncConsumer(jobs, totalMessages, TARGET_QUEUE_NAME, brokerURL, amqUser, amqPassword);

        for (String jobName : jobs) {
            Session session = consumer.getSession();
            String messageQueueName = "JOBS." + jobName;
            Destination destination = session.createQueue(messageQueueName);
            MessageConsumer messageConsumer = session.createConsumer(destination);
            System.out.println("Setting listener for " + messageQueueName);
            messageConsumer.setMessageListener(consumer);
        }

        consumer.receivedCount.await(30, TimeUnit.SECONDS);

        consumer.close();

        System.out.println("Received " + consumer.suspendCount.get() + " messages on suspend queue, " + consumer.deleteCount.get() + " on delete queue");
    }

    public void sendMessages() throws JMSException {
        Producer producer = new Producer(totalMessages, jobs, TARGET_QUEUE_NAME, brokerURL, amqUser, amqPassword);
        producer.sendAllMessages();

        System.out.println("Sent " + producer.suspendCount.get() + " on suspend queue, " + producer.deleteCount.get() + " on delete queue");
        producer.close();
    }

    public static void main(String[] args) throws Exception {
        amqUser = System.getProperty("AMQ_USER", amqUser);
        amqPassword = System.getProperty("AMQ_PASSWORD", amqPassword);
        brokerURL = System.getProperty("BROKER_URL", brokerURL);
        ACTION = System.getProperty("ACTION", "send");

        System.out.println("Starting");
        PersistenceTest pt = new PersistenceTest();
        if (ACTION.equalsIgnoreCase("send")) {
            pt.sendMessages();
        } else {
            pt.receiveMessages();
        }

    }
}
