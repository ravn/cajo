rem build a monolithic server and separate client application:

rem build the client:
javac gnu\cajo\invoke\Client.java
rmic  -v1.2 gnu.cajo.invoke.Remote
jar   cfm client.jar client.mft gnu\cajo\invoke\*.class
del   gnu\cajo\invoke\*.class

rem build the proxy:
javac example\Builder.java
java  example.Builder
jar   cf proxy.jar example\*.class example\include\*.* example\gui\*.class gnu\cajo\utils\*.class
del   example\*.class
del   example\gui\*.class
del   example\include\proxy.ser
del   gnu\cajo\invoke\*.class
del   gnu\cajo\utils\*.class

rem build the server:
javac example\Main.java
rmic  -v1.2 gnu.cajo.invoke.Remote
jar cfm server.jar example\example.mft example\*.class gnu\cajo\invoke\*.class gnu\cajo\utils\*.class proxy.jar
del   example\*.class
del   gnu\cajo\invoke\*.class
del   gnu\cajo\utils\*.class
del   proxy.jar
