aws cloudformation create-stack --stack-name tvarit-test --template-body 'file://C://Users//sachi_000//dev//Projects//tvarit-Project//tvarit-maven//tvarit-tomcat-plugin-test//src//main//resources//vpc-infra.template' --capabilities CAPABILITY_IAM --parameters 'file://C://Users//sachi_000//dev//Projects//tvarit-Project//tvarit-maven//tvarit-tomcat-plugin-test//src//main//resources//create_infra_stack_sample_parameters.json'