package gnu.cajo.invoke;

/*
 * Full-Privilige Security Manager
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, at version 2.1 of the license, or any
 * later version.  The license differs from the GNU General Public License
 * (GPL) to allow this library to be used in proprietary applications. The
 * standard GPL would forbid this.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * To receive a copy of the GNU General Public License visit their website at
 * http://www.gnu.org or via snail mail at Free Software Foundation Inc.,
 * 59 Temple Place Suite 330, Boston MA 02111-1307 USA
 */

/**
 * This utility class creates a trivial SecurityManager for developing proxy
 * hosting clients.  It allows trusted clients and servers to operate
 * without restriction, and without the need for a security policy file.  It
 * effectively allows both clients and proxies <b>full permissions</b> on the
 * machine.  While convenient for development purposes, this clearly would be
 * very unwise to use in an untrusted environment.  In production, the user
 * better impose his own security policy using the 2 interpreter switces:<p>
 * <code>-Djava.security.manager -Djava.security.policy=someURL</code><p>
 * This URL/file would contain the restrictions governing what both the
 * client code and loaded proxy code are permitted to do. A minimal, but
 * functional policy file would contain at least the following:<p><pre>
 * grant {
 *   permission java.net.SocketPermission "*:1024-", "accept";
 *   permission java.net.SocketPermission "*", "connect";
 * };</pre><p>This would allow the client, and its loaded code to open
 * server sockets on port 1024 and higher, and to connect to remote hosts
 * on any port, nothing else.  Slightly more permissive than an applet
 * sandbox, but still very safe for hosting machines.  Any code assigning
 * any SecurityManager should enclose the assignment in a try/catch
 * block, as the operation may be restricted by the user via the technique
 * described above. The assignment would result in the throwing of a
 * SecurityException. <p>If the <i>client</i> code is trusted, then a more
 * flexible policy file could be used such as:<p><pre>
 * grant codeBase "file:${java.class.path}" {
 *    permission java.security.AllPermission;
 * };
 * grant {
 *   permission java.net.SocketPermission "*:1024-", "accept";
 *   permission java.net.SocketPermission "*", "connect";
 * };</pre><p>This will allow classes loaded from the local filesystem full
 * permissions, while only allowing downloaded code to make socket
 * connections in the manner of the first policy file.<p>
 * <i>Note:</i> to allow proxies to run within this VM invites the
 * possibility of a <b>denial of service attack</b>, i.e. a proxy or, other
 * object, could consume all the VMs memory and compute cycles maliciously,
 * or even accidentially.  Therefore, it is recommended that proxy hosting
 * only be done either on protected networks, or with an expendible VM.
 */
public final class NoSecurityManager extends SecurityManager {
   /**
    * Nothing is performed in the constructor.  This class exists only to
    * short-circuit the permission checking mechanism of the Java runtime
    * by overriding the checkPermission method with a bodyless
    * implementation.
    */
   public NoSecurityManager() {}
   /**
    * In accordance with the SecurityManager paradigm, this method simply
    * returns, to indicate that a requested client operation is permitted.
    * Otherwise it could throw a SecurityException, but it never does.
    * This means, that without an explicitly specified policy file used
    * in the startup of an application using this security manager, BOTH
    * the client, <b>and its loaded proxies</b> can do anything they want.
    */
   public void checkPermission(java.security.Permission perm) {}
}
