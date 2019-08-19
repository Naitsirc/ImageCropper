ImageCropper
=========
A custom image cropper view library on Android

Features
=========
- Doesn't need file access permissions
- Support moving/scale the crop window freely by finger
- Support rotate the image when cropping
- Easy to integrate into your app

ScreenShot
=========
<img src="ImageCropper.jpg" width="800" height="1280" />

Dependency 
=========
(1) Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

(2) Add the dependency
```groovy
allprojects {
    implementation 'com.github.Naitsirc:ImageCropper:-SNAPSHOT'
}
```

Usage
=========
Use the CropImageView class
```java
com.jhuster.imagecropper.CropImageView
```

Call these methods to interact with CropImageView

```java
//1. Initialize the CropImageView
private void init() {

    cropImageView = (CropImageView) findViewById(R.id.CropWindow);

    original = BitmapFactory.decodeResource(getResources(), R.drawable.cats);

    cropImageView.initialize(original);

}

//2. Apply the CropImageView events to your layout buttons
public void onClickRotate(View v) {
    cropImageView.rotate();
    cropImageView.invalidate();
}

public void onClickReset(View v) {
    cropImageView.reset();
    cropImageView.invalidate();
}

public void onClickZoom(View v) {
    cropImageView.zoom();
    cropImageView.invalidate();
}

public void onClickCrop(View v) {
    Bitmap x = cropImageView.getCropBitmap();
    cropImageView.initialize(x);
    cropImageView.invalidate();
}

```



