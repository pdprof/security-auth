print "Configure SAML properties..."
AdminTask.configureTrustAssociation('-enable true')
AdminTask.configureInterceptor('[-interceptor com.ibm.ws.security.web.saml.ACSTrustAssociationInterceptor -customProperties ["sso_1.sp.acsUrl=http://localhost:9080/samlsps/acs","sso_1.sp.idMap=idAssertion","sso_1.sp.login.error.page=pdprof.security.saml.PdprofAuthnRequestProvider","sso_1.sp.targetUrl=http://localhost:9080/security.auth"]]');
AdminTask.setAdminActiveSecuritySettings('[-customProperties ["com.ibm.websphere.security.DeferTAItoSSO=com.ibm.ws.security.web.saml.ACSTrustAssociationInterceptor","com.ibm.websphere.security.InvokeTAIbeforeSSO=com.ibm.ws.security.web.saml.ACSTrustAssociationInterceptor"]]')
AdminTask.setJVMSystemProperties('[-serverName server1 -nodeName DefaultNode01 -propertyName pdprof.security.saml.ssoUrl -propertyValue http://localhost:8080/realms/pdprof/protocol/saml]')
AdminTask.setJVMSystemProperties('[-serverName server1 -nodeName DefaultNode01 -propertyName pdprof.security.saml.acsUrl -propertyValue http://localhost:9080/samlsps/acs]')
AdminConfig.save()
print "Install security.auth.war..."
AdminApp.install('/work/config/security.auth.war', '[ -nopreCompileJSPs -distributeApp -nouseMetaDataFromBinary -appname security_auth -createMBeansForResources -noreloadEnabled -nodeployws -validateinstall warn -noprocessEmbeddedConfig -filepermission .*\.dll=755#.*\.so=755#.*\.a=755#.*\.sl=755 -noallowDispatchRemoteInclude -noallowServiceRemoteInclude -asyncRequestDispatchType DISABLED -nouseAutoLink -noenableClientModule -clientMode isolated -novalidateSchema -contextroot /security.auth -MapModulesToServers [[ security.auth security.auth.war,WEB-INF/web.xml WebSphere:cell=DefaultCell01,node=DefaultNode01,server=server1 ]] -MapWebModToVH [[ security.auth security.auth.war,WEB-INF/web.xml default_host ]] -CtxRootForWebMod [[ security.auth security.auth.war,WEB-INF/web.xml /security.auth ]]]' )
AdminConfig.save()
print "addTrustedRealms..."
AdminTask.addTrustedRealms('[-communicationType inbound -realmList http://localhost:8080/realms/pdprof]')
AdminConfig.save()
print "exportSAMLSpMetadata..."
AdminTask.exportSAMLSpMetadata('-spMetadataFileName /work/spMetadata-was90.xml -ssoId 1')
AdminConfig.save()
print "importSAMLIdpMetadata..."
AdminTask.importSAMLIdpMetadata('-idpMetadataFileName /work/idpMetadata.xml -idpId 1 -ssoId 1 -signingCertAlias keycloak')
AdminConfig.save()
print "Set security appEnabled true..."
security = AdminConfig.list('Security')
AdminConfig.modify(security, [['appEnabled', 'true']])
print "Map app role to all authenticated user in SAML(trusted) realm..."
AdminApp.edit('security_auth', '[ -MapRolesToUsers [[ AllAuthenticated AppDeploymentOption.No AppDeploymentOption.No "" "" AppDeploymentOption.Yes "" "" ]]]' )
AdminConfig.save()
