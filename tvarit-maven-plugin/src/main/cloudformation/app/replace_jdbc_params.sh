#!/usr/bin/env bash
replaceCmd(){
        sed -i "s/{$1}/$2/g" /opt/tomcat/conf/Catalina/localhost/web_app.xml
}

replaceCmd $1 $2