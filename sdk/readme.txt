Welcome to the cajo project!
http://cajo.java.net
            ___  _
           / ,_`(_) ___
          / __ \ _ / _ \
         / / _` | | (_) }
         \ \__,_; ;\___/
          \ \___/ /
           \_____/

This SDK is intended to provide the fastest possible introduction to the
fundamental capabilities of the cajo project, and its four primary goals:

* To seamlessly link JVMs transparently over the network
* To provide dynamic transfer of controller objects to distribute workload
* To provide dynamic transfer of agent objects to distribute workload
* To furnish remote Graphical User Interfaces

Getting Started:

If you have not already, please visit the SDK summary page at the cajo
project: http://java.net/projects/cajo/pages/Home it provides a very quick
overview.

An ant script, build.xml is provided; you can either convert it into a
project in your favourite IDE, or use it with command line tools. The
design is so straightforward, no special IDE plug-ins are necessary.

The ant script has four targets:

 - javadoc         create a complete javadoc site, in a new doc directory
 - build(default)  construct the example server, its controller, and view
 - startserver     begin operation of the server (with default arguments)
 - startclient     start a client application to connect to the server

Once the doc directory is created, simply open your browser to
doc/index.html to view the detailed documentation.

The purpose of this SDK is to provide a starting point. Once you are familiar
with its components, feel free to overwrite, and augment it, to meet your
needs. To create a new project, simply start with a fresh copy of the SDK.

Your comments, suggestions, and questions are most welcome at the SDK users
forum: http://java.net/projects/cajo/forums/userforum

Licence Information:
The cajo grail.jar library provided in the SDK is licensed under the GNU
LGPL v3.0, or at your option, any later version published by the Free
Software Foundation. The demonstration java files are licensed under the
Apache Licence 2.0.
