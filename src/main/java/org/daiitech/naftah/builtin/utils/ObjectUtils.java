package org.daiitech.naftah.builtin.utils;

import static org.daiitech.naftah.parser.NaftahParserHelper.*;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Vocabulary;
import org.daiitech.naftah.builtin.lang.*;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.DefaultContext;
import org.daiitech.naftah.parser.NaftahParser;

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
    if (obj instanceof String str) return !str.isBlank();

    // Array
    if (obj.getClass().isArray()) return Array.getLength(obj) > 0;

    // Collection (e.g., List, Set)
    if (obj instanceof Collection<?> collection) return !collection.isEmpty();

    // Map (e.g., HashMap)
    if (obj instanceof Map<?, ?> map) return !map.isEmpty();

    if (obj instanceof Map.Entry<?, ?> entry)
      return isTruthy(entry.getKey()) && isTruthy(entry.getValue());

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
      return Array.getLength(obj) == 0 || Arrays.stream((Object[]) obj).allMatch(Objects::isNull);
    }
    if (obj instanceof Collection<?> collection) {
      return collection.isEmpty() || collection.stream().allMatch(Objects::isNull);
    }
    if (obj instanceof Map<?, ?> map) {
      return map.isEmpty()
          || map.entrySet().stream()
              .allMatch(
                  entry -> Objects.isNull(entry.getKey()) || Objects.isNull(entry.getValue()));
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

  public static String getNaftahType(Parser parser, Class<?> javaType) {
    Vocabulary vocabulary = parser.getVocabulary();
    if (Objects.isNull(javaType))
      return getFormattedTokenSymbols(
          vocabulary, org.daiitech.naftah.parser.NaftahLexer.VAR, false);
    else {
      if (Boolean.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.BOOLEAN, false);
      if (Character.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.CHAR, false);
      if (Byte.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.BYTE, false);
      if (Short.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.SHORT, false);
      if (Integer.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.INT, false);
      if (Long.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.LONG, false);
      if (Float.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.FLOAT, false);
      if (Double.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.DOUBLE, false);
      if (String.class.isAssignableFrom(javaType))
        return getFormattedTokenSymbols(
            vocabulary, org.daiitech.naftah.parser.NaftahLexer.STRING_TYPE, false);
    }
    return getFormattedTokenSymbols(vocabulary, org.daiitech.naftah.parser.NaftahLexer.VAR, false);
  }

  public static boolean isBuiltinType(Object obj) {
    if (obj == null) return false;
    Class<?> cls = obj.getClass();
    return cls == BuiltinFunction.class
        || cls == JvmFunction.class
        || cls == DeclaredFunction.class
        || cls == DeclaredParameter.class
        || cls == DeclaredVariable.class
        || cls == DynamicNumber.class;
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

  public static boolean isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(Object obj) {
    if (obj == null) return false;

    // Simple value
    if (isSimpleType(obj)) return true;

    // Builtin value
    if (isBuiltinType(obj)) return true;

    // Array of simple or recursive types
    if (obj.getClass().isArray()) {
      int len = Array.getLength(obj);
      for (int i = 0; i < len; i++) {
        Object element = Array.get(obj, i);
        if (!isTruthy(element)) continue;
        if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(element)) return false;
      }
      return true;
    }

    // Collection of simple or recursive types
    if (obj instanceof Collection<?> collection) {
      for (Object item : collection) {
        if (!isTruthy(item)) continue;
        if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(item)) return false;
      }
      return true;
    }

    // Map with simple keys and values
    if (obj instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (!isTruthy(entry)) continue;
        if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(entry.getKey())) return false;
        if (!isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(entry.getValue())) return false;
      }
      return true;
    }

    // Anything else is not allowed
    return false;
  }

  public static Object applyOperation(Object left, Object right, BinaryOperation operation) {
    if (left == null || right == null) throw new NaftahBugError("لا يمكن أن تكون الوسائط فارغة.");

    // Number vs Number
    if (left instanceof Number number && right instanceof Number number1) {
      return operation.apply(number, number1);
    }

    // Number vs Boolean/Character/String
    if (left instanceof Number number) {
      return operation.apply(number, right);
    }

    if (right instanceof Number number) {
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

    // String vs Character
    if (left instanceof String s && right instanceof Character character) {
      return operation.apply(s, String.valueOf(character));
    }

    // Collection vs Collection (element-wise)
    if (left instanceof Collection<?> collection1 && right instanceof Collection<?> collection2) {
      return CollectionUtils.applyOperation(collection1, collection2, operation);
    }

    // Array vs Array (element-wise)
    if (left.getClass().isArray() && right.getClass().isArray()) {
      return CollectionUtils.applyOperation((Object[]) left, (Object[]) right, operation);
    }

    // Collection vs Number (scalar multiplication)
    if (left instanceof Collection<?> collection && right instanceof Number number) {
      return CollectionUtils.applyOperation(collection, number, operation);
    }

    // Number vs Collection (scalar multiplication)
    if (left instanceof Number number && right instanceof Collection<?> collection) {
      return CollectionUtils.applyOperation(collection, number, operation);
    }

    // Array vs Number (scalar multiplication)
    if (left.getClass().isArray() && right instanceof Number number) {
      return CollectionUtils.applyOperation((Object[]) left, number, operation);
    }

    // Number vs Array (scalar multiplication)
    if (left instanceof Number number && right.getClass().isArray()) {
      return CollectionUtils.applyOperation((Object[]) right, number, operation);
    }

    // Map vs Map (element-wise value multiplication)
    if (left instanceof Map<?, ?> map && right instanceof Map<?, ?> map1) {
      return CollectionUtils.applyOperation(map, map1, operation);
    }

    // Map vs Number (multiply all values by scalar)
    if (left instanceof Map<?, ?> map && right instanceof Number number) {
      return CollectionUtils.applyOperation(map, number, operation);
    }

    // Number vs Map (multiply all values by scalar)
    if (left instanceof Number number && right instanceof Map<?, ?> map) {
      return CollectionUtils.applyOperation(map, number, operation);
    }

    throw BinaryOperation.newNaftahBugError(operation, left, right);
  }

  public static Object applyOperation(Object a, UnaryOperation operation) {
    if (a == null) throw new NaftahBugError("لا يمكن أن يكون الوسيط فارغًا.");

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
      return CollectionUtils.applyOperation((Object[]) a, operation);
    }

    // Map
    if (a instanceof Map<?, ?> map) {
      return CollectionUtils.applyOperation(map, operation);
    }

    throw UnaryOperation.newNaftahBugError(operation, a);
  }

  public static int booleanToInt(boolean aBoolean) {
    return aBoolean ? 1 : 0;
  }

  public static boolean intToBoolean(int i) {
    return i > 0 && i % 2 == 0;
  }

  public static String booleanToString(boolean b) {
    return b ? "صحيح" : "خطأ";
  }

  public static String arrayToString(Object obj) {
    if (obj == null) {
      return NULL;
    }

    Class<?> objClass = obj.getClass();

    if (!objClass.isArray()) {
      return obj.toString(); // not an array
    }

    // Handle primitive arrays
    if (obj instanceof int[]) return Arrays.toString((int[]) obj);
    if (obj instanceof long[]) return Arrays.toString((long[]) obj);
    if (obj instanceof double[]) return Arrays.toString((double[]) obj);
    if (obj instanceof float[]) return Arrays.toString((float[]) obj);
    if (obj instanceof boolean[]) return Arrays.toString((boolean[]) obj);
    if (obj instanceof char[]) return Arrays.toString((char[]) obj);
    if (obj instanceof byte[]) return Arrays.toString((byte[]) obj);
    if (obj instanceof short[]) return Arrays.toString((short[]) obj);

    // Handle object arrays
    return replaceAllNulls(Arrays.toString((Object[]) obj));
  }

  public static String getNaftahValueToString(Object o) {
    if (o == null) return NULL;
    if (o instanceof Boolean aBoolean) return booleanToString(aBoolean);
    if (o.getClass().isArray()) return arrayToString(o);
    return replaceAllNulls(o.toString());
  }

  public static Object getNaftahValue(Object o) {
    if (o == null) return NULL;
    if (o instanceof Boolean aBoolean) return booleanToString(aBoolean);
    return o;
  }

  public static String replaceAllNulls(String s) {
    return s.replaceAll("null", NULL);
  }
}
