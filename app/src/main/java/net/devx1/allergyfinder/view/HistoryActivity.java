package net.devx1.allergyfinder.view;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import net.devx1.allergyfinder.R;
import net.devx1.allergyfinder.components.HistoryListAdapter;
import net.devx1.allergyfinder.db.DbOperations;
import net.devx1.allergyfinder.model.History;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity { //history
	private String user;
	ListView historyList;
	Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        user = getIntent().getStringExtra("user");

        historyList = findViewById(R.id.historyList);

	    final List<History> history = DbOperations.retrieveHistory(this, user);

	    HistoryListAdapter adapter = new HistoryListAdapter(this, 0, history);

	    historyList.setAdapter(adapter);

	    historyList.setOnItemClickListener(
		    new AdapterView.OnItemClickListener() {
			    @Override
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				    new AlertDialog.Builder(context)
					    .setTitle(history.get(position).getStatus())
					    .setMessage(history.get(position).getAllergies())
					    .create()
					    .show();
			    }
		    }
	    );
    }
}
