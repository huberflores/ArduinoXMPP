#!/bin/sh

pid=`ps -ef | grep XMPPProject | awk '/java/{print $2}'`
kill -9 $pid

