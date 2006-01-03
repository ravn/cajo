rem build separate server, client, and proxy jars:

rem build the library
call library

rem build the client:
javac -target 1.2 -source 1.2 gnu\cajo\invoke\Client.java
rmic  -v1.2 gnu.cajo.invoke.Remote
jar   cfm client.jar client.mft gnu\cajo\invoke\*.class
del   gnu\cajo\invoke\*.class

rem build the proxy:
javac -target 1.2 -source 1.2 example\Builder.java
java  example.Builder
jar   cf proxy.jar example\*.class example\include\*.* example\gui\*.class gnu\cajo\utils\BaseProxy*.class gnu\cajo\utils\ProxyLoader.class
del   example\*.class
del   example\gui\*.class
del   example\include\proxy.ser
del   gnu\cajo\invoke\*.class
del   gnu\cajo\utils\*.class

rem build the server:
javac -target 1.2 -source 1.2 example\Main.java
jar   cfm server.jar example\example.mft example\*.class gnu\cajo\utils\ProxyLoader.class gnu\cajo\utils\CodebaseServer.class
rem I am still not sure why CodebaseServer MUST be included in the jar...
del   example\*.class
del   gnu\cajo\invoke\*.class
del   gnu\cajo\utils\*.class
