package com.example.wojtekkurylo.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wojtekkurylo.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by wojtekkurylo on 19.07.2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

	/**
	 * Constructs a new {@link InventoryCursorAdapter}.
	 *
	 * @param context The context
	 * @param cursor  The cursor from which to get the data.
	 */
	public InventoryCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {

		// Find and cast the View's (API 25) - casting required :)
		ImageView imageView = (ImageView) view.findViewById(R.id.image);
		TextView nameView = (TextView) view.findViewById(R.id.name);
		TextView quantityView = (TextView) view.findViewById(R.id.quantity);
		TextView priceView = (TextView) view.findViewById(R.id.price);
		Button saleButton = (Button) view.findViewById(R.id.sale_button);

		// Taking the column ID
		int imageColumnIndex = cursor.getColumnIndexOrThrow(InventoryEntry.PRODUCT_IMAGE);
		int nameColumnIndex = cursor.getColumnIndexOrThrow(InventoryEntry.PRODUCT_NAME);
		int quantityColumnIndex = cursor.getColumnIndexOrThrow(InventoryEntry.PRODUCT_QUANTITY);
		int priceColumnIndex = cursor.getColumnIndexOrThrow(InventoryEntry.PRODUCT_PRICE);

		// Receiving the rest of resources:
		String nameInput = cursor.getString(nameColumnIndex);
		final String quantityListItem = cursor.getString(quantityColumnIndex);

		String priceInput = cursor.getString(priceColumnIndex);

		// Receiving resources associated with column index and row of interest
		// Image in SQLite is stored as byte array
		if (cursor.getBlob(imageColumnIndex) != null) {
			byte[] imageInput = cursor.getBlob(imageColumnIndex);
			Bitmap bmp = BitmapFactory.decodeByteArray(imageInput, 0, imageInput.length);

			// Setting the value from cursor to the list_item.xml
			imageView.setImageBitmap(bmp);
		} else {
			// If there was no photo added, add default
			imageView.setImageResource(R.drawable.add_image);
		}

		nameView.setText(nameInput);
		quantityView.setText(quantityListItem);
		priceView.setText(priceInput);

		//get row id
		final int rowID = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
		Log.e("ICA", "row ID: " + rowID);

		// Reduce the quantity by 1 , do not go below 0;
		saleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uriSelectedId = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowID);
				Log.e("ICA", "uri id : " + uriSelectedId);
				updateQuantity(context, quantityListItem, uriSelectedId);
			}
		});
	}

	// Method to Update out of bindView
	private void updateQuantity(Context context, String quantity, Uri uriSelectedId) {

		int quantityInt = Integer.parseInt(quantity);

		if (quantityInt == 0) {
			Toast.makeText(context, "We are out of stock ! Sorry", Toast.LENGTH_SHORT).show();
		} else {

			int updatedQuantityInt = quantityInt - 1;

			//mQuantityListItem = (String.valueOf(productQuantityBeforeRemoveInt));
			// Create a ContentValues with value, where columns names are the keys;
			ContentValues updatesValues = new ContentValues();
			String updatedQuantityString = String.valueOf(updatedQuantityInt);
			updatesValues.put(InventoryEntry.PRODUCT_QUANTITY, updatedQuantityString);

			// Defines a variable to contain the number of updated rows
			int rowsUpdated = 0;

			rowsUpdated = context.getContentResolver().update(
					uriSelectedId,                      // the user dictionary content URI
					updatesValues,                          // the columns to update
					null,                                   // the column to select on
					null);                                  // the value to compare to

			// Show a toast message depending on whether or not the insertion was successful
			if (rowsUpdated == 0) {
				Toast.makeText(context, "Upss.. sth went wrong", Toast.LENGTH_SHORT).show();

			} else {
				// Otherwise, the insertion was successful and we can display a toast.
				Toast.makeText(context, "Sold !", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
