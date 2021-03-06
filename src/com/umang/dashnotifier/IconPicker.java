package com.umang.dashnotifier;

import java.io.File;

import com.umang.dashnotifier.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.AdapterView.OnItemClickListener;

public class IconPicker implements Preference.OnPreferenceClickListener {
	public static final String RESOURCE_NAME = "resource_name";
	public static final String PACKAGE_NAME = "package_name";
	public static final String TAG = "IconPicker";
	public static final int REQUEST_PICK_SYSTEM = 0;
	public static final int REQUEST_PICK_GALLERY = 1;
	public static final int REQUEST_PICK_ICON_PACK = 2;
	private Activity mParent;
	private Resources mResources;
	private Preference mPreference;
	private OnIconPickListener mIconListener;
	String extNumber;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	String icon;
	
	
	public static interface OnIconPickListener {
		public abstract void iconPicked();
	}
	
	public IconPicker(Activity activity, OnIconPickListener oniconpicklistener, String s) {
		mParent = activity;
		mIconListener = oniconpicklistener;
		extNumber = s;
		mResources = activity.getResources();
		preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		editor = preferences.edit();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		mPreference = preference;
		Log.v(TAG, mParent.getCacheDir().getAbsolutePath());
		icon = preferences.getString(mPreference.getKey(), "dummy");
		pickIcon();
		return true;
	}

	public void pickIcon() {

		String[] items = new String[2];
		items[0] = mResources
				.getString(R.string.icon_picker_dn_icons_title);
		items[1] = mResources.getString(R.string.icon_picker_gallery_title);

		new AlertDialog.Builder(mParent, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
				.setTitle(R.string.icon_picker_title)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						showChosen(item);
					}
				}).show();
	}

	private void showChosen(final int type) {
		if (type == REQUEST_PICK_SYSTEM) {
			ListView list = new ListView(mParent);
			final IconAdapter adapter = new IconAdapter();

			final Dialog dialog = new Dialog(mParent,
					android.R.style.Theme_DeviceDefault_Dialog);
			

			dialog.setTitle(R.string.icon_picker_title);
			dialog.setContentView(list);

			list.setDivider(mResources
					.getDrawable(android.R.drawable.divider_horizontal_dark));
			list.setAdapter(adapter);
			list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			if (adapter != null && adapter.getItemPosition(icon) != -1)
				list.setItemChecked(adapter.getItemPosition(icon), true);

			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent();
					intent.putExtra(RESOURCE_NAME,
							adapter.getItemReference(position));
					System.out.println(adapter.getItemReference(position));
					editor.putString(mPreference.getKey(),
							adapter.getItemReference(position));
					editor.remove((new StringBuilder("iconExt")).append(extNumber).toString());
					editor.commit();
					mIconListener.iconPicked();
					dialog.dismiss();
				}
			});
			dialog.show();
		} else if (type == REQUEST_PICK_GALLERY) {

			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			mParent.startActivityForResult(photoPickerIntent,
					REQUEST_PICK_GALLERY);

		} /*
		 * else if (type == REQUEST_PICK_ICON_PACK) { Intent iconPackIntent =
		 * new Intent(ICON_ACTION);
		 * startFragmentOrActivityForResult(iconPackIntent, type, fragmentId); }
		 */
	}

	class IconAdapter extends BaseAdapter {
		String[] labels;
		TypedArray icons;

		public IconAdapter() {
			labels = mResources.getStringArray(R.array.icon_labels);
			icons = mResources.obtainTypedArray(R.array.icons);

		}

		@Override
		public int getCount() {
			return labels.length;
		}

		@Override
		public Object getItem(int position) {
			return icons.getDrawable(position);
		}

		public String getItemReference(int position) {
			String name = icons.getString(position);
			int separatorIndex = name.lastIndexOf(File.separator);
			int periodIndex = name.lastIndexOf('.');
			return name.substring(separatorIndex + 1, periodIndex);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (convertView == null) {
				view = View.inflate(mParent,
						android.R.layout.simple_list_item_activated_1, null);
			}

			TextView label = (TextView) view.findViewById(android.R.id.text1);
			
			label.setText(labels[position]);
			label.setTextColor(mResources
					.getColor(android.R.color.primary_text_dark));

			Drawable icon = ((Drawable) getItem(position)).mutate();
			int bound = mParent.getResources().getDimensionPixelSize(
					R.dimen.shortcut_picker_left_padding);
			int padding = mParent.getResources().getDimensionPixelSize(
					R.dimen.icon_padding);
			icon.setBounds(0, 0, bound, bound);
			label.setCompoundDrawables(icon, null, null, null);
			label.setCompoundDrawablePadding(padding);
			

			return view;
		}

		public int getItemPosition(String iconName) {
			for (int i = 0; i < icons.length(); i++) {
				if (getItemReference(i).equals(iconName))
					return i;
			}
			return -1;
		}
	}

	
}
