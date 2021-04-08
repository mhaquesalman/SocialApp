package com.salman.socialapp.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AgoDateParser {

   companion object {

       private const val SECOND_MILLIS: Long = 1000
       private const val MINUTE_MILLIS: Long = 60 * SECOND_MILLIS
       private const val HOUR_MILLIS: Long = 60 * MINUTE_MILLIS
       private const val DAY_MILLIS: Long = 24 * HOUR_MILLIS
       private const val MONTHS_MILLIS: Long = 30 * DAY_MILLIS
       private const val YEARS_MILLIS: Long = 12 * MONTHS_MILLIS

       fun calculateAgo(dateTime: Long): String {
           var time = dateTime
           if (time < 1000000000000L) {
               // if timestamp given in seconds, convert to millis
               time *= 1000
           }
           val now = System.currentTimeMillis()
           if (time > now || time <= 0) {
               return ""
           }

           val diff = now - time
           return when {
               diff < MINUTE_MILLIS -> {
                   "just now"
               }
               diff < 2 * MINUTE_MILLIS -> {
                   "a min ago"
               }
               diff < 50 * MINUTE_MILLIS -> {
                   "" + diff / MINUTE_MILLIS + " mins ago"
               }
               diff < 90 * MINUTE_MILLIS -> {
                   "an hour ago"
               }
               diff < 24 * HOUR_MILLIS -> {
                   "" + diff / HOUR_MILLIS + " hours ago"
               }
               diff < 48 * HOUR_MILLIS -> {
                   "yesterday"
               }
               diff < 30 * DAY_MILLIS -> {
                   "" + diff / DAY_MILLIS + " days ago"
               }
               diff < 12 * MONTHS_MILLIS -> {
                   "" + diff / MONTHS_MILLIS + " months ago"
               }
               else -> {
                   "" + diff / YEARS_MILLIS + " yeas ago"
               }
           }
       }

       @Throws(ParseException::class)
       fun getTimeAgo(givenDate: String?): String {
           val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
           val date: Date = sdf.parse(givenDate)
           return calculateAgo(date.getTime())
       }

   }

}