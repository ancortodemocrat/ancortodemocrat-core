include Expes.mk

install:
	git submodule init
	git submodule update
	mvn package

dev-install: install
	mvn install
