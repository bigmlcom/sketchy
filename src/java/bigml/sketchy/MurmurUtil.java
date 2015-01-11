/*
 * Copyright 2014 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package bigml.sketchy;

import byte_transforms.CassandraMurmurHash;
import java.nio.ByteBuffer;

public class MurmurUtil {

  public static long hash(Object o, long seed) {
    Class cls = o.getClass();
    
    ByteBuffer bytes;
    
    if (cls == Double.class) {
      bytes = ByteBuffer.allocate(8);
      bytes.putDouble((Double) o);
    } else if (cls == Long.class) {
      bytes = ByteBuffer.allocate(8);
      bytes.putLong((Long) o);
    } else if (cls == String.class) {
      byte[] rawBytes = ((String) o).getBytes();
      bytes = ByteBuffer.allocate(rawBytes.length);
      bytes.put(rawBytes);
    } else if (cls == Integer.class) {
      bytes = ByteBuffer.allocate(4);
      bytes.putInt((Integer) o);
    } else if (cls == Short.class) {
      bytes = ByteBuffer.allocate(2);
      bytes.putShort((Short) o);
    } else if (cls == Byte.class) {
      bytes = ByteBuffer.allocate(1);
      bytes.put((Byte) o);
    } else {
      return 0;
    }
    bytes.rewind();
    return CassandraMurmurHash.hash2_64(bytes, 0, bytes.remaining(), seed);
  }
}
