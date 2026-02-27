.PHONY: deploy

deploy:
	mvn clean deploy -pl=logback-classic -am -DskipTests -Ddisable.checks=true -T 1
