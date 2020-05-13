package net.devx1.allergyfinder.ml;

import android.graphics.Bitmap;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class ML {

    public static void getTextFromImage(Bitmap image) {
        FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(image);
        FirebaseVisionTextRecognizer textRecognizer =
                FirebaseVision.getInstance().getOnDeviceTextRecognizer();

    }
}
