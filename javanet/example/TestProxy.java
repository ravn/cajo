package example;

public final class TestProxy extends gnu.cajo.utils.PanelProxy {
   // don't initialize non-transient variables in declaration, save that for
   // the builder class, to prevent having to transmit extra code.
   static final int ARRIVED = 0,
      STARTED  = ARRIVED + 1, CALL = STARTED + 1, ACK   = CALL + 1,
      HELLO    = ACK     + 1, BACK = HELLO   + 1, TITLE = BACK + 1,
      RESPONSE = TITLE   + 1;
   example.gui.Display a, b, c, d, e;
   // public TestProxy() { constructor code usually goes in a builder class }
   public void run() {
      try {
         setName(strings[TITLE]);
         System.out.println(strings[ARRIVED]);
         d.display(strings[ARRIVED] + '\n', true);
         System.out.println(strings[STARTED]);
         System.out.println(strings[CALL] + strings[HELLO]);
         d.display(strings[CALL] + "\n\t" + strings[HELLO] + '\n');
         System.out.println(strings[RESPONSE]);
         d.display(strings[RESPONSE] + "\n\t");
         String result = (String)server.
            invoke("callback", new Object[] { remoteThis, strings[HELLO] });
         System.out.println(result);
         d.display(result + '\n');
         d.repaint();
      } catch(Exception x) { x.printStackTrace(System.err); }
   }
   public String callback(String message) {
      System.out.println(strings[BACK] + message);
      d.display(strings[BACK] + "\n\t" + message + '\n');
      d.repaint();
      return strings[ACK];
   }
   public String toString() { return "Test Proxy"; }
}
