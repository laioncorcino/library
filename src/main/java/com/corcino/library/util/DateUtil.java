package com.corcino.library.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil {

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(localDateTime);
    }

}
