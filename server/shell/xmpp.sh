#!/bin/sh

java -cp "target/XMPPProject-0.0.1-SNAPSHOT.jar:target/lib/commons-dbcp-1.4.jar:target/lib/commons-pool-1.5.4.jar:target/lib/h2-1.3.165.jar:target/lib/jackson-core-asl-1.9.5.jar:target/lib/jackson-mapper-asl-1.9.5.jar:target/lib/smack-3.1.0.jar:target/lib/smackx-3.1.0.jar:target/lib/log4j-1.2.16.jar" ee.ut.ati.masters.xmpp.client.XmppMain
