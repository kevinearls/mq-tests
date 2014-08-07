This project contains some simple smoke tests for testing a JBOSS Fuse A-MQ Cartridge for OpenShift.

1. There are three standard JUnit tests, BasicBrokerTest, BasicTopicTest, and DurableConsumerTest.  These can be
run with the command:

mvn -DAMQ_USER=user -DAMQ_PASSWORD=password -DBROKER_URL="tcp://localhost:61616" clean install

2. There is a simple presistence test which requires restarting the broker (or on OSE restarting the app)

   - mvn exec:java -DACTION=send -DAMQ_USER=user -DAMQ_PASSWORD=password -DBROKER_URL="tcp://localhost:61616" -Dexec.mainClass=com.fusesource.amq.qe.PersistenceTest 
   - restart the broker or app
   - mvn exec:java -DACTION=receive -DAMQ_USER=user -DAMQ_PASSWORD=password -DBROKER_URL="tcp://localhost:61616" -Dexec.mainClass=com.fusesource.amq.qe.PersistenceTest 
   
   Verify that the number of messages sent and received are equal.
   