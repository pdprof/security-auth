package pdprof.security.saml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

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
			
			com.ibm.websphere.ssl.JSSEHelper jsseHelper = com.ibm.websphere.ssl.JSSEHelper.getInstance();
			Properties prop = jsseHelper.getProperties("NodeDefaultSSLSettings"); 
			
	        PrivateKey privateKey = getPrivateKey(prop); //getPrivateKey();
	        X509Certificate cert = getCert(prop); // getCert();
			
			InputSource is = new InputSource(new StringReader(authnMessage));
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);
			
			Element node = (Element)document.getElementsByTagName("samlp:AuthnRequest").item(0);
	        Attr idAttr = document.createAttribute("ID");
	        idAttr.setValue(requestId);
	        node.setAttributeNode(idAttr);
	        node.setIdAttribute("ID", true);
	        
	        XMLSignatureFactory xmlSignFactory = XMLSignatureFactory.getInstance("DOM");

	        ArrayList<Transform> refTransformList = new ArrayList<Transform>();
	        refTransformList.add(xmlSignFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec)null));
	        refTransformList.add(xmlSignFactory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec)null));

	        DigestMethod digestMethod = xmlSignFactory.newDigestMethod(DigestMethod.SHA256, null);
	        
	        // Use ID which was set before.
	        Reference ref = xmlSignFactory.newReference("#" + requestId, 
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
	
	public PrivateKey getPrivateKey (Properties prop) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException {
		KeyStore ks = KeyStore.getInstance(prop.getProperty("com.ibm.ssl.keyStoreType"));
		ks.load(new FileInputStream(prop.getProperty("com.ibm.ssl.keyStore")), prop.getProperty("com.ibm.ssl.keyStorePassword").toCharArray());
		PrivateKey privatekey = (PrivateKey) ks.getKey("default", prop.getProperty("com.ibm.ssl.keyStorePassword").toCharArray());
		return privatekey;
	}
	
	private X509Certificate getCert(Properties prop) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		KeyStore ts = KeyStore.getInstance(prop.getProperty("com.ibm.ssl.trustStoreType"));
		ts.load(new FileInputStream(prop.getProperty("com.ibm.ssl.trustStore")), prop.getProperty("com.ibm.ssl.trustStorePassword").toCharArray());
		X509Certificate cert = (X509Certificate) ts.getCertificate("root");
		return cert;
	}

}
