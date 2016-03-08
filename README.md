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

with Picasso
```java
int width = 100;
int height = 100;
Picasso.with(mContext)
  .load(R.drawable.demo)
  .transform(new SmartCropTransformation(width, height))
  .into((ImageView) findViewById(R.id.image));
```
