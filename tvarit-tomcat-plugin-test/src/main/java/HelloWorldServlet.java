import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Created by sachi_000 on 2/28/2016.
 */
@WebServlet(name = "HelloWorldServlet", urlPatterns = "/HelloWorldServlet")
public class HelloWorldServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final PrintWriter responseWriter = response.getWriter();
        String hostName;
        final InetAddress localHost;
        try {
            localHost = InetAddress.getLocalHost();
            hostName = localHost.getHostName();
        } catch (UnknownHostException e) {
            hostName = "Unkonwn";
        }
        responseWriter.println("<div>Server Host: " + hostName + "</div>");
        responseWriter.println("<div>Cookies</div>");
        final Cookie[] cookies = request.getCookies();
        responseWriter.println("<table border=''>");
        responseWriter.println(
                "<thead><th>Name</th><th>Value</th><th>Domain</th><th>Path</th><th>MaxAge</th>"
        );
        if (cookies != null) {
            Arrays.asList(cookies).forEach(cookie -> {
                responseWriter.println(
                        "<tr><td>" + cookie.getName() + "</td><td>" + cookie.getValue() + "</td><td>" + cookie.getDomain() + "</td><td>" + cookie.getPath() + "</td><td>" + cookie.getMaxAge() + "</td>" + "</tr>"

                );
            });
        }

        responseWriter.println("</table>");
        responseWriter.println("<div>headers</div>");
        responseWriter.println("<table border=''>");
        responseWriter.println(
                "<thead><th>Name</th><th>Value</th></thead>"
        );
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String eachHeaderName = headerNames.nextElement();
            final Enumeration<String> eachHeaderValues = request.getHeaders(eachHeaderName);
            while (eachHeaderValues.hasMoreElements()) {
                String eachHeaderValue = eachHeaderValues.nextElement();
                responseWriter.println("<tr><td>" + eachHeaderName + "</td><td>" + eachHeaderValue + "</td></tr>");
            }
        }
        responseWriter.println("</table>");

        InitialContext cxt = null;
        try {
            cxt = new InitialContext();
        } catch (NamingException e) {
            throw new ServletException(e);
        }

        DataSource ds = null;
        try {
            ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/postgres");
        } catch (NamingException e) {
            throw new ServletException(e);
        }

        if (ds == null) {
            throw new ServletException("Data source not found!");
        }
        DatabaseMetaData databaseMetaData;
        final Connection connection;
        try {
            connection = ds.getConnection();
            databaseMetaData = connection.getMetaData();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        try {
            responseWriter.println("<span>Found data source: " + databaseMetaData.getDatabaseProductName() + ", connected as db user " + databaseMetaData.getUserName() + "</span>");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        response.setContentType("text/html");
        response.getWriter().close();

    }
}
