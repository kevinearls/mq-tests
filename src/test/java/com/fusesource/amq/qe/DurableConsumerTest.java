package com.fusesource.amq.qe;

import com.fusesource.amq.qe.util.DurableAsyncTopicConsumer;
import com.fusesource.amq.qe.util.TopicProducer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kearls on 06/08/14.
 */
public class DurableConsumerTest {
    protected static String brokerURL = "tcp://localhost:61616";
    protected static String amqUser ="admin";
    protected static String amqPassword ="biteme";
    private static final String TOPIC_NAME = "Durable Topic";
    private static final String CLIENT_ID = "Fred";
    private static final Integer MESSAGES = 100;


    @Before
    public void setUp() throws Exception {
        amqUser = System.getProperty("AMQ_USER", amqUser);
        amqPassword = System.getProperty("AMQ_PASSWORD", amqPassword);
        brokerURL = System.getProperty("BROKER_URL", brokerURL);
    }

    /**
     * 1. Subscribe to a durable topic
     * 2. Disconnect the consumer
     * 3. Send some messages
     * 4. Restart the consumer
     * @throws Exception
     */
    @Test
    public void simpleDurableConsumerTest() throws Exception  {
        // Call with 0 messages expected; create the topic, subscribe to it, and disconnect
        DurableAsyncTopicConsumer durableConsumer = new DurableAsyncTopicConsumer(CLIENT_ID, TOPIC_NAME, brokerURL, amqUser, amqPassword, 0);
        durableConsumer.go();
        durableConsumer.close();

        // Send a bunch of messages
        TopicProducer topicProducer = new TopicProducer("Durable Topic", MESSAGES, brokerURL, amqUser, amqPassword);
        topicProducer.sendAllMessages();

        // Reconnect and make sure we get all messages
        durableConsumer = new DurableAsyncTopicConsumer(CLIENT_ID, TOPIC_NAME, brokerURL, amqUser, amqPassword, MESSAGES);
        int messagesReceived = durableConsumer.go();
        durableConsumer.close();

        assertEquals(MESSAGES.intValue(), messagesReceived);
    }

}
