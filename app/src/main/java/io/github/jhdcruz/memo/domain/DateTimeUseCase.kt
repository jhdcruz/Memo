package io.github.jhdcruz.memo.domain

import android.content.Context
import android.text.format.DateFormat
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

fun LocalDateTime.toTimestamp(): Timestamp {
    // Convert LocalDateTime to Instant
    val instant = this.atZone(ZoneId.systemDefault()).toInstant()

    // Convert Instant to java.util.Date
    val date = Date.from(instant)

    // Convert Date to Timestamp
    return Timestamp(date)
}

fun Timestamp.toLocalDateTime(): LocalDateTime {
    // Convert Timestamp to Date
    val date = toDate()

    // Convert Date to LocalDateTime
    return date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun Timestamp.format(context: Context): String {
    // check if system uses 24 hour time format

    val is24Hour = DateFormat.is24HourFormat(context)
    val formatter =
        if (is24Hour) {
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
        } else {
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")
        }

    return this.toLocalDateTime().format(formatter)
}

fun createTimestamp(
    millis: Long,
    hour: Int? = null,
    minute: Int? = null,
): Timestamp {
    // Convert system milliseconds to LocalDateTime
    var localDateTime =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()

    if (hour != null && minute != null) {
        // Adjust the hour and minute of LocalDateTime
        localDateTime = localDateTime.withHour(hour).withMinute(minute)
    }

    // Convert LocalDateTime to Instant
    val instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()

    // Convert Date to Timestamp
    return Timestamp(Date.from(instant))
}

fun Timestamp.dateUntil(): String {
    val dueDate = this.toLocalDateTime()

    // for some reason its 1 minute advanced
    val currentDate = LocalDateTime.now().minusMinutes(1)

    val months = ChronoUnit.MONTHS.between(currentDate, dueDate)
    val days = ChronoUnit.DAYS.between(currentDate, dueDate)
    val hours = ChronoUnit.HOURS.between(currentDate, dueDate)
    val minutes = ChronoUnit.MINUTES.between(currentDate, dueDate)

    return when {
        minutes in 1..59 -> "${minutes}m"
        hours in 1..23 -> "${hours}H"
        days in 1..30 -> "${days}D"
        months in 1..12 -> "${months}M"
        currentDate > dueDate -> "Overdue"
        else -> "Now"
    }
}
