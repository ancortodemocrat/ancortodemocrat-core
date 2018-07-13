include Expes.mk

CORE_JAR=target/ancor2-0.0.1-SNAPSHOT-jar-with-dependencies.jar

JAR_exec=ancor2.jar

install:
	git checkout master
	git submodule init
	git submodule update
	mvn package
	cp $(CORE_JAR) $(JAR_exec)

dev-install: install
	mvn install
