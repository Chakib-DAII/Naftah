package org.daiitech.naftah.parser.time;


import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.time.ArabicDate;
import org.daiitech.naftah.builtin.time.ArabicDateTime;
import org.daiitech.naftah.builtin.time.ArabicTemporal;
import org.daiitech.naftah.builtin.time.ArabicTemporalPoint;
import org.daiitech.naftah.builtin.time.ArabicTime;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.parser.ArabicDateParser;
import org.daiitech.naftah.parser.ArabicDateParserBaseVisitor;
import org.daiitech.naftah.parser.NaftahParserHelper;
import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;

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
	public ArabicTime visitTime(ArabicDateParser.TimeContext ctx) {
		return (ArabicTime) visit(ctx.zonedOrOffsetTimeSpecifier());
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
		int hour = NumberUtils.parseDynamicNumber(ctx.NUMBER(0).getText()).intValue();
		int minute = NumberUtils.parseDynamicNumber(ctx.NUMBER(1).getText()).intValue();

		Integer second = NaftahParserHelper.hasChild(ctx.NUMBER(2)) ?
				NumberUtils.parseDynamicNumber(ctx.NUMBER(2).getText()).intValue() :
				null;
		Integer nano = NaftahParserHelper.hasChild(ctx.NUMBER(3)) ?
				NumberUtils.parseDynamicNumber(ctx.NUMBER(3).getText()).intValue() :
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
}
