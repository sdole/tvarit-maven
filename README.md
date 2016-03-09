#Tvarit
##About
Tvarit is a dev ops automation project for continuous delivery of JEE apps using Maven and AWS platforms.
 
##Goals
<ol>
<li>Provide a completely automatic path to take code from every code commit to production and further on to retirement or rollback</li>
<li>Support application dependencies (with contracts) in every environment<li>
<li>Be automatically scalable, available and cost effective<li>
</ol>

##Current status
This plugin can be used as it is today to deploy a standalone webapp (WAR) to an automatically scalable environment in AWS. Actual capabilities of the environment can be customized by customizing the underlying AWS CloudFormation templates.


##Usage
Tvarit Maven Plugin is available in Maven Central
###AWS Credentials
Make access id and secret key available in environment as. This access id should be allowed all [IAM permissions](#automaton-permissions) to run the plugin.
```xml
  <profiles>
		<profile>
			<id>default</id>
			<properties>
				<myAccessKey>accessKey</myAccessKey>
				<mySecretKey>secretKey</mySecretKey>
			</properties>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
		</profile> 
	</profiles>
```
###Configure the Maven plugin.
####Step 1: Create all infrastructure needed in AWS
Do this only once. This will create a cloudformation stack consisting of a VPC and other resources in your account. If you already have required setup or plan to create this manually, skip this step. Please see [vpc infrastructure template](/tvarit-maven-plugin/src/main/resources/vpc-infra.template) for resources you will need.
This execution configuration can optionally take a parameter named templateUrl pointing to an S3 hosted cloudformation template. If set, resources specified in that template will be created instead.
To run this goal:
```
mvn -P makeinfra tvarit:make-infra@make-infra     <-- To use this syntax, you'll need at least ver 3.3.3 of Maven
```
```xml
  <profile>
            <id>makeinfra</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>io.tvarit</groupId>
                        <artifactId>tvarit-maven-plugin</artifactId>
                        <version>0.0.1-SNAPSHOT</version>
                        <executions>
                            <execution>
                                <id>make-infra</id>
                                <goals>
                                    <goal>make-infra</goal>
                                </goals>
                                <configuration>
                                    <projectName>mycoolproject</projectName>
                                    <domainName>mycooldomainname.io</domainName>
                                    <bucketName>mycoolproject-automation</bucketName>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
```



####Create an autoscaling group in EC2 for app servers
Do this only once. Go to your AWS console and obtain the IAM instance profile, IAM role and bucket ARN that were created in the step above. Use those here. Please see [autoscaling template](https://github.com/sdole/tvarit-maven/blob/master/tvarit-maven-plugin/src/main/resources/autoscaling.template) for resources needed in this step.
This execution configuration can optionally take a parameter named templateUrl pointing to an S3 hosted cloudformation template. If set, resources specified in that template will be created instead.
To run this goal:
```
mvn -P makeasg tvarit:setup-autoscaling@setup-as     <-- To use this syntax, you'll need at least ver 3.3.3 of Maven
```
```xml
   <profile>
              <id>makeasg</id>
              <build>
                  <plugins>
                      <plugin>
                          <groupId>io.tvarit</groupId>
                          <artifactId>tvarit-maven-plugin</artifactId>
                          <version>0.0.1-SNAPSHOT</version>
                          <executions>
                              <execution>
                                  <id>setup-as</id>
                                  <goals>
                                      <goal>setup-autoscaling</goal>
                                  </goals>
                                  <configuration>
                                      <projectName>mycoolproject</projectName>
                                      <tvaritInstanceProfile>IAM instance profile</tvaritInstanceProfile>
                                      <bucketName>Bucket Name</bucketName>
                                      <tvaritRoleArn>IAM role</tvaritRoleArn>
                                  </configuration>
                              </execution>
                          </executions>
                      </plugin>
                  </plugins>
              </build>
          </profile>
```

####Deploy app and launch servers into the auto scaling group - [work in progress](https://github.com/sdole/tvarit-maven/issues/13)
To run this goal:
```
mvn -P deploy-app deploy 
```

```xml
<profile>
            <id>deploy-app</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>io.tvarit</groupId>
                        <artifactId>tvarit-maven-plugin</artifactId>
                        <version>0.0.1-SNAPSHOT</version>
                        <executions>
                            <execution>
                                <id>deploy</id>
                                <goals>
                                    <goal>deploy-tomcat-ui-app</goal>
                                </goals>
                                <phase>deploy</phase>
                                <configuration>
                                    <bucketName>Bucket name</bucketName>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
```
##Automaton Permissions
The Automaton user is the user who runs the maven plugin. In a developer workstation, that will be the developer. On a continuous integration server, such as Teamcity or Jenkins, it may be a new IAM user created for automation purposes. This user will need a wide range of permissions including at least the following.
<ul>
<li>S3 Buckets, Objects, create, read </li>
<li>Lambda, Functions, create</li>
<li>IAM, role, instance profile, policy, trusts, create.</li>
<li>EC2/Autoscaling, launch configurations, auto scaling groups, ELB, create, read</li>
<li>CloudFormation, S3, read</li>
</ul>
Additionally, the following trust relationships will be needed:
<ul>
<li>CloudFormation assume role</li>
</ul>

##License
Tvarit is licensed under [GPLv3](https://www.gnu.org/licenses/gpl.txt)