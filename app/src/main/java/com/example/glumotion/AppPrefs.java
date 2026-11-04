package com.example.glumotion;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small settings saved on the phone with SharedPreferences
 * (fast to read, work without internet): glucose unit, reminders, step counter.
 */
public class AppPrefs {

    private static final String FILE = "glumotion_prefs";

    public static final String UNIT_MG = "mg/dL";
    public static final String UNIT_MMOL = "mmol/L";

    // Opens the preferences file of the app
    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    // ---------- Glucose unit ----------

    // Reads the glucose unit (mg/dL by default)
    public static String getGlucoseUnit(Context context) {
        return prefs(context).getString("glucose_unit", UNIT_MG);
    }

    // Saves the glucose unit
    public static void setGlucoseUnit(Context context, String unit) {
        prefs(context).edit().putString("glucose_unit", unit).apply();
    }

    // Shows a mg/dL value in the unit the user chose: 126 → "126" or "7.0"
    public static String formatGlucose(Context context, int mgdl) {
        if (getGlucoseUnit(context).equals(UNIT_MMOL)) {
            double mmol = Math.round(mgdl / 18.0 * 10) / 10.0;
            return String.valueOf(mmol);
        }
        return String.valueOf(mgdl);
    }

    // ---------- Reminders ----------

    // true if the daily reminders are on
    public static boolean isRemindersOn(Context context) {
        return prefs(context).getBoolean("reminders_on", false);
    }

    // Turns the daily reminders on / off
    public static void setRemindersOn(Context context, boolean on) {
        prefs(context).edit().putBoolean("reminders_on", on).apply();
    }

    // Reminder time saved as minutes from midnight (8:30 = 510)
    public static int getReminderMinutes(Context context, String key, int defaultMinutes) {
        return prefs(context).getInt("reminder_" + key, defaultMinutes);
    }

    // Saves the time of a reminder (minutes from midnight)
    public static void setReminderMinutes(Context context, String key, int minutes) {
        prefs(context).edit().putInt("reminder_" + key, minutes).apply();
    }

    // ---------- Step counter (the phone counts steps since it was turned on) ----------

    // Day of the saved step counter start
    public static String getStepsBaseDay(Context context) {
        return prefs(context).getString("steps_base_day", "");
    }

    // Step counter value at the start of that day
    public static float getStepsBase(Context context) {
        return prefs(context).getFloat("steps_base", 0);
    }

    // Saves "the counter value at the start of today"
    public static void setStepsBase(Context context, String day, float base) {
        prefs(context).edit().putString("steps_base_day", day).putFloat("steps_base", base).apply();
    }

    // ---------- Training now? (the coach gets the sensor alerts only during training) ----------

    // true while the workout page is open
    public static boolean isInWorkout(Context context) {
        return prefs(context).getBoolean("in_workout", false);
    }

    // Saves if the user is training now
    public static void setInWorkout(Context context, boolean inWorkout) {
        prefs(context).edit().putBoolean("in_workout", inWorkout).apply();
    }

    // ---------- Last emergency alert (so the same danger doesn't send 10 SMS) ----------

    // Time of the last emergency alert
    public static long getLastEmergencyTime(Context context) {
        return prefs(context).getLong("last_emergency", 0);
    }

    // Saves the time of the last emergency alert
    public static void setLastEmergencyTime(Context context, long time) {
        prefs(context).edit().putLong("last_emergency", time).apply();
    }
}
