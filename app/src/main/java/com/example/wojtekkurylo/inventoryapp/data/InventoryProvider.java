package com.example.wojtekkurylo.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.wojtekkurylo.inventoryapp.data.InventoryContract.InventoryEntry;

import static android.R.attr.value;


/**
 * Created by wojtekkurylo on 19.07.2017.
 */

public class InventoryProvider extends ContentProvider {

	// Initialize the PetDbHelper Instance to create // get access to SQLDatabase
	public InventoryDBHelper mInventoryDBHelper;

	// Code to return in Cursor all Table "parts"
	private static final int PARTS = 100;

	// Code to return in Cursor row with the ID of choice from table "parts"
	private static final int PART_ID = 101;

	/**
	 * URI matcher object to match a context URI to a corresponding code.
	 * The input passed into the constructor represents the code to return for the root URI.
	 * It's common to use NO_MATCH as the input for this case.
	 */
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	// Static initializer. This is run the first time anything is called from this class.
	static {
		// The content URI of the form "com.example.wojtekkurylo.inventoryapp/parts" will map to the
		// integer code {@link #PARTS}. This URI is used to provide access to MULTIPLE rows
		// of the pets table.
		sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PARTS, PARTS);

		// The content URI of the form "com.example.wojtekkurylo.inventoryapp/parts/#" will map to the
		// integer code {@link #PART_ID}. This URI is used to provide access to ONE single row
		// of the pets table.

