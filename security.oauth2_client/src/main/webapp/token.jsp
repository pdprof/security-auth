<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.Iterator" %>
<% 
int size = request.getParameterMap().size();
String parameters[] = new String[size];
int x = 0;
Iterator<String> it = request.getParameterNames().asIterator();
while(it.hasNext()) {
	 parameters[x] = it.next();
	 x = x + 1;
}

String code = request.getParameter("code");
if (code == null)
	code = (String)session.getAttribute("code");
else 
	session.setAttribute("code", code);

String curl1 = "curl -v -k -u " + session.getAttribute("client_id") + ":" + session.getAttribute("client_secret")
	+ " -X POST -d 'grant_type=authorization_code&redirect_uri=" + session.getAttribute("redirect_uri") + "&code=" + code +"' 'https://" 
    + session.getAttribute("keycloak_host") + ":8443/auth/realms/" + session.getAttribute("keycloak_realm") + "/protocol/openid-connect/token' | jq";

String curl2 = "curl -v --data 'client_secret=" + session.getAttribute("client_secret") 
	+ "&client_id=" + session.getAttribute("client_id") + "&username=" + session.getAttribute("username")
	+ "&token=" + session.getAttribute("token") + "' https://" + session.getAttribute("keycloak_host") + ":8443/auth/realms/"
	+ session.getAttribute("keycloak_realm") + "/protocol/openid-connect/token/introspect -k| jq";
	
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Request an access code</title>
</head>
<body>
<ul>
<li>Request to get /auth endpoint URL</li>
<li>Request an access code using /auth endpoint</li>
<li>Get an access_code back</li>
<li>Use access_code to request a token using /token endpoint<br>
<%=curl1%>
</li>
<li>Get a token back</li>
<li><font color="red">Validate our token using /introspect endpoint</font><br>
<%=curl2%>
</li>
<li>Get a response with a status for our token (if it active or not)</li>
</ul>
</body>
</html>