package net.devx1.allergyfinder.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.devx1.allergyfinder.R;
import net.devx1.allergyfinder.db.DbOperations;

public class AuthActivity extends AppCompatActivity {
	Button btnLogin, btnRegister;
	EditText etUsername, etPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) { // oncreate
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);

		btnLogin = findViewById(R.id.btnLogin);
		btnRegister = findViewById(R.id.btnRegister);

		etUsername = findViewById(R.id.etUsername);
		etPassword = findViewById(R.id.etPassword);

		btnLogin.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					login();
				}
			}
		);

		btnRegister.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					register();
				}
			}
		);
	}

	private void login(){
		String username = etUsername.getText().toString();
		String password = etPassword.getText().toString();

		if (validateFilness(username, password)){
			if (DbOperations.isUserExists(getBaseContext(), username, password)){
				goHome(username);
			}
			else {
				Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void register(){
		String username = etUsername.getText().toString();
		String password = etPassword.getText().toString();

		if (validateFilness(username, password)) {
			long id = DbOperations.insertUser(getBaseContext(), username, password);
			if (id == -1) {
				Toast.makeText(this, "User Already Exists", Toast.LENGTH_SHORT).show();
			}
			else {
				goHome(username);
			}
		}
	}

	private boolean validateFilness(String username, String password){
		if (username.equals("") || password.equals("")){
			Toast.makeText(this, "Please Fill All Inputs", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private void goHome(String user){
		Intent intent = new Intent(AuthActivity.this, MainActivity.class);
		intent.putExtra("user", user);
		startActivity(intent);
	}
}
