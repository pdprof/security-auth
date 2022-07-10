<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<% 
int size = request.getParameterMap().size();
String parameters[] = new String[size];
int x = 0;
while(request.getParameterNames().hasMoreElements()) {
	 parameters[x] = request.getParameterNames().nextElement();
}

String code = request.getParameter("code");
if (code == null)
	code = (String)session.getAttribute("code");
else 
	session.setAttribute("code", code);

String curl1 = "curl -v -k -u " + session.getAttribute("client_id") + ":" + session.getAttribute("client_secret")
	+ " -X POST -d 'grant_type=authorization_code&redirect_uri=" + session.getAttribute("redirect_uri") + "&code=" + code +"' 'https://" 
    + session.getAttribute("keycloak_host") + ":8443/auth/realms/" + session.getAttribute("keycloak_realm") + "/protocol/openid-connect/token' | jq";

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
<li>Get an access_code back<br>
<table>
<%
for(int i=0; i<size; i++) {
%>
<tr><td><%=parameters[i]%></td><td><%=request.getParameter(parameters[i])%></td></tr>
<%
}
%>
</table></li>
<li><font color="red">Use access_code to request a token using /token endpoint</font><br>
<%=curl1%>
</li>
<li>Get a token back<br>
<table>
<form action="client" method="get">
<tr><td>username</td><td><input name="username" type="text" size="40"></td></tr>
<tr><td>token</td><td><input name="token" type="text" size="40"></td></td></tr>
<tr><td></td><td><input type="submit" value="Get curl command of introspect the token"></td></tr>
<input type="hidden" name="mode" value="token">
</form>
</table>
</li>
<li>Validate our token using /introspect endpoint</li>
<li>Get a response with a status for our token (if it active or not)</li>
</ul>
</body>
</html>