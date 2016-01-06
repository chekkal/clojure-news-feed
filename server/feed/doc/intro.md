# Introduction to feed

An illustrative sample web service implementing a basic news feed capability.

This was developed with the following dependencies...

clojure 1.7.0

iced tea 2.5.6

cassandra 2.1.11

postgresql 9.4

mysql 5.6.27

zookeeper 3.4.5

kafka 2.11

redis 3.0.5

solr 5.3.1

## Usage

This is a fun learning adventure for writing non-trivial web services in clojure so you have to start a lot of services in order to get it to work.

### Initial, one time set up

cd ~/oss/kafka/kafka_2.11-0.8.2.2

bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic feed

cd ~/apps/solr-5.3.1

bin/solr create_core -c outbound

Copy everything from the solr folder from this github repository to the ~/apps/solr-5.3.1/server/solr/outbound folder.

create a feed database with a user/password of feed/feed

psql feed <~/git/clojure-news-feed/server/feed/etc/schema.postgre.sql

cd ~/oss/apache-cassandra-2.1.11/bin

./cqlsh <~/git/clojure-news-feed/server/feed/etc/schema.cassandra.sql

cd ~/git/clojure-news-feed/server/support

mvn clean install

cd ~/git/clojure-news-feed/server/feed

You may need to edit ~/git/clojure-news-feed/server/feed/etc/config.cli 
and you will also need to set the APP_CONFIG environment variable too (see below).

Right now, I never see the building of uberjar complete even after 30 minutes.

lein ring uberjar

### starting all the services

cd ~/oss/apache-cassandra-2.1.11/bin

./cassandra -f

cd ~/oss/kafka/kafka_2.11-0.8.2.2

bin/zookeeper-server-start.sh config/zookeeper.properties

bin/kafka-server-start.sh config/server.properties

cd ~/oss/redis/redis-3.0.5/src 

./redis-server

cd ~/apps/solr-5.3.1

bin/solr start

cd ~/git/clojure-news-feed/server/feed

the tilde notation does not get honored here

export APP_CONFIG=/home/glenn/git/clojure-news-feed/server/feed/etc/config.clj

lein run

### Testing

curl -d name=Moe http://localhost:3000/participant/new

curl -d name=Larry http://localhost:3000/participant/new

curl -d name=Curly http://localhost:3000/participant/new

curl -d from=1 -d to=2 http://localhost:3000/friends/new

curl -d from=1 -d to=3 http://localhost:3000/friends/new

curl -d from=1 -d occurred="2014-01-03" -d subject="testing service" -d story="full end to end testing of the service" http://localhost:3000/outbound/new

curl http://localhost:3000/inbound/2

curl -d terms=testing http://localhost:3000/outbound/search

start a kafka consumer

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic feed --from-beginning

run the load test tool

### standing up the service in EC2

At the time of this writing, lein no longer can create an uberjar of this service so we have to run the service using the source code.

wget http://download.oracle.com/otn-pub/java/jdk/7u1-b08/jdk-7u1-linux-i586.rpm

sudo rpm -i jdk-7u1-linux-i586.rpm

sudo ln -s /usr/java/jdk1.7.0_79/bin/java /etc/alternatives/java

sudo /usr/sbin/alternatives --install /usr/bin/java java  /usr/java/jdk1.7.0_79/bin/java 2

sudo /usr/sbin/alternatives --config java

sudo ln -s /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.91.x86_64/jre/lib lib

wget http://www.us.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz

gunzip apache-maven-3.3.9-bin.tar.gz

tar -xf apache-maven-3.3.9-bin.tar

export PATH=$PATH:/home/ec2-user/apache-maven-3.3.9/bin

wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein

mkdir bin

mv lein bin

chmod a+x bin/lein

sudo yum install git

git clone https://github.com/gengstrand/clojure-news-feed.git

cd clojure-news-feed/server/feed/support

mvn clean install

cd ..

vi etc/config.clj

export APP_CONFIG=/home/ec2-user/clojure-news-feed/server/feed/etc/config.clj

lein run

