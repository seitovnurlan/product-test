package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Мок-класс для управления временем в тестах.
 * Позволяет задавать и получать искусственное "текущее" время.
 */
public class MockTimeProvider {

    private LocalDateTime currentTime;

    /**
     * Устанавливает текущее время в ISO-формате, например: "2025-01-01T00:00:00"
     */
    public void setCurrentTime(String isoDateTime) {
        this.currentTime = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * Возвращает текущее заданное время, либо реальное время, если не задано.
     */
    public LocalDateTime getCurrentTime() {
        return currentTime != null ? currentTime : LocalDateTime.now();
    }
}
