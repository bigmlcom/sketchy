/*
 * Copyright 2014 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package bigml.sketchy;

import com.google.common.hash.HashFunction;

public class SipUtil {

  public static long hash(HashFunction hasher, Object o) {
    Class cls = o.getClass();
    
    if (cls == Double.class) {
      return hasher.hashLong(Double.doubleToLongBits((Double) o)).asLong();
    }
    if (cls == Long.class) {
      return hasher.hashLong((Long) o).asLong();
    }
    if (cls == Integer.class || o == Short.class || o == Byte.class) {
      return hasher.hashLong(((Number) o).longValue()).asLong();
    }
    if (cls == String.class) {
      return hasher.hashUnencodedChars((CharSequence) o).asLong();
    }

    return 0;
  }
}
