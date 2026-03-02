.PHONY: deploy

deploy:
	mvn clean deploy -Dmaven.test.skip=true -Ddisable.checks=true -T 1C -pl=logback-classic -am
