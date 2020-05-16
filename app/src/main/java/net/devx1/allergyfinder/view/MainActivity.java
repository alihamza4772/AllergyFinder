package net.devx1.allergyfinder.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import net.devx1.allergyfinder.model.History;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
	ImageButton btnMyAllergies, btnScan;
	ImageView imgHistory;
	Bitmap image;

	Context context = this;
	String currentPhotoPath;
	AlertDialog dialog;

	TextView txtStatus;
	ProgressBar progressBar;
	ImageView imgStatus;
	static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnMyAllergies = findViewById(R.id.btnMyAllergies);
		btnScan = findViewById(R.id.btnScan);
		imgHistory = findViewById(R.id.imgHistory);

		btnMyAllergies.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(
						new Intent(
							MainActivity.this, MyAllergiesActivity.class
						)
					);
				}
			}
		);

		btnScan.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dispatchTakePictureIntent();
				}
			}
		);

		imgHistory.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(
						new Intent(
							MainActivity.this, HistoryActivity.class
						)
					);
				}
			}
		);

		LayoutInflater li = getLayoutInflater();
		View v = li.inflate(R.layout.dialog_processing, null);
		txtStatus = v.findViewById(R.id.txtStatus);
		progressBar = v.findViewById(R.id.progress);
		imgStatus = v.findViewById(R.id.imageStatus);

		dialog = new AlertDialog.Builder(context).create();
		dialog.setView(v);
		dialog.setButton(
			AlertDialog.BUTTON_NEGATIVE,
			"Cancel",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}
		);
		dialog.setButton(
			AlertDialog.BUTTON_POSITIVE,
			"Okay",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
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

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				Log.d("toasting", "File Creation Failure");
				Log.d("toasting", ex.getMessage());
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
				Log.d("toasting", Objects.requireNonNull(e.getMessage()));
			}
			detectTextFromImage();
		} else {
			Log.d("toasting", "Capture Failure");
			finishActivity(REQUEST_IMAGE_CAPTURE);
		}
	}

	void detectTextFromImage() {
		txtStatus.setText("Fetching Text...");
		dialog.show();

		FirebaseVisionImage fvi = FirebaseVisionImage.fromBitmap(image);
		FirebaseVisionTextRecognizer fvtr = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

		final Task<FirebaseVisionText> results = fvtr.processImage(fvi)
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
						Log.d("toasting", "Failed Processing Image");
					}
				}
			);
	}

	private void identifyLanguage(final FirebaseVisionText firebaseVisionText) {
		txtStatus.setText("Detecting Language...");
		FirebaseLanguageIdentification identification =
			FirebaseNaturalLanguage.getInstance()
				.getLanguageIdentification();
		identification.identifyLanguage(firebaseVisionText.getText())
			.addOnSuccessListener(
				new OnSuccessListener<String>() {
					@Override
					public void onSuccess(@Nullable final String languageCode) {
						assert languageCode != null;
						if (!languageCode.equals("und")) {
							if (languageCode.equals("en")) {
								findAllergies(firebaseVisionText.getText());
							} else {
								translateLanguageFrom(languageCode, firebaseVisionText);
							}
						} else {
							txtStatus.setText("Language is Undefined");
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

	private void translateLanguageFrom(final String languageCode,
	                                   final FirebaseVisionText firebaseVisionText) {
		txtStatus.setText("Translating " + new Locale(languageCode).getDisplayLanguage() + "...");
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
										findAllergies(translatedText);
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
	}

	private void findAllergies(String translatedText) {
		txtStatus.setText("Finding Allergies...");
		translatedText = translatedText.toLowerCase();
		List<String> allergies = DbOperations.retrieveText(context);
		List<String> foundAllergies = new ArrayList<>();
		for (String allergy : allergies) {
			if (translatedText.contains(allergy.toLowerCase())) {
				foundAllergies.add(allergy);
			}

		}

		StringBuilder textToSet = new StringBuilder();

		String historyStatus = "";
		if (foundAllergies.size() > 0) {
			imgStatus.setImageResource(R.drawable.cross);
			textToSet.append("Found Allergic Items\n");
			for (String allergy : foundAllergies) {
				textToSet.append("\n").append(allergy);
			}
			historyStatus = "alergic";
		} else {
			imgStatus.setImageResource(R.drawable.check);
			textToSet.append("No Allergic Items Found");
			historyStatus = "safe";
		}

		imgStatus.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		txtStatus.setText(textToSet);
		Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
		cancelButton.setVisibility(View.GONE);
		DbOperations.history(context, currentPhotoPath, historyStatus, textToSet.toString());
		Toast.makeText(context, "" + DbOperations.retrieveHistory(context).size(), Toast.LENGTH_SHORT).show();
	}
}
