# IMAGician
An Android App for Copyright Protection Based on Steganography Technology - UMICH EECS 441 Project

**Note**: To install Plugin for Python, add 

```kotlin
pluginManagement {
    repositories {
        maven { url "https://chaquo.com/maven" }
    }
}
```

 to `setting.gradle`.

And in `build.gradle(app)/defaultConfig/python`, set the following to `python3` on your device.

```kotlin
		buildPython "D:/Python38_64/python.exe" // change to your path to python3
```

