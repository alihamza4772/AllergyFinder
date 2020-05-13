package net.devx1.allergyfinder.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class    MyAllergiesActivity extends AppCompatActivity {
    Button btnAddAllergy;
    ListView listAllergies;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_allergies);

        btnAddAllergy = findViewById(R.id.btnAddAllergy);
        listAllergies = findViewById(R.id.listAllergies);

        btnAddAllergy.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                                    if (!inp.equals("")){
                                        DbOperations.insert(context, inp);
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
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            List<String> allergies = new ArrayList<>();
            for (Allergic allergic: DbOperations.retrieve(context)){
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
                                        DbOperations.delete(
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

//            Toast.makeText(context, Long.toString(DbOperations.retrieve(context).size()) , Toast.LENGTH_SHORT).show();
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
