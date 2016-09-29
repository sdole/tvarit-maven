import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
        responseWriter.println("<div>" + hostName + "</div>");
        responseWriter.println("<div>cookies</div>");
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
            })
            ;
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
        response.setContentType("text/html");
        response.getWriter().close();

    }
}
