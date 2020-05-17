package net.devx1.allergyfinder.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.devx1.allergyfinder.R;
import net.devx1.allergyfinder.model.History;

import java.util.List;

public class HistoryListAdapter extends ArrayAdapter<History> {
	public HistoryListAdapter(@NonNull Context context, int resource, @NonNull List<History> objects) {
		super(context, resource, objects);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View v = convertView;

		if(v == null){
			LayoutInflater li = LayoutInflater.from(getContext());
			v = li.inflate(R.layout.history_item, null);
		}

		History item = getItem(position);

		assert item != null;
		Bitmap bitmap = BitmapFactory.decodeFile(item.getPath());
		Bitmap resized = Bitmap.createScaledBitmap(bitmap, 100, 100, true);

		ImageView image = v.findViewById(R.id.historyImage);
		TextView text = v.findViewById(R.id.historyStatus);

		image.setImageBitmap(resized);
		text.setText(item.getStatus());

		return v;
	}
}
