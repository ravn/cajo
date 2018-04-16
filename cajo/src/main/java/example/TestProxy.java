package example;

public final class TestProxy extends gnu.cajo.utils.BaseProxy {
	// Don't initialize non-transient variables in declaration, save that for
	// the builder class, to prevent having to transmit extra code.
	static final int ARRIVED = 0, // string table indicies
			STARTED = ARRIVED + 1, CALL = STARTED + 1, ACK = CALL + 1, HELLO = ACK + 1, BACK = HELLO + 1,
			TITLE = BACK + 1, RESPONSE = TITLE + 1;
	example.gui.Display a, b, c, d, e; // user-interface components

	public TestProxy() {
		container = new Panel(); // this proxy has a GUI
		runnable = new MainThread() { // the proxy's thread run at the client
			public void run() { // auto-launched on arrival at the client's VM.
				try {
					container.setName(strings[TITLE]);
					System.out.println(strings[ARRIVED]);
					d.display(strings[ARRIVED] + '\n', true);
					System.out.println(strings[STARTED]);
					System.out.println(strings[CALL] + strings[HELLO]);
					d.display(strings[CALL] + "\n\t" + strings[HELLO] + '\n');
					System.out.println(strings[RESPONSE]);
					d.display(strings[RESPONSE] + "\n\t");
					String result = (String) item.invoke("callback", new Object[] { remoteThis, strings[HELLO] });
					System.out.println(result);
					d.display(result + '\n');
					d.repaint();
				} catch (Exception x) {
					x.printStackTrace(System.err);
				}
			}
		};
	}

	// All of the proxy's public methods are remotely callable. Below is the
	// interface created by this object:
	public String callback(String message) { // sole public interface method
		System.out.println(strings[BACK] + message);
		d.display(strings[BACK] + "\n\t" + message + '\n');
		d.repaint();
		return strings[ACK];
	}

	// All proxies should uniquely identify themselves, but it is not required.
	public String toString() {
		return "Test Proxy";
	}
}
