package net.devx1.allergyfinder.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import net.devx1.allergyfinder.R;
import net.devx1.allergyfinder.components.HistoryListAdapter;
import net.devx1.allergyfinder.db.DbOperations;
import net.devx1.allergyfinder.model.History;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
	private String user;
	ListView historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        user = getIntent().getStringExtra("user");

        historyList = findViewById(R.id.historyList);

	    List<History> history = DbOperations.retrieveHistory(this, user);

	    HistoryListAdapter adapter = new HistoryListAdapter(this, 0, history);

	    historyList.setAdapter(adapter);
    }
}
