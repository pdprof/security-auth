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

UserId is pdprof. Password is passw0rd


## Test on OpenShift

After you setup CRC described at [icp4a-helloworld](https://github.com/pdprof/icp4a-helloworld)

You can use following script. 
```
./setup-openshift.sh
```

After this, you can access to https://ldap-route-default.apps-crc.testing/security.auth/

Other test is same with docker.
