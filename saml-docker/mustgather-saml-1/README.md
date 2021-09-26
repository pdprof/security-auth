# MustGather Hands-On SAML AUTH case

## Start saml container on docker or openshift

Follow steps described at parent folder's README 


## Access to SAML Login page

Access to following URL by your browser

- https://localhost:9443/security.auth/

Please use

```
User: pdprof
Passowrd: password
```


## Do logout from the site

Access to following URL by your browser

- https://localhost:10443/security.auth/logout



## Check trace

Please check /logs/trace.log to investigate ssl behavior
And please use browser trace (F12 key) to investigate how login page is redeirected for saml.


