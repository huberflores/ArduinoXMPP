package ee.ut.ati.masters;

import ee.ut.ati.masters.http.ArduinoHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

public class Main {

	public static void main(String[] args) throws Exception {

		//Start the web server
		Server server = new Server(8080);

		ContextHandler context = new ContextHandler();
		context.setContextPath("/data");
		context.setResourceBase(".");
		context.setClassLoader(Thread.currentThread().getContextClassLoader());
		context.setHandler(new ArduinoHandler());
		server.setHandler(context);
		server.start();
		server.join();
	}
}
