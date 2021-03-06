package com.dxy.android.statistics.util;

import java.util.Calendar;

/**
 * Simple Date and time utils.
 * brokge@gmail.com
 */
public class DateUtils {
    /**
     * Calendar objects are rather expensive: for heavy usage it's a good idea to use a single instance per thread
     * instead of calling Calendar.getInstance() multiple times. Calendar.getInstance() creates a new instance each
     * time.
     */
    public static final class DefaultCalendarThreadLocal extends ThreadLocal<Calendar> {
        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }
    }

    private static ThreadLocal<Calendar> calendarThreadLocal = new DefaultCalendarThreadLocal();

    public static long getTimeForDay(int year, int month, int day) {
        return getTimeForDay(calendarThreadLocal.get(), year, month, day);
    }

    /**
     * @param calendar helper object needed for conversion
     */
    public static long getTimeForDay(Calendar calendar, int year, int month, int day) {
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTimeInMillis();
    }

    /**
     * Sets hour, minutes, seconds and milliseconds to the given values. Leaves date info untouched.
     */
    public static void setTime(Calendar calendar, int hourOfDay, int minute, int second, int millisecond) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
    }

    /**
     * Readable yyyyMMdd int representation of a day, which is also sortable.
     */
    public static int getDayAsReadableInt(long time) {
        Calendar cal = calendarThreadLocal.get();
        cal.setTimeInMillis(time);
        return getDayAsReadableInt(cal);
    }

    /**
     * Readable yyyyMMdd int representation of a day, which is also sortable.
     */
    public static String getDayAsReadableString(long time) {
        Calendar cal = calendarThreadLocal.get();
        cal.setTimeInMillis(time);
        return getDayAsReadableString(cal);
    }

    /**
     * Readable yyyyMMdd representation of a day, which is also sortable.
     */
    public static int getDayAsReadableInt(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        return year * 10000 + month * 100 + day + hour;
    }

    /**
     * Readable yyyyMMdd representation of a day, which is also sortable.
     */
    public static String getDayAsReadableString(Calendar calendar) {
        int millsecodes = calendar.get(Calendar.MILLISECOND);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(year);
        stringBuilder.append(month);
        stringBuilder.append(day);
        stringBuilder.append(hour);
        stringBuilder.append(minute);
        stringBuilder.append(millsecodes);
        return stringBuilder.toString();
    }

    /**
     * Returns midnight of the given day.
     */
    public static long getTimeFromDayReadableInt(int day) {
        return getTimeFromDayReadableInt(calendarThreadLocal.get(), day, 0);
    }

    /**
     * @param calendar helper object needed for conversion
     */
    public static long getTimeFromDayReadableInt(Calendar calendar, int readableDay, int hour) {
        int day = readableDay % 100;
        int month = readableDay / 100 % 100;
        int year = readableDay / 10000;

        calendar.clear(); // We don't set all fields, so we should clear the calendar first
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.YEAR, year);

        return calendar.getTimeInMillis();
    }

    public static int getDayDifferenceOfReadableInts(int dayOfBroadcast1, int dayOfBroadcast2) {
        long time1 = getTimeFromDayReadableInt(dayOfBroadcast1);
        long time2 = getTimeFromDayReadableInt(dayOfBroadcast2);

        // Don't use getDayDifference(time1, time2) here, it's wrong for some days.
        // Do float calculation and rounding at the end to cover daylight saving stuff etc.
        float daysFloat = (time2 - time1) / 1000 / 60 / 60 / 24f;
        return Math.round(daysFloat);
    }

    public static int getDayDifference(long time1, long time2) {
        return (int) ((time2 - time1) / 1000 / 60 / 60 / 24);
    }

    public static long addDays(long time, int days) {
        Calendar calendar = calendarThreadLocal.get();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTimeInMillis();
    }

    public static void addDays(Calendar calendar, int days) {
        calendar.add(Calendar.DAY_OF_YEAR, days);
    }

    /**
     * 获取时间戳
     *
     * @return 返回时间戳的string类型
     */
    public static String getTimeStamp() {
        Long tsLong = System.currentTimeMillis();
        return DateUtils.getDayAsReadableString(tsLong);
    }
}
