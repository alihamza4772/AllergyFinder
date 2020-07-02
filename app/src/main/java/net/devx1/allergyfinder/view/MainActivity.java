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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity { //main
	private String username;
	ImageButton btnMyAllergies, btnScan, btnHistory;
	Bitmap image;

	Context context = this;
	String currentPhotoPath;
	StatusDialog dialog;

	ImageButton actionBarProfile, actionBarAdd;

	static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		username = getIntent().getStringExtra("user");
		actionBarProfile = findViewById(R.id.btnProfile);
		actionBarAdd = findViewById(R.id.btnAdd);

		btnMyAllergies = findViewById(R.id.btnMyAllergies);
		btnScan = findViewById(R.id.btnScan);
		btnHistory = findViewById(R.id.btnHistory);

		btnMyAllergies.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(MainActivity.this, MyAllergiesActivity.class);
					i.putExtra("user", username);
					startActivity(i);
				}
			}
		);

		btnScan.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					takePicture();
				}
			}
		);

		btnHistory.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(
						MainActivity.this, HistoryActivity.class
					);

					i.putExtra("user", username);

					startActivity(
						i
					);
				}
			}
		);

		dialog = new StatusDialog(context);

		actionBarProfile.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(MainActivity.this, ProfileActivity.class);
					i.putExtra("user", username);
					startActivity(i);
				}
			}
		);

		actionBarAdd.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(MainActivity.this, MyAllergiesActivity.class);
					i.putExtra("user", username);
					i.putExtra("start", "");
					startActivity(i);
				}
			}
		);
	}

	private File createImageFile() throws IOException {
		String timeStamp =
			new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("en")).format(new Date());

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

	private void takePicture() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			File photoFile = null;
			try {
				photoFile = createImageFile();

				Uri photoURI = FileProvider.getUriForFile(this,
					"net.devx1.allergyfinder.fileprovider",
					photoFile
				);

				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			} catch (IOException ex) {
				Log.d("devx1 - takePicture", ex.getMessage());
				dialog.updateStatus("Camera Failed. Try Again!");
				dialog.updateButtonName("Okay");
				dialog.show();
			}
		} else {
			Log.d("devx1 - takePicture", "Null");
			dialog.updateStatus("Camera Failed. Try Again!");
			dialog.updateButtonName("Okay");
			dialog.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			try {
				image = BitmapFactory.decodeFile(currentPhotoPath);
			} catch (Exception e) {
				Log.d("toasting", Objects.requireNonNull(e.getMessage()));
				dialog.updateStatus("Image Failed");
				dialog.updateButtonName("Okay");
				dialog.show();
				return;
			}
			detectTextFromImage();
		} else {
			finishActivity(REQUEST_IMAGE_CAPTURE);
			dialog.updateStatus("Capture Failure");
			dialog.updateButtonName("Okay");
			dialog.show();
		}
	}

	void detectTextFromImage() {
		dialog.updateStatus("Fetching Text...");
		dialog.updateButtonName("Cancel");
		dialog.show();

		FirebaseVisionImage fvi = null;
		try {
			fvi = FirebaseVisionImage.fromFilePath(context,
				Uri.parse("file://" + currentPhotoPath));
		} catch (IOException e) {
			e.printStackTrace();
			dialog.updateStatus("Image Failed! Try Again!");
			dialog.updateButtonName("Okay");
			return;
		}
		FirebaseVisionTextRecognizer fvtr = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

		fvtr.processImage(fvi)
			.addOnSuccessListener(
				new OnSuccessListener<FirebaseVisionText>() {
					@Override
					public void onSuccess(final FirebaseVisionText firebaseVisionText) {
						identifyLanguage(firebaseVisionText);
					}
				}
			)
			.addOnFailureListener(
				new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						dialog.updateStatus("Text fetch failed! Try Again!");
						dialog.updateButtonName("Okay");
					}
				}
			);
	}

	private void identifyLanguage(final FirebaseVisionText firebaseVisionText) {
		dialog.updateStatus("Detecting Language...");
		dialog.updateButtonName("Cancel");

		FirebaseLanguageIdentification identification =
			FirebaseNaturalLanguage.getInstance()
				.getLanguageIdentification();

		identification.identifyLanguage(firebaseVisionText.getText())
			.addOnSuccessListener(
				new OnSuccessListener<String>() {
					@Override
					public void onSuccess(@Nullable final String languageCode) {
						if (!Objects.equals(languageCode, "und")) {
							if (Objects.equals(languageCode, "en")) {
								findAllergies(firebaseVisionText.getText());
							} else {
								translateLanguageFrom(languageCode, firebaseVisionText);
							}
						} else {
							dialog.updateStatus("Undefined Language");
							dialog.updateButtonName("Okay");
						}
					}
				})
			.addOnFailureListener(
				new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d("devx1 - langId", "Language Identification Failed! Try Again!");
						dialog.updateStatus("Language Identification Failed! Try Again!");
						dialog.updateButtonName("Okay");
					}
				});
	}

	private void translateLanguageFrom(final String languageCode,
	                                   final FirebaseVisionText firebaseVisionText) {
		dialog.updateStatus(new Locale(languageCode).getDisplayLanguage() + " Language Detected");

		Integer sourceLanguage;
		try {
			sourceLanguage = FirebaseTranslateLanguage.languageForLanguageCode(languageCode);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		FirebaseTranslatorOptions options =
			new FirebaseTranslatorOptions.Builder()
				.setSourceLanguage(sourceLanguage)
				.setTargetLanguage(FirebaseTranslateLanguage.EN)
				.build();

		final FirebaseTranslator translator =
			FirebaseNaturalLanguage.getInstance().getTranslator(options);


		dialog.updateStatus("Downloading " + new Locale(languageCode).getDisplayLanguage() + ".." +
				".\nIt may cost you internet data!");
		dialog.updateButtonName("Cancel");

		translator.downloadModelIfNeeded()
			.addOnSuccessListener(
				new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void v) {
						dialog.updateStatus("Translating...");
						dialog.updateButtonName("Cancel");
						translator.translate(firebaseVisionText.getText())
							.addOnSuccessListener(
								new OnSuccessListener<String>() {
									@Override
									public void onSuccess(@NonNull String translatedText) {
										findAllergies(translatedText);
									}
								})
							.addOnFailureListener(
								new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										Log.d("devx1 - translation", "Translation Failed! Try " +
											"Again!");
										dialog.updateStatus("Translation Failed! Try Again!");
										dialog.updateButtonName("Okay");
									}
								});
					}
				})
			.addOnFailureListener(
				new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d("devx1 - download", "Language Pack Download Failed! Try Again!");
						dialog.updateStatus("Language Pack Download Failed! Try Again!");
						dialog.updateButtonName("Okay");
					}
				});
	}

	private void findAllergies(String translatedText) {
		dialog.updateStatus("Finding Allergies...");
		dialog.updateButtonName("Cancel");

		translatedText = translatedText.toLowerCase();
		List<String> allergies = DbOperations.retrieveText(context, username);

		List<String> foundAllergies = new ArrayList<>();
		for (String allergy : allergies) {
			if (translatedText.contains(allergy.toLowerCase())) {
				foundAllergies.add(allergy);
			}
		}

		StringBuilder respText = new StringBuilder();

		String historyStatus = "This product is ";
		if (foundAllergies.size() > 0) {
			respText.append("This product contains items that you are allergic to:\n");
			for (String allergy : foundAllergies) {
				respText.append(allergy).append("\n");
			}

			historyStatus = "allergic";
		} else {
			respText.append("No Allergic Items Found");
			historyStatus = "safe";
		}

		dialog.updateStatus(respText.toString());
		dialog.updateButtonName("Okay");
		DbOperations.insertHistory(context, username, currentPhotoPath, historyStatus,
			respText.toString());
	}
}
