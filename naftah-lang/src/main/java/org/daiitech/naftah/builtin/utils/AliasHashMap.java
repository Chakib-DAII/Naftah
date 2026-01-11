package org.daiitech.naftah.builtin.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;

import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.utils.reflect.ClassUtils.getBuiltinFunctionName;

/**
 * A specialized {@link HashMap} that supports aliasing of keys.
 *
 * <p>This map allows associating multiple alias keys with a single canonical key,
 * enabling any alias to retrieve the value associated with the canonical key.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * AliasHashMap<String, String> map = new AliasHashMap<>();
 * map.put("NA", "Naftah", "NFTH", "Naftah");
 *
 * map.get("NA");      // returns "Naftah"
 * map.get("NFTH");    // returns "Naftah"
 * map.get("Naftah");  // returns "Naftah"
 * }</pre>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Chakib Daii
 */
public class AliasHashMap<K, V> extends HashMap<K, List<V>> {

	/**
	 * Internal map storing alias → canonical key relationships.
	 */
	private final Map<K, Set<K>> aliasToKeysMap = new HashMap<>();

	/**
	 * Returns a {@link Collector} that groups {@link BuiltinFunction} instances by their canonical function name,
	 * storing the results in an {@link AliasHashMap} where:
	 * <ul>
	 * <li>The key is the canonical function name as returned by {@code getFunctionInfo().name()}.</li>
	 * <li>The value is a {@code List} of {@link BuiltinFunction} instances sharing that name.</li>
	 * <li>Each function's declared aliases ({@code getFunctionInfo().aliases()}) are registered in the resulting
	 * map,
	 * allowing lookup by alias as well.</li>
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
	public static Collector<BuiltinFunction, AliasHashMap<String, BuiltinFunction>, AliasHashMap<String, BuiltinFunction>> toAliasGroupedByName() {
		return Collector
				.of(
					AliasHashMap::new,
					(map, fn) -> {
						boolean useQualifiedName = fn
								.getProviderInfo()
								.useQualifiedName() | fn
										.getFunctionInfo()
										.useQualifiedName();

						boolean useQualifiedAliases = fn
								.getProviderInfo()
								.useQualifiedAliases() | fn
										.getFunctionInfo()
										.useQualifiedAliases();

						String providerName = fn
								.getProviderInfo()
								.name();

						String canonicalKey = getBuiltinFunctionName(   useQualifiedName,
																		providerName,
																		fn
																				.getFunctionInfo()
																				.name(),
																		true);
						map.computeIfAbsent(canonicalKey, k -> new ArrayList<>()).add(fn);
						for (String alias : fn.getFunctionInfo().aliases()) {
							var maybeQualifiedAlias = getBuiltinFunctionName(   useQualifiedAliases,
																				providerName,
																				alias,
																				false);

							fillAliasToKeyMap(map, maybeQualifiedAlias, canonicalKey, fn);
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
						left.aliasToKeysMap.putAll(right.aliasToKeysMap);

						return left;
					},
					Collector.Characteristics.IDENTITY_FINISH
				);
	}

	/**
	 * Fills the alias-to-key mapping for a given {@link AliasHashMap}.
	 * <p>
	 * This method registers a mapping from a given alias to its canonical key.
	 * If the alias already exists in the map, a {@link NaftahBugError} will be thrown to prevent
	 * alias overriding.
	 * </p>
	 *
	 * <p>
	 * Function aliases are intended to be immutable and globally unique. Attempting to override
	 * or redefine an existing alias is considered a bug in the builtin function provider.
	 * </p>
	 *
	 * @param <K>          the type of keys (aliases and canonical keys)
	 * @param <V>          the type of mapped values
	 * @param map          the {@link AliasHashMap} to update
	 * @param alias        the alias key to register
	 * @param canonicalKey the canonical key associated with the alias
	 * @param fn           the {@link BuiltinFunction} used for error reporting
	 */
	public static <K, V> void fillAliasToKeyMap(AliasHashMap<K, V> map, K alias, K canonicalKey, BuiltinFunction fn) {
		map.aliasToKeysMap.computeIfAbsent(alias, k -> new HashSet<>()).add(canonicalKey);
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
		super.computeIfAbsent(canonicalKey, k -> new ArrayList<>()).add(value);
		for (K alias : aliases) {
			aliasToKeysMap.computeIfAbsent(alias, k -> new HashSet<>()).add(canonicalKey);
		}
	}

	/**
	 * Copies all mappings from the specified map into this map.
	 * <p>
	 * This method behaves like {@link java.util.HashMap#putAll(Map)} for regular entries,
	 * but also preserves alias relationships when the source map is an instance of
	 * {@link AliasHashMap}.
	 * </p>
	 *
	 * <p>When copying from another {@code AliasHashMap}, all alias-to-canonical-key
	 * mappings from the source are merged into this map. If any alias already exists
	 * in this map, a {@link NaftahBugError} will be thrown to prevent alias
	 * overriding.</p>
	 *
	 * @param map The map whose mappings are to be copied into this map.
	 * @throws NaftahBugError if an alias conflict occurs during merging.
	 * @see #fillAliasToKeyMap(AliasHashMap, Object, Object, BuiltinFunction)
	 */
	@Override
	public void putAll(Map<? extends K, ? extends List<V>> map) {
		super.putAll(map);
		if (map instanceof AliasHashMap<?, ?> aliasHashMap) {
			for (Map.Entry<?, ?> e : aliasHashMap.aliasToKeysMap.entrySet()) {
				//noinspection unchecked
				K key = (K) e.getKey();
				//noinspection unchecked
				Set<K> canonicalKeys = (Set<K>) e.getValue();
				for (K ck : canonicalKeys) {
					fillAliasToKeyMap(this, key, ck, null);
				}
			}
		}
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null}
	 * if this map contains no mapping for the key or its aliases.
	 *
	 * <p>Checks canonical keys first, then aliases.</p>
	 *
	 * @param key The key (or alias) whose associated value is to be returned.
	 * @return a list of associated values, or {@code null} if none exists.
	 */
	@Override
	public List<V> get(Object key) {
		//noinspection SuspiciousMethodCalls
		if (aliasToKeysMap.containsKey(key)) {
			Set<K> canonicalKeys = aliasToKeysMap.get(key);

			if (canonicalKeys == null || canonicalKeys.isEmpty()) {
				return super.getOrDefault(key, null);
			}
			//noinspection unchecked
			canonicalKeys.add((K) key);

			return canonicalKeys
					.stream()
					.map(super::get)          // Map<K, Set<V>> → Set<V>
					.filter(Objects::nonNull) // skip missing keys
					.flatMap(List::stream)     // flatten Set<Set<V>> → V
					.toList();
		}
		else {
			return super.getOrDefault(key, null);
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
		return aliasToKeysMap.containsKey(key) || super.containsKey(key);
	}
}
