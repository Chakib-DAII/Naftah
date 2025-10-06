package org.daiitech.naftah.builtin.utils.op;

import org.daiitech.naftah.errors.NaftahBugError;

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

	/**
	 * Creates a {@link NaftahBugError} indicating that no enum constant was found
	 * for the given operator symbol in the specified {@code Operation} enum class.
	 *
	 * @param operation the enum class where the operator was expected
	 * @param op        the operator symbol (e.g., "+", "AND", etc.)
	 * @return a {@code NaftahBugError} with an Arabic error message
	 */
	static NaftahBugError newNaftahBugNoEnumValueError(Class<? extends Operation> operation, String op) {
		return new NaftahBugError(String
				.format(
						"لا يوجد ثابت في التعداد %s بالرمز '%s'.",
						operation.getName(),
						op
				));
	}

}
