package pdprof.security.oauth2_client;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jdk.internal.icu.text.NormalizerBase.Mode;

/**
 * Servlet implementation class Client
 */
@WebServlet("/client")
public class Client extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Client() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mode = request.getParameter("mode");
		HttpSession sess = request.getSession();
		String keycloak_host = "localhost";
		String keycloak_realm = "pdprof";
		String redirect_url =  request.getRequestURI();
		String scope = "apiconnect";
		String client_id = "api-services";
		String client_secret = "changeme";
		String username = "api-user";
		String token = "changeme";
		if (sess.isNew() || mode == null) {
			RequestDispatcher dp = request.getRequestDispatcher("/index.jsp");
			dp.forward(request, response);
		} else if(mode.equals("auth")) {
			keycloak_host = request.getParameter("keycloak_host");
			keycloak_realm = request.getParameter("keycloak_realm");
			redirect_url = request.getParameter("redirect_url");
			scope = request.getParameter("scope");
			client_id = request.getParameter("client_id");
			client_secret = request.getParameter("client_secret");
			
			sess.setAttribute("keycloak_host", keycloak_host);
			sess.setAttribute("keycloak_realm", keycloak_realm);
			sess.setAttribute("redirect_url", redirect_url);
			sess.setAttribute("scope", scope);
			sess.setAttribute("client_id", client_id);
			sess.setAttribute("client_secret", client_secret);
			RequestDispatcher dp = request.getRequestDispatcher("/auth.jsp");
			dp.forward(request, response);
		} else if (mode.equals("token")){ 
			username = request.getParameter("username");
			token = request.getParameter("token");
			
			sess.setAttribute("username", username);
			sess.setAttribute("token", token);
			RequestDispatcher dp = request.getRequestDispatcher("/token.jsp");
			dp.forward(request, response);
		} else {
			response.getWriter().append("Served at: ").append(request.getContextPath());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
