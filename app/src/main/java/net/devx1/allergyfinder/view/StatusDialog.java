package net.devx1.allergyfinder.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import net.devx1.allergyfinder.R;


class StatusDialog {
	private AlertDialog dialog;
	private TextView txtStatus;
	private Button btnAction;

	StatusDialog(Context ctx){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

		LayoutInflater inflater = LayoutInflater.from(ctx);
		View view = inflater.inflate(R.layout.status_dialog, null);

		builder.setView(view);
		dialog = builder.create();

		txtStatus = view.findViewById(R.id.txtStatus);
		btnAction = view.findViewById(R.id.btnAction);
		btnAction.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			}
		);
	}

	public void updateStatus(String status){
		txtStatus.setText(status);
	}

	public void show(){
		dialog.show();
	}

	public void updateButtonName(String name){
		btnAction.setText(name);
	}

}
