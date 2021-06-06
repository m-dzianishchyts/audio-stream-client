package by.bsuir.audio_stream.client.util;

public final class TimeFormatUtils {

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MILLISECONDS_PER_SECOND = 1000;

    private TimeFormatUtils() {
    }

    public static String convertSecondsToMinutesAndSeconds(long seconds) {
        long minuteOfHour = seconds / SECONDS_PER_MINUTE;
        long secondOfMinute = minuteOfHour > 0 ? seconds % SECONDS_PER_MINUTE : seconds;
        String minutesAndSeconds = String.format("%d:%02d", minuteOfHour, secondOfMinute);
        return minutesAndSeconds;
    }

    public static String convertMillisecondsToMinutesAndSeconds(long milliseconds) {
        long seconds = milliseconds / MILLISECONDS_PER_SECOND;
        String minutesAndSeconds = convertSecondsToMinutesAndSeconds(seconds);
        return minutesAndSeconds;
    }
}
