package com.example.wojtekkurylo.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by wojtekkurylo on 19.07.2017.
 */

public class InventoryContract {

	/**
	 * Content Authority for URI request (equal to android:authorities= in ANDROID MANIFEST)
	 */
	public static final String CONTENT_AUTHORITY = "com.example.wojtekkurylo.inventoryapp";

	/**
	 * To make this a usable URI, we use the parse method which takes in a URI string and returns a Uri.
	 */
	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/**
	 * This store the path to Table "parts" in "InventoryStore.db"
	 */
	public static final String PATH_PARTS = "parts";

	/**
	 * Private constructor to prevent instantianting contract class
	 */
	private InventoryContract() {
		throw new AssertionError("No InventoryContract Instance for You !");
	}

	/**
	 * Inner Class defines the DailyRoutine table contents in constants
	 */
	public static class InventoryEntry implements BaseColumns {

		/**
		 * The MIME type of the {@link #CONTENT_URI} for a list of pets.
		 */
		public static final String CONTENT_LIST_TYPE =
				ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PARTS;

		/**
		 * The MIME type of the {@link #CONTENT_URI} for a single pet.
		 */
		public static final String CONTENT_ITEM_TYPE =
				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PARTS;


		public static final String TABLE_NAME = "parts";
		/**
		 * Names of columns headers stored in constants
		 */
		public static final String ID = BaseColumns._ID;
		public static final String PRODUCT_IMAGE = "product_image";
		public static final String PRODUCT_NAME = "product_name";
		public static final String PRODUCT_QUANTITY = "product_quantity";
		public static final String PRODUCT_PRICE = "product_price";
		public static final String SUPPLIER_EMAIL = "supplier_email";

		/**
		 * Uri used to query for all rows and columns in table and to insert new item
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PARTS);
	}
}
