package com.tag18team.tag18;
import java.util.List;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
public class ItemAdapter extends ArrayAdapter<Item> {
	private List<Item> itemList;
	private Context context;
	private boolean uses_tags=false;
	public ItemAdapter(List<Item> itemList, Context ctx, boolean uses_tags) {
		super(ctx, R.layout.img_row_layout, itemList);
		this.uses_tags=uses_tags;
		this.itemList = itemList;
		this.context = ctx;
	}
	public int getCount() {
		return itemList.size();
	}
	public Item getItem(int position) {
		return itemList.get(position);
	}
	public long getItemId(int position) {
		return itemList.get(position).hashCode();
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ItemHolder holder = new ItemHolder();
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.img_row_layout, null);
			TextView tv = (TextView) v.findViewById(R.id.e_name);
			TextView distView = (TextView) v.findViewById(R.id.dist);
			ImageView imageView = (ImageView) v.findViewById(R.id.img);
			ImageView imageView2 = (ImageView) v.findViewById(R.id.img2);
			if (uses_tags) imageView2.setImageResource(R.drawable.planet);
			////////////////////////////////////////////////////////
			holder.ItemNameView = tv;
			holder.distView = distView;
			holder.icon = imageView;
			v.setTag(holder);
		}
		else 
			holder = (ItemHolder) v.getTag();
		
		Item p = itemList.get(position);
		holder.ItemNameView.setText(p.getName());
		holder.distView.setText("" + p.getPathOrDescription());
		//holder.external.setChecked(p.isExternal());
		holder.icon.setImageResource(R.drawable.planet);
		//////////////////////////////////////////////////////
		return v;
	}
	public static class ItemHolder {
		public TextView ItemNameView;
		public TextView distView;
		public ImageView icon;
		public CheckBox external;
	}
}