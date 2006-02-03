rem build the complete project codebase jar:

javac -classpath . -g:none -target 1.2 -source 1.2 gnu\cajo\invoke\*.java
javac -classpath . -g:none -target 1.2 -source 1.2 gnu\cajo\utils\*.java
javac -classpath . -g:none -target 1.2 -source 1.2 gnu\cajo\utils\extra\*.java
rmic  -classpath . -v1.2 gnu.cajo.invoke.Remote
del   gnu\cajo\utils\ProxyLoader.class
del   gnu\cajo\utils\BaseProxy*.class
jar   cf cajo.jar gnu\cajo\invoke\*.class gnu\cajo\utils\*.class gnu\cajo\utils\extra\*.class
del   gnu\cajo\invoke\*.class
del   gnu\cajo\utils\*.class
del   gnu\cajo\utils\extra\*.class
