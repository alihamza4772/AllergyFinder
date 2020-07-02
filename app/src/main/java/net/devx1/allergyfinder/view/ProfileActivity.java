package net.devx1.allergyfinder.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.devx1.allergyfinder.R;
import net.devx1.allergyfinder.db.DbOperations;

public class ProfileActivity extends AppCompatActivity {  // profile
	private String user;
	private ImageView profile;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		user = getIntent().getStringExtra("user");

		TextView username = findViewById(R.id.username);
		username.setText(user);

		profile = findViewById(R.id.imgProfile);
		LoadImage loadImage = new LoadImage();
		loadImage.execute(user);

		Button logout = findViewById(R.id.btnLogout);
		logout.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(ProfileActivity.this, AuthActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(i);
				}
			}
		);
	}

	private class LoadImage extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... strings) {
			return DbOperations.getUserPicture(context, user);
		}

		@Override
		protected void onPostExecute(String str) {
			super.onPostExecute(str);
			Bitmap image = null;
			if ("null".equals(str)){
				Log.d("profile", "null");
				image = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
			}
			else {
				Log.d("profile", "not null");
				image = BitmapFactory.decodeFile(str);
			}
			profile.setImageBitmap(image);
		}
	}
}
