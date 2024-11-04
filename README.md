# Android-TimeProvider
Proof of concept app for syncing backend time.

# Guide
The basic concept is the app listens for a device reboot event with a broadcast receiver.
When the app is restarted the SystemClock.elapsedRealtime() is reset so I have to listen
for the event to recalculate the time using last synced time and elapsedRealTime() value.
Using the elapsed time and the last sync time, the app can determine what the real sync time is 
without needing to do another web call.

This requires a RECEIVE_BOOT_COMPLETED permission.

