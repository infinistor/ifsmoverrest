#/bin/sh
java -jar -Dconfigure=./etc/ifsmoverRest.conf -Dlogback.configurationFile=./etc/ifsmoverRestLog.xml ./lib/ifsmoverRest.jar &