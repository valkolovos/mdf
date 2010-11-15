package org.mdf.mockdata;

import java.lang.management.ManagementFactory;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class exists because our JBoss implementation does not export the MBean server
 * to JMX. It is not necessary (or even recommended) to use this class for Atlas.
 * 
 * @since 3.0
 *
 */
public class MBeanServerPublisher implements InitializingBean {
    
    private String _port = "9999";
    private String _shortHostName = System.getProperty("OrbitzHostApp");
    private String _version = System.getProperty("OrbitzHostVersion");
    private JMXConnectorServer _cs;
    
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        Registry registry = LocateRegistry.getRegistry(Integer.parseInt(_port));
        try {
            registry.list();
        } catch (ConnectException e) {
            Logger.getLogger(getClass()).info("Unable to connect to port " + _port + ", trying to create registry.");
            registry = LocateRegistry.createRegistry(Integer.parseInt(_port));
        }
        String mbeanServerName = "service:jmx:rmi:///jndi/rmi://localhost:" + _port + "/server-" + _shortHostName + "-" + _version + "-0";
        Remote r = null;
        try {
            r = registry.lookup(mbeanServerName);
        } catch (NotBoundException e) {
        }
        if (r != null) {
            registry.unbind(mbeanServerName);
        }

        // Retrieve the PlatformMBeanServer.
        //
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // Environment map.
        //
        HashMap env = new HashMap();

        // Create an RMI connector server.
        //
        // As specified in the JMXServiceURL the RMIServer stub will be
        // registered in the RMI registry running in the local host on
        // port _port with the name "mbeanServerName".
        //
        JMXServiceURL url =
            new JMXServiceURL(mbeanServerName);
        _cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);

        // Start the RMI connector server.
        //
        _cs.start();
    }
    
    public void shutdown() throws Exception {
        _cs.stop();
    }
    
    public void setPort(String port) {
        _port = port;
    }
    
    public void setShortHostName(String shortHostName) {
        _shortHostName = shortHostName;
    }
    
    public void setVersion(String version) {
        _version = version;
    }

}
