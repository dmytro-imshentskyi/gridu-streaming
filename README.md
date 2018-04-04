Hello! 

This project is the final exit project for internal course in the Grid Dynamics.
To kick off application locally you have to follow this manual and set up your local environment.

Requirements:

1. Java 8+ 
2. Scala 2.11.8
2. SBT 0.13+ 
3. Kafka 1.0.0 
4. Cassandra 3.11

Recommendations: 

1. Go to `KAFKA_HOME/bin` and launch zookeeper service by:  
`zookeeper-server-start.sh ../config/zookeeper.properties `

Zookeeper will to be run on localhost:2181 by default

2. Launch kafka-server service:
`kafka-server-start.sh ../config/server.properties `

Kafka will to be run on localhost:9092 by default

3. Go to `CASSANDRA_HOME/bin` and start your cassandra service by command:
cassandra

4. Checkout repository and import your project as new SBT project with IntellijIDEA. 
Refresh project and download all dependencies. 

5. Launch ETL flume pipeline. Go to FLUME_HOME/bin and run
`sh flume-ng agent -n adv-agent -c conf -f PROJECT_HOME/src/main/resources/flumeEventsToKafka.properties`

6. Run Demo.scala application. Enjoy my first InStreaming application.


