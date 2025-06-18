package org.daiitech.naftah.builtin.utils.op;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.booleanToInt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.intToBoolean;
import static org.daiitech.naftah.builtin.utils.StringUtils.charWiseModulo;
import static org.daiitech.naftah.builtin.utils.StringUtils.stringToInt;

import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.StringUtils;

/**
 * @author Chakib Daii
 */
public enum BinaryOperation implements Operation {
  // Arithmetic
  ADD {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.add(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.add(left, right);
    }
  },
  SUBTRACT {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.subtract(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.subtract(left, right);
    }
  },
  MULTIPLY {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.multiply(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return new String(new char[right.intValue()]).replace('\0', character);
      } else if (left instanceof String string) {
        return StringUtils.multiply(string, right.intValue());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.charWiseMultiply(left, right);
    }
  },
  DIVIDE {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.divide(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return StringUtils.divide(string, right.intValue());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String[] apply(String left, String right) {
      return StringUtils.divide(left, right);
    }
  },
  MODULO {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.modulo(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(String left, String right) {
      return charWiseModulo(left, right);
    }
  },

  // Comparison
  GREATER_THAN {
    @Override
    public Boolean apply(Number left, Number right) {
      return NumberUtils.compare(left, right) > 0;
    }

    @Override
    public Boolean apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return (Boolean) apply(left, (boolean) aBoolean);
      } else if (right instanceof Character character) {
        return (Boolean) apply(left, (char) character);
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return (Boolean) apply((boolean) aBoolean, right);
      } else if (left instanceof Character character) {
        return (Boolean) apply((char) character, right);
      } else if (left instanceof String string) {
        return apply(stringToInt(string), right);
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(String left, String right) {
      return StringUtils.compare(left, right) > 0;
    }
  },
  GREATER_THAN_EQUALS {
    @Override
    public Boolean apply(Number left, Number right) {
      return NumberUtils.compare(left, right) >= 0;
    }

    @Override
    public Boolean apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return (Boolean) apply(left, (boolean) aBoolean);
      } else if (right instanceof Character character) {
        return (Boolean) apply(left, (char) character);
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return (Boolean) apply((boolean) aBoolean, right);
      } else if (left instanceof Character character) {
        return (Boolean) apply((char) character, right);
      } else if (left instanceof String string) {
        return apply(stringToInt(string), right);
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(String left, String right) {
      return StringUtils.compare(left, right) >= 0;
    }
  },
  LESS_THAN {
    @Override
    public Boolean apply(Number left, Number right) {
      return NumberUtils.compare(left, right) < 0;
    }

    @Override
    public Boolean apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return (Boolean) apply(left, (boolean) aBoolean);
      } else if (right instanceof Character character) {
        return (Boolean) apply(left, (char) character);
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return (Boolean) apply((boolean) aBoolean, right);
      } else if (left instanceof Character character) {
        return (Boolean) apply((char) character, right);
      } else if (left instanceof String string) {
        return apply(stringToInt(string), right);
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(String left, String right) {
      return StringUtils.compare(left, right) < 0;
    }
  },
  LESS_THAN_EQUALS {
    @Override
    public Boolean apply(Number left, Number right) {
      return NumberUtils.compare(left, right) <= 0;
    }

    @Override
    public Boolean apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return (Boolean) apply(left, (boolean) aBoolean);
      } else if (right instanceof Character character) {
        return (Boolean) apply(left, (char) character);
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return (Boolean) apply((boolean) aBoolean, right);
      } else if (left instanceof Character character) {
        return (Boolean) apply((char) character, right);
      } else if (left instanceof String string) {
        return apply(stringToInt(string), right);
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(String left, String right) {
      return StringUtils.compare(left, right) <= 0;
    }
  },
  EQUALS {
    @Override
    public Boolean apply(Number left, Number right) {
      return NumberUtils.equals(left, right);
    }

    @Override
    public Boolean apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return (Boolean) apply(left, (boolean) aBoolean);
      } else if (right instanceof Character character) {
        return (Boolean) apply(left, (char) character);
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return (Boolean) apply((boolean) aBoolean, right);
      } else if (left instanceof Character character) {
        return (Boolean) apply((char) character, right);
      } else if (left instanceof String string) {
        return apply(stringToInt(string), right);
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(String left, String right) {
      return StringUtils.equals(left, right);
    }
  },
  NOT_EQUALS {
    @Override
    public Boolean apply(Number left, Number right) {
      return NumberUtils.compare(left, right) != 0;
    }

    @Override
    public Boolean apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return (Boolean) apply(left, (boolean) aBoolean);
      } else if (right instanceof Character character) {
        return (Boolean) apply(left, (char) character);
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return (Boolean) apply((boolean) aBoolean, right);
      } else if (left instanceof Character character) {
        return (Boolean) apply((char) character, right);
      } else if (left instanceof String string) {
        return apply(stringToInt(string), right);
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Boolean apply(String left, String right) {
      return !StringUtils.equals(left, right);
    }
  },

  // Bitwise
  BITWISE_AND {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.and(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.and(left, right);
    }
  },
  BITWISE_OR {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.or(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.or(left, right);
    }
  },
  BITWISE_XOR {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.xor(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.xor(left, right);
    }
  },
  ELEMENTWISE_ADD {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.xor(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.charWiseAdd(left, right);
    }
  },
  ELEMENTWISE_SUBTRACT {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.xor(left, NumberUtils.not(right));
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.charWiseSubtract(left, right);
    }
  },
  ELEMENTWISE_MULTIPLY {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.and(left, right);
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.charWiseMultiply(left, right);
    }
  },
  ELEMENTWISE_DIVIDE {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.shiftRight(left, right.intValue());
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.charWiseDivide(left, right);
    }
  },
  ELEMENTWISE_MODULO {
    @Override
    public Number apply(Number left, Number right) {
      return NumberUtils.and(left, NumberUtils.subtract(right, 1));
    }

    @Override
    public Object apply(Number left, Object right) {
      if (right instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
      } else if (right instanceof Character character) {
        return (char) ((Number) apply(left, (char) character)).intValue();
      } else if (right instanceof String string) {
        return apply(left, stringToInt(string));
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public Object apply(Object left, Number right) {
      if (left instanceof Boolean aBoolean) {
        return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
      } else if (left instanceof Character character) {
        return (char) ((Number) apply((char) character, right)).intValue();
      } else if (left instanceof String string) {
        return apply(string, right.toString());
      }
      throw BinaryOperation.newUnsupportedOperationException(this, left, right);
    }

    @Override
    public String apply(String left, String right) {
      return StringUtils.charWiseModulo(left, right);
    }
  };

  public abstract Object apply(Number left, Number right);

  public Object apply(Number left, char right) {
    return apply(left, (int) right);
  }

  public Object apply(char left, Number right) {
    return apply((int) left, right);
  }

  public Object apply(Number left, boolean right) {
    return apply(left, booleanToInt(right));
  }

  public Object apply(boolean left, Number right) {
    return apply(booleanToInt(left), right);
  }

  public abstract Object apply(Number left, Object right);

  public abstract Object apply(Object left, Number right);

  public Object apply(char left, char right) {
    var result = apply((int) left, (int) right);
    if (result instanceof Number number) {
      return (char) number.intValue();
    } else if (result instanceof Boolean aBoolean) {
      return aBoolean;
    }
    return result;
  }

  public Object apply(boolean left, boolean right) {
    var result = apply(booleanToInt(left), booleanToInt(right));
    if (result instanceof Number number) {
      return intToBoolean(number.intValue());
    } else if (result instanceof Boolean aBoolean) {
      return aBoolean;
    }
    return result;
  }

  public abstract Object apply(String left, String right);

  public static UnsupportedOperationException newUnsupportedOperationException(
      Operation binaryOperation, Object left, Object right) {
    return new UnsupportedOperationException(
        binaryOperation
            + " not supported for types: "
            + left.getClass()
            + " and "
            + right.getClass());
  }
}
