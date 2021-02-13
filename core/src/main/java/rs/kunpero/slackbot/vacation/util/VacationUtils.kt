package rs.kunpero.slackbot.vacation.util

import java.time.LocalDate

class VacationUtils {
    companion object {
        @JvmStatic
        fun isWithinRange(date: LocalDate, from: LocalDate, to: LocalDate): Boolean {
            return !(date.isBefore(from) || date.isAfter(to))
        }

        @JvmStatic
        fun convertListToString(list: List<String>?): String {
            if (list.isNullOrEmpty()) {
                return String()
            }
            return list.distinct().joinToString(",")
        }

        @JvmStatic
        fun wrapIntoInlineMarkdown(value: String):String {
            return String.format("`%s`", value)
        }
    }
}