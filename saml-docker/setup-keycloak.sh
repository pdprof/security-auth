#!/bin/bash
KEYCLOAK_USER=admin
KEYCLOAK_PASSWORD=password

# KCADM path have to be changed when keycloak/keycloak docker images is used.
KCADM=/opt/jboss/keycloak/bin/kcadm.sh
KEYCLOAK_URL="http://localhost:8080/auth"
if [ ! -f $KCADM ]; then
    KCADM=/opt/keycloak/bin/kcadm.sh
    KEYCLOAK_URL=http://localhost:8080
fi
$KCADM create realms -s realm=pdprof -s enabled=true --no-config --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM create users -r pdprof -s username=pdprof -s email="pdprof@must.gather" -s enabled=true --no-config --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM set-password -r pdprof --username pdprof --new-password $KEYCLOAK_PASSWORD --no-config --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM create users -r pdprof -s username=api-user -s email="api-user@must.gather" -s enabled=true --no-config --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM set-password -r pdprof --username api-user --new-password $KEYCLOAK_PASSWORD --no-config --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM create clients -r pdprof -s clientId=api-services -s "redirectUris=[\"http://localhost:9080/security.oauth2_client/*\"]" -i --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD > /tmp/client_id.txt
CID=`cat /tmp/client_id.txt`
$KCADM create clients/$CID/client-secret -r pdprof --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM get clients/$CID/client-secret -r pdprof --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD > /tmp/client_secret.txt
$KCADM create clients/$CID/roles -r pdprof -s name=api-consumer -s 'description=API Consumer role' --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM add-roles -r pdprof --uusername api-user --cclientid api-services --rolename api-consumer --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM create client-scopes -r pdprof -s name=apiconnect -s protocol=openid-connect --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM get client-scopes -r pdprof --fields id,name --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD | grep -B 1 apiconnect | head -1 | awk -F \" '{print $4}' > /tmp/client_scope.txt
SID=`cat /tmp/client_scope.txt`
$KCADM update clients/$CID/optional-client-scopes/$SID -r pdprof --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM update realms/master -s sslRequired=NONE --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM update realms/pdprof -s sslRequired=NONE --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
