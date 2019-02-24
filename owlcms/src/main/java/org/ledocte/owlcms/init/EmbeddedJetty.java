/***
 * Copyright (c) 2018-2019 Jean-François Lamy
 * 
 * This software is licensed under the the Apache 2.0 License amended with the
 * Commons Clause.
 * License text at https://github.com/jflamy/owlcms4/master/License
 * See https://redislabs.com/wp-content/uploads/2018/10/Commons-Clause-White-Paper.pdf
 */
package org.ledocte.owlcms.init;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.vaadin.flow.server.startup.ServletContextListeners;

import ch.qos.logback.classic.Logger;

/**
 * The Class Main.
 */
public class EmbeddedJetty {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(EmbeddedJetty.class);

    /**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String... args) throws Exception {
		// Redirect java.util.logging logs to SLF4J
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		
    	// read server.port parameter from -D"server.port"=9999 on java command line
    	Integer serverPort = Integer.getInteger("server.port",8080);
    	logStart(serverPort);
        new EmbeddedJetty().run(serverPort, "/"); //$NON-NLS-1$
    }
	
	/**
	 * Run.
	 *
	 * @param port the port
	 * @param contextPath the context path
	 * @throws Exception the exception
	 */
	public void run(int port, String contextPath) throws Exception {
        URL webRootLocation = this.getClass().getResource("/META-INF/resources/"); //$NON-NLS-1$
        URI webRootUri = webRootLocation.toURI();

        WebAppContext context = new WebAppContext();
        context.setBaseResource(Resource.newResource(webRootUri));
        context.setContextPath(contextPath);
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
        context.setConfigurationDiscovered(true);
        context.setConfigurations(new Configuration[]{
                new AnnotationConfiguration(),
                new WebInfConfiguration(),
                new WebXmlConfiguration(),
                new MetaInfConfiguration(),
                new FragmentConfiguration(),
                new EnvConfiguration(),
                new PlusConfiguration(),
                new JettyWebXmlConfiguration()
        });
        context.getServletContext().setExtendedListenerTypes(true);
        context.addEventListener(new ServletContextListeners());

        Server server = new Server(port);
        server.setHandler(context);

        server.start();
        logger.info("started on port {}", port); //$NON-NLS-1$
        server.join();
    }

	private static void logStart(Integer serverPort) throws IOException, ParseException {
		InputStream in = EmbeddedJetty.class.getResourceAsStream("/build.properties"); //$NON-NLS-1$
		Properties buildProps = new Properties();
		buildProps.load(in);
		Properties props = buildProps;
    	String version = props.getProperty("version"); //$NON-NLS-1$
		String buildTimestamp = props.getProperty("buildTimestamp"); //$NON-NLS-1$
		//String buildZone = props.getProperty("buildZone");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$
		format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
		Date date = format.parse(buildTimestamp);
		//format.setTimeZone(TimeZone.getTimeZone(buildZone));
		SimpleDateFormat homeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String homeTimestamp = homeFormat.format(date);
		logger.info("owlcms {} (built {})",version,homeTimestamp);
	}
}