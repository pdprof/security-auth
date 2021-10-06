#!/bin/bash
if [ ! -f trapit ]; then
     echo "===================="
     echo "Please download trapit from https://www.ibm.com/support/pages/node/709009 and put it on the same directory."
     echo "===================="
     exit 1
fi
chmod 755 trapit

docker build -t openldap -f Dockerfile.openldap .
docker run -d -p 1389:389 --name ldap openldap --loglevel debug
#docker build -t liberty-saml .
#docker run -d -p 9080:9080 -p 9443:9443 --name ol liberty-saml
#sleep 30
