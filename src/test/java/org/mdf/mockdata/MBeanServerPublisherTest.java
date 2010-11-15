package org.mdf.mockdata;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.testng.annotations.Test;

public class MBeanServerPublisherTest {
    @Test
    public void happyPath() throws Exception {
        Thread t = null;
        final AtomicBoolean started = new AtomicBoolean(false);
        final MBeanServerPublisher publisher = new MBeanServerPublisher();
        try {
            Runnable r = new Runnable() {
                public void run() {
                    publisher.setShortHostName("mock-data-framework");
                    publisher.setVersion("99.999");
                    try {
                        publisher.afterPropertiesSet();
                        started.set(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            t = new Thread(r);
            t.start();
            while (!started.get()) {
                Thread.sleep(100);
            }
            String connectURL = "service:jmx:rmi:///jndi/rmi://localhost:9999/server-mock-data-framework-99.999-0";
            JMXServiceURL u = new JMXServiceURL(connectURL);
            JMXConnector con = JMXConnectorFactory.connect(u);
            MBeanServerConnection mbsc = con.getMBeanServerConnection();
            mbsc.queryMBeans(null, null);
        } finally {
            try {
                publisher.shutdown();
            } catch (Throwable throwable) {
            }
            if (t != null) {
                t.interrupt();
            }
        }
    }
}
