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

sed s/localhost:9080/$ACCESS_HOST:9080/g setup-keycloak.sh > setup-keycloak.sh.custom
chmod 755 setup-keycloak.sh.custom

docker build -t liberty-idp -f Dockerfile.keycloak .
docker run -d -p 8080:8080 -p 8443:8443 --name kc liberty-idp start-dev
sleep 100
docker exec kc /tmp/setup-keycloak.sh
curl http://localhost:8080/realms/pdprof/protocol/saml/descriptor > resources/security/idpMetadata.xml
sed -i s/localhost:8080/$ACCESS_HOST:8080/g resources/security/idpMetadata.xml
docker build -t liberty-saml .
docker run -d -p 9080:9080 -p 9443:9443 --name ol liberty-saml
sleep 30
curl -k https://localhost:9443/ibm/saml20/defaultSP/samlmetadata > spMetadata.xml
sed -i s/localhost:9443/$ACCESS_HOST:9443/g spMetadata.xml
echo "access to http://$ACCESS_HOST:8080/admin/master/console/#/realms/pdprof/clients and create clients with spMetadata.xml"
echo "\n\n***** CLIENT SECRET of clientId: api-services *****"
docker cp kc:/tmp/client_secret.txt client_secret.txt
cat client_secret.txt
echo "***** END *****"
