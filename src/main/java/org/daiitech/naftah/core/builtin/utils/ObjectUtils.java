package org.daiitech.naftah.core.builtin.utils;

import static org.daiitech.naftah.core.parser.NaftahParserHelper.getQualifiedName;
import static org.daiitech.naftah.core.parser.NaftahParserHelper.hasChild;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.daiitech.naftah.core.parser.NaftahParser;
import org.daiitech.naftah.utils.DefaultContext;

/**
 * @author Chakib Daii
 */
public final class ObjectUtils {

  public static boolean isTruthy(Object obj) {
    if (obj == null) return false;

    // Boolean
    if (obj instanceof Boolean bool) return bool;

    // Number (includes Integer, Double, etc.)
    if (obj instanceof Number num) {
      // TODO: enhance to support all kind of numbers (using DynamicNumber)
      double value = num.doubleValue();
      return value != 0.0 && !Double.isNaN(value);
    }

    // String
    if (obj instanceof String str) return !str.isEmpty();

    // Array
    if (obj.getClass().isArray()) return Array.getLength(obj) > 0;

    // Collection (e.g., List, Set)
    if (obj instanceof Collection<?> collection) return !collection.isEmpty();

    // Map (e.g., HashMap)
    if (obj instanceof Map<?, ?> map) return !map.isEmpty();

    // Other objects (non-null) are truthy
    return true;
  }

  public static Object not(Object value) {
    try {
      // arithmetic negation
      return NumberUtils.negate(value);
    } catch (Exception e) {
      // logical negation
      return !isTruthy(value);
    }
  }

  /**
   * Determine whether the given object is empty.
   *
   * <p>This method supports the following object types.
   *
   * <ul>
   *   <li>{@code Optional}: considered empty if not {@link Optional#isPresent()}
   *   <li>{@code Array}: considered empty if its length is zero
   *   <li>{@link CharSequence}: considered empty if its length is zero
   *   <li>{@link Collection}: delegates to {@link Collection#isEmpty()}
   *   <li>{@link Map}: delegates to {@link Map#isEmpty()}
   * </ul>
   *
   * <p>If the given object is non-null and not one of the aforementioned supported types, this
   * method returns {@code false}.
   *
   * @param obj the object to check
   * @return {@code true} if the object is {@code null} or <em>empty</em>
   */
  public static boolean isEmpty(Object obj) {
    if (obj == null) {
      return true;
    }

    if (obj instanceof Optional<?> optional) {
      return optional.isEmpty();
    }
    if (obj instanceof CharSequence charSequence) {
      return charSequence.isEmpty();
    }
    if (obj.getClass().isArray()) {
      return Array.getLength(obj) == 0;
    }
    if (obj instanceof Collection<?> collection) {
      return collection.isEmpty();
    }
    if (obj instanceof Map<?, ?> map) {
      return map.isEmpty();
    }

    // else
    return false;
  }

  public static Class<?> getJavaType(ParserRuleContext naftahTypeContext) {
    if (naftahTypeContext instanceof NaftahParser.ReturnTypeContext returnTypeContext) {
      if (returnTypeContext instanceof NaftahParser.VoidReturnTypeContext) {
        return Void.class;
      } else if (returnTypeContext
          instanceof NaftahParser.TypeReturnTypeContext typeReturnTypeContext) {
        NaftahParser.TypeContext typeContext = typeReturnTypeContext.type();
        if (typeContext instanceof NaftahParser.VarTypeContext) {
          return Object.class;
        } else if (typeContext instanceof NaftahParser.BuiltInTypeContext builtInTypeContext) {
          return getJavaType(builtInTypeContext.builtIn());
        } else if (typeContext
            instanceof NaftahParser.QualifiedNameTypeContext qualifiedNameTypeContext) {
          NaftahParser.QualifiedNameContext qualifiedNameContext =
              qualifiedNameTypeContext.qualifiedName();
          var qualifiedName = getQualifiedName(qualifiedNameContext);
          return DefaultContext.getJavaType(qualifiedName);
        }
      }
    } else if (naftahTypeContext instanceof NaftahParser.VarTypeContext) {
      return Object.class;
    } else if (naftahTypeContext instanceof NaftahParser.BuiltInTypeContext builtInTypeContext) {
      return getJavaType(builtInTypeContext.builtIn());
    } else if (naftahTypeContext instanceof NaftahParser.BuiltInContext builtInContext) {
      return getJavaType(builtInContext);
    } else if (naftahTypeContext
        instanceof NaftahParser.QualifiedNameTypeContext qualifiedNameTypeContext) {
      NaftahParser.QualifiedNameContext qualifiedNameContext =
          qualifiedNameTypeContext.qualifiedName();
      var qualifiedName = getQualifiedName(qualifiedNameContext);
      return DefaultContext.getJavaType(qualifiedName);
    } else if (naftahTypeContext
        instanceof NaftahParser.QualifiedNameContext qualifiedNameContext) {
      var qualifiedName = getQualifiedName(qualifiedNameContext);
      return DefaultContext.getJavaType(qualifiedName);
    }
    return Object.class;
  }

