rem build the complete project codebase jar:

javac gnu\cajo\invoke\*.java
javac gnu\cajo\utils\*.java
javac gnu\cajo\utils\extra\*.java
rmic  -v1.2 gnu.cajo.invoke.Remote
jar   cf cajo.jar gnu\cajo\invoke\*.class gnu\cajo\utils\*.class gnu\cajo\utils\extra\*.class
del   gnu\cajo\invoke\*.class
del   gnu\cajo\utils\*.class
del   gnu\cajo\utils\extra\*.class
