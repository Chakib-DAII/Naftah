package org.daiitech.naftah.builtin.utils.op;

/**
 * Represents a general operation in the Naftah language.
 * <p>
 * This is a sealed interface that restricts its implementations
 * to {@link UnaryOperation} and {@link BinaryOperation}.
 * </p>
 *
 * @author Chakib Daii
 */
public sealed interface Operation permits UnaryOperation, BinaryOperation {
}
