package com.tc.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeSplitter {
    public static List<LocalDateTime[]> splitByHour(Date startDate, Date endDate) {
        List<LocalDateTime[]> segments = new ArrayList<>();

        LocalDateTime start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 计算第一个整点时间
        LocalDateTime current = start.withMinute(0).withSecond(0).withNano(0);
        if (current.isBefore(start)) {
            current = current.plusHours(1);
        }

        while (current.isBefore(end)) {
            LocalDateTime next = current.plusHours(1);
            segments.add(new LocalDateTime[]{current, next.isAfter(end) ? end : next});
            current = next;
        }

        return segments;
    }

    public static void main(String[] args) {
        Date start = new Date(2024 - 1900, 1, 11, 10, 30);
        Date end = new Date(2024 - 1900, 1, 11, 14, 15);

        List<LocalDateTime[]> segments = splitByHour(start, end);

        for (LocalDateTime[] segment : segments) {
            System.out.println("From: " + segment[0] + " To: " + segment[1]);
        }
    }
}
