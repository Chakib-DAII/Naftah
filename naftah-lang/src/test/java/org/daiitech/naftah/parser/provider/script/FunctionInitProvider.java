package org.daiitech.naftah.parser.provider.script;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocableNotFoundError;


public class FunctionInitProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments
						.of(false,
							"""
							جافا:لغة:ابكديفجهجكلمنوبكرستيفوكسز()
							""",
							null,
							newNaftahInvocableNotFoundError("جافا:لغة:ابكديفجهجكلمنوبكرستيفوكسز",
															1,
															0)),
					Arguments
							.of(true,
								"""
								جافا:لغة:كائن() لا_يساوي فارغ
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								@جافا:لغة:كائن() لا_يساوي فارغ
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								ثابت معرف_مستخدم : جافا:أدة:معرف_مستخدم تعيين جافا:أدة:معرف_مستخدم(6161415689025233999؛5488940234982179551)
								معرف_مستخدم
								""",
								NaftahObject.of(new UUID(6161415689025233999L, 5488940234982179551L)),
								null),
					Arguments
							.of(true,
								"""
								أورغ:داعيتاك:نفطة:مدرجة_مدرجة:لغة:ديناميك_رقم(1)
								""",
								NaftahObject.of(DynamicNumber.of(1)),
								null),
					Arguments
							.of(true,
								"""
								@أورغ:داعيتاك:نفطة:مدرجة_مدرجة:لغة:ديناميك_رقم("01033301")
								""",
								NaftahObject.of(DynamicNumber.of("01033301")),
								null),
					Arguments
							.of(true,
								"""
								جافا:أدة:صفائف_القائمة()
								""",
								NaftahObject.of(new ArrayList<>()),
								null),
					Arguments
							.of(true,
								"""
								جافا:أدة:صفائف_القائمة(11000)
								""",
								NaftahObject.of(new ArrayList<>(11000)),
								null),
					Arguments
							.of(true,
								"""
								@جافا:أدة:صفائف_القائمة([1؛100؛0])
								""",
								NaftahObject.of(new ArrayList<>(List.of(1, 100, 0))),
								null));
	}
}
