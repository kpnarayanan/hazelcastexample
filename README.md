# hazelcastexample
The example hazelcast implementation tests the communication between two devices where the devices could be servers, containers, VMs, etc. 

In this example, there are two projects nammely sender and the receiver that are deployed on two docker containers. The application is developed in vertx and it uses its embedded event bus to publish and subscribe for messages.

The source code for the sender is placed in the folder: HazelcastClusterManager-Sender while the code for the receiver is placed under HazelcastClusterManager-Receiver.

The two projects primarily contains the following files: build.gradle, cluster.xml and a src folder which has two packages: java and resources. 
The Java directory contains two folders: example and io. The .java files in the io directory was taken from the github: https://github.com/vert-x3/vertx-hazelcast/tree/master/src.

Building sender and receiver
----------------------------
Generate the FAT jar: gradle --info clean assemble from the top directory.

Generate the shadow jar: gradle --info shadowJar from the top directory.

The JARS are persisted in the build/libs directory. 

Run the sender on container 1: java -jar /<build/libs/name of the sender FAT jar/> -cluster -cluster-host <IP address of the container hosting the sender (container 1 IP address)>

Run the receiver on container 2: java -jar /<build/libs/name of the receiver FAT jar/> -cluster -cluster-host <IP address of the container hosting the receiver (container 2 IP address)>
