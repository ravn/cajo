package gnu.cajo.utils.extra;

import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/*
 * Serialized Object Encrypter
 * Copyright (c) 2004 John Catherino
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
 * To receive a copy of the GNU Lesser General Public License visit their
 * website at http://www.fsf.org/licenses/lgpl.html or via snail mail at Free
 * Software Foundation Inc., 59 Temple Place Suite 330, Boston MA 02111-1307
 * USA
 */

/**
 * This class is used to hash a wrapped object.  Primarily it is intended to
 * simply and automatically convert a serialized object representation in to,
 * and out of plaintext, for transmission over the network.<p>
 *
 * The underlying paradigm is that rather than incurring the extensive
 * computational overhead of running encrypted streams between clients and
 * servers, it much more efficient to encrypt only those specific objects
 * containing sensitive information. In fact, this approach is even stronger
 * than conventional encrypted streams, because each object could employ a
 * different key, and <b>even</b> different cryptographical algorithms!<p>
 *
 * Ideally, the client and the server would both have the derived class in
 * their local codebases, as it will otherwise need to be sent over the
 * wire from a codebase server, severely compromising security.<p>
 *
 * Special thanks to Jeff Genender at
 * http://www.savoirtech.com/roller/page/jgenender/20040806#cipher_text_for_storage
 * for the idea, and the admonition, to improve HashProxy security.<p>
 *
 * <i>Note:</i> since this class uses the javax.crypto packages, it may not
 * function on some J2ME devices.
 *
 * @version 1.1, 07-Aug-04 Fixed security hole with the static key/cipher.
 * @author John Catherino
 */
public class CryptObject extends gnu.cajo.utils.ZippedProxy {
   private final void writeObject(ObjectOutputStream out) throws IOException {
      try{
         Cipher cipher = Cipher.getInstance(CIPHER);
         cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, CIPHER));
         payload = cipher.doFinal(payload);
      } catch (Exception e){ throw new IOException(e.toString()); }
      out.defaultWriteObject();
   }
   private final void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      try{
         Cipher cipher = Cipher.getInstance(CIPHER);
         cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, CIPHER));
         payload = cipher.doFinal(payload);
      } catch (Exception e){ throw new IOException(e.toString()); }
   }
   /**
    * The cryptographic algorithm used to secure the object. In this case
    * triple DES. To use a different algorithm, subclass and assign it a
    * new identifier, but <b>only</b> in a static initializer block!
    */
   protected static String CIPHER;
   /**
    * A 24 byte array of data to create the secret key used to encrypt and
    * decrypt the object. It should be completely randomly chosen values.
    * The key is declared static, to prevent it from being sent along with
    * the encrypted object, over the network. Don't overwrite the key here,
    * subclass and assign it a unique value, but <b>only</b> in a static
    * initializer block!
    */
   protected static byte[] KEY;
   static { // base class static initializer block:
      CIPHER = "DESede"; // not really necessary to override
      KEY = new byte[] { // definitely necessary to override
         (byte)0x76, (byte)0x6F, (byte)0xBA, (byte)0x39, (byte)0x31, (byte)0x2F,
         (byte)0x0D, (byte)0x4A, (byte)0xA3, (byte)0x90, (byte)0x55, (byte)0xFE,
         (byte)0x55, (byte)0x65, (byte)0x61, (byte)0x13, (byte)0x34, (byte)0x82,
         (byte)0x12, (byte)0x17, (byte)0xAC, (byte)0x77, (byte)0x39, (byte)0x19
      };
   }
   /**
    * The constructor simply invokes the superclass ZippedProxy constructor.
    * @param proxy The object, to be hashed at the server, and unhashed at
    * the client. It is for any object needing secure transmission between
    * remote Virtual Machines.
    */
   public CryptObject(Object object) { super(object); }
}
