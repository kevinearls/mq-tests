package com.fusesource.amq.qe;

import com.fusesource.amq.qe.util.AsyncConsumer;
import com.fusesource.amq.qe.util.Producer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by kearls on 06/08/14.
 *
 * Send 1000 total messages, 500 to each of 2 queues, with 1 consumer.
 */
public class BasicBrokerTest {
    protected static final Logger LOG = LoggerFactory.getLogger(BasicBrokerTest.class);

    protected static final String TARGET_QUEUE_NAME = "queue://JOBS.suspend";
    protected static String brokerURL = "tcp://localhost:61616";
    protected static String amqUser ="admin";
    protected static String amqPassword ="biteme";

    private static Integer totalMessages = 1000;
    public static String jobs[] = new String[]{"delete", "suspend"};

    @Before
    public void setUp() throws Exception {
        amqUser = System.getProperty("AMQ_USER", amqUser);
        amqPassword = System.getProperty("AMQ_PASSWORD", amqPassword);
        brokerURL = System.getProperty("BROKER_URL", brokerURL);
    }

    @Test
    public void simpleProducerConsumerTest() throws Exception {
        Producer producer = new Producer(BasicBrokerTest.totalMessages, jobs, TARGET_QUEUE_NAME, brokerURL, amqUser, amqPassword);
        AsyncConsumer consumer = new AsyncConsumer(jobs, totalMessages, TARGET_QUEUE_NAME, brokerURL, amqUser, amqPassword);

        for (String jobName : BasicBrokerTest.jobs) {
            Session session = consumer.getSession();
            String messageQueueName = "JOBS." + jobName;
            Destination destination = session.createQueue(messageQueueName);
            MessageConsumer messageConsumer = session.createConsumer(destination);
            LOG.info("Setting listener for " + messageQueueName);
            messageConsumer.setMessageListener(consumer);
        }

        producer.sendAllMessages();
        consumer.receivedCount.await(10, TimeUnit.SECONDS);

        consumer.close();

        System.out.println("Sent     " + producer.suspendCount.get() + " messages on suspend queue, " + producer.deleteCount.get() + " on delete queue");
        System.out.println("Received " + consumer.suspendCount.get() + " messages on suspend queue, " + consumer.deleteCount.get() + " on delete queue");

        assertEquals(totalMessages / jobs.length, consumer.suspendCount.get());
        assertEquals(totalMessages / jobs.length, consumer.deleteCount.get());
    }
}
