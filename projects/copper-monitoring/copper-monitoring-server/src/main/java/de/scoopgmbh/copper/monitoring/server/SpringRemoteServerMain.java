/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitoring.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import de.scoopgmbh.copper.monitoring.core.CopperMonitorInterface;

public class SpringRemoteServerMain {
	
	static final Logger logger = LoggerFactory.getLogger(SpringRemoteServerMain.class);
	private Server server;
	private final CopperMonitorInterfaceFactory copperMonitorInterfaceFactory;
	private final int port;
	private final String host; 
	
	public SpringRemoteServerMain(CopperMonitorInterfaceFactory copperMonitorInterfaceFactory, int port, String host) {
		super();
		this.copperMonitorInterfaceFactory = copperMonitorInterfaceFactory;
		this.port = port;
		this.host = host;
	}

	public void start() {
		logger.info("Starting Copper-Monitor-Server (jetty)");

		server = new Server();
		SocketConnector connector = new SocketConnector();
		connector.setPort(port);
		connector.setHost(host);
		server.setConnectors(new Connector[] { connector });

		ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", true, false);
		
//		org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
//		rootLogger.setLevel(Level.ALL);


		//prepare injecting bean to spring
		XmlWebApplicationContext webApplicationContext = new XmlWebApplicationContext(){
			@Override
			protected String[] getDefaultConfigLocations() {
				return new String[]{"classpath:/de/scoopgmbh/copper/monitoring/server/DispatcherServlet.xml"};
			}
		};
		DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();
		parentBeanFactory.registerSingleton("parentWorkaround", this);
		GenericApplicationContext parentContext = new GenericApplicationContext(parentBeanFactory);
		parentContext.refresh();
		webApplicationContext.setParent(parentContext);
		
		DispatcherServlet dispatcherServlet = new DispatcherServlet(webApplicationContext);
		ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
		servletContextHandler.addServlet(servletHolder, "/*");
		server.setHandler(servletContextHandler);
		
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**inject bean to spring context ( called from spring )
	 */
	@SuppressWarnings("unused")
	private CopperMonitorInterface createSpringBeanWorkaround(){
		return copperMonitorInterfaceFactory.createCopperMonitorInterface();
	}

	private void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	public static void main(String[] args) {
		if (!new File(args[0]).exists() || args.length==0){
			throw new IllegalArgumentException("valid property file loctaion must be passed as argument invalid: "+(args.length>0?new File(args[0]).getAbsolutePath():"nothing"));
		}
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(args[0]));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		final Integer port = Integer.valueOf((String) properties.getProperty("webapp.jetty.listener.port"));
		final String host = (String)properties.getProperty("webapp.jetty.host");
		
		PropertyConfigurator.configure(args[1]);
		System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");
		
		SpringRemoteServerMain springRemoteServerMain;//TODO jmx access
		springRemoteServerMain = new SpringRemoteServerMain(null,port,host);

		
		try {
			springRemoteServerMain.start();
			System.in.read();
			springRemoteServerMain.stop();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	



}