package net.devx1.allergyfinder.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import net.devx1.allergyfinder.R;
import net.devx1.allergyfinder.db.DbOperations;
import net.devx1.allergyfinder.model.Allergic;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class AllergyScanActivity extends AppCompatActivity {
    ScrollView scrollView;
    TextView txtScanned, txtLoading;
    Bitmap image;

    Context context = this;
    String currentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergy_scan);

        scrollView = findViewById(R.id.scroll);
        txtScanned = findViewById(R.id.txtScanned);
        txtLoading = findViewById(R.id.txtLoading);

        dispatchTakePictureIntent();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("toasting", "File Creation Failure");
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "net.devx1.allergyfinder.fileprovider",
                        photoFile
                );

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                image = BitmapFactory.decodeFile(currentPhotoPath);
            } catch (Exception e) {
                Log.d("toasting", e.getMessage());
            }
            detectTextFromImage();
        } else {
            Log.d("toasting", "Capture Failure");
            finish();
        }
    }

    void detectTextFromImage() {
        FirebaseVisionImage fvi = FirebaseVisionImage.fromBitmap(image);
        FirebaseVisionTextRecognizer fvtr = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        final Task<FirebaseVisionText> results = fvtr.processImage(fvi)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(final FirebaseVisionText firebaseVisionText) {
                                FirebaseLanguageIdentification identification =
                                        FirebaseNaturalLanguage.getInstance()
                                                .getLanguageIdentification();
                                identification.identifyLanguage(firebaseVisionText.getText())
                                        .addOnSuccessListener(
                                                new OnSuccessListener<String>() {
                                                    @Override
                                                    public void onSuccess(@Nullable final String languageCode) {
                                                        if (!languageCode.equals("und")) {
                                                            FirebaseTranslatorOptions options =
                                                                    new FirebaseTranslatorOptions.Builder()
                                                                            .setSourceLanguage(FirebaseTranslateLanguage.languageForLanguageCode(languageCode))
                                                                            .setTargetLanguage(FirebaseTranslateLanguage.EN)
                                                                            .build();
                                                            final FirebaseTranslator translator =
                                                                    FirebaseNaturalLanguage.getInstance().getTranslator(options);

                                                            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                                                                    .requireWifi()
                                                                    .build();

                                                            translator.downloadModelIfNeeded(conditions)
                                                                    .addOnSuccessListener(
                                                                            new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void v) {
                                                                                    translator.translate(firebaseVisionText.getText())
                                                                                            .addOnSuccessListener(
                                                                                                    new OnSuccessListener<String>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(@NonNull String translatedText) {
                                                                                                            translatedText = translatedText.toLowerCase();
                                                                                                            List<String> allergies = DbOperations.retrieveText(context);
                                                                                                            List<String> foundAllergies = new ArrayList<>();
                                                                                                            for (String allergy: allergies){
                                                                                                                if (translatedText.contains(allergy.toLowerCase())){
                                                                                                                    foundAllergies.add(allergy);
                                                                                                                }

                                                                                                            }

                                                                                                            StringBuilder textToSet = new StringBuilder();

                                                                                                            if (foundAllergies.size() > 0){
                                                                                                                textToSet.append("Found Allergic Items");
                                                                                                                for (String allergy : foundAllergies){
                                                                                                                    textToSet.append("\n").append(allergy);
                                                                                                                }
                                                                                                            }
                                                                                                            else {
                                                                                                                textToSet.append("No Allergic Items Found");
                                                                                                            }

                                                                                                            txtLoading.setVisibility(View.INVISIBLE);
                                                                                                            txtScanned.setText(textToSet);
                                                                                                            scrollView.setVisibility(View.VISIBLE);
                                                                                                        }
                                                                                                    })
                                                                                            .addOnFailureListener(
                                                                                                    new OnFailureListener() {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                                            Toast.makeText(context, "Translation Failure", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                }
                                                                            })
                                                                    .addOnFailureListener(
                                                                            new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                        } else {
                                                            Toast.makeText(context, "Language is Undefined", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(context, "Language Recognition Failure", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                scrollView.setVisibility(View.VISIBLE);
                                txtLoading.setVisibility(View.INVISIBLE);
                                txtScanned.setText("Scan Fail\n" + e.getMessage());
                            }
                        }
                );
    }
}
