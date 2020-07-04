# What is spiderweb

日本語の説明は [こちら](https://github.com/maachang/spiderweb/blob/master/README_JP.md)

spiderweb specifies the IP address range for each node group or sets the terminal name, and manages the connection information between each other when spiderweb is running.

_

_

# How to create a jar file

※ It is assumed that git command and apache ant are installed.

```sh
$ git clone https://github.com/maachang/spiderweb.git
$ cd spiderweb
$ ant
```

_

_

# How to use spiderweb

First, you need to create config information that describes the IP address range and terminal name for each node group for managing connection information.

＜Exsample＞

conf/spiderweb.conf
```js
{
   "white": [
     ["192.168.1.0", "192.168.1.255"]
   ],
   "yellow": [
     ["172.16.1.100", "172.16.1.120"],
     ["172.16.1.200", "172.16.1.225"],
     ["172.16.1.254"],
     ["test3"]
   ],
   "green": [
     ["10.0.0.1", "10.0.0.100"],
     ["test1"],
     ["test2"]
   ]
}
````

```
＜Description of conf/spiderweb.conf＞

  [white] IP address range of the node group
   192.168.1. (0-255)
  [yellow] IP address range and terminal name of the node group.
    172.16.1. (100-120)
    172.16.1. (200-225)
    172.16.1.254
    [test3]
  [green] IP address range and terminal name of node group
    10.0.0. (1-100)
    [test1]
    [test2]
```

If you actually run the following sample program, try creating the above config file name according to your environment.

Next, since spiderweb is a 'library' that provides a single jar file, it is necessary to implement a java program.

＜Example of using spiderweb-java program sample＞

SpiderWebExsample.java
```java
import spiderweb.*;
import java.util.*;

public class SpiderWebExsample {
  public static fianl void main(String[] args) throws Exception {
    long nextTime = -1L;

    // spiderweb initialization.
    SpiderWeb spiderweb = new SpiderWeb();

    // When the mode is 'Synchronous mode', the connection management information 
    while(true) {

      // If the mode has not changed to 'Synchronous mode', do nothing.
      // Or there is no node group If there is no group in connection management, do nothing.
      if(spiderweb.getMode() != SpiderWeb.TYPE_SYNC || spiderweb.isEmpty()) {
        Thread.sleep(500);
        continue;
      // Output connection management information group for each node group once every 5 seconds.
      } else if(System.currentTimeMillis() < nextTime) {
        Thread.sleep(500);
        continue;
      }
      // Set the next display time.
      nextTime = System.currentTimeMillis() + 5000L;

      System.out.println();

      // Get node group list and display information for each node group.
      Iterator<String> nodeGroups = spiderweb.nodeGroups();
      while(nodeGroups.hasNext()) {

        // Get one node group.
        SpiderWebList list = spiderweb.get(nodeGroups.next());
        System.out.println("<" + list.getNodeGroupName() + ">");

        // Output node group connection management information.
        int len = list.size();
        for(int i = 0; i < len; i ++) {
          System.out.println("  [" + list.get(no) + "]");
        }
      }
    }
  }

}
```

Compile and run the sample.

(Example when spiderweb.jar version is 0.0.1)

＜Compile＞

```sh
javac -classpath ./spiderweb-0.0.1.jar SpiderWebExsample.java
```

＜Execution＞

```sh
java -classpath ./spiderweb-0.0.1.jar SpiderWebExsample
```

By running this program on multiple terminals, you can display the terminal connection group of those terminals.

_

_

# Finally

I hope this tool will make it easier to synchronize your device.