# Pickers
Pickers demo and library for Android.

The purpose here is to be copy-pasteable combined date time picker dialogs but also to
check important parts of the UI state machine used in other projects not ready for release.

This should be buildable in Android Studio with files given.

To use the DialogFragment in your own code:
```kotlin
fun showDateTimePickerDialog() {
	DateTimePickerFragment.newInstance(ldt)
	{ view, ldt -> ... }
	.show(...)
}
```

The non-dialog picker is also available for use - use a `com.moshy.pickers.DateTimePicker` element
in the layout xml and set the `onDateTimeChangedListener` callback in a manner similar to the
dialog example above. 

# What makes this special?
1. In the time picker, the seconds picker implements full rollover. What this means is advancing XX:YY:59 by a second will produce XX:(YY+1):0. This works for both minute and minute-to-hour cascade rollovers.
2. In the time picker, there is an option for a day change callback. This is very convenient for a combined date-time picker as one can update date by adding or removing a day as needed.
3. Seconds has been added to accessibility events.
4. Java 8 Time API (JSR-310). No need to deal with mutability hazards of Calendar and it's clearer that the picker represents and deals with local time. This also allows use of public getter and private setter, which will prevent unexpected state changes.
