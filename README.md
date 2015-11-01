# libniceAppAndroid

This App is using for develop jni of libnice.


Development step :

Step1. Sync project libnice4android and libgstreamer4android.
Step2. Set your ndk path and libniceAppAndroid path in libnice4android.
Step3. Using Eclipse to open this project.


How to use this App:

1. You should have two android phones or devices (cpu : arm)
2. Install ZXing QRcode app to android phones/devices
3. Click initlibnice button, then it will show your QRCode of Sdp information.
(get self sdp informatin and show)
4. Click getRemoteSdp button to scan QRCode which is showing on your other devices.
(get other sdp information)
5. Click setRemoteSdp button , and click "SEND", both on two devices at the same time better.
6. Then your can click SendToRemote button to send string to other devies lo.
(Just like a chat room)


