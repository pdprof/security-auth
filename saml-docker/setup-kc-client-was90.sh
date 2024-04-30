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

$KCADM create clients -r pdprof -f /tmp/client_was90.json -s "attributes={\"saml.client.signature\":\"false\"}" -i --server $KEYCLOAK_URL --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
