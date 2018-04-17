-- JohnCatherino - 10 Jun 2005

Why script cajo?
===
I think the first step in answering that question is to answer: _Why script at all?_

Scripts are typically small programs, which can work in either of two ways:

They can be typed in at the keyboard, and be executed, tested, and modified, interactively.
They can be stored in files, which can be read by the script interpreter and executed automatically.
Scripts provide a fast test/debug turn around cycle, when used interactively. Scripts can also provide the potential for end users to actively customise an existing application. Scripts are not a replacement, or short-cut for compiled Java code, but rather a very powerful complement. It is far more flexible and readable than XML files; which many other projects use.
The scripting language I will be discussing is BeanShell.

Why BeanShell?

Three main reasons:

* BeanShell uses the same syntax as Java, so the learning curve is small!
* BeanShell is overwhelmingly approved in JSR #274, and could likely become part of Java SE soon.
* BeanShell is completely free, it is licensed under the LGPL, just like cajo.

So now back to the original question: Why script cajo?

There are four particularly interesting applications of scripting the cajo project.

First it can allow scripts to orchestrate the interaction between ordinary compiled remote objects.

Second it can allow scripted objects to be called from compiled remote objects, to dynamically customise functionality.

Third it can allow script objects to dynamically interact with each other.

Finally, you can easily incorporate this scripting support into your own application. BeanShell provides the ability to 'drop-in' scripting capability, to allow users to dynamically customise the application.

The simple experiments below will progressively demonstrate the features of beanshell, to harness the power of cajo to script cooperation between Java Virtual Machines.

Requirements for the experiments:

* Download the latest version of BeanShell. You can either compile it yourself, or grab the precompiled binary.
* Download the latest version of cajo. Again, you can either compile it yourself, or grab the precompiled binary.
* Make sure both are in the classpath, either explicitly during the invocation of the beanshell interpreter, or in the environment variable. 

For example, the BeanShell interpreter can be launched in three distinct ways:

    java -cp bsh-xxx.jar;cajo.jar bsh.Interpreter filename [args]     // to run a script file

-or-

    java -cp bsh-xxx.jar;cajo.jar bsh.Interpreter     // to run the text-mode console

-or-

    java -cp bsh-xxx.jar;cajo.jar bsh.Console     // to run the GUI editor

**Experiment 1:** Make a script object remotely accessible:

    import gnu.cajo.invoke.Remote;
    import gnu.cajo.utils.ItemServer;

    Object test = new Object() { // example script object
       public String hello(String msg) {
          print("Hello called: " + msg);
          return "gotit!";
       }
       public void plural(String one, String two) {
          print("Plurality: " + one + " and " + two);
       }
       public String getDescription() {
          return
             "Remote BeanShell object Demonstration\n" +
             "This otherwise ordinary scripted object " +
             "is made remotely invocable.";
       }
    };
    
    Remote.config(null, 1198, null, 0); // use port 1198 for example 
    
    // make accessible under the name "example":
    ItemServer.bind(test, "example");

This can allow you to dynamically create remotely accessible objects that can be used by other scripts, or by compiled objects as well.

**Experiment 2:** Interact with remote cajo objects, compiled or scripted:

    import gnu.cajo.invoke.Remote;
    
    // get a remote object reference:
    Object item  = Remote.getItem("//serverName:1198/example");
    
    print(Remote.invoke(item, "getDescription", null));

You can invoke any public method on the remote object.

**Experiment 3**: Transparent use of remote objects:

Since BeanShell and cajo both use Java reflection as the basis for object method invocation; cajo makes it possible to have remote method invocations appear syntactically identical to local ones, using a very small wrapper.

    import gnu.cajo.invoke.Remote;
    
    wrap(String url) { // generic cajo object wrapper
       item = Remote.getItem(url);
       invoke(method, args) { item.invoke(method, args); }
       return this;
    }
    
    // get the example item reference:
    Object item = wrap("//serverName:1198/example");
    
    print(item.hello("from a client")); // say hi to the item
    
    item.plural("gin", "tonic"); // invoke its public method: plural

Younger developers sometimes call this technique Aspect Oriented Programming, while veteran C and Lisp programmers prefer the original more general term; Macro Programming. However we will call it a wrapper, in an attempt at a neutral nomenclature.

**Experiment 4:** A generic cajo graphical proxy host:

The following could be used with the main project server example, or the simple proxy server example.

    import gnu.cajo.invoke.*;
    
    // allow VM to run without a security policy file:
    System.setSecurityManager(new NoSecurityManager());
    // otherwise use an RMISecurityManager
    
    item  = Remote.getItem("//remoteHost/"); // get remote item reference
    
    proxy = Remote.invoke(item, "getProxy", null); // request its proxy
    
    if (proxy instanceof java.rmi.MarshalledObject)
      proxy = proxy.get(); // it canonically comes in a MarshalledObject
    
    proxy = Remote.invoke(proxy, "init", new Remote(proxy)); // request its GUI
    frame(proxy); // put it on screen

Display any remote item's GUI proxy!