package org.daiitech.naftah.parser.time;


import java.time.Duration;
import java.time.Period;

import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.time.ArabicDate;
import org.daiitech.naftah.builtin.time.ArabicDateTime;
import org.daiitech.naftah.builtin.time.ArabicDuration;
import org.daiitech.naftah.builtin.time.ArabicPeriod;
import org.daiitech.naftah.builtin.time.ArabicPeriodWithDuration;
import org.daiitech.naftah.builtin.time.ArabicTemporal;
import org.daiitech.naftah.builtin.time.ArabicTemporalAmount;
import org.daiitech.naftah.builtin.time.ArabicTemporalPoint;
import org.daiitech.naftah.builtin.time.ArabicTime;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.parser.ArabicDateParser;
import org.daiitech.naftah.parser.ArabicDateParserBaseVisitor;
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
 * <p>This class extends {@link ArabicDateParserBaseVisitor} and provides implementations for
 * converting parse tree nodes into corresponding {@link ArabicTemporal} objects, such as
 * {@link ArabicDate}, {@link ArabicTime}, and {@link ArabicDateTime}.</p>
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
public class DefaultArabicDateParserVisitor extends ArabicDateParserBaseVisitor<Object> {

	private final ArabicDateParser parser;

	public DefaultArabicDateParserVisitor(ArabicDateParser parser) {
		this.parser = parser;
	}

