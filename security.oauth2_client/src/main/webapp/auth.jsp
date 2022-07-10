<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<% String link = "https://" + session.getAttribute("keycloak_host") + ":8443/auth/realms/"
	+ session.getAttribute("keycloak_realm") + "/protocol/openid-connect/auth?response_type=code&redirect_uri="
	+ session.getAttribute("redirect_uri") + "&scope=" + session.getAttribute("scope")
	+ "&client_id=" + session.getAttribute("clinet_id"); %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Request an access code</title>
</head>
<body>
<ul>
<li>Request to get /auth endpoint URL</li>
<li><font color="red">Request an access code using /auth endpoint</font><br>
	<a href="<%=link%>"><%=link%></a></li>
<li>Get an access_code back</li>
<li>Use access_code to request a token using /token endpoint</li>
<li>Get a token back</li>
<li>Validate our token using /introspect endpoint</li>
<li>Get a response with a status for our token (if it active or not)</li>
</ul>
</body>
</html>