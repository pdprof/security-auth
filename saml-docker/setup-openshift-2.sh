#!/bin/bash
$HOME/kubeadmin

oc registry login
docker login `oc registry info`

# pull openliberty docker repository
BUILD_DATE=`date +%Y%m%d`

docker stop kc
docker stop ol
docker commit kc idp:${BUILD_DATE}
docker commit ol saml:${BUILD_DATE}

docker tag idp:${BUILD_DATE} $(oc registry info)/$(oc project -q)/idp:${BUILD_DATE}
docker tag saml:${BUILD_DATE} $(oc registry info)/$(oc project -q)/saml:${BUILD_DATE}

docker push $(oc registry info)/$(oc project -q)/idp:${BUILD_DATE}
docker push $(oc registry info)/$(oc project -q)/saml:${BUILD_DATE}

sed -i s/image-registry.openshift-image-registry.svc:5000/default-route-openshift-image-registry.apps-crc.testing/g kubernetes.yaml
sed -i s/"\[project-name\]"/$(oc project -q)/g kubernetes.yaml
sed -i s/"\[build-date\]"/${BUILD_DATE}/g kubernetes.yaml

oc create secret generic docker-user-secret --from-file=.dockerconfigjson=$HOME/.docker/config.json --type=kubernetes.io/dockerconfigjson
oc apply -f kubernetes.yaml
