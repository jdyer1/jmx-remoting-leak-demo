package jdyer1.jmx;

import java.util.Date;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class DemonstrateResourceLeak {
    public static void main(String[] args) throws Exception {
        
        if(args.length==0) {
            System.out.println("Usage: DemonstrateResourceLeak [use-jboss-jmx-remote-http? Y|N]");
            System.exit(1);
        }
        String jmxUrl = null;
        if(args[0].equalsIgnoreCase("Y")) {
            jmxUrl = "service:jmx:remote+http://localhost:1234";
            System.out.println("Using jboss jmx remoting url: " + jmxUrl);
        } else if(args[0].equalsIgnoreCase("N")) {
            jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:1234/jmxrmi";
            System.out.println("Using non-jboss jmx rmi url: " + jmxUrl);
        } else {
            System.out.println("Usage: DemonstrateResourceLeak [use-jboss-jmx-remote-http? Y|N]");
            System.exit(1);
        }
        
                
        JMXServiceURL url = new JMXServiceURL(jmxUrl);
        int i = 0;
        int nonNull = 0;
        int exceptionThrownClosing = 0;
        
        //Do an initial GC to get a baseline free memory.
        System.gc();
        long initialBytesFree = Runtime.getRuntime().freeMemory();
        long halfInitialBytesFree = (long) (initialBytesFree  / 2);
        System.out.println(new Date() + " | begin with  bytes free: " + initialBytesFree);
        
        while (true) {
            JMXConnector connector = null;
            try {
                connector = JMXConnectorFactory.connect(url);
            } catch (Exception e) {
                if(i==0) {
                    e.printStackTrace();
                }
                if (connector != null) {
                    nonNull++;
                    try {
                        connector.close();
                    } catch (Exception e1) {
                        exceptionThrownClosing++;
                    }

                }
            }
            if (connector != null) {
                System.out.println("Success! ending");
                break;
            }
            i++;
            if (i % 1000 == 0) {
                
                //Wait a second so the finalizer thread has an opportunity to do some work
                Thread.sleep(1000);
                
                //Do a full GC before measuring again
                System.gc();
                
                long bytesFree = Runtime.getRuntime().freeMemory();
                System.out.println(new Date() + " | tried " + i + " | returned non-null " + nonNull
                    + " | exception thrown closing " + exceptionThrownClosing + " bytes Free= " + bytesFree);
                if(bytesFree < halfInitialBytesFree) {
                    System.out.println("Half of the memory is gone, even after full gc. exiting.");
                    break;
                }
                if (i > 100000) {
                    System.out.println("Finished 100000 iterations. exiting.");
                    break;
                }
            }
        }

    }
}
