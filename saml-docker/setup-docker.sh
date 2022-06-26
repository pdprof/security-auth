#!/bin/bash
#if [ ! -f trapit ]; then
#     echo "===================="
#     echo "Please download trapit from https://www.ibm.com/support/pages/node/709009 and put it on the same directory."
#     echo "===================="
#     exit 1
#fi
#chmod 755 trapit
if [ -z $ACCESS_HOST ]; then
  echo 'Please set the "ACCESS_HOST" environment variable and try again.' 
  exit 2
fi
mkdir -p resources/security

docker build -t liberty-idp -f Dockerfile.keycloak .
docker run -d -p 8080:8080 --name kc liberty-idp
sleep 100
docker exec kc /tmp/setup-keycloak.sh
curl http://localhost:8080/auth/realms/pdprof/protocol/saml/descriptor > resources/security/idpMetadata.xml
sed -i s/localhost:8080/$ACCESS_HOST:8080/g resources/security/idpMetadata.xml
docker build -t liberty-saml .
docker run -d -p 9080:9080 -p 9443:9443 --name ol liberty-saml
sleep 30
curl -k https://localhost:9443/ibm/saml20/defaultSP/samlmetadata > spMetadata.xml
sed -i s/localhost:9443/$ACCESS_HOST:9443/g spMetadata.xml
echo "access to http://$ACCESS_HOST:8080/auth/admin/master/console/#/realms/pdprof/clients and create clients with spMetadata.xml"
