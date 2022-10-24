#!/bin/bash
if [ -z $ACCESS_HOST ]; then
  echo 'Please set the "ACCESS_HOST" environment variable and try again.' 
  exit 2
fi
mkdir -p resources/security

sed s/localhost:9080/$ACCESS_HOST:9080/g configureSaml.py > configureSaml.py.custom
chmod 755 configureSaml.py.custom

docker build -t was90-saml -f Dockerfile.was90 .
docker run -d -p 10080:9080 -p 10443:9443 --name was90-saml was90-saml
sleep 30
docker cp was90-saml:/work/spMetadata-was90.xml .
sed -i s/localhost:9443/$ACCESS_HOST:9443/g spMetadata-was90.xml
echo "access to http://$ACCESS_HOST:8080/auth/admin/master/console/#/realms/pdprof/clients and create clients with spMetadata-was90.xml"
