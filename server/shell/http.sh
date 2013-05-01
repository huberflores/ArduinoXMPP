#!/bin/sh

nohup java -cp target/XMPPProject-0.0.1-SNAPSHOT-full.jar ee.ut.ati.masters.Main </dev/null 2>&1 >> logfile.log &
