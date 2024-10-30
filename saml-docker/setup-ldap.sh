#!/bin/bash
if [ ! "$(docker image list -q ldap)" ]; then
	docker build -t ldap -f ../ldap-docker/Dockerfile.openldap ../ldap-docker/
fi

if [ ! "$(docker ps -a | grep ldap)" ]; then
	docker run -d -p 1389:389 --name ldap ldap --loglevel debug
fi

docker exec kc /tmp/setup-kc-ldap.sh
