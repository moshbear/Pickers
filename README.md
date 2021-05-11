# Pickers
Pickers demo and library for Android.

The purpose here is to be copy-pasteable date and time picker dialogs but also to
check important parts of the UI state machine used in other projects not ready for release.

This should be buildable in Android Studio with files given.

To use the DialogFragment(s) in your own code:
```kotlin
fun showDatePickerDialog() {
	val c = Calendar.getInstance()
	DatePickerFragment.newInstance(c)
	{ view, y, m, d -> ... }
	.show(...)
}
fun showTimePickerDialog() {
	val c = Calendar.getInstance()
	TimePickerFragment.newInstance(c)
	{ view, h, m, s -> ... }
	.show(...)
}
```

The non-dialog form of one picker followed by another in a Layout has not been tested.
Doing so would entail orientation-aware layouts, which is on the to-do list.
