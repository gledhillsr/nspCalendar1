#/bin/bash
# make sure to explicitly list all ".jar"
#export CLASSPATH="/var/lib/tomcat7/webapps/calendar-1/WEB-INF/classes:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-lang3-3.5.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-ses-1.11.815.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/mysql-connector-java-5.1.6.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/httpclient-4.5.5.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-core-2.10.1.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-ses-1.11.470.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/mail-1.4.7.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/activation-1.1.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-codec-1.10.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-databind-2.10.1.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-core-1.11.470.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/joda-time-2.8.1.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-annotations-2.10.1.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-logging-1.1.3.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/httpcore-4.4.9.jar"

export CLASSPATH="/var/lib/tomcat7/webapps/calendar-1/WEB-INF/classes"

export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-core-1.11.815.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-ses-1.11.815.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-codec-1.11.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-lang3-3.10.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-logging-1.1.3.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/dom4j-2.1.3.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/httpclient-4.5.9.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/httpcore-4.4.11.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/ion-java-1.0.2.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-core-2.15.3.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-databind-2.15.3.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-annotations-2.15.3.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-dataformat-cbor-2.6.7.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/javax.mail-api-1.6.2.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jmespath-java-1.11.815.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/joda-time-2.8.1.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/mysql-connector-java-8.0.28.jar"
export CLASSPATH="$CLASSPATH:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/protobuf-java-3.6.1.jar"

java -cp $CLASSPATH org.nsponline.calendar.DailyReminder