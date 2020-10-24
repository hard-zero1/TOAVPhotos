# TOAV Photos
*(Stands for "take, organize and view photos")*

This (not very sophisticated) Android app allows you to take photos and save them in a preselected directory.

You can view the photos and scroll/swipe between them, even if they are in different but sibling directories. That allows you to group photos but still have them together. (Example: You can take photos of your lectures and sort them in directories by date. Then, you can easily find the photos you look for and when scrolling/swiping through them it is marked when you reach the next/previous day but you can simply continue scrolling.

A directory tree can be maintained within the app where you can order the photos and directories and name the directories.

**You are welcome to open issues or pull requests for features you miss the most, changes you wish, problems or bugs you face or suggestions you want to make!  
You can also write me to hardzero01+github@gmail.com.**

## Install

If you don't have Android Studio, you can download the [TOAVPhotos_v1.0.3.apk](https://github.com/hard-zero1/TOAVPhotos/releases/download/v1.0.3/TOAVPhotos_v1.0.3.apk) release file to the Android device. Open the downloaded .apk file to install the app. You will be required to allow the installation from unknown sources in the settings. (You might need to do that before opening the .apk file. The option should be called "Unknown sources" and be located in some "Security" section of the system settings.)  
*On Android 5.0 (Lollipop) or higher*, when first starting the app you will be asked to select a directory. A folder "TOAVPhotos" will be created in the selected directory where you can put the file tree with your photos. You won't be able to navigate through the app to places higher in the file system tree than the directory that you selected. It can't be changed in the future without uninstalling and reinstalling the app. The first time you open the camera view you will be asked for the permission the access the camera.  
*On older Android versions* (the lowest supported version is Android 4.1, Jelly Bean) you will be asked for file system access permission when first starting the app and the "TOAVPhotos" directory will be created on the highest accessible file system tree level.  

If you have Android Studio, you can import the project as decribed below. Then, you can use Android Studio to build and install the app.

## How to use (once installed)
In the main view, one half of the display shows the photos in the directory you are currently in and the other half shows the subdirectories. Tap on a subdirectory to enter it and tap on the "Up" button to go upwards in the file tree. The initial directoy when opening the app is the "TOAVPhotos" directory created on the first app launch.

To scroll through the photos, you need to start swiping on the left or right edge of a photo, or at its info text. Elsewhere on the photo you can pinch to zoom. Tap anywhere to open the photo in full-screen.

You can choose if you want to scroll vertically or horizontally by the switch in the middle of the main view (also applies for scrolling/swiping in full screen view).

In the full screen view, you can swipe through the photos and past the boundaries of the directory to the next/previous sibling directory. Use your device's back button to get back to the main view.

Tap and hold anywhere on a photo or directory in the main view to get the corresponding options dialog. There, you can choose to delete it, move it to a new position (select the corresponding number) or rename it (for directories).

The "Take Photo" button brings you to the camera. If you are using Android 5.0 (Lollipop) or higher, you will get to the app's camera view. If you use an older version of Android, you will be redirected to an external camera app.
Photos you make will be appended to the photos in the current directory (even if you were redirected to an external camera app)

In the app's camera view, there are two sliders: One is for zoom, the other one for brightness. You can also pinch to zoom. Tap anywhere to select that point as target for the autofocus. Use your device's back button to get back to the main view.


## Import project to Android Studio
Clone the repository to your file system. Open Android Studio, select "File" -> "New" -> "Import Project" and choose the project that you just cloned.

Instead you can also go to Android Studio, select "File" -> "New" -> "Project from Version Control...", select Git, enter "https://github.com/hard-zero1/TOAVPhotos" as URL and choose a place in your file system.

