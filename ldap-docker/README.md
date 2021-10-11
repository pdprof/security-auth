# LDAP AUTH Application

## Requirements

- [Docker](https://www.docker.com/)

## Test on Docker

### Build docker image

```
./setup-docker.sh
```

### Start docker 

Docker containers already started with setup-docker.sh command

Please check kc and ol for related containers.

Now you can access to https://localhost:9443/security.auth/

User Table

|User   |Password|
|---    |---     |
|wsadmin|passw0rd|
|pdprof |passw0rd|
|myuser |passw0rd|


Check OpenLDAP server response with following command.
```
ldapsearch -x -H ldap://localhost:389 -D "cn=admin,dc=pdprof,dc=mustgather" -b "dc=pdprof,dc=mustgather" -w "passw0rd"

You can see openldap admin user and password with this command.
```


## Test on OpenShift

After you setup CRC described at [icp4a-helloworld](https://github.com/pdprof/icp4a-helloworld)

You can use following script to build image for OpenShift. Same image name is used with test with docker, please delete images before this.

```
./setup-openshift-1.sh
```

After you build the image, please issue follow to deply them to OpenShift.
```
./setup-openshift-2.sh
```

After this, you can access to http://auth-route-default.apps-crc.testing/security.auth/

Other test is same with docker.


# Notes:

If you want to remove bindUser and bindPassword from server.xml, you have to change openldap access configuration

If you want to chage userPassword salt, you need to find proper configuration to do so. 
