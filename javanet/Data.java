import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;

/*
 * Relational Datamodel with optional independent data serialization, and
 * deserialization only when necessary.
 *
 * Copyright (c) 2004 John Catherino
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, at version 2.1 of the license, or any
 * later version.  The license differs from the GNU General Public License
 * (GPL) to allow this library to be used in proprietary applications. The
 * standard GPL would forbid this. As the source declares the class in the
 * unnamed package, you may place it in any package you wish, with no
 * licensing affects on the other classes in the package.
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
 * This class supports the storage of data, into entries, into tables for fast
 * and efficient storage and retrieval. A data element can contain either
 * application specific data, or an entry to another data table, or to an
 * entire table itself. The data elements, entries, and tables will all be
 * serialized into separate files, which will then only be deserialized if they
 * are needed for a particular operation. This will greatly reduce both table
 * load time, and memory consumption. For convienience, all of these serialized
 * objects will be contained in a single zipped archive.<p>
 * If an application does not require all the features of a generic relational
 * database engine, this structure may suffice optimally. It is very small, and
 * extremely fast. The entity/relation schemata are implemented via the table
 * contents, and queries are implemented via Java method algorithms. Elements
 * cannot be set to null values, therefore referential integrity is assured in
 * the model.
 *
 * @version 1.0, 15-May-04 Initial release
 * @author John Catherino
 */
public abstract class Data implements Serializable {
   private static final void
      writeObject(Object object, String fileName) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(object);
      oos.flush();
      oos.close();
      byte bytes[] = baos.toByteArray();
      zos.putNextEntry(new java.util.jar.JarEntry(fileName));
      zos.write(bytes, 0, bytes.length);
      zos.flush();
      zos.closeEntry();
   }
   private static final Object readObject(String fileName) {
      try {
         ObjectInputStream ois = new ObjectInputStream(
            zipFile.getInputStream(zipFile.getEntry(fileName))
         );
         Object object = ois.readObject();
         ois.close();
         return object;
      } catch(Exception x) { x.printStackTrace(System.err); }
      return null;
   }
   private final void writeObject(ObjectOutputStream out) throws IOException {
      if (dirty) {
         dirty = false;
         writeObject(data, name);
      }
      out.defaultWriteObject();
   }
   private transient Object data;
   private final String name;
   /**
    * This is the target file for the database when it is serialized. It will
    * contain all data, elements, and referenced tables, saved into separate
    * directories.
    */
   protected static ZipFile zipFile;
   /**
    * This is main output stream used for all data during serialization. It
    * writes into the destination zip file. Later it is also the file name
    * from which data can be read as needed.
    */
   protected static ZipOutputStream zos;
   /**
    * This flag indicates that a data object has been modified since loading.
    * It is used to signal when the disc image of the data needs to be updated.
    * It will be set to true during construction automatically.
    */
   protected transient boolean dirty;
   /**
    * A constructor for use solely by application specific subclasses. It will
    * set the dirty flag automatically. Subclasses should only trivially extend
    * this class for type definition, adding any member data will greatly
    * increase the size of the resulting archive file.
    * @param data The information being encapsulated; either a table, an entry,
    * or an application specific data object.
    * @throws IllegalArgumentException If the data argument is null.
    */
   protected Data(Object data) {
      if (data == null) throw new IllegalArgumentException("Null Data");
      this.data = data;
      name = Integer.toString(hashCode(), Character.MAX_RADIX);
      dirty = true;
   }
   /**
    * This method assigns a value to the data element.
    * @param data The data reference to be contained by the element, it can
    * be application specific data, a table entry, or a table itself.
    * @throws IllegalArgumentException If the data reference to be assigned is
    * not to the same class, or a subclass of the current reference, or is null.
    */
   public void set(Object data) {
      if (data == null) throw new IllegalArgumentException("Null Data");
      if (!this.data.getClass().isAssignableFrom(data.getClass()))
         throw new IllegalArgumentException("Incompatible Data");
      this.data = data;
      dirty  = true;
   }
   /**
    * This method returns the internal data value. If the data is not currently
    * loaded into memory, it will be, to satisfy this operation.
    * @return The data reference contained by the element. It can be
    * application data, a table entry, or a table reference.
    */
   public Object get() {
      if (data == null) data = readObject(name);
      return data;
   }
   /**
    * This method overrides the default implementation to ensure that two
    * elements referencing an equivalent member data object, return true. If
    * the data is serialized to its own file and not currently loaded, it will
    * be, to satisfy this operation.
    * @param object The object or element to compare for equality.
    * @return True if the element's data are equivalent, false otherwise.
    */
   public boolean equals(Object object) {
      if (getClass().isAssignableFrom(object.getClass()))
         return get().equals(((Data)object).get());
      else return false;
   }
   /**
    * This method overrides the default implementation to provide the
    * description of the member data, not the element object itself. If the
    * data is not currently loaded into memory, it will be, to satisfy this
    * operation.
    * @return A description of the internal data, returned from its
    * toString() method invocation.
    */
   public String toString() { return get().toString(); }
   /**
    * This class contains an array of application specific data elements. These
    * elements may contain references either to application specific data,
    * other table entries, or to tables. Generally entries are specific to a
    * table, or class of tables. Subclasses should only trivially extend
    * this class for type definition, adding any member data will greatly
    * increase the size of the resulting archive file.
    */
   public static abstract class Entry extends Data {
      /**
       * A constructor for use solely by application specific subclasses.
       * @param data The array of data objects representing the table
       * entry.
       * @throws IllegalArgumentException If the elements argument is null, or
       * any of the argument's elements are null.
       */
      protected Entry(Data data[]) {
         super(data);
         for (int i = 0; i < data.length; i++)
            if (data[i] == null)
               throw new IllegalArgumentException("Null Data");
      }
      /**
       * This method assigns a value to an element in the entry. If the entry
       * object is saved, and has not been loaded into memory, it will be to
       * satisfy this operation.
       * @param index The location in the element array to overwrite.
       * @param data The data element reference to be contained in the entry's
       * array.
       * @throws IllegalArgumentException If the element to be assigned is not
       * if the same class, or a subclass, of the current element, or is null.
       */
      public void set(int index, Data data) {
         ((Data[])get())[index].set(data);
         dirty = true;
      }
      /**
       * This method returns the specified data element from the entry. If the
       * element has not yet been loaded into memory, it will be, to satisfy
       * this operation.
       * @param index The location in the element array to return.
       * @return The element reference contained by specified index.
       */
      public Data get(int index) { return ((Data[])get())[index]; }
      /**
       * This method overrides the default implementation to provide the
       * description of the internal data elements, not the entry object
       * itself.
       * @return A comma separated description of the internal data elements,
       * as returned by their toString() method invocations, contained within
       * parentheses.
       */
      public String toString() {
         StringBuffer sb = new StringBuffer("(" + get(0));
         for (int i = 1; i < ((Data[])get()).length; i++) {
            sb.append(", ");
            sb.append(get(i));
         }
         sb.append(")");
         return sb.toString();
      }
      /**
       * This method overrides the default implementation to ensure that two
       * entries holding equivalent data elements, return true. It is used by
       * the Table class in its add method, to prevent the addition of
       * duplicate entries. It will iterate through all of its elements
       * comparing each for equality with the provided entry.
       * @param object The entry to compare for equality.
       * @return True if the entry's elements are equivalent, false otherwise.
       */
      public boolean equals(Object object) {
         if (getClass().isAssignableFrom(object.getClass())) {
            Data these[] = (Data[])get();
            Data those[] = (Data[])((Entry)object).get();
            for (int i = 0; i < these.length; i++)
               if (!these[i].equals(those[i])) return false;
            return true;
         } else return false;
      }
   }
   /**
    * This class supports the storage of data objects as a list of entries in
    * a table. The entity/relation schema is implemented via the entry
    * and table contents.  Queries are application specific, and implemented
    * via methods. Subclasses should only trivially extend this class for type
    * definition, adding any member data will greatly increase the size of the
    * resulting archive file.
    *
    * @version 1.0, 15-May-04 Initial release
    * @author John Catherino
    */
   public static abstract class Table extends Data {
      /**
       * This generic object is used to identify, describe, and version the
       * database. It is intended to provide insight about the version and
       * schema represented in the table object tree. It describes the entire
       * data file, therefore there is only one. Its format and meaning are
       * entirely application specific.
       */
      public static Object versionInfo;
      /**
       * A constructor for use solely by application specific subclasses. It
       * is essentially an unordered list of entry objects.
       */
      protected Table() { super(new LinkedList()); }
      /**
       * This method adds a entry to the table. If the table entries are
       * not currently loaded into memory, they will be, to satisfy this
       * operation.
       * @param entry The entry object to be added to the table.
       * @return The entry provided as the argument.
       * @throws IllegalArgumentException If the entry to be added is not the
       * same class, or a subclass, of the first entry in the table if present,
       * or if there already is an instance of the element in the table, or if
       * the argument is null.
       */
      public Entry add(Entry entry) {
         if (entry == null)throw new IllegalArgumentException("Null Entry");
         LinkedList entries = (LinkedList)get();
         if (entries.size() != 0) {
            if (!entries.getFirst().getClass().isAssignableFrom(
               entry.getClass()))
                  throw new IllegalArgumentException("Incompatible Entry");
            if (entries.contains(entry))
               throw new IllegalArgumentException("Duplicate Entry");
         }
         entries.add(entry);
         dirty = true;
         return entry;
      }
      /**
       * This method removes the corresponding entry from the table. If the
       * entries are not currently loaded, they will be, to satisfy this
       * operation. This method will not damage referential integrity, as the
       * element is not destroyed, merely removed from reference in the table.
       * If no other data objects reference it, it will be garbage collected.
       * If the table entries have not yet been loaded into memory, they will
       * be, to satisfy this operation.
       * @param entry The entry to be removed from the table.
       * @return True on successful deletion, false if the table does not
       * contain the element.
       */
      public boolean remove(Entry entry) {
         if (entry == null)throw new IllegalArgumentException("Null Entry");
         if (((LinkedList)get()).remove(entry) == true) {
            dirty = true;
            return true;
         } else return false;
      }
      /**
       * This method returns an iterator of all the entry objects contained in
       * the table. If the entries are not currently loaded into memory, they
       * will be, to satisfy this operation.
       * @return An iterator which can be used to cycle through all the
       * table entries.
       */
      public Iterator entries() { return ((LinkedList)get()).iterator(); }
      /**
       * This method loads a table previously saved to disc. Initially only
       * the empty table reference and the version info will be loaded. Then
       * entries and data will be loaded, but only as needed to satisfy the
       * requests provided of the database. It assumes all the needed data is
       * contained in a single zipped archive file.
       * @param zipFileName The path/file name in which to load the serialized
       * image of the table and all its entries. The extension .zip will be
       * appended automatically, as its format is expected to be a zipped
       * serialized object archive.
       * @return A reference to the table, typically cast by the caller,
       * to the appropriate subclass.
       */
      public static Table load(String zipFileName)
         throws ClassNotFoundException, IOException {
         zipFile = new ZipFile(zipFileName + ".zip");
         ObjectInputStream ois = new ObjectInputStream(
            zipFile.getInputStream(zipFile.getEntry("main"))
         );
         versionInfo = ois.readObject();
         Object object = ois.readObject();
         ois.close();
         return (Table)object;
      }
      /**
       * This method saves the entire table to disc. It is generally used only
       * by the highest level table of the database. It will save the main
       * table reference, and its version info object, then cause each entry to
       * serialize to its own files. This will in turn cause all the data
       * objects to serialize to their own files. For efficiency, all the files
       * will be contained in a single zip archive.
       * @param zipFileName The path/file name in which to save the serialized
       * image of the table, all its entries, and all of its data. The
       * extension .zip will be appended automatically, to indicate that the
       * file is a zipped serialized object archive.
       * @throws IOException If the write operation failed, or was not
       * permitted, or the filename is inavalid.
       */
      public void save(String zipFileName) throws IOException {
         zos = new ZipOutputStream(
            new FileOutputStream(zipFileName + ".zip")
         );
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(versionInfo);
         oos.writeObject(this);
         oos.flush();
         oos.close();
         byte bytes[] = baos.toByteArray();
         zos.putNextEntry(new java.util.jar.JarEntry("main"));
         zos.write(bytes, 0, bytes.length);
         zos.flush();
         zos.closeEntry();
         zos.close();
      }
      /**
       * This method overrides the default implementation to provide the
       * the description of the internal entries and data, instead of the table
       * object itself. If <u>all</u> of the entries and all of the data are
       * not currently loaded, they will be, to satisfy this operation.
       * @return A newline delimited description of the internal entries, as
       * returned by their toString() method invocations, contained within
       * {} braces.
       */
      public String toString() {
         StringBuffer sb = new StringBuffer("{\n");
         for (Iterator i = entries(); i.hasNext(); sb.append('\n'))
            sb.append(i.next());
         sb.append('}');
         return sb.toString();
      }
      /**
       * This method overrides the default implementation to ensure that two
       * tables holding equivalent entries and data, will return true.
       * It will iterate through all of its entries comparing each for equality
       * with the provided table. <i>Note:</i> this operation must load
       * <u>every</u> element in the both tables into memory.
       * @param object The table to compare for equality.
       * @return True if the table's content are equivalent, false otherwise.
       */
      public boolean equals(Object object) {
         if (getClass().isAssignableFrom(object.getClass())) {
            Iterator entries = entries();
            Iterator objects = ((Table)object).entries();
            while(entries.hasNext())
               if (!entries.next().equals(objects.next())) return false;
            return true;
         } else return false;
      }
   }
}
