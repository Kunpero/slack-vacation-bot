package rs.kunpero.vacation.util;

import java.time.LocalDate;
import java.util.List;

public class VacationUtils {

    public static boolean isWithinRange(LocalDate date, LocalDate from, LocalDate to) {
        return !(date.isBefore(from) || date.isAfter(to));
    }

    public static String convertListToString(List<String> list) {
        return String.join(",", list);
    }
}
