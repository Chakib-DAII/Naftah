package org.daiitech.naftah.builtin.lang;

import java.util.List;

/**
 * @author Chakib Daii
 */
public record NaftahFunctionProvider(String name, String description, List<String> functionNames) {

  public static NaftahFunctionProvider of(String name, String description, String[] functionNames) {
    return new NaftahFunctionProvider(name, description, List.of(functionNames));
  }
}
