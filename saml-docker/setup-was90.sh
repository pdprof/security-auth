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
KEYCLOAK_URL=http://localhost:8080
TOKEN_URL="${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token"
AUTH="Authorization: bearer $(curl -d client_id=admin-cli -d username=admin -d password=password -d grant_type=password ${TOKEN_URL} | jq -r '.access_token')"
CONVERTER_URL="http://localhost:8080/admin/realms/pdprof/client-description-converter"
export CLIENT_JSON=$(curl -X POST -H "${AUTH}"  -H 'content-type: application/json' ${CONVERTER_URL} --data-binary @spMetadata-was90.xml)
echo $CLIENT_JSON > client_was90.json
docker cp client_was90.json kc:/tmp/
docker exec kc /tmp/setup-kc-client-was90.sh
