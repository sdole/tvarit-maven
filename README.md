#Tvarit
##About
Tvarit is an dev ops automation project for Wildfly JEE, Maven and AWS platforms. It can be used to automate continuous deployments of wildfly based JEE apps to AWS environments using maven.
##Immediate goals
1. Provide a maven plugin that can be used inside a war build to deploy to AWS

##Medium term goals
1. Leverage clustered servers with a domain controller and AWS ELBs

##Long term goals
1. Create fenced environments for test and production based on declared dependencies in pom.xml or another mechanism of declaring dependencies. (e.g. if App A depends on App B's RESTful services, then, Tvarit should be able to deploy both App A and App B in a fenced network - such as VPC).

##Features
Using Tvarit developers can deploy a war file in [wildfly](http://wildfly.org/) standalone mode or in a clustered domain mode. Please see [feature files](https://github.com/sdole/tvarit-maven/blob/master/tvarit-maven-plugin/StandaloneMode.feature) for a BDD style description of what Tvarit does.

##Main components of Tvarit
###VPC and related AWS resources
Tvarit deploys a wildfly server on EC2 instance and makes it available to the world. For this, it creates a VPC, security groups, subnet, internet gateway, EC2 instances, IP address and other AWS resources. 
###Stacks and Layers
Stacks and Layers are AWS Opsworks resources within which EC2 instances are started.
###Chef recipes
Tvarit uses recipes from the opscode market place as well as a wrapper recipe of its own that is hosted on github.


##How to contribute
###Initial setup
Tools required:

- [Chef DK](https://downloads.chef.io/chef-dk/)
- [Vagrant](https://www.vagrantup.com/downloads.html)
- [Kitchen](http://kitchen.ci/)
- [Berkshelf](http://berkshelf.com/)
- [Oracle virtual box](https://www.virtualbox.org/wiki/Downloads)
- Maven and other java tools, IDEs etc

###Kitchen test (recipe only)
This sub-section shows how to run the [Tvarit](https://github.com/sdole/tvarit/tree/master/tvarit-cookbook) cookbook on developer workstation with [Kitchen CI](http://kitchen.ci).

Assuming windows for developer workstation:
- navigate to the tvarit-cookbook folder in PowerShell
- run command "kitchen converge". Depending on your internet speed, this could take upto 10 minutes or more.
- after converge is complete, run "kitchen login"
- verify that wildfly and java 8 are installed. For example:
```
    vagrant@default-ubuntu-trusty64:~$ java -version
    java version "1.8.0_40"
    Java(TM) SE Runtime Environment (build 1.8.0_40-b26)
    Java HotSpot(TM) 64-Bit Server VM (build 25.40-b25, mixed mode)
    vagrant@default-ubuntu-trusty64:~$
```
###Deploy to AWS
1. Install the maven plugin in your local repo with `mvn install`
2. Run the maven goal from the test project `mvn compile`

###Reading list
* http://docs.aws.amazon.com/opsworks/latest/userguide/cookbooks-101.html
* http://docs.aws.amazon.com/opsworks/latest/userguide/workingcookbook.html

