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

# Create a client from spMetadata.json file.
$KCADM create components -r pdprof \
    -s name="ldap" \
    -s providerId=ldap \
    -s providerType=org.keycloak.storage.UserStorageProvider \
    -s 'config.fullSyncPeriod=["-1"]' \
    -s 'config.changedSyncPeriod=["-1"]' \
    -s 'config.cachePolicy=["DEFAULT"]' \
    -s 'config.editMode=["READ_ONLY"]' \
    -s 'config.syncRegistrations=["false"]' \
    -s 'config.importEnabled=["false"]' \
    -s 'config.vendor=["other"]' \
    -s 'config.usernameLDAPAttribute=["uid"]' \
    -s 'config.rdnLDAPAttribute=["uid"]' \
    -s 'config.uuidLDAPAttribute=["uid"]' \
    -s 'config.userObjectClasses=["inetOrgPerson, organizationalPerson"]' \
    -s 'config.connectionUrl=["ldap://centos:1389"]' \
    -s 'config.usersDn=["dc=pdprof,dc=mustgather"]' \
    -s 'config.authType=["simple"]' \
    -s 'config.bindDn=["cn=admin,dc=pdprof,dc=mustgather"]' \
    -s 'config.bindCredential=["passw0rd"]' \
    -s 'config.searchScope=["2"]' \
    -s 'config.useTruststoreSpi=["always"]' \
    -s 'config.connectionPooling=["false"]' \
    -s 'config.pagination=["false"]' \
    -s 'config.useKerberosForPasswordAuthentication=["false"]'　\
    --server $KEYCLOAK_URL --realm master \
    --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
