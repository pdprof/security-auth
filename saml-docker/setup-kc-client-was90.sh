#!/bin/bash
KEYCLOAK_USER=admin
KEYCLOAK_PASSWORD=password
KCADM=/opt/jboss/keycloak/bin/kcadm.sh
$KCADM create clients -r pdprof -f /tmp/client_was90.json -s "attributes={\"saml.client.signature\":\"false\"}" -i --server http://localhost:8080/auth --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
