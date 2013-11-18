export LOGBACK_CONFIG_FILE_LOCATION=/etc/logback.xml
java -Xmx2048m -Djava.util.logging.config.file=etc/jdk14.properties -Dlogback.configurationFile=${LOGBACK_CONFIG_FILE_LOCATION} -cp target/UBTWrapperOwlim-0.0.1.jar:target/dependency/*:$CLASSPATH edu.lehigh.swat.bench.ubt.Test $1 $2 $3