		// In this case, the "#" wildcard is used where "#" can be substituted for an integer.
		sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PARTS + "/#", PART_ID);

	}

	/**
	 * Initialize the provider and the database helper object.
	 */
	@Override
	public boolean onCreate() {
		// Initialize the InventoryDbHelper Instance to create // get access to SQLDatabase
		mInventoryDBHelper = new InventoryDBHelper(getContext());

		//true if the provider was successfully loaded, false otherwise
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// Create and/or open a database to read from it
		SQLiteDatabase db = mInventoryDBHelper.getReadableDatabase();

		/**
		 * Utility class to aid in matching URIs in content providers.
		 */

		// .match compare the static number associated with received Uri from ContentResolver
		int match = sUriMatcher.match(uri);
		Cursor cursor;

		switch (match) {
			case PARTS:
				// content://com.example.wojtekkurylo.inventoryapp/parts
				cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			case PART_ID:
				// content://com.example.wojtekkurylo.inventoryapp/parts/#
				selection = InventoryEntry._ID + "=?";
				// WHERE   id = "/#";
				// take ID from "/#" and use it in selectionArgs
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

				cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			default:
				throw new IllegalArgumentException("Cannot query unknown URI " + uri);
		}

		// If we want to be notified of any changes in database
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case PARTS:
				return InventoryEntry.CONTENT_LIST_TYPE;
			case PART_ID:
				return InventoryEntry.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		// Data Validation
		// Take the value imputed by user && check it
		byte [] arrayCheck = values.getAsByteArray(InventoryEntry.PRODUCT_IMAGE);
		if(arrayCheck == null){
			Toast.makeText(getContext(), "Item requires a image", Toast.LENGTH_SHORT).show();
			// The method will return int = 0;
			return null;
		}

		// Take the value imputed by user && check it
		String nameCheck = values.getAsString(InventoryEntry.PRODUCT_NAME);
		if (nameCheck.equals("")) {
			Toast.makeText(getContext(), "Item requires a name", Toast.LENGTH_SHORT).show();
			// The method will return Uri = null;
			return null;
		}
		String emailCheck = values.getAsString(InventoryEntry.SUPPLIER_EMAIL);
		if (emailCheck.equals("")) {
			Toast.makeText(getContext(), "Item requires email to supplier", Toast.LENGTH_SHORT).show();
			// The method will return Uri = null;
			return null;
		}
		Integer quantityCheck = values.getAsInteger(InventoryEntry.PRODUCT_QUANTITY);
		if (quantityCheck == 0 || quantityCheck == -1) {
			Toast.makeText(getContext(), "Item requires quantity value > 0", Toast.LENGTH_SHORT).show();
			// The method will return Uri = null;
			return null;
		}
		Integer priceCheck = values.getAsInteger(InventoryEntry.PRODUCT_PRICE);
		if (priceCheck == -1) {
			Toast.makeText(getContext(), "Item requires price value", Toast.LENGTH_SHORT).show();
			// The method will return Uri = null;
			return null;
		}


//		if (values.containsKey(InventoryEntry.PRODUCT_QUANTITY)) {
//			if (quantityCheck.equals(checkValue)) {
//				Toast.makeText(getContext(), "Item requires quantity value", Toast.LENGTH_SHORT).show();
//				// The method will return Uri = null;
//				return null;
//			}
//		}

		// Create and/or open a database to read from it
		SQLiteDatabase db = mInventoryDBHelper.getWritableDatabase();

		/**
		 * Utility class to aid in matching URIs in content providers.
		 */

		// .match compare the static number associated with received Uri from ContentResolver
		int match = sUriMatcher.match(uri);
		long newRowId;

		switch (match) {
			case PARTS:
				newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);
				if (newRowId == -1) {
					Log.e("InventoryProvider", "Failed to insert new Pet, newRowId: " + newRowId);
					Toast.makeText(getContext(), "Error with saving pet", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				throw new IllegalArgumentException("Insertion is not supported for " + uri);
		}

		// add Id of the new row to uri passed to insert()
		Uri uriWithID = ContentUris.withAppendedId(uri, newRowId);

		// notify all listeners of changes:
		try {
			getContext().getContentResolver().notifyChange(uri, null);
		} catch (NullPointerException e) {
			Log.e("InventoryProvider", "Problem in insert method with notify all listeners", e);
		}

		return uriWithID;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		// get Writable Database
		SQLiteDatabase db = mInventoryDBHelper.getWritableDatabase();

		int rowsDeleted;
		int match = sUriMatcher.match(uri);
		switch (match) {
			case PARTS:

				// Delete all rows that match the selection and selection args
				rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
				db.execSQL("UPDATE sqlite_sequence SET seq == 0;");
				break;

			case PART_ID:

				// delete selected row - OPTION
				// SQLite statement: DELETE pets WHERE _id=5
				// URI: content://com.example.android.pets/pets/5
				// Selection: “id=?”
				// SelectionArgs: { “5” }
				selection = InventoryEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				Log.e("InventoryProvider", "Id of row to delete: " + ContentUris.parseId(uri));

				// Delete a single row given by the ID in the URI
				rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
				return rowsDeleted;
			default:
				throw new IllegalArgumentException("Cannot query unknown URI " + uri);
		}


		if (rowsDeleted > 0) {
			// notify all listeners of changes:
			try {
				getContext().getContentResolver().notifyChange(uri, null);
			} catch (NullPointerException e) {
				Log.e("InventoryProvider", "Problem in insert method with notify all listeners", e);
			}
		}

		//return number of rows deleted
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		// Data Validation
		// Returns true if this object has the Image value.
		if(values.containsKey(InventoryEntry.PRODUCT_IMAGE)){
			// If has value, take this value and check what is inside
			byte [] arrayCheck = values.getAsByteArray(InventoryEntry.PRODUCT_IMAGE);
			if(arrayCheck == null){
				Toast.makeText(getContext(), "Item requires a image", Toast.LENGTH_SHORT).show();
				// The method will return int = 0;
				return 0;
			}
		}
		// Returns true if this object has the named value.
		if (values.containsKey(InventoryEntry.PRODUCT_NAME)) {
			// If has value, take this value and check what is inside
			String nameCheck = values.getAsString(InventoryEntry.PRODUCT_NAME);
			if (nameCheck.equals("")) {
				Toast.makeText(getContext(), "Item requires a name", Toast.LENGTH_SHORT).show();
				// The method will return int = 0;
				return 0;
			}
		}

		// Returns true if this object has the email value.
		if (values.containsKey(InventoryEntry.SUPPLIER_EMAIL)) {
			// If has value, take this value and check what is inside
			String emailCheck = values.getAsString(InventoryEntry.SUPPLIER_EMAIL);
			if (emailCheck.equals("")) {
				Toast.makeText(getContext(), "Item requires email to supplier", Toast.LENGTH_SHORT).show();
				// The method will return int = 0;
				return 0;
			}
		}

		// Returns true if this object has the quantity value.
		if (values.containsKey(InventoryEntry.PRODUCT_QUANTITY)) {
			// If has value, take this value and check what is inside
			// Cast the int to Object - Integer
			Integer quantityCheck = values.getAsInteger(InventoryEntry.PRODUCT_QUANTITY);
			if (quantityCheck == null || quantityCheck < 0) {
				Toast.makeText(getContext(), "Item requires quantity value", Toast.LENGTH_SHORT).show();
				// The method will return int = 0;
				return 0;
			}
		}

		// Returns true if this object has the price value.
		if (values.containsKey(InventoryEntry.PRODUCT_PRICE)) {
			// If has value, take this value and check what is inside
			// Cast the int to Object - Integer
			Integer priceCheck = values.getAsInteger(InventoryEntry.PRODUCT_PRICE);
			if (priceCheck == null || priceCheck < 0) {
				Toast.makeText(getContext(), "Item requires price value", Toast.LENGTH_SHORT).show();
				// The method will return int = 0;
				return 0;
			}
		}

		// If there are no values to update, then don't try to update the database
		if (values.size() == 0) {
			return 0;
		}

		// Create and/or open a database to read from it
		SQLiteDatabase db = mInventoryDBHelper.getWritableDatabase();

		int rowsCorrected;
		int match = sUriMatcher.match(uri);

		switch (match) {
			case PARTS:
				rowsCorrected = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			case PART_ID:

				selection = InventoryEntry._ID + "=?";
				// WHERE   id = "/#";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				// Perform the update on the database and get the number of rows affected
				rowsCorrected = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Cannot query unknown URI " + uri);
		}

		// If 1 or more rows were updated, then notify all listeners that the data at the
		// given URI has changed
		if (rowsCorrected != 0) {
			// notify all listeners of changes:
			try {
				getContext().getContentResolver().notifyChange(uri, null);
			} catch (NullPointerException e) {
				Log.e("InventoryProvider", "Problem in insert method with notify all listeners", e);
			}
		}

		// Return the number of rows updated
		return rowsCorrected;
	}
}
