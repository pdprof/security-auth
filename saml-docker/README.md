# SAML AUTH Application

## Requirements

- [Docker](https://www.docker.com/)

## Test on Docker

### Build docker image

```
setup-docker.sh
```

### Start docker 
```
mkdir -p ~/pdprof/share
docker run --rm -p 9080:9080 -p 9443:9443 -v ~/pdprof:/pdprof:z saml
```

Now you can access https://localhost:9443/helloAuth/auth


## Test on OpenShift

After you setup CRC described at [icp4a-helloworld](https://github.com/pdprof/icp4a-helloworld)

You can use following script. 
```
setup-openshift.sh
```

Now you can access to http://db-connections-route-default.apps-crc.testing/ssl

Other test steps are same with docker.


# Note

To create docker for keycloak realm json file

[Exporting a realm](https://hub.docker.com/r/jboss/keycloak)

docker run -d -p 18080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=password --name keycloak jboss/keycloak

Create pdprof realm by GUI

docker exec -it keycloak /opt/jboss/keycloak/bin/standalone.sh -Djboss.socket.binding.port-offset=100 -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.realmName=pdprof -Dkeycloak.migration.usersExportStrategy=REALM_FILE -Dkeycloak.migration.file=/tmp/pdprof_realm.json

docker cp keycloak:/tmp/pdprof_realm.json .

docker build -f Dockerfile.keycloak .

docker run -e KEYCLOAK_IMPORT=/tmp/pdprof-realm.json liberty-idp
