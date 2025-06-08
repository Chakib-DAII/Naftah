package org.daiitech.naftah.utils;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.Map;

/**
 * @author Chakib Daii
 **/
public class NaftahParseTreeProperty<V> extends ParseTreeProperty<V> {

    public Map<ParseTree, V> getAll() { return annotations; }

    public void copyFrom(NaftahParseTreeProperty<V> other) { annotations.putAll(other.getAll()); }

}
