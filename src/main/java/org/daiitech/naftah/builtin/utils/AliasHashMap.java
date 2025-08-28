package org.daiitech.naftah.builtin.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import org.daiitech.naftah.builtin.lang.BuiltinFunction;

/**
 * A specialized HashMap that supports aliasing of keys.
 *
 * <p>This map allows you to associate multiple alias keys with a single
 * canonical key, so that any of the aliases can be used to retrieve the value
 * of the canonical key.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * AliasHashMap<String, String> map = new AliasHashMap<>();
 * map.put("NA", "Naftah", "NFTH", "Naftah");
 *
 * map.get("NA");      // "Naftah"
 * map.get("NFTH");     // "Naftah"
 * map.get("Naftah"); // "Naftah"
 * }</pre>
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @author Chakib Daii
 */
public class AliasHashMap<K, V> extends HashMap<K, V> {

	/**
	 * Internal map storing alias → canonical key mappings.
	 */
	private final Map<K, K> aliasToKeyMap = new HashMap<>();

	/**
	 * Returns a {@link Collector} that groups {@link BuiltinFunction} instances by their canonical function name,
	 * storing the results in an {@link AliasHashMap} where:
	 * <ul>
	 *     <li>The key is the canonical function name as returned by {@code getFunctionInfo().name()}.</li>
	 *     <li>The value is a {@code List} of {@link BuiltinFunction} instances sharing that name.</li>
	 *     <li>Each function's declared aliases ({@code getFunctionInfo().aliases()}) are registered in the resulting
	 *     map,
	 *         allowing lookup by alias as well.</li>
	 * </ul>
	 *
	 * <p>This collector can be used to efficiently index built-in functions by their primary name and aliases,
	 * allowing flexible name-based lookup in environments such as the Naftah runtime.</p>
	 *
	 * <p>The resulting {@code AliasHashMap} supports retrieval via both canonical names and aliases.</p>
	 *
	 * <p><strong>Example usage:</strong></p>
	 * <pre>{@code
	 * AliasHashMap<String, List<BuiltinFunction>> functionsByName =
	 *     getBuiltinMethods(Builtin.class)
	 *         .stream()
	 *         .collect(toAliasGroupedByName());
	 * }</pre>
	 *
	 * @return a collector that groups {@code BuiltinFunction}s by canonical name and registers their aliases
	 * @see org.daiitech.naftah.builtin.lang.NaftahFunction#name()
	 * @see org.daiitech.naftah.builtin.lang.NaftahFunction#aliases()
	 * @see AliasHashMap
	 */

	public static Collector<BuiltinFunction, AliasHashMap<String, List<BuiltinFunction>>, AliasHashMap<String,
			List<BuiltinFunction>>> toAliasGroupedByName() {
		return Collector
				.of(
						AliasHashMap::new,
						(map, fn) -> {
							String canonicalKey = fn.getFunctionInfo().name();
							map.computeIfAbsent(canonicalKey, k -> new ArrayList<>()).add(fn);
							for (String alias : fn.getFunctionInfo().aliases()) {
								map.aliasToKeyMap.put(alias, canonicalKey);
							}
						},
						(left, right) -> {
							for (Map.Entry<String, List<BuiltinFunction>> entry : right.entrySet()) {
								left.merge(entry.getKey(), entry.getValue(), (list1, list2) -> {
									list1.addAll(list2);
									return list1;
								});
							}

							// Merge alias maps
							left.aliasToKeyMap.putAll(right.aliasToKeyMap);

							return left;
						},
						Collector.Characteristics.IDENTITY_FINISH
				);
	}


	/**
	 * Associates the specified value with the specified canonical key,
	 * and registers any number of alias keys that map to the same value.
	 *
	 * <p>If the canonical key already exists in the map, it will be overwritten.
	 * Aliases will always overwrite previous alias mappings.</p>
	 *
	 * @param canonicalKey The main key to associate with the value.
	 * @param value        The value to be associated with the canonical key.
	 * @param aliases      Optional alias keys that should also retrieve the same value.
	 */
	@SafeVarargs
	public final void put(K canonicalKey, V value, K... aliases) {
		super.put(canonicalKey, value);
		for (K alias : aliases) {
			aliasToKeyMap.put(alias, canonicalKey);
		}
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null}
	 * if this map contains no mapping for the key or its aliases.
	 *
	 * <p>Checks canonical keys first, then aliases.</p>
	 *
	 * @param key The key (or alias) whose associated value is to be returned.
	 * @return The value associated with the key or alias, or {@code null} if none exists.
	 */
	@Override
	public V get(Object key) {
		if (super.containsKey(key)) {
			return super.get(key);
		}
		else if (aliasToKeyMap.containsKey(key)) {
			return super.get(aliasToKeyMap.get(key));
		}
		else {
			return null;
		}
	}

	/**
	 * Returns {@code true} if this map contains a mapping for the specified key or any of its aliases.
	 *
	 * @param key The key (or alias) whose presence in this map is to be tested.
	 * @return {@code true} if the map contains the key or any of its aliases.
	 */
	@Override
	public boolean containsKey(Object key) {
		return aliasToKeyMap.containsKey(key) || super.containsKey(key);
	}
}
