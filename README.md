# WiFiList
View your saved WiFi passwords on Android 11 and later without root!

## Requirements
- A device running Android 11 or later with wireless ADB or access to a computer with ADB.
- The [Shizuku app](https://shizuku.rikka.app).

## Why?
It's useful to be able to see your saved WiFi networks' passwords. Maybe you've forgotten the password or maybe it some long base64 string that purposely can't be remembered.
Being able to see your saved WiFi passwords avoids having to remember to write them down somewhere and still keeps them close to you.

While some Android skins, like Pixel UI and One UI, have methods for sharing saved WiFi networks with other devices, they aren't perfect.

- For one, the device you're sharing to has to support scanning a QR code to connect to a WiFi network.
- The generated QR code does contain the network password in plaintext, but to get it, you need to scan the QR code with a different device or screenshot it and scan it locally, and then extract the text. And this has to be done per-network.
- On Pixel UI 13, the password is directly shown in plaintext under the QR code, but it's still a per-network process.

## How It Works
The `getPrivilegedConfiguredNetworks()` API has been available in Android for a while, and it returns saved WiFi networks with the password included.
But, as the name might imply, it's a privileged API, meaning only system-level apps with the system-level permission can access it (normally).
With the release of Android 11, two things happened: we got user-facing wireless ADB, and the shell user was given the permission necessary to use `getPrivilegedConfiguredNetworks()`.
The wireless ADB method allows creating a shell-level process completely on-device with the only requirement being that the device needs to be connected to WiFi. Shizuku handles creating that process and guiding the user while also providing an API for WiFiList to hook into it.
The added permission means the shell process created by Shizuku is able to access `getPrivilegedConfiguredNetworks()`.

All of this combined means WiFiList can show you all your saved networks along with their passwords.

## Privacy
WiFiList is (almost) fully open source. Feel free to browse the code to confirm security.

Aside from crash reports, zero data is collected and there is zero internet connectivity. *Your passwords remain on your device.*

## Error Reporting
WiFiList uses Bugsnag for error reporting as of version 1.1.2. Previous versions use Firebase Crashlytics.

<a href="https://www.bugsnag.com"><img src="https://assets-global.website-files.com/607f4f6df411bd01527dc7d5/63bc40cd9d502eda8ea74ce7_Bugsnag%20Full%20Color.svg" width="200"></a>
