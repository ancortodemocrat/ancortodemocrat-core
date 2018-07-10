include Expes.mk

install:
	git checkout master
	git submodule init
	git submodule update
	mvn package

dev-install: install
	mvn install
