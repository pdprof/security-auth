<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Index Page</title>
</head>
<body>
<ul>
<li><font color="red">Request to get /auth endpoint URL</font><br>
<table>
<form action="client" method="get">
<tr><td>Keycloak host</td><td><input name="keycloak_host" type="text" size="40"></td></tr>
<tr><td>Keycloak realm</td><td><input name="keycloak_realm" type="text" size="40"></td></td></tr>
<tr><td>Redirect uri</td><td><input name="redirect_uri" type="text" size="40"></td></tr>
<tr><td>scope</td><td><input name="scope" type="text" size="40"></td></tr>
<tr><td>client_id</td><td><input name="client_id" type="text" size="40"></td></tr>
<tr><td>client_secret</td><td><input name="client_secret" type="text" size="40"></td></tr>
<tr><td></td><td><input type="submit" value="Get /auth endpoint link"></td></tr>
<input type="hidden" name="mode" value="auth">
</form>
</table>
</li>
<li>Request an access code using /auth endpoint</li>
<li>Get an access_code back</li>
<li>Use access_code to request a token using /token endpoint</li>
<li>Get a token back</li>
<li>Validate our token using /introspect endpoint<br>
	curl </li>
<li>Get a response with a status for our token (if it active or not)</li>
</ul>
</body>
</html>