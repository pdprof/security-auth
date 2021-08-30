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
