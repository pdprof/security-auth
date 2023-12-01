#!/bin/bash
KEYCLOAK_URL=http://localhost:8080
TOKEN_URL="${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token"
AUTH="Authorization: bearer $(curl -d client_id=admin-cli -d username=admin -d password=password -d grant_type=password ${TOKEN_URL} | jq -r '.access_token')"
CONVERTER_URL="http://localhost:8080/admin/realms/pdprof/client-description-converter"
SAML_XML="@spMetadata.xml"
curl -X POST -H "${AUTH}"  -H 'content-type: application/json' ${CONVERTER_URL} --data-binary ${SAML_XML=} | jq .
# curl -H "${AUTH}" $KEYCLOAK_URL/admin/realms/pdprof/clients | jq .
