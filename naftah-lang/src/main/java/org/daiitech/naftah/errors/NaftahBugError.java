/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.daiitech.naftah.errors;

import java.io.Serial;

/**
 * This class represents an error that is thrown when a bug
 * is recognized inside the runtime. Basically it is thrown when a
 * constraint is not fulfilled that should be fulfilled.
 * <p>
 * It extends {@link AssertionError}, indicating a serious, unexpected condition
 * that the program should never reach under normal circumstances.
 * <p>
 * Optionally carries a cause {@link Throwable} and a custom bug message.
 * <p>
 * The error message is prefixed with Arabic messages like
 * {@code "خطأ برمجي!"} to indicate developer or logic-level bugs.
 *
 * @author Chakib Daii
 */
public class NaftahBugError extends AssertionError {

	/**
	 * Serialization ID.
	 */
	@Serial
	private static final long serialVersionUID = -9165076784700059275L;

	/**
	 * The underlying exception that caused this error, if any.
	 */
	private final Throwable exception;

	/**
	 * The line number on which the 1st character of this token was matched, line=1..n.
	 */
	private final int line;

	/**
	 * The index of the first character of this token relative to the beginning of the line at which it occurs, 0..n-1.
	 */
	private final int column;

	/**
	 * The descriptive message text for the bug error.
	 */
	private String message;

	/**
	 * Constructs a {@code NaftahBugError} using the given message text.
	 *
	 * @param message the error message describing the bug
	 */
	public NaftahBugError(String message) {
		this(message, null);
	}

	/**
	 * Constructs a {@code NaftahBugError} using a descriptive message and the line number along with the column.
	 *
	 * @param msg    the error message describing the bug
	 * @param line   The line number on which the 1st character of this token was matched
	 * @param column The index of the first character of this token relative to the beginning of the line at which
	 *               it occurs
	 */
	public NaftahBugError(String msg, int line, int column) {
		this(msg, null, line, column);
	}

	/**
	 * Constructs a {@code NaftahBugError} using the given cause.
	 *
	 * @param exception the underlying cause of this error
	 */
	public NaftahBugError(Throwable exception) {
		this(null, exception);
	}

	/**
	 * Constructs a {@code NaftahBugError} using both a descriptive message
	 * and an underlying exception cause.
	 *
	 * @param msg       the error message describing the bug
	 * @param exception the underlying cause of this error
	 */
	public NaftahBugError(String msg, Throwable exception) {
		this(msg, exception, -1, -1);
	}

	/**
	 * Constructs a {@code NaftahBugError} using a descriptive message
	 * and an underlying exception cause and the line number along with the column.
	 *
	 * @param msg       the error message describing the bug
	 * @param exception the underlying cause of this error
	 * @param line      The line number on which the 1st character of this token was matched
	 * @param column    The index of the first character of this token relative to the beginning of the line at which
	 *                  it occurs
	 */
	public NaftahBugError(String msg, Throwable exception, int line, int column) {
		this.exception = exception;
		this.message = msg;
		this.line = line;
		this.column = column;
	}

	/**
	 * Returns the string representation of this error,
	 * which delegates to {@link #getMessage()}.
	 *
	 * @return the error message
	 */
	@Override
	public String toString() {
		return getMessage();
	}

	/**
	 * Returns the detailed error message. If a message was provided during
	 * construction, it will be returned prefixed with {@code "خطأ برمجي!"}.
	 * Otherwise, it will return the message from the exception cause, prefixed with
	 * {@code "خطأ برمجي! استثناء غير ملتقط: "} and localized using
	 * {@link ExceptionLocalizer#localizeException(Throwable)}.
	 *
	 * @return the formatted bug message
	 */
	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("💥 خطأ برمجي!\n");

		if (line != -1 && column != -1) {
			builder.append("📍 السطر: %d، العمود: %d%n".formatted(line, column));
		}

		builder.append(doGetMessage());

		return builder.toString();
	}

	/**
	 * Constructs the internal message based on the presence of a user-defined message
	 * and an optional exception.
	 *
	 * @return the constructed error detail string
	 */
	private String doGetMessage() {
		if (message != null) {
			if (exception != null) {
				return "%s استثناء ملتقط: %s"
						.formatted(message, ExceptionLocalizer.localizeException(exception));
			}
			return message;
		}

		return "استثناء غير ملتقط: " + ExceptionLocalizer.localizeException(exception);
	}

	/**
	 * Returns the underlying cause of this bug error, if available.
	 *
	 * @return the exception that caused this bug, or {@code null} if none
	 */
	@Override
	public Throwable getCause() {
		return this.exception;
	}

	/**
	 * Returns the raw bug message text without formatting or localization.
	 *
	 * @return the original bug message if set, otherwise the cause's message
	 */
	public String getBugText() {
		if (message != null) {
			return message;
		}
		else {
			return exception.getMessage();
		}
	}

	/**
	 * Updates the descriptive bug message.
	 *
	 * @param msg the new bug message
	 */
	public void setBugText(String msg) {
		this.message = msg;
	}
}
