package testutils;

import java.time.LocalDateTime;

/**
 * Мок-класс для управления временем в тестах.
 * Позволяет задавать и получать искусственное "текущее" время.
 */
public class MockTimeProvider {

//    private LocalDateTime currentTime;
    private static LocalDateTime fixedTime = null;

    public static void setFixedTime(LocalDateTime dateTime) {
        fixedTime = dateTime;
    }

    /**
     * Очищает фиксацию времени (возвращает реальное время).
     */
    public static void clear() {
        fixedTime = null;
    }

    /**
     * Возвращает либо заданное время, либо текущее.
     */
    public static LocalDateTime now() {
        return fixedTime != null ? fixedTime : LocalDateTime.now();
    }
    /**
     * Устанавливает текущее время в ISO-формате, например: "2025-01-01T00:00:00"
     */
//    public void setCurrentTime(String isoDateTime) {
//        this.currentTime = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
//        MockTimeProvider mockTime = new MockTimeProvider();
//        mockTime.setCurrentTime("2023-01-01T09:00:00");

//    }

    /**
     * Возвращает текущее заданное время, либо реальное время, если не задано.
     */
//    public LocalDateTime getCurrentTime() {
//
//        return currentTime != null ? currentTime : LocalDateTime.now();
//    }

}
