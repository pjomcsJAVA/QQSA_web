package logic;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class App{


    public static void main(String[] args) throws Exception {
        System.out.println("Hello World");


        Server server = new Server();

        Connector connector = new SelectChannelConnector();
        connector.setPort(8081);
        connector.setHost("127.0.0.1");
        server.addConnector(connector);

        WebAppContext wac = new AliasEnhancedWebAppContext();
        wac.setContextPath("/home");
        wac.setBaseResource(
                new ResourceCollection(
                        new String[] {"./src/main/webapp", "./target"}));
        wac.setResourceAlias("/WEB-INF/classes/", "/classes/");

        server.setHandler(wac);
        server.setStopAtShutdown(true);
        server.start();
        server.join();
    }
}