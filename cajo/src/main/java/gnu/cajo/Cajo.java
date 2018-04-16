package gnu.cajo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import gnu.cajo.invoke.Invoke;
import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;
import gnu.cajo.utils.Multicast;
import gnu.cajo.utils.extra.TransparentItemProxy;

/*
 * A Generic Standard Interface to the cajo distributed computing library.
 * Copyright (C) 2007 John Catherino
 * The cajo project: https://cajo.dev.java.net
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This file Cajo.java is part of the cajo library.
 *
 * The cajo library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation, at version 3 of the licence, or (at your
 * option) any later version.
 *
 * The cajo library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public Licence for more details.
 *
 * You should have received a copy of the GNU Lesser General Public Licence
 * along with this library. If not, see http://www.gnu.org/licenses/lgpl.html
 */

/**
 * This class provides a reference implementation of the Grail Standard
 * Interface using the cajo library.
 *
 * @author John Catherino
 * @version 1.0, 21-Aug-07
 */
public final class Cajo implements Grail {
	private final int ttl;
	private final Multicast multicast;
	private final Remote regref;
	private final Registrar registrar = new Registrar();
	private final HashSet items = new HashSet(), duds = new HashSet();
	private boolean running;

	/**
	 * This internal use only helper class maintains a registry of exported objects.
	 * It uses UDP multicasts to find other instances of registries, and shares
	 * references between them.
	 */
	private final class Registrar {
		/**
		 * This method is called either when a Cajo instance starts up, or exports an
		 * object reference. All operating servers will request the collection of
		 * references owned by the remote JVM, and correspondingly send a copy of their
		 * registries.
		 *
		 * @param multicast
		 *            A reference to the announcing JVM
		 * @return null To keep the multicast object listening
		 * @throws Exception
		 *             if the request for remote references failed, or the sending its
		 *             reference collection failed, for either network, or application
		 *             specific reasons.
		 */
		public Object multicast(Multicast multicast) {
			try {
				java.rmi.server.RemoteServer.getClientHost();
				throw new RuntimeException("multicast cannot be called remotely");
			} catch (java.rmi.server.ServerNotActiveException x) {
			}
			if (!multicast.item.equals(regref))
				try { // ignore self-calls
					Object refs[] = items.size() > 0 ? items.toArray() : null;
					register((Object[]) multicast.item.invoke("request", null));
					if (refs != null)
						multicast.item.invoke("register", refs);
				} catch (Exception x) {
				} // registration attempt failed
			return null; // keep listening
		}

		/**
		 * This method is called by remote JVMs to provide a copy of their registry, in
		 * response to a startup or export announcement. The registries will be merged,
		 * and duplicate references discarded.
		 *
		 * @param elements
		 *            A collection of searchable remote object references
		 */
		public void register(Object elements[]) { // include all responders
			if (elements != null && elements.length > 0)
				synchronized (items) {
					for (int i = 0; i < elements.length; i++)
						if (!(duds.contains(elements[i])))
							items.add(elements[i]);
				}
		}

		/**
		 * This method allows remote Cajo objects to request the contents of this
		 * registry.
		 *
		 * @return The collection of searchable remote object references currently
		 *         registered
		 */
		public Object[] request() {
			return items.toArray();
		}
	}

	/**
	 * This internal use only helper class automatically removes unresponsive remote
	 * references from the registry.
	 */
	private final class Purger implements Invoke {
		private static final long serialVersionUID = 1L;
		private final Object object;

		private Purger(Object object) {
			this.object = object;
		}

		/**
		 * This method, invoked transparently when any remote object method is called,
		 * monitors the progress of the invocation. If the call results in a
		 * java.rmi.RemoteException, the server object reference will be deleted from
		 * the internal registry, and its reference blocked. All exceptions will be
		 * propagated out to the client.
		 *
		 * @param method
		 *            The name of the method to be invoked
		 * @param args
		 *            The arguments to be provided to the method
		 * @return The result, if any, returned by the remote procedure call
		 * @throws Exception
		 *             For either network, or remote object logic related reasons
		 */
		public Object invoke(String method, Object args) throws Exception {
			try {
				return Remote.invoke(object, method, args);
			} catch (RemoteException x) { // on network invocation failure
				synchronized (items) {
					items.remove(object); // discard non-working reference
					duds.add(object); // blacklist, to prevent re-registration
				}
				throw x;
			}
		}
	}

	/**
	 * This internal use only helper class scans local and remote exported objects
	 * to see if any have methods matching the requested method and field set.
	 */
	private static final class Searchable implements Invoke {
		private static final long serialVersionUID = 1L;
		private final Object target, fvalues[];
		private final String[] mnames, fnames;
		private final Class[] mreturns, margs[], ftypes;

		private Searchable(Object object, Object target) {
			this.target = target;
			Field allFields[] = object.getClass().getFields();
			ArrayList fieldList = new ArrayList();
			for (int i = 0; i < allFields.length; i++) {
				int modifiers = allFields[i].getModifiers();
				if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & Modifier.FINAL) != 0) // static or instance
					fieldList.add(allFields[i]);
			}
			fnames = new String[fieldList.size()];
			ftypes = new Class[fnames.length];
			fvalues = new Object[fnames.length];
			for (int i = 0; i < fnames.length; i++) {
				Field field = (Field) fieldList.get(i);
				try {
					field.setAccessible(true);
				} catch (SecurityException x) {
				}
				fnames[i] = field.getName();
				ftypes[i] = field.getType();
				try {
					fvalues[i] = field.get(null);
				} catch (Exception x) {
				}
			}
			Method methods[] = object.getClass().getMethods();
			mnames = new String[methods.length];
			mreturns = new Class[methods.length];
			margs = new Class[methods.length][];
			for (int i = 0; i < methods.length; i++) {
				mnames[i] = methods[i].getName();
				mreturns[i] = methods[i].getReturnType();
				margs[i] = methods[i].getParameterTypes();
			}
		}

		/**
		 * This method, invoked transparently when any registered object method is
		 * called, checks the exported object's method signatures and public static
		 * <i>(or instance)</i> fields for a match with the set provided by the client.
		 */
		public Object invoke(String method, Object args) throws Exception {
			if (method == null) { // special case lookup signal
				Class ireturns[] = (Class[]) ((Object[]) args)[0];
				String inames[] = (String[]) ((Object[]) args)[1];
				Class iargs[][] = (Class[][]) ((Object[]) args)[2];
				String ifields[] = (String[]) ((Object[]) args)[3];
				Class itypes[] = (Class[]) ((Object[]) args)[4];
				Object ivalues[] = (Object[]) ((Object[]) args)[5];
				if (ifields.length > fnames.length || inames.length > mnames.length)
					return Boolean.FALSE;
				matching: for (int i = 0; i < ifields.length; i++) {
					for (int j = 0; j < fnames.length; j++)
						if (ifields[i].equals(fnames[j]))
							if (Remote.autobox(itypes[i]).isAssignableFrom(Remote.autobox(ftypes[j]))
									&& (ivalues[i] == null && fvalues[j] == null
											|| (ivalues != null && ivalues[i].equals(fvalues[j]))))
								continue matching;
							else
								return Boolean.FALSE;
					return Boolean.FALSE;
				}
				scanning: for (int i = 0; i < inames.length; i++) {
					matching: for (int j = 0; j < mnames.length; j++)
						if (mnames[j].equals(inames[i]) && (ireturns[i] == void.class
								|| Remote.autobox(ireturns[i]).isAssignableFrom(Remote.autobox(mreturns[j])))) {
							if (margs[j].length == iargs[i].length) {
								for (int k = 0; k < margs[j].length; k++)
									if (!Remote.autobox(margs[j][k]).isAssignableFrom(Remote.autobox(iargs[i][k])))
										continue matching;
								continue scanning;
							}
						}
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			} else
				return Remote.invoke(target, method, args);
		}
	}

	/**
	 * The default constructor announces the cajo object on the cajo IANA standard
	 * address and port, with a default ttl value of 16. <br>
	 * <i><u>Note</u>:</i> invoke gnu.cajo.invoke.Remote.config, and construct a
	 * gnu.cajo.utils.CodebaseServer if needed, to configure the JVM <i>before</i>
	 * invoking this constructor.
	 *
	 * @throws IOException
	 *             If the startup announcement datagram packet could not be sent
	 */
	public Cajo() throws IOException {
		this(16, "224.0.23.162", 1198);
	}

	/**
	 * This constructor creates and announces the cajo registry. <br>
	 * <i><u>Note</u>:</i> invoke gnu.cajo.invoke.Remote.config, and construct a
	 * gnu.cajo.utils.CodebaseServer if needed, to configure the JVM <i>before</i>
	 * invoking this constructor.
	 *
	 * @param ttl
	 *            The time for interface export announcements to live, it is
	 *            decremented each time it is passed to a new router, a value of 0
	 *            confines the announcement to the local subnet, the max value of
	 *            255 could theoretically traverse the entire internet (assuming no
	 *            routers blocked datagram packets)
	 * @param The
	 *            UDP multicast address on which to make registry announcements,
	 *            normally it is the address assigned to the cajo project by the
	 *            IANA: 224.0.23.162
	 * @param the
	 *            UDP port number on which to make registry announcements, normally
	 *            it is typically the cajo project IANA assigned TCP port number:
	 *            1198
	 * @throws IOException
	 *             If the multicast UDP socket could not be created
	 */
	public Cajo(int ttl, String address, int port) throws IOException {
		this.ttl = ttl;
		regref = new Remote(registrar);
		multicast = new Multicast(address, port);
		multicast.listen(registrar);
		multicast.announce(regref, ttl);
	}

	/**
	 * This method makes any local object's public methods, whether instance or
	 * static, remotely invocable. As the object being remoted is already
	 * instantiated, there is no <i>artificial</i> requirement for it to implement a
	 * no-arg constructor. If not all methods are safe to be made remotely
	 * invocable, then wrap the object with a special-case <a
	 * href=http://en.wikipedia.org/wiki/Decorator_pattern> decorator</a>.
	 * <p>
	 * <i><u>Note</u>:</i> this method is not idempotent; if an object is exported
	 * more than once, it will be registered each time, you generally do not want to
	 * do this.<br>
	 * Also, this method will remote the registrar, and announce its startup on the
	 * export of the first service object.
	 *
	 * @param object
	 *            The local <a
	 *            href=http://en.wikipedia.org/wiki/Plain_Old_Java_Object> POJO</a>
	 *            to be made remotely invocable, i.e. there is no requirement for it
	 *            to implement any special interfaces, nor to be derived from any
	 *            particular class
	 * @throws IOException
	 *             If the announcement datagram packet could not be sent
	 */
	public void export(Object object) throws IOException {
		export(object, object);
	}

	/**
	 * This method makes any local object's public methods, whether instance or
	 * static, remotely invocable. As the object being remoted is already
	 * instantiated, there is no <i>artificial</i> requirement for it to implement a
	 * no-arg constructor. If not all methods are safe to be made remotely
	 * invocable, then wrap the object with a special-case <a
	 * href=http://en.wikipedia.org/wiki/Decorator_pattern> decorator</a>.
	 * <p>
	 * <i><u>Note</u>:</i> this method is not idempotent; if an object is exported
	 * more than once, it will be registered each time, you generally do not want to
	 * do this.
	 *
	 * @param object
	 *            The local <a
	 *            href=http://en.wikipedia.org/wiki/Plain_Old_Java_Object> POJO</a>
	 *            to be made remotely invocable, i.e. there is no requirement for it
	 *            to implement any special interfaces, nor to be derived from any
	 *            particular class
	 * @param target
	 *            The local object on which to invoke methods; this is used when the
	 *            object parameter is wrapped e.g. in a MonitorItem or AuditorItem
	 * @return A remote reference to the exported object, to manually share with
	 *         other JVMs as an argument or return, and to use when unexporting a
	 *         locally exported object.
	 * @throws IOException
	 *             If the announcement datagram packet could not be sent
	 */
	public Remote export(Object object, Object target) throws IOException {
		if (!running) {
			running = true;
			ItemServer.bind(regref, "registrar");
		}
		Remote retval = new Remote(new Searchable(object, target));
		items.add(retval);
		multicast.announce(regref, ttl);
		return retval;
	}

	/**
	 * This method removes a previously exported local object, from the local
	 * registry. It does <i>not</i> remove the reference from remote registries.
	 * <br>
	 * <i><u>Note</u>:</i> the remote reference will still be usable by other remote
	 * JVMs. To deactivate the reference, call its inherited unexport method.
	 *
	 * @param object
	 *            The exported object reference
	 * @return If the reference was successfully unexported, true, false if it has
	 *         either already been, or never has been, exported
	 */
	public boolean unexport(Remote object) {
		return items.remove(object);
	}

	/**
	 * This method finds all remotely invocable objects, supporting the specified
	 * method and field set. The method and field set is a <i>client</i> defined
	 * interface. It specifies the method signatures and final fields required. By
	 * convention, client based load balancing can be implemented simply selecting a
	 * reference from the returned array <i>at random.</i> <br>
	 * <i><u>Note</u>:</i> if the interface declares any static final fields, these
	 * will also be matched for type, and equality against final static <i>(or
	 * instance)</i> fields on the remote object.
	 *
	 * @param methodSetInterface
	 *            The interface of methods and fields that remote objects are
	 *            required to support, the arguments and returns do not need to
	 *            match exacly, they can be subclasses, or coercable primitive types
	 * @return An array of remote object references, implementing the specified
	 *         method and field collection
	 * @throws Exception
	 *             For reflection access, if prohibited by security policy
	 */
	public Object[] lookup(Class methodSetInterface) throws Exception {
		Field fields[] = methodSetInterface.getFields();
		String fnames[] = new String[fields.length];
		Class ftypes[] = new Class[fields.length];
		Object values[] = new Object[fields.length];
		for (int i = 0; i < fields.length; i++) {
			try {
				fields[i].setAccessible(true);
			} catch (SecurityException x) {
			}
			fnames[i] = fields[i].getName();
			ftypes[i] = fields[i].getType();
			values[i] = fields[i].get(null);
		}
		Method methods[] = methodSetInterface.getMethods();
		Class returns[] = new Class[methods.length];
		String names[] = new String[methods.length];
		Class args[][] = new Class[methods.length][];
		for (int i = 0; i < methods.length; i++) {
			returns[i] = methods[i].getReturnType();
			names[i] = methods[i].getName();
			args[i] = methods[i].getParameterTypes();
		}
		Object params = new Object[] { returns, names, args, fnames, ftypes, values };
		ArrayList list = new ArrayList();
		Object elements[] = items.toArray();
		for (int i = 0; i < elements.length; i++)
			try {
				if (Boolean.TRUE.equals(Remote.invoke(elements[i], null, params)))
					list.add(new Purger(elements[i]));
			} catch (RemoteException x) { // purge dud references
				synchronized (items) {
					items.remove(elements[i]);
					duds.add(elements[i]);
				}
			} catch (Exception x) {
			} // method or field types unknown
		return list.toArray();
	}

	/**
	 * This method instantiates a <a
	 * href=http://java.sun.com/j2se/1.3/docs/guide/reflection/proxy.html> Dynamic
	 * Proxy</a> at the client, which implements the method set specified. This
	 * allows a remote object reference to be used in a semantically identical
	 * fashion as if it were local. The proxies can be freely passed between JVMs,
	 * or persisted to storage for later use.
	 *
	 * @param reference
	 *            A reference to a remote object returned by the lookup method of
	 *            this interface, though actually, any object reference implementing
	 *            the client method set would work
	 * @param methodSetInterface
	 *            The set <i>(or subset)</i> of public methods, static or instance,
	 *            that the object reference implements, the objects need not match
	 *            exactly, they can be subclasses, or coercable primitive types
	 * @return An object implementing the method set interface provided.
	 */
	public Object proxy(Object reference, Class methodSetInterface) {
		return TransparentItemProxy.getItem(reference, new Class[] { methodSetInterface });
	}

	/**
	 * This method is used to allow clients to pass references to its own local
	 * objects, to other JVMs. Normally all arguments are passed by value, meaning
	 * copies are sent to the remote JVM. Sometimes however, what is needed is for
	 * all users to have a reference to the same object instance, on which to
	 * perform operations.
	 *
	 * @param object
	 *            The local client object for which remote pass-by-reference is
	 *            sought
	 * @return A dynamic proxy object, implementing all of the interfaces of the
	 *         wrapped object argument, it will even work in the local context
	 * @throws RemoteException
	 *             If a remote reference could not be created for the object
	 *             argument, typically for network configuration related issues
	 */
	public static Object proxy(Object object) throws RemoteException {
		HashSet interfaces = new HashSet();
		for (Class c = object.getClass(); c != null; c = c.getSuperclass())
			interfaces.addAll(Arrays.asList(c.getInterfaces()));
		return TransparentItemProxy.getItem(new Remote(object).clientScope(),
				(Class[]) interfaces.toArray(new Class[0]));
	}

	/**
	 * This method is used to manually collect remote registry entries. The specific
	 * addresses or host names of the remote JVMs must be known. It is used to reach
	 * JVMs that for some reason are not accessible by UDP. The method will also
	 * share all of the local registry references.<br>
	 * <i><u>Note</u>:</i> you will generally want to export all of your service
	 * objects first, before making calls to register.
	 *
	 * @param hostname
	 *            The address or domain name of a remote grail JVM
	 * @param port
	 *            The TCP port on which the object is being shared, typically it
	 *            1198
	 * @throws Exception
	 *             Various types, related to network related errors: invalid host
	 *             name, host unavailable, host unreachable, etc...
	 */
	public void register(String hostname, int port) throws Exception {
		Object reg = Remote.getItem("//" + hostname + ':' + port + "/registrar");
		Object refs[] = items.size() > 0 ? items.toArray() : null;
		registrar.register((Object[]) Remote.invoke(reg, "request", null));
		if (refs != null)
			Remote.invoke(reg, "register", refs);
	}

	/**
	 * Technically this method is unrelated to the class, it is used to furnish
	 * library version information. It provides an execution point called when the
	 * library jar is executed. It simply copies the contents of the internal
	 * readme.txt file to the console.
	 *
	 * @throws IOException
	 *             If the readme.txt file cannot be found, unlikely
	 */
	public static void main(String args[]) throws IOException {
		java.io.InputStream is = Cajo.class.getResourceAsStream("/readme.txt");
		byte text[] = new byte[is.available()];
		is.read(text);
		is.close();
		System.out.println(new String(text));
	}
}
