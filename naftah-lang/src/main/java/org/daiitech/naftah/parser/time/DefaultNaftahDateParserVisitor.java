// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.time;


import java.time.Duration;
import java.time.Period;

import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.time.NaftahDate;
import org.daiitech.naftah.builtin.time.NaftahDateTime;
import org.daiitech.naftah.builtin.time.NaftahDuration;
import org.daiitech.naftah.builtin.time.NaftahPeriod;
import org.daiitech.naftah.builtin.time.NaftahPeriodWithDuration;
import org.daiitech.naftah.builtin.time.NaftahTemporal;
import org.daiitech.naftah.builtin.time.NaftahTemporalAmount;
import org.daiitech.naftah.builtin.time.NaftahTemporalPoint;
import org.daiitech.naftah.builtin.time.NaftahTime;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.parser.NaftahDateParser;
import org.daiitech.naftah.parser.NaftahDateParserBaseVisitor;
import org.daiitech.naftah.parser.NaftahParserHelper;
import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;

import static org.daiitech.naftah.utils.time.Constants.DAY;
import static org.daiitech.naftah.utils.time.Constants.HOUR;
import static org.daiitech.naftah.utils.time.Constants.MINUTE;
import static org.daiitech.naftah.utils.time.Constants.MONTH;
import static org.daiitech.naftah.utils.time.Constants.NANOSECOND;
import static org.daiitech.naftah.utils.time.Constants.SECOND;
import static org.daiitech.naftah.utils.time.Constants.YEAR;

/**
 * A default visitor implementation for parsing Arabic date and time expressions.
 *
 * <p>This class extends {@link NaftahDateParserBaseVisitor} and provides implementations for
 * converting parse tree nodes into corresponding {@link NaftahTemporal} objects, such as
 * {@link NaftahDate}, {@link NaftahTime}, and {@link NaftahDateTime}.</p>
 *
 * <p>It handles:
 * <ul>
 * <li>Date and calendar specifiers</li>
 * <li>Time with optional AM/PM markers</li>
 * <li>Time zones or offsets</li>
 * <li>Integration of date and time into a single temporal object</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 */
public class DefaultNaftahDateParserVisitor extends NaftahDateParserBaseVisitor<Object> {

	private final NaftahDateParser parser;

	public DefaultNaftahDateParserVisitor(NaftahDateParser parser) {
		this.parser = parser;
	}

