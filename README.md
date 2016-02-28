SmartCrop [![Circle CI](https://circleci.com/gh/yaeda/SmartCrop.svg?style=svg)](https://circleci.com/gh/yaeda/SmartCrop)
=========================

```java
float aspect = 16f / 9f;
Frame frame = new Frame.Builder()
  .bitmap(bitmap)
  .build();
SmartCrop smartCrop = new SmartCrop.Builder().build();
CropResult cropResult = smartCrop.crop(frame, aspect);
```
