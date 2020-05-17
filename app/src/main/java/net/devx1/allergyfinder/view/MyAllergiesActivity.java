package net.devx1.allergyfinder.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import net.devx1.allergyfinder.R;
import net.devx1.allergyfinder.db.DbOperations;
import net.devx1.allergyfinder.model.Allergic;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyAllergiesActivity extends AppCompatActivity {
	ListView listAllergies;
	final Context context = this;
	private String user;

	ImageButton actionBarAdd, actionBarProfile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_allergies);

		user = getIntent().getStringExtra("user");

		actionBarAdd = findViewById(R.id.btnAdd);
		actionBarProfile = findViewById(R.id.btnProfile);

		listAllergies = findViewById(R.id.listAllergies);

		actionBarAdd.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					addAllergy();
				}
			}
		);

		actionBarProfile.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(MyAllergiesActivity.this, ProfileActivity.class);
					i.putExtra("user", user);
					startActivity(i);
				}
			}
		);

		if (getIntent().hasExtra("start")) {
			addAllergy();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			List<String> allergies = new ArrayList<>();
			for (Allergic allergic : DbOperations.retrieveAllergies(context, user)) {
				allergies.add(allergic.getAllergicTo());
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.text_list_item, allergies);
			listAllergies.setAdapter(adapter);
			listAllergies.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
						new AlertDialog.Builder(context)
							.setTitle("Delete?")
							.setMessage((CharSequence) listAllergies.getItemAtPosition(position))
							.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										DbOperations.deleteAllergy(
											context,
											(String) listAllergies.getItemAtPosition(position)
										);
									}
								})
							.create()
							.show();
					}
				}
			);
		}
	}

	private void addAllergy() {
		LayoutInflater li = getLayoutInflater();
		View inpView = li.inflate(R.layout.input_allergy, null);

		final EditText et = inpView.findViewById(R.id.etAllergy);

		new AlertDialog.Builder(context)
			.setView(inpView)
			.setPositiveButton("Add",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String inp = et.getText().toString();
						if (!inp.equals("")) {
							long id = DbOperations.insertAllergy(context, user, inp);
							if (id == -1) {
								Toast.makeText(context, "Insertion Failed",
									Toast.LENGTH_SHORT).show();
							}
						}
					}
				})
			.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
			.create()
			.show();
	}
}