	/**
	 * Starts the visiting process by parsing the program and
	 * visiting the resulting parse tree.
	 *
	 * @return the result of visiting the parse tree
	 */
	public NaftahTemporal visit() {
		// Parse the input and get the parse tree
		ParseTree tree = parser.root();
		return (NaftahTemporal) visit(tree);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTemporalPoint visitNow(NaftahDateParser.NowContext ctx) {
		return (NaftahTemporalPoint) visit(ctx.nowSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTemporalPoint visitNowAsDateTime(NaftahDateParser.NowAsDateTimeContext ctx) {
		NaftahDate date = NaftahDateParserHelper
				.currentDate(   this,
								ctx.calendarSpecifier(),
								ctx.zoneOrOffsetSpecifier());


		NaftahTime time = NaftahDateParserHelper.currentTime(this, ctx.zoneOrOffsetSpecifier());

		return NaftahDateTime
				.of(
					date,
					time
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahDate visitNowAsDate(NaftahDateParser.NowAsDateContext ctx) {
		return NaftahDateParserHelper.currentDate(this, ctx.calendarSpecifier(), ctx.zoneOrOffsetSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTime visitNowAsTime(NaftahDateParser.NowAsTimeContext ctx) {
		return NaftahDateParserHelper.currentTime(this, ctx.zoneOrOffsetSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTemporalPoint visitDateTime(NaftahDateParser.DateTimeContext ctx) {
		return (NaftahTemporalPoint) visit(ctx.dateTimeSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTime visitTime(NaftahDateParser.TimeContext ctx) {
		return (NaftahTime) visit(ctx.zonedOrOffsetTimeSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTemporalPoint visitDateTimeSpecifier(NaftahDateParser.DateTimeSpecifierContext ctx) {
		NaftahDate date = (NaftahDate) visit(ctx.dateSpecifier());

		if (NaftahParserHelper.hasChild(ctx.zonedOrOffsetTimeSpecifier())) {
			NaftahTime time = (NaftahTime) visit(ctx.zonedOrOffsetTimeSpecifier());

			return NaftahDateTime
					.of(
						date,
						time
					);
		}
		return date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahDate visitDateSpecifier(NaftahDateParser.DateSpecifierContext ctx) {
		NaftahDate.Calendar calendar = NaftahParserHelper.hasChild(ctx.calendarSpecifier()) ?
				(NaftahDate.Calendar) visit(ctx.calendarSpecifier()) :
				NaftahDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);

		int day = NumberUtils.parseDynamicNumber(ctx.NUMBER(0).getText()).intValue();
		String arabicMonth = ctx.MONTH_NAME().getText();
		int year = NumberUtils.parseDynamicNumber(ctx.NUMBER(1).getText()).intValue();
		var date = NaftahDate.Date.of(day, arabicMonth, calendar.chronology(), year);

		return NaftahDate
				.of(date,
					calendar
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTime visitZonedOrOffsetTimeSpecifier(NaftahDateParser.ZonedOrOffsetTimeSpecifierContext ctx) {
		NaftahTime.Time time = (NaftahTime.Time) visit(ctx.timeSpecifier());

		NaftahTime.ZoneOrOffset zoneOrOffset = NaftahParserHelper.hasChild(ctx.zoneOrOffsetSpecifier()) ?
				(NaftahTime.ZoneOrOffset) visit(ctx.zoneOrOffsetSpecifier()) :
				null;

		return NaftahTime
				.of(time,
					zoneOrOffset
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTime.Time visitTimeSpecifier(NaftahDateParser.TimeSpecifierContext ctx) {
		int numberIndex = 0;
		int hour = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex++).getText()).intValue();
		int minute = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex++).getText()).intValue();

		Integer second = NaftahParserHelper.hasChild(ctx.NUMBER(numberIndex)) ?
				NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex++).getText()).intValue() :
				null;
		Integer nano = NaftahParserHelper.hasChild(ctx.NUMBER(numberIndex)) ?
				TemporalUtils.parseFractionToNanos(ctx.NUMBER(numberIndex).getText()) :
				null;
		Boolean isPM = NaftahParserHelper.hasChild(ctx.AMPM()) ?
				TemporalUtils.isPM(ctx.AMPM().getText()) :
				null;

		return NaftahTime.Time.of(hour, minute, second, nano, isPM);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTime.ZoneOrOffset visitZoneSpecifier(NaftahDateParser.ZoneSpecifierContext ctx) {
		return NaftahTime.ZoneOrOffset.ofZone(ctx.ARABIC_WORDS().getText());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTime.ZoneOrOffset visitOffsetSpecifier(NaftahDateParser.OffsetSpecifierContext ctx) {
		return NaftahTime.ZoneOrOffset.ofOffset(ctx.OFFSET().getText());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahDate.Calendar visitCalendarSpecifier(NaftahDateParser.CalendarSpecifierContext ctx) {
		return NaftahDate.Calendar
				.of(ctx.ARABIC_WORDS().getText(),
					ChronologyUtils.getChronologyByName(ctx.ARABIC_WORDS().getText()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTemporalAmount visitPeriodWithDuration(NaftahDateParser.PeriodWithDurationContext ctx) {
		NaftahPeriod naftahPeriod = (NaftahPeriod) visit(ctx.periodSpecifier());

		if (NaftahParserHelper.hasChild(ctx.timeAmount())) {
			NaftahDuration naftahDuration = (NaftahDuration) visit(ctx.timeAmount());

			return NaftahPeriodWithDuration.of(naftahPeriod, naftahDuration);
		}

		return naftahPeriod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahDuration visitDuration(NaftahDateParser.DurationContext ctx) {
		return (NaftahDuration) visit(ctx.durationSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTemporalAmount visitBetweenTemporalAmount(NaftahDateParser.BetweenTemporalAmountContext ctx) {
		return (NaftahTemporalAmount) visit(ctx.betweenSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahDuration visitDurationSpecifier(NaftahDateParser.DurationSpecifierContext ctx) {
		return (NaftahDuration) visit(ctx.timeAmount());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahDuration visitTimeAmount(NaftahDateParser.TimeAmountContext ctx) {
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		int millis = 0;
		int nanos = 0;
		String hourText = HOUR;
		String minuteText = MINUTE;
		String secondText = SECOND;
		String nanoText = NANOSECOND;
		int numberIndex = 0;
		if (NaftahParserHelper.hasChild(ctx.HOUR())) {
			hours = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex++).getText()).intValue();
			hourText = ctx.HOUR().getText();
		}
		if (NaftahParserHelper.hasChild(ctx.MINUTE())) {
			minutes = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex++).getText()).intValue();
			minuteText = ctx.MINUTE().getText();
		}
		if (NaftahParserHelper.hasChild(ctx.SECOND())) {
			seconds = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex++).getText()).intValue();
			secondText = ctx.SECOND().getText();

			if (NaftahParserHelper.hasChild(ctx.DOT())) {
				millis = TemporalUtils.parseMillisFraction(ctx.NUMBER(numberIndex++).getText());
			}
		}
		if (NaftahParserHelper.hasChild(ctx.NANOSECOND())) {
			nanos = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex).getText()).intValue();
			nanoText = ctx.NANOSECOND().getText();
		}


		return NaftahDuration
				.of(
					NaftahDuration.DurationDefinition
							.of(hours,
								hourText,
								minutes,
								minuteText,
								seconds,
								millis,
								secondText,
								nanos,
								nanoText),
					Duration
							.ofHours(hours)
							.plusMinutes(minutes)
							.plusSeconds(seconds)
							.plusMillis(millis)
							.plusNanos(nanos)
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahPeriod visitPeriodSpecifier(NaftahDateParser.PeriodSpecifierContext ctx) {
		return (NaftahPeriod) visit(ctx.dateAmount());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahPeriod visitDateAmount(NaftahDateParser.DateAmountContext ctx) {
		int years = 0;
		int months = 0;
		int days = 0;
		String yearText = YEAR;
		String monthText = MONTH;
		String dayText = DAY;
		int numberIndex = 0;
		if (NaftahParserHelper.hasChild(ctx.YEAR())) {
			years = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex++).getText()).intValue();
			yearText = ctx.YEAR().getText();
		}
		if (NaftahParserHelper.hasChild(ctx.MONTH())) {
			months = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex++).getText()).intValue();
			monthText = ctx.MONTH().getText();
		}
		if (NaftahParserHelper.hasChild(ctx.DAY())) {
			days = NumberUtils.parseDynamicNumber(ctx.NUMBER(numberIndex).getText()).intValue();
			dayText = ctx.DAY().getText();
		}


		return NaftahPeriod
				.of(
					NaftahPeriod.PeriodDefinition.of(years, yearText, months, monthText, days, dayText),
					Period.of(years, months, days)
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTemporalAmount visitBetweenSpecifier(NaftahDateParser.BetweenSpecifierContext ctx) {
		NaftahTemporalPoint left = (NaftahTemporalPoint) visit(ctx.betweenTimeSpecifier(0));
		NaftahTemporalPoint right = (NaftahTemporalPoint) visit(ctx.betweenTimeSpecifier(1));
		return NaftahDateParserHelper.getArabicTemporalAmountBetween(left, right);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NaftahTemporalPoint visitBetweenTimeSpecifier(NaftahDateParser.BetweenTimeSpecifierContext ctx) {
		NaftahTemporalPoint result;
		if (NaftahParserHelper.hasChild(ctx.nowSpecifier())) {
			result = (NaftahTemporalPoint) visit(ctx.nowSpecifier());
		}
		else if (NaftahParserHelper.hasChild(ctx.dateTimeSpecifier())) {
			result = (NaftahTemporalPoint) visit(ctx.dateTimeSpecifier());
		}
		else {
			result = (NaftahTemporalPoint) visit(ctx.zonedOrOffsetTimeSpecifier());
		}
		return result;
	}
}
