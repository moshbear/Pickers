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
