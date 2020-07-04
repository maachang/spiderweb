# spiderwebとは

English documents [here](https://github.com/maachang/spiderweb/blob/master/README.md)

spiderwebはノードグループ単位でIPアドレスの範囲を指定、または端末名を設定して、spiderwebが起動している場合に互いの端末で接続情報を管理します。

_

_

# jarファイルの作成方法

※ git コマンド と apache antがインストールされてることが前提です.

```sh
$ git clone https://github.com/maachang/spiderweb.git
$ cd spiderweb
$ ant
```

_

_

# spiderwebの使い方

まずはじめに接続情報を管理するための、ノードグループ毎のIPアドレス範囲、端末名を記載したconfig情報を作成する必要があります。

＜サンプル＞

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
＜conf/spiderweb.conf の説明＞

 [white]ノードグループのIPアドレスの範囲
  192.168.1.(0 - 255)
 [yellow]ノードグループのIPアドレスの範囲と端末名.
   172.16.1.(100 - 120)
   172.16.1.(200 - 225)
   172.16.1.254
   [test3]
 [green]ノードグループのIPアドレスの範囲と端末名
   10.0.0.(1 - 100)
   [test1] 
   [test2]
```
実際に以下のサンプルプログラムを動かす場合、上記のconfigファイル名で、自分の環境に合わせて作成してみてください.

次にspiderwebはjarファイル単体を提供する「ライブラリ」なので、javaプログラムの実装を行う必要があります.

＜spiderweb利用例 - javaプログラムサンプル＞

SpiderWebExsample.java
```java
import spiderweb.*;
import java.util.*;

public class spiderwebExsample {
  public static fianl void main(String[] args) throws Exception {
    long nextTime = -1L;

    // spiderwebの初期化.
    SpiderWeb spiderweb = new SpiderWeb();

    // モードが「同期モード」で５秒に一度、ノードグループ毎の接続管理情報群を表示する.
    while(true) {

      // モードが「同期モード」に移行してない場合は、何もしない.
      // もしくは、１つもノードグループが存在しない・接続管理上群が存在しない場合は、何もしない.
      if(spiderweb.getMode() != spiderweb.TYPE_SYNC || spiderweb.isEmpty()) {
        Thread.sleep(500);
        continue;
      // ５秒に一度、ノードグループ毎の接続管理情報群を出力する.
      } else if(System.currentTimeMillis() < nextTime) {
        Thread.sleep(500);
        continue;
      }
      // 次の表示時間を設定.
      nextTime = System.currentTimeMillis() + 5000L;

      System.out.println();

      // ノードグループ一覧を取得して、ノードグループ毎の情報を表示.
      Iterator<String> nodeGroups = spiderweb.nodeGroups();
      while(nodeGroups.hasNext()) {

        // 1つのノードグループを取得.
        spiderwebList list = spiderweb.get(nodeGroups.next());
        System.out.println("<" + list.getNodeGroupName() + ">");

        // ノードグループの接続管理情報を出力する.
        int len = list.size();
        for(int i = 0; i < len; i ++) {
          System.out.println("  [" + list.get(no) + "]");
        }
      }
    }
  }

}
```

サンプルをコンパイルして実行.

(spiderweb.jarのバージョンが 0.0.1 の場合の例)

＜コンパイル＞
```sh
javac -classpath ./spiderweb-0.0.1.jar SpiderWebExsample.java
```

＜実行＞
```sh
java -classpath ./spiderweb-0.0.1.jar SpiderWebExsample
```

このプログラムを、複数の端末で動かすことで、それらの端末の端末接続群が表示できます。

_

_

# 最後に

このツールによって、端末やサーバの同期を取る処理が、簡単になることを願ってます。