#/bin/bash
# make sure to explicitly list all ".jar"
export CLASSPATH="/var/lib/tomcat7/webapps/calendar-1/WEB-INF/classes:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-lang3-3.5.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/mysql-connector-java-5.1.6.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/httpclient-4.3.6.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-core-2.6.2.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-ses-1.10.11.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/mail-1.4.7.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/activation-1.1.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-codec-1.6.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-databind-2.6.2.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-core-1.10.11.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/joda-time-2.8.1.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-annotations-2.6.2.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-logging-1.1.3.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/httpcore-4.3.3.jar"

java -cp $CLASSPATH org.nsponline.calendar.DailyReminder