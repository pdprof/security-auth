#!/bin/bash
KEYCLOAK_USER=admin
KEYCLOAK_PASSWORD=password
KCADM=/opt/jboss/keycloak/bin/kcadm.sh
$KCADM create realms -s realm=pdprof -s enabled=true --no-config --server http://localhost:8080/auth --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM create users -r pdprof -s username=pdprof -s email="pdprof@must.gather" -s enabled=true --no-config --server http://localhost:8080/auth --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM set-password -r pdprof --username pdprof --new-password $KEYCLOAK_PASSWORD --no-config --server http://localhost:8080/auth --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
$KCADM update realms/master -s sslRequired=NONE --server http://localhost:8080/auth --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
