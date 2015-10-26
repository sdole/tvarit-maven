Feature: deploy war file to a wildfly instance running in standalone mode
  Scenario: Run maven plugin with deploy to ops phase without any infrastructure being already present
    Given No infrastructure is already present in AWS
    When deploy phase is run
    Then all infrastructure should be created
    Then instance should be started
    Then app should be deployed
    Then application should be available on port 80
    Then management interface should not be available on port 9990
    Then wildfly should be run in standalone mode

  Scenario: Run maven plugin with deploy to ops phase vpc and stacks infrastructure being already present
    Given All VPC, stack and layer resources are already present
    When deploy phase is run
    Then instance should be started
    Then app should be deployed
    Then application should be available on port 80
    Then management interface should not be available on port 9990
    Then wildfly should be run in standalone mode

  Scenario: Run maven plugin with deploy to ops phase vpc and some error occurs when creating infrastructure
    Given No infrastructure is already present in AWS
    When deploy phase has been run
    When there is an error creating infrastructure
    Then halt all processing immediately and leave system as it was
