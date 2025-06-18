package org.daiitech.naftah.core.builtin.utils;

import static org.daiitech.naftah.core.parser.NaftahParserHelper.getQualifiedName;
import static org.daiitech.naftah.core.parser.NaftahParserHelper.hasChild;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.daiitech.naftah.core.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.core.builtin.utils.op.UnaryOperation;
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
      return Array.getLength(obj) == 0 || Arrays.stream((Object[])obj).allMatch(Objects::isNull);
    }
    if (obj instanceof Collection<?> collection) {
      return collection.isEmpty() || collection.stream().allMatch(Objects::isNull);
    }
    if (obj instanceof Map<?, ?> map) {
      return map.isEmpty() || map.entrySet().stream().allMatch(entry -> Objects.isNull(entry.getKey()) || Objects.isNull(entry.getValue()));
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

  public static Object applyOperation(Object left, Object right, BinaryOperation operation) {
    if (left == null || right == null)
      throw new IllegalArgumentException("Arguments cannot be null");

    // Number vs Number
    if (left instanceof Number number && right instanceof Number number1) {
      return operation.apply(number, number1);
    }

    // Number vs Boolean/Character/String
    if(left instanceof Number number){
      return operation.apply(number, right);
    }

    if(right instanceof Number number){
      return operation.apply(left, number);
    }

    // Boolean vs Boolean
    if (left instanceof Boolean aBoolean && right instanceof Boolean aBoolean1) {
      return operation.apply(aBoolean, aBoolean1);
    }

    // Character vs Character
    if (left instanceof Character character && right instanceof Character character1) {
      return operation.apply(character, character1);
    }

    // String vs String
    if (left instanceof String s && right instanceof String s1) {
      return operation.apply(s, s1);
    }

    // Collection vs Collection (element-wise)
    if (left instanceof Collection<?> collection1 && right instanceof Collection<?> collection2) {
      return CollectionUtils. applyOperation(collection1, collection2, operation);
    }

    // Array vs Array (element-wise)
    if (left.getClass().isArray() && right.getClass().isArray()) {
      return CollectionUtils. applyOperation((Object[])left, (Object[])right, operation);
    }

    // Collection vs Number (scalar multiplication)
    if (left instanceof Collection<?> collection && right instanceof Number number) {
      return CollectionUtils. applyOperation(collection, number, operation);
    }

    // Number vs Collection (scalar multiplication)
    if (left instanceof Number number && right instanceof Collection<?> collection) {
      return CollectionUtils. applyOperation(collection, number, operation);
    }

    // Array vs Number (scalar multiplication)
    if (left.getClass().isArray() && right instanceof Number number) {
      return CollectionUtils. applyOperation((Object[]) left, number, operation);
    }

    // Number vs Array (scalar multiplication)
    if (left instanceof Number number && right.getClass().isArray()) {
      return CollectionUtils. applyOperation((Object[]) right, number, operation);
    }

    // Map vs Map (element-wise value multiplication)
    if (left instanceof Map<?, ?> map && right instanceof Map<?, ?> map1) {
      return CollectionUtils. applyOperation(map, map1, operation);
    }

    // Map vs Number (multiply all values by scalar)
    if (left instanceof Map<?, ?> map  && right instanceof Number number) {
      return CollectionUtils. applyOperation(map, number, operation);
    }

    // Number vs Map (multiply all values by scalar)
    if (left instanceof Number number && right instanceof Map<?, ?> map) {
      return CollectionUtils. applyOperation(map, number, operation);
    }

    throw new UnsupportedOperationException(operation + " not supported for types: " + left.getClass() + " and " + right.getClass());
  }

  public static Object applyOperation(Object a, UnaryOperation operation) {
    if (a == null)
      throw new IllegalArgumentException("Arguments cannot be null");

    // Number
    if (a instanceof Number number) {
      return operation.apply(number);
    }

    // Boolean
    if (a instanceof Boolean aBoolean) {
      return operation.apply(aBoolean);
    }

    // Character
    if (a instanceof Character character) {
      return operation.apply(character);
    }

    // String
    if (a instanceof String s) {
      return operation.apply(s);
    }

    // Collection
    if (a instanceof Collection<?> collection) {
      return CollectionUtils.applyOperation(collection, operation);
    }

    // Array
    if (a.getClass().isArray()) {
      return CollectionUtils. applyOperation((Object[])a, operation);
    }

    // Map
    if (a instanceof Map<?, ?> map) {
      return CollectionUtils. applyOperation(map, operation);
    }

    throw new UnsupportedOperationException(operation + " not supported for type: " + a.getClass());
  }

  public static int booleanToInt(boolean aBoolean) {
    return aBoolean ? 1 : 0;
  }
  public static boolean intToBoolean(int i) {
    return i > 0 && i % 2 == 0;
  }

}
