Welcome to the cajo project!
https://cajo.dev.java.net
           ___  _
          / ,_`(_) ___
         / __ \ _ / _ \
        / / _` | | (_) }
        \ \__,_; ;\___/
         \ \___/ /
          \_____/

This SDK is intended to provide the fastest possible introduction to the
capabilities of the cajo project, and its three primary goals:

* To seamlessly link JVMs transparently over the network
* To provide dynamic transfer of controller objects to distribute workload
* To furnish remote Graphical User Interfaces

Getting Started:

An ant script, build.xml is provided; you can either convert it into a
project in your favourite IDE, or use it with command line tools. The
design is so straightforward, no special IDE plug-ins are necessaru.

The ant script has four targets:

javadoc         create a complete javadoc site, in a new doc directory
build(default)  construct the example server, its controller, and view
start           begin operation of the server (with default arguments)
client          start a client application to connect to the server

Once the doc directory is created, simply open your browser to
doc/index.html to view the detailed documentation.

The purpose of this SDK is to provide a starting point. Once you understand
its components, feel free to overwrite, and augment it, to meet your needs.
To begin another project, simply start with another fresh copy of the SDK.

Licence Information:
The cajo grail.jar library provided in the SDK is licensed under the GNU
LGPL v3.0, or at your option, any later version published by the Free
Software Foundation. The demonstration java files are released into the
public domain, to use and modify without condition. For complete details on
the GNU Lesser General Public Licence, please visit the following URL:

http://www.gnu.org/licenses/lgpl.html

For 64-bit developers:
The grail.jar provided in the SDK is compoled for a 32-bit JRE. If you wish
to 64-bit, download the cajo source https://cajo.dev.java.net/cajo.zip and
execute the ant grail target with a 64-bit compiler.
