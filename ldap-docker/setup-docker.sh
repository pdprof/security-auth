#!/bin/bash
#if [ ! -f trapit ]; then
#     echo "===================="
#     echo "Please download trapit from https://www.ibm.com/support/pages/node/709009 and put it on the same directory."
#     echo "===================="
#     exit 1
#fi
chmod 755 trapit

DOCKER_HOST=172.17.0.1

docker build -t ldap -f Dockerfile.openldap .
docker run -d -p 1389:389 --name ldap ldap --loglevel debug
#Change for Docker
sed -i s/{ldap-host}/$DOCKER_HOST/g config/server.xml
docker build -t auth .
docker run -d -p 9080:9080 -p 9443:9443 --name ll auth
#sleep 30
