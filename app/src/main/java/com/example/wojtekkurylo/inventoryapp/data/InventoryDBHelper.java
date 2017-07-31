package com.example.wojtekkurylo.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.wojtekkurylo.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by wojtekkurylo on 19.07.2017.
 */

public class InventoryDBHelper extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "InventoryStore.db";


	// Constants used while onCreate and/or onUpgrade is called by constructor
	public static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
					InventoryEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					InventoryEntry.PRODUCT_IMAGE + " BLOB," +
					InventoryEntry.PRODUCT_NAME + " TEXT NOT NULL," +
					InventoryEntry.PRODUCT_QUANTITY + " INTEGER DEFAULT 0," +
					InventoryEntry.PRODUCT_PRICE + " INTEGER DEFAULT 0," +
					InventoryEntry.SUPPLIER_EMAIL + " TEXT);";

	/**
	 * Public Constructor
	 *
	 * @param context of the correct activity
	 */
	public InventoryDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Upgrade if new version available :
		db.execSQL(SQL_CREATE_ENTRIES);
	}
}
