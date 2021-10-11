#!/bin/bash
if [ ! -f trapit ]; then
     echo "===================="
     echo "Please download trapit from https://www.ibm.com/support/pages/node/709009 and put it on the same directory."
     echo "===================="
     exit 1
fi
chmod 755 trapit

docker build -t ldap -f Dockerfile.openldap .
#Change for OpenShift
sed -i s/{ldap-host}/ldap-service/g config/server.xml
docker build -t auth .
