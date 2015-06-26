package com.example.dynamicsocial;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyListAdapter extends ArrayAdapter<ListRowModel> {
  private Context context;
  private List<ListRowModel> rows;

  public MyListAdapter(Context context, List<ListRowModel> rows) {
    super(context, R.layout.activity_main, rows);
    this.context = context;
    this.rows = rows;
  }

  public static int getImageId(Context context, String imageName) {
	    return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.list_row, parent, false);
    TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
    TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
    
    ListRowModel model = rows.get(position);
    
    firstLine.setText(model.getName());
    secondLine.setText(model.getContent());
    imageView.setImageResource(getImageId(context, model.getIcon()));

    return rowView;
  }
} 