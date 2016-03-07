#Tvarit
##About
Tvarit is an dev ops automation project for continuous delivery of JEE apps using Maven and AWS platforms. 

##Usage
Make access id and secret key available in environment as 
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
Configure the Maven plugin.
###Step 1: Create all infrastructure needed in AWS
Do this only once. This will create a cloudformation stack in your account.
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
                                <phase>deploy</phase>
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



###Create an autoscaling group in EC2 for app servers
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
                                  <phase>deploy</phase>
                                  <configuration>
                                      <projectName>jamesbond</projectName>
                                      <tvaritInstanceProfile>jamesbond-infra-tvaritInstanceProfile-1GTE4MDYQDWQ1</tvaritInstanceProfile>
                                      <bucketName>tvarit-jamesbond-automation</bucketName>
                                      <tvaritRoleArn>arn:aws:iam::085224677438:role/jamesbond-infra-tvaritRole-HUMRQBCNG1K5</tvaritRoleArn>
                                  </configuration>
                              </execution>
                          </executions>
                      </plugin>
                  </plugins>
              </build>
          </profile>
```

###Deploy app and launch servers - work in progress!
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
                                    <bucketName>tvarit-jamesbond-automation</bucketName>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
```
