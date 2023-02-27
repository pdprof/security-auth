#!/bin/bash
if [ -z $ACCESS_HOST ]; then
  echo 'Please set the "ACCESS_HOST" environment variable and try again.' 
  exit 2
fi

sed s/localhost:9080/$ACCESS_HOST:7080/g was90/configureSaml.py > was90/configureSaml.py.custom
sed -i s/localhost:8080/$ACCESS_HOST:8080/g was90/configureSaml.py.custom
chmod 755 was90/configureSaml.py.custom

docker build -t was90-saml -f Dockerfile.was90 .
docker run -d -p 7060:9060 -p 7043:9043 -p 7080:9080 -p 7443:9443 --name was90-saml was90-saml
sleep 30
docker cp was90-saml:/work/spMetadata-was90.xml .
docker cp was90-saml:/opt/IBM/WebSphere/AppServer/profiles/AppSrv01/config/cells/DefaultCell01/nodes/DefaultNode01/key.p12 .
sed -i s/localhost:9080/$ACCESS_HOST:7080/g spMetadata-was90.xml
sed -i s/localhost:9443/$ACCESS_HOST:7443/g spMetadata-was90.xml
echo "access to http://$ACCESS_HOST:8080/auth/admin/master/console/#/realms/pdprof/clients and create clients with spMetadata-was90.xml"
