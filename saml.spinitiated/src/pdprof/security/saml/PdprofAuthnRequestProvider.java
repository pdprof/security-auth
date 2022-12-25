package pdprof.security.saml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

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
				+ "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\""
				+ " Version=\"2.0\" " + "IssueInstant=\"" + UTC.format(new java.util.Date())
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


		try {
	        PrivateKey privateKey = getPrivateKey();
	        X509Certificate cert = getCert();
			
			InputSource is = new InputSource(new StringReader(authnMessage));
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);
			
			Element node = (Element)document.getElementsByTagName("samlp:AuthnRequest").item(0);
	        Attr idAttr = document.createAttribute("id");
	        idAttr.setValue(requestId);
	        node.setAttributeNode(idAttr);
	        node.setIdAttribute("id", true);
	        
	        XMLSignatureFactory xmlSignFactory = XMLSignatureFactory.getInstance("DOM");

	        ArrayList<Transform> refTransformList = new ArrayList<Transform>();
	        refTransformList.add(xmlSignFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec)null));
	        refTransformList.add(xmlSignFactory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec)null));

	        DigestMethod digestMethod = xmlSignFactory.newDigestMethod(DigestMethod.SHA256, null);
	        
	        // Use ID which was set before.
	        Reference ref = xmlSignFactory.newReference("#1", 
	                digestMethod, 
	                refTransformList, null, null);

	        CanonicalizationMethod cm = xmlSignFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec)null);

	        SignatureMethod sm = xmlSignFactory.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null);

	        SignedInfo signedInfo = xmlSignFactory.newSignedInfo(cm, sm, Collections.singletonList(ref));

	        KeyInfoFactory kif = xmlSignFactory.getKeyInfoFactory();
	        X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
	        KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

	        DOMSignContext dsc = new DOMSignContext(privateKey, node);
	        
	        XMLSignature signature = xmlSignFactory.newXMLSignature(signedInfo, keyInfo);
	        signature.sign(dsc);
	        
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer trans = tf.newTransformer();
	        StringWriter swr = new StringWriter();
	        trans.transform(new DOMSource(document), new StreamResult(swr));
	        
	        authnMessage = swr.toString();
        
		} catch (Exception e)  {
			e.printStackTrace();
		}
        
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
	
	public PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String keyStr = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDHVcx4MXlFBMwt" + 
				"Di5jkJJtF0OUZonWD5U7dQKmD5ug4gxOqKym2MHdYAtNXo/zvjaqdb/lsB8H9R0s" + 
				"m2Dv17djg2k+C450E1bTXxUwexEpkfSZ4kb+BT9sVw8CmzBqSqF59fe3YmhiTRPg" + 
				"Boay9TyzGCpv2BEq67trYWYbJfSajm8q9GnOCNu6gsHmQL3ySbz9HhOcb4wKpsF8" + 
				"rIJg73Car4h74iItIwuRxwl7txuyzK6Ar5MUJFqUKVhaAB/eYZzlqsBYtQTDqQwC" + 
				"JYOFbUtHsnCvm5UD+ivOYiywA9CmGYYG3846q6C+lZp3mbyEe+GTnLBowLmtxEma" + 
				"EyJWOYYFAgMBAAECggEBAKfQvdE6pYPLpDESTU+ZOQ0KluRq5wQGHnbt7YrsfPMX" + 
				"G5FlQ+U7ewrqftlmEP79VnxvoVy3x4glfag+L3/8NfJbgdCwXf+vAFv3IpmsIijD" + 
				"LhAXhfj5ZgnJyWNCT6JMrmFFCIWlHYgUp/TkyaYD/FQythdu6hUeXKzsVM/qRmGO" + 
				"p/38jmdjz1UyW01hfX4n76qBetqy82jkHMIio2Kt+2jRARAIWyAWildvBpCrPwvI" + 
				"aPPfaObr3I8v1cndKS5Fini87aSpeEH4HN6JNmiWABB7MzOTq8nC1gmdRnQCmtUZ" + 
				"oVSwACKC3wrFTn4mUPkWy3NS5/P94MpRp5vBk1Oc+AECgYEA42Kq15vubl6yBcsW" + 
				"/9L6q32jfwLD/N45vTFOlZI67zHeQFXZDQiPLpSW4qNF/JwnaijCSjptbutL9Io5" + 
				"yMYzLiqFsNTV2cK/fDcbmmqaiEWIoGSASt0XDw9n1LS9cX4J9bR1VvxiWFnOol6d" + 
				"UnS8EVFNbfvHPshph/CAORpMFQECgYEA4Gt6bVPcpg2bcsBDr0uAaNY2D9f05YDt" + 
				"55TuHaGbyKyRIbBE4pW3t9elLa+hZ7wWUYwWKuAYbzRKfLhGKeegX/v3a83RGHvb" + 
				"meCO218oR9+ZiKjWTpva9NKPs18l0uKqMKC7DBQdTuaBm0ZjUkMC9+uIfa6QgQwm" + 
				"qRCG6qhcHQUCgYEAxz9HOsUdacM6tY7faW4jTzA5mur+d872w0y4cqH/WGfXO4K+" + 
				"Pgh5BrUXfiLaCd9/PivXDBokmGRAW1jgB4l9gX+rRGdLuJRJHHxhiK7PGIftj7Zh" + 
				"ILiZIw45yo5RzmhGK/JkO8POHWMciPTlYEKAJaCbe7t7PQ16Q68/fEoJzwECgYEA" + 
				"kqEmyK/2hvh1DLDaiHpIWDc983QiqqFmz9zbB7lD1AYMfXpyR6mS9CeN4R/T0bdu" + 
				"zbE9+p2Y2W/NC1hLX63bd1tl+aXsbJbgkNXMAUFXTD6oTkYiYBItKbE5+vS7/eHj" + 
				"WQprrCSoUZkCLbX7xw/JrLrT32pjOC38RJJr1H5UURECgYEAnjb+biAVrHWUghld" + 
				"Ogo6ch86cgKSZbLOhpSbpE4RMklqKAE43jqlGp5fydVCYvytMJzvEnEN4QI23rpM" + 
				"ONZ6VI7txv4UVsyTpqiQCdQX3TjAcBKGtN6keJH4yibMfT+djxKN1JCAOoivFPwi" + 
				"f89ecCVze6nvJeFYCczmpRDqrk8=";			
	    byte[] encoded = Base64.getDecoder().decode(keyStr);
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
	    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
	
	}
	
	private X509Certificate getCert() throws IOException, CertificateException {	
		String certStr = "-----BEGIN CERTIFICATE-----\r\n" + 
				"MIIDFzCCAf8CAQEwDQYJKoZIhvcNAQELBQAwUDELMAkGA1UEBhMCSlAxDjAMBgNV\r\n" + 
				"BAgMBVRva3lvMQ0wCwYDVQQHDARLb3RvMQ8wDQYDVQQKDAZQZHByb2YxETAPBgNV\r\n" + 
				"BAMMCGxvY2FsaG9zMCAXDTIyMTIyNTEzMjAxNVoYDzMwMjIwNDI3MTMyMDE1WjBR\r\n" + 
				"MQswCQYDVQQGEwJKUDEOMAwGA1UECAwFVG9reW8xDTALBgNVBAcMBEtvdG8xDzAN\r\n" + 
				"BgNVBAoMBlBkcHJvZjESMBAGA1UEAwwJbG9jYWxob3N0MIIBIjANBgkqhkiG9w0B\r\n" + 
				"AQEFAAOCAQ8AMIIBCgKCAQEAx1XMeDF5RQTMLQ4uY5CSbRdDlGaJ1g+VO3UCpg+b\r\n" + 
				"oOIMTqisptjB3WALTV6P8742qnW/5bAfB/UdLJtg79e3Y4NpPguOdBNW018VMHsR\r\n" + 
				"KZH0meJG/gU/bFcPApswakqhefX3t2JoYk0T4AaGsvU8sxgqb9gRKuu7a2FmGyX0\r\n" + 
				"mo5vKvRpzgjbuoLB5kC98km8/R4TnG+MCqbBfKyCYO9wmq+Ie+IiLSMLkccJe7cb\r\n" + 
				"ssyugK+TFCRalClYWgAf3mGc5arAWLUEw6kMAiWDhW1LR7Jwr5uVA/orzmIssAPQ\r\n" + 
				"phmGBt/OOqugvpWad5m8hHvhk5ywaMC5rcRJmhMiVjmGBQIDAQABMA0GCSqGSIb3\r\n" + 
				"DQEBCwUAA4IBAQCdArJKLUFf1Ail4UXARfxceWb2+UMJoHEK2tZn7TBEqgE9+DMy\r\n" + 
				"ZvsSPFo/sWZXSVZ4MMgNIRmQ09ebLt9ojIw1qGDRscSndsGl5q0Sb/5480bBaUTM\r\n" + 
				"8Wni0871b28FnVyqKZJxZszXzgVl0o3KJWgHjYdRsHfHCW084Yw+lUnm2Pp64YZj\r\n" + 
				"CVZOJ2Xxf3gW/GUCD50iW0EEQG1n+dc5Jefik6leGvVz5IJiUi1a/C7/JP5/zxSb\r\n" + 
				"U1iix90DWdm4CDpmxvgJu0pi7HCMK1d11sxCyD8PLS8I6LyHp+QUV3J0SeyCVRsJ\r\n" + 
				"wp/QyWTY5bgnOv9ZEmhHHOi9q/ZEV5Xe/F70\r\n" + 
				"-----END CERTIFICATE-----";
	    CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream is = new ByteArrayInputStream(certStr.getBytes());
	    X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
	    return cert;
	}

}
