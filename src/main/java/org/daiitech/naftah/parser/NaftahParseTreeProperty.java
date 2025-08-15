package org.daiitech.naftah.parser;

import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Extension of ANTLR's ParseTreeProperty to expose
 * internal annotations map and support copying from
 * another NaftahParseTreeProperty.
 *
 * @param <V> the type of values stored in the property
 * @author Chakib Daii
 */
public class NaftahParseTreeProperty<V> extends ParseTreeProperty<V> {

	/**
	 * Returns the internal map holding parse tree nodes and their associated values.
	 *
	 * @return a map of ParseTree keys to values of type V
	 */
	public Map<ParseTree, V> getAll() {
		return annotations;
	}

	/**
	 * Copies all annotations from another NaftahParseTreeProperty instance
	 * into this one, effectively merging the two.
	 *
	 * @param other another NaftahParseTreeProperty to copy from
	 */
	public void copyFrom(NaftahParseTreeProperty<V> other) {
		annotations.putAll(other.getAll());
	}
}
