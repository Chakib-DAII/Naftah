package org.daiitech.naftah.core.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;

/**
 * @author Chakib Daii
 **/
public class NaftahParserHelper {

    public static  <T extends Tree> boolean hasChild(T tree) {
        return tree != null;
    }

    public static  Object visit(org.daiitech.naftah.core.parser.NaftahParserBaseVisitor naftahParserBaseVisitor, ParseTree tree) {
        return naftahParserBaseVisitor.visit(tree);
    }

}