  public static Class<?> getJavaType(NaftahParser.BuiltInContext builtInContext) {
    if (hasChild(builtInContext.BOOLEAN())) return Boolean.class;
    if (hasChild(builtInContext.CHAR())) return Character.class;
    if (hasChild(builtInContext.BYTE())) return Byte.class;
    if (hasChild(builtInContext.SHORT())) return Short.class;
    if (hasChild(builtInContext.INT())) return Integer.class;
    if (hasChild(builtInContext.LONG())) return Long.class;
    if (hasChild(builtInContext.FLOAT())) return Float.class;
    if (hasChild(builtInContext.DOUBLE())) return Double.class;
    if (hasChild(builtInContext.STRING_TYPE())) return String.class;
    return Object.class;
  }

  public static boolean isSimpleType(Object obj) {
    if (obj == null) return false;
    Class<?> cls = obj.getClass();

    return cls.isPrimitive()
        || cls == String.class
        || cls == Integer.class
        || cls == Long.class
        || cls == Short.class
        || cls == Double.class
        || cls == Float.class
        || cls == Byte.class
        || cls == Boolean.class
        || cls == BigDecimal.class
        || cls == BigInteger.class
        || cls == Character.class;
  }

  public static boolean isSimpleOrCollectionOrMapOfSimpleType(Object obj) {
    if (obj == null) return false;

    // Simple value
    if (isSimpleType(obj)) return true;

    // Array of simple or recursive types
    if (obj.getClass().isArray()) {
      int len = Array.getLength(obj);
      for (int i = 0; i < len; i++) {
        Object element = Array.get(obj, i);
        if (!isSimpleOrCollectionOrMapOfSimpleType(element)) return false;
      }
      return true;
    }

    // Collection of simple or recursive types
    if (obj instanceof Collection<?>) {
      for (Object item : (Collection<?>) obj) {
        if (!isSimpleOrCollectionOrMapOfSimpleType(item)) return false;
      }
      return true;
    }

    // Map with simple keys and values
    if (obj instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (!isSimpleOrCollectionOrMapOfSimpleType(entry.getKey())) return false;
        if (!isSimpleOrCollectionOrMapOfSimpleType(entry.getValue())) return false;
      }
      return true;
    }

    // Anything else is not allowed
    return false;
  }

  public static Object multiply(Number a, Object b, boolean safe) {
      if(b instanceof Boolean aBoolean) {
        int multiplier = aBoolean ? 1 : 0;
        return NumberUtils.multiply(a, multiplier);
      } else if(b instanceof Character character) {
        return new String(new char[a.intValue()]).replace('\0', character);
      } else if(b instanceof String string) {
        return  string.repeat(a.intValue());
      }

    if (safe)
      return null;

    throw new UnsupportedOperationException("Multiplication not supported for types: " + a.getClass() + " and " + b.getClass());
  }
  public static Object multiply(Object a, Object b, boolean safe) {
    // TODO: add more validations
    if (a == null || b == null)
      throw new IllegalArgumentException("Arguments cannot be null");

    // Number * Number
    if (a instanceof Number && b instanceof Number) {
      return NumberUtils.multiply(a, b);
    }

    // Number * Boolean/Character/String
    if(a instanceof Number number){
      return multiply(number, b, true);
    }

    if(b instanceof Number number){
      return multiply(number, a, true);
    }

    // Boolean * Boolean
    if (a instanceof Boolean aBoolean && b instanceof Boolean aBoolean1) {
      int multiplier = aBoolean ? 1 : 0;
      int multiplier1 = aBoolean1 ? 1 : 0;
      return NumberUtils.multiply(multiplier, multiplier1);
    }

    // Collection * Collection (element-wise)
    if (a instanceof Collection<?> collection1 && b instanceof Collection<?> collection2) {
      return CollectionUtils.multiply((Collection<Number>) collection1, (Collection<Number>) collection2);
    }

    // Array * Array (element-wise)
    if (a.getClass().isArray() && b.getClass().isArray()) {
      return CollectionUtils.multiply((Number[])a, (Number[])b);
    }

    // Collection * Number (scalar multiplication)
    if (a instanceof Collection && b instanceof Number number) {
      return CollectionUtils.multiply((Collection<Number>) a, number);
    }

    // Number * Collection (scalar multiplication)
    if (a instanceof Number number && b instanceof Collection) {
      return CollectionUtils.multiply((Collection<Number>) b, number);
    }

    // Array * Number (scalar multiplication)
    if (a.getClass().isArray() && b instanceof Number number) {
      return CollectionUtils.multiply((Number[]) a, number);
    }

    // Number * Array (scalar multiplication)
    if (a instanceof Number number && b.getClass().isArray()) {
      return CollectionUtils.multiply((Number[]) b, number);
    }

    // Map * Map (element-wise value multiplication)
    if (a instanceof Map && b instanceof Map) {
      return CollectionUtils.multiply((Map<Object, Number>) a, (Map<Object, Number>) b);
    }

    // Map * Number (multiply all values by scalar)
    if (a instanceof Map && b instanceof Number number) {
      return CollectionUtils.multiply((Map<Object, Number>) a, number);
    }

    // Number * Map (multiply all values by scalar)
    if (a instanceof Number number && b instanceof Map) {
      return CollectionUtils.multiply((Map<Object, Number>) b, number);
    }

    if (safe)
      return null;

    throw new UnsupportedOperationException("Multiplication not supported for types: " + a.getClass() + " and " + b.getClass());
  }


}
