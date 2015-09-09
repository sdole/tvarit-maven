#Tvarit
##About
Tvarit is an dev ops automation project for Wildfly JEE, Maven and AWS platforms. It can be used to automate continuous deployments of wildfly based JEE apps to AWS environments using maven.
##Immediate goals
1. Provide a maven plugin that can be used inside a war build to deploy to AWS

##Medium term goals
1. Leverage clustered servers with a domain controller and AWS ELBs

##Long term goals
1. Create fenced environments for test and production based on declared dependencies in pom.xml or another mechanism of declaring dependencies. (e.g. if App A depends on App B's RESTful services, then, Tvarit should be able to deploy both App A and App B in a fenced network - such as VPC).


