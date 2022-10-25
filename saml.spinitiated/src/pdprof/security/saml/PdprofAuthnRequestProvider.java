package pdprof.security.saml;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ws.wssecurity.token.UTC;
import com.ibm.websphere.security.NotImplementedException;
import com.ibm.wsspi.security.web.saml.AuthnRequestProvider;

public class PdprofAuthnRequestProvider implements AuthnRequestProvider {

	public HashMap<String, String> getAuthnRequest(HttpServletRequest req, String errorMsg, String acsUrl,
			ArrayList<String> ssoUrls) throws NotImplementedException {

		System.out.println("Create SAML AuthnRequest \n Date :: "+new Date());
		HashMap<String, String> map = new HashMap<String, String>();

		String ssoUrl = getSSOURL();
		map.put(AuthnRequestProvider.SSO_URL, ssoUrl);
		System.out.println("ssoUrl:: "+ssoUrl);
		
		String reqURI = req.getRequestURI();
		System.out.println("RequestURI:: "+reqURI);
		System.out.println("ssoUrls:: "+ssoUrls);
		
		String scheme = req.getScheme();
	    String serverName = req.getServerName();
	    int serverPort = req.getServerPort();
	    String contextPath = req.getContextPath();
	    
	    String relayState = generateRandom();
	    // If the URL is ACS URL then do not set relayState parameter with constructed URL
	    if(!reqURI.contains("samlsps") && !reqURI.contains("error")) {
	    	StringBuilder url = new StringBuilder();
		    url.append(scheme).append("://").append(serverName);
		    if (serverPort != 80 && serverPort != 443) {
		        url.append(":").append(serverPort);
		    }
		    url.append(contextPath);
		    System.out.println("URL:: "+url.toString());
		    relayState=url.toString();
	    }
	    
		map.put(AuthnRequestProvider.RELAY_STATE, relayState);
		System.out.println("RelayState:: "+relayState);
		
		String requestId = generateRandom();
		map.put(AuthnRequestProvider.REQUEST_ID, requestId);
		System.out.println("RequestId:: "+requestId);
		
		String acsURL = getACSURL();
		String issuer = acsUrl;
		String destination = ssoUrl;
		System.out.println("acsURL:: "+acsURL);

		// create AuthnRequest- Authentications methods
		// urn:federation:authentication:windows
		// urn:oasis:names:tc:SAML:2.0:ac:classes:Password
		// urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos
		String authnMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" " + "ID=\"" + requestId
				+ "\" Version=\"2.0\" " + "IssueInstant=\"" + UTC.format(new java.util.Date())
				+ "\" ForceAuthn=\"false\" IsPassive=\"false\""
				+ " ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" "
				+ "AssertionConsumerServiceURL=\"" + acsURL + "\" " + "Destination=\"" + destination + "\"> "
				+ "<saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" + issuer
				+ "</saml:Issuer> <samlp:NameIDPolicy"
				+ " Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:transient\"" + " SPNameQualifier=\"mysp\""
				+ " AllowCreate=\"true\" /> <samlp:RequestedAuthnContext Comparison=\"exact\"> "
				+ "<saml:AuthnContextClassRef xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">"
				+ " urn:oasis:names:tc:SAML:2.0:ac:classes:Password </saml:AuthnContextClassRef>"
				+ "</samlp:RequestedAuthnContext> </samlp:AuthnRequest>";

		System.out.println("Before encoding authnMessage :"+authnMessage);
		String encodedAuth = Base64.getEncoder().encodeToString(authnMessage.getBytes());
		System.out.println("After encoding authnMessage :"+encodedAuth);
		
		map.put(AuthnRequestProvider.AUTHN_REQUEST, encodedAuth);
		return map;
	}

	private String generateRandom() {
		Object obj = new Object();
		return Integer.toHexString(obj.hashCode());
	}

	@Override
	public String getIdentityProviderOrErrorURL(HttpServletRequest arg0, String arg1, String arg2,
			ArrayList<String> arg3) throws NotImplementedException {
		return null;
	}
	
	private String getSSOURL() {
		return System.getProperty("pdprof.security.saml.ssoUrl", "http://localhost:8080/auth/realms/pdprof/protocol/saml");
	}
	
	private String getACSURL() {
		return System.getProperty("pdprof.security.saml.acsUrl", "http://localhost:7080/samlsps/acs");
	}

}
