# JMX-Remoting Memory Leak Demonstration
Using `org.jboss.remoting:jboss-remoting:5.0.8.Final` and `org.jboss.remotingjmx:remoting-jmx:3.0.0.Final`, a memory leak occurs whenever a new JMX connection is unsuccessful.  These are the versions used by JBoss EAP 7.2.0 and Wildfly 14.  The memory leak occurs in the JMX client, that is, the monitoring software, not in Jboss/Wildfly itself.  

To run:
1. clone the project
2. run `mvn clean install`
3. To see the memory leak with jboss-remoting: `java -Xms256m -Xmx256m -jar target/jmx-remoting-fail-demo.jar Y`
4. To see no leak with rmi-jmx: `java -Xms256m -Xmx256m -jar target/jmx-remoting-fail-demo.jar N`

In `org.jboss.remotingjmx.RemotingConnector`, the instance of `org.jboss.remoting3.Endpoint` created in method `internalRemotingConnect` is not closed upon failure.  The `Endpoint` contains an inner class instance extending `org.jboss.remoting3.EndpointImpl$MapRegistration` which has a Finalizer (via parent `AbstractHandleableCloseable`).  These Finalizer instances accumulate in the heap and are not destroyed via GC.


```
$ cat /proc/version
Linux version 5.0.9-200.fc29.x86_64 (mockbuild@bkernel03.phx2.fedoraproject.org) (gcc version 8.3.1 20190223 (Red Hat 8.3.1-2) (GCC)) #1 SMP Mon Apr 22 00:55:30 UTC 2019

$ java -version
openjdk version "1.8.0_201"
OpenJDK Runtime Environment (build 1.8.0_201-b09)
OpenJDK 64-Bit Server VM (build 25.201-b09, mixed mode)
```

```
$ java -Xms512m -Xmx512m -jar target/jmx-remoting-fail-demo.jar Y
Using jboss jmx remoting url: service:jmx:remote+http://localhost:1234
Fri May 17 09:20:38 CDT 2019 | begin with  bytes free: 511813928
May 17, 2019 9:20:38 AM org.jboss.remoting3.EndpointImpl <clinit>
INFO: JBoss Remoting version 5.0.8.Final
May 17, 2019 9:20:38 AM org.xnio.Xnio <clinit>
INFO: XNIO version 3.5.3.Final
May 17, 2019 9:20:38 AM org.xnio.nio.NioXnio <clinit>
INFO: XNIO NIO Implementation Version 3.5.1.Final
May 17, 2019 9:20:39 AM org.wildfly.security.Version <clinit>
INFO: ELY00001: WildFly Elytron version 1.1.0.Final
Fri May 17 09:20:41 CDT 2019 | tried 1000 | returned non-null 0 | exception thrown closing 0 bytes Free= 498788544
Fri May 17 09:20:43 CDT 2019 | tried 2000 | returned non-null 0 | exception thrown closing 0 bytes Free= 485292320
Fri May 17 09:20:45 CDT 2019 | tried 3000 | returned non-null 0 | exception thrown closing 0 bytes Free= 472604824
Fri May 17 09:20:46 CDT 2019 | tried 4000 | returned non-null 0 | exception thrown closing 0 bytes Free= 459641096
Fri May 17 09:20:48 CDT 2019 | tried 5000 | returned non-null 0 | exception thrown closing 0 bytes Free= 446955408
Fri May 17 09:20:49 CDT 2019 | tried 6000 | returned non-null 0 | exception thrown closing 0 bytes Free= 434269992
Fri May 17 09:20:51 CDT 2019 | tried 7000 | returned non-null 0 | exception thrown closing 0 bytes Free= 421290208
Fri May 17 09:20:53 CDT 2019 | tried 8000 | returned non-null 0 | exception thrown closing 0 bytes Free= 408342680
Fri May 17 09:20:54 CDT 2019 | tried 9000 | returned non-null 0 | exception thrown closing 0 bytes Free= 395657496
Fri May 17 09:20:56 CDT 2019 | tried 10000 | returned non-null 0 | exception thrown closing 0 bytes Free= 382973432
Fri May 17 09:20:58 CDT 2019 | tried 11000 | returned non-null 0 | exception thrown closing 0 bytes Free= 370286192
Fri May 17 09:21:00 CDT 2019 | tried 12000 | returned non-null 0 | exception thrown closing 0 bytes Free= 357602984
Fri May 17 09:21:01 CDT 2019 | tried 13000 | returned non-null 0 | exception thrown closing 0 bytes Free= 344329192
Fri May 17 09:21:03 CDT 2019 | tried 14000 | returned non-null 0 | exception thrown closing 0 bytes Free= 331645144
Fri May 17 09:21:05 CDT 2019 | tried 15000 | returned non-null 0 | exception thrown closing 0 bytes Free= 318436648
Fri May 17 09:21:07 CDT 2019 | tried 16000 | returned non-null 0 | exception thrown closing 0 bytes Free= 305753328
Fri May 17 09:21:09 CDT 2019 | tried 17000 | returned non-null 0 | exception thrown closing 0 bytes Free= 293069496
Fri May 17 09:21:11 CDT 2019 | tried 18000 | returned non-null 0 | exception thrown closing 0 bytes Free= 280396320
Fri May 17 09:21:13 CDT 2019 | tried 19000 | returned non-null 0 | exception thrown closing 0 bytes Free= 267712224
Fri May 17 09:21:14 CDT 2019 | tried 20000 | returned non-null 0 | exception thrown closing 0 bytes Free= 255028392
Half of the memory is gone, even after full gc. exiting.
```

```
$ java -Xms512m -Xmx512m -jar target/jmx-remoting-fail-demo.jar N
Using non-jboss jmx rmi url: service:jmx:rmi:///jndi/rmi://localhost:1234/jmxrmi
Fri May 17 09:21:25 CDT 2019 | begin with  bytes free: 511813800
Fri May 17 09:21:26 CDT 2019 | tried 1000 | returned non-null 0 | exception thrown closing 0 bytes Free= 512451696
Fri May 17 09:21:28 CDT 2019 | tried 2000 | returned non-null 0 | exception thrown closing 0 bytes Free= 512514264
Fri May 17 09:21:29 CDT 2019 | tried 3000 | returned non-null 0 | exception thrown closing 0 bytes Free= 512509248
Fri May 17 09:21:30 CDT 2019 | tried 4000 | returned non-null 0 | exception thrown closing 0 bytes Free= 512508992
... etc ...
Fri May 17 09:23:16 CDT 2019 | tried 99000 | returned non-null 0 | exception thrown closing 0 bytes Free= 512493360
Fri May 17 09:23:18 CDT 2019 | tried 100000 | returned non-null 0 | exception thrown closing 0 bytes Free= 512493360
Fri May 17 09:23:19 CDT 2019 | tried 101000 | returned non-null 0 | exception thrown closing 0 bytes Free= 512493360
Finished 100000 iterations. exiting.


```

