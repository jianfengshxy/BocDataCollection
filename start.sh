#!/bin/bash

BOC_HOME="/www/boc"

JAVA_OPTS="-Xms5g -Xmx5g -XX:PermSize=128m -XX:MaxPermSize=128m"

LOG4j_OPTS="-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"

JPROFILE="-agentpath:/opt/jprofiler8/bin/linux-x64/libjprofilerti.so=port=8849"

nohup java $JAVA_OPTS  $LOG4j_OPTS -Dlog4j.configuration.file=$BOC_HOME/log4j2.xml -Dconfig.file=$BOC_HOME/application.conf -jar $BOC_HOME/de-assembly.jar > $BOC_HOME/log_dc.log &