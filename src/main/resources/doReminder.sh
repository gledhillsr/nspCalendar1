#/bin/bash
export CLASSPATH="/var/lib/tomcat7/webapps/calendar-1/WEB-INF/classes:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-core-1.10.11.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/aws-java-sdk-ses-1.10.11.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/commons-logging-1.1.3.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/httpclient-4.3.6.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/httpcore-4.3.3.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-annotations-2.5.0.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-core-2.5.3.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/jackson-databind-2.5.3.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/mysql-connector-java-5.1.6.jar:/var/lib/tomcat7/webapps/calendar-1/WEB-INF/lib/joda-time-2.8.1.jar"
#-rw-r--r-- 1 tomcat tomcat   62983 Sep 14 14:43 activation-1.1.jar
#-rw-r--r-- 1 tomcat tomcat  232771 Sep 19 15:50 commons-codec-1.6.jar
#-rw-r--r-- 1 tomcat tomcat  521157 Sep 17 20:32 mail-1.4.7.jar

java -cp $CLASSPATH org.nsponline.calendar.DailyReminder