/*
 * Copyright 2014 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package bigml.sketchy;

public class MurmurUtil {

  public static long hash(Object o, long seed) {
    Class cls = o.getClass();
    
    byte[] bytes;
    if (cls == Double.class) {
      bytes = toBytes(Double.doubleToLongBits((Double) o));
    } else if (cls == Long.class) {
      bytes = toBytes((Long) o);
    } else if (cls == Integer.class || o == Short.class || o == Byte.class) {
      bytes = toBytes(((Number) o).longValue());
    } else if (cls == String.class) {
      bytes = ((String) o).getBytes();
    } else {
      return 0;
    }
    
    return MurmurHash.hash64(bytes, bytes.length, (int) seed);
  }
  
  private static byte[] toBytes(long val) {
     byte [] b = new byte[8];
     for (int i = 7; i > 0; i--) {
       b[i] = (byte) val;
       val >>>= 8;
     }
     b[0] = (byte) val;
     return b;
   }
}