	/**
	 * Starts the visiting process by parsing the program and
	 * visiting the resulting parse tree.
	 *
	 * @return the result of visiting the parse tree
	 */
	public ArabicTemporal visit() {
		// Parse the input and get the parse tree
		ParseTree tree = parser.root();
		return (ArabicTemporal) visit(tree);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTemporalPoint visitNow(ArabicDateParser.NowContext ctx) {
		return (ArabicTemporalPoint) visit(ctx.nowSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTemporalPoint visitNowAsDateTime(ArabicDateParser.NowAsDateTimeContext ctx) {
		ArabicDate date = ArabicDateParserHelper
				.currentDate(   this,
								ctx.calendarSpecifier(),
								ctx.zoneOrOffsetSpecifier());


		ArabicTime time = ArabicDateParserHelper.currentTime(this, ctx.zoneOrOffsetSpecifier());

		return ArabicDateTime
				.of(
					date,
					time,
					TemporalUtils
							.createDateTime(date.date(),
											date.calendar(),
											time.time(),
											time.zoneOrOffset())
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicDate visitNowAsDate(ArabicDateParser.NowAsDateContext ctx) {
		return ArabicDateParserHelper.currentDate(this, ctx.calendarSpecifier(), ctx.zoneOrOffsetSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTime visitNowAsTime(ArabicDateParser.NowAsTimeContext ctx) {
		return ArabicDateParserHelper.currentTime(this, ctx.zoneOrOffsetSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTemporalPoint visitDateTime(ArabicDateParser.DateTimeContext ctx) {
		return (ArabicTemporalPoint) visit(ctx.dateTimeSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTime visitTime(ArabicDateParser.TimeContext ctx) {
		return (ArabicTime) visit(ctx.zonedOrOffsetTimeSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTemporalPoint visitDateTimeSpecifier(ArabicDateParser.DateTimeSpecifierContext ctx) {
		ArabicDate date = (ArabicDate) visit(ctx.dateSpecifier());

		if (NaftahParserHelper.hasChild(ctx.zonedOrOffsetTimeSpecifier())) {
			ArabicTime time = (ArabicTime) visit(ctx.zonedOrOffsetTimeSpecifier());

			return ArabicDateTime
					.of(
						date,
						time,
						TemporalUtils
								.createDateTime(date.date(),
												date.calendar(),
												time.time(),
												time.zoneOrOffset())
					);
		}
		return date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicDate visitDateSpecifier(ArabicDateParser.DateSpecifierContext ctx) {
		ArabicDate.Calendar calendar = NaftahParserHelper.hasChild(ctx.calendarSpecifier()) ?
				(ArabicDate.Calendar) visit(ctx.calendarSpecifier()) :
				ArabicDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);

		int day = NumberUtils.parseDynamicNumber(ctx.NUMBER(0).getText()).intValue();
		String arabicMonth = ctx.MONTH_NAME().getText();
		int year = NumberUtils.parseDynamicNumber(ctx.NUMBER(1).getText()).intValue();
		var date = ArabicDate.Date.of(day, arabicMonth, calendar.chronology(), year);

		return ArabicDate
				.of(date,
					calendar,
					TemporalUtils.createDate(day, date.monthValue(), year, calendar.chronology())
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTime visitZonedOrOffsetTimeSpecifier(ArabicDateParser.ZonedOrOffsetTimeSpecifierContext ctx) {
		ArabicTime.Time time = (ArabicTime.Time) visit(ctx.timeSpecifier());

		ArabicTime.ZoneOrOffset zoneOrOffset = NaftahParserHelper.hasChild(ctx.zoneOrOffsetSpecifier()) ?
				(ArabicTime.ZoneOrOffset) visit(ctx.zoneOrOffsetSpecifier()) :
				null;

		return ArabicTime
				.of(time,
					zoneOrOffset,
					TemporalUtils
							.createTime(
										time,
										zoneOrOffset
							)
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTime.Time visitTimeSpecifier(ArabicDateParser.TimeSpecifierContext ctx) {
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

		return ArabicTime.Time.of(hour, minute, second, nano, isPM);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTime.ZoneOrOffset visitZoneSpecifier(ArabicDateParser.ZoneSpecifierContext ctx) {
		return ArabicTime.ZoneOrOffset.ofZone(ctx.ARABIC_WORDS().getText());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTime.ZoneOrOffset visitOffsetSpecifier(ArabicDateParser.OffsetSpecifierContext ctx) {
		return ArabicTime.ZoneOrOffset.ofOffset(ctx.OFFSET().getText());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicDate.Calendar visitCalendarSpecifier(ArabicDateParser.CalendarSpecifierContext ctx) {
		return ArabicDate.Calendar
				.of(ctx.ARABIC_WORDS().getText(),
					ChronologyUtils.getChronologyByName(ctx.ARABIC_WORDS().getText()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTemporalAmount visitPeriodWithDuration(ArabicDateParser.PeriodWithDurationContext ctx) {
		ArabicPeriod arabicPeriod = (ArabicPeriod) visit(ctx.periodSpecifier());

		if (NaftahParserHelper.hasChild(ctx.timeAmount())) {
			ArabicDuration arabicDuration = (ArabicDuration) visit(ctx.timeAmount());

			return ArabicPeriodWithDuration.of(arabicPeriod, arabicDuration);
		}

		return arabicPeriod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicDuration visitDuration(ArabicDateParser.DurationContext ctx) {
		return (ArabicDuration) visit(ctx.durationSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTemporalAmount visitBetweenTemporalAmount(ArabicDateParser.BetweenTemporalAmountContext ctx) {
		return (ArabicTemporalAmount) visit(ctx.betweenSpecifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicDuration visitDurationSpecifier(ArabicDateParser.DurationSpecifierContext ctx) {
		return (ArabicDuration) visit(ctx.timeAmount());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicDuration visitTimeAmount(ArabicDateParser.TimeAmountContext ctx) {
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


		return ArabicDuration
				.of(
					ArabicDuration.DurationDefinition
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
	public ArabicPeriod visitPeriodSpecifier(ArabicDateParser.PeriodSpecifierContext ctx) {
		return (ArabicPeriod) visit(ctx.dateAmount());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicPeriod visitDateAmount(ArabicDateParser.DateAmountContext ctx) {
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


		return ArabicPeriod
				.of(
					ArabicPeriod.PeriodDefinition.of(years, yearText, months, monthText, days, dayText),
					Period.of(years, months, days)
				);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTemporalAmount visitBetweenSpecifier(ArabicDateParser.BetweenSpecifierContext ctx) {
		ArabicTemporalPoint left = (ArabicTemporalPoint) visit(ctx.betweenTimeSpecifier(0));
		ArabicTemporalPoint right = (ArabicTemporalPoint) visit(ctx.betweenTimeSpecifier(1));
		return ArabicDateParserHelper.getArabicTemporalAmountBetween(left, right);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArabicTemporalPoint visitBetweenTimeSpecifier(ArabicDateParser.BetweenTimeSpecifierContext ctx) {
		ArabicTemporalPoint result;
		if (NaftahParserHelper.hasChild(ctx.nowSpecifier())) {
			result = (ArabicTemporalPoint) visit(ctx.nowSpecifier());
		}
		else if (NaftahParserHelper.hasChild(ctx.dateTimeSpecifier())) {
			result = (ArabicTemporalPoint) visit(ctx.dateTimeSpecifier());
		}
		else {
			result = (ArabicTemporalPoint) visit(ctx.zonedOrOffsetTimeSpecifier());
		}
		return result;
	}
}
