.PHONY: deploy

deploy:
	mvn clean deploy -DskipTests=true -Ddisable.checks=true -T 1C -pl=logback-classic -am

install:
	mvn clean install -DskipTests=true -Ddisable.checks=true -T 1C -pl=logback-classic -am
