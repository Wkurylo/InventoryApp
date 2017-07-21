package com.example.wojtekkurylo.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wojtekkurylo.inventoryapp.data.InventoryContract;
import com.example.wojtekkurylo.inventoryapp.data.InventoryContract.InventoryEntry;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.bitmap;
import static android.graphics.Bitmap.createScaledBitmap;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	// Cast all Views
	@BindView(R.id.image_detail)
	ImageView mImageView;
	@BindView(R.id.product_name)
	EditText mProductName;
	@BindView(R.id.product_manufacturer_email)
	EditText mSupplierEmail;
	@BindView(R.id.minus_one)
	Button mRemoveOne;
	@BindView(R.id.plus_one)
	Button mAddOne;
	@BindView(R.id.quantity_value)
	EditText mQuantity;
	@BindView(R.id.product_price)
	EditText mPrice;
	@BindView(R.id.order_button)
	Button mOrderMore;

	/**
	 * Final value used by camera intent
	 */
	static final int REQUEST_IMAGE_CAPTURE = 1;

	/**
	 * Final value used by gallery intent
	 */
	static final int RESULT_LOAD_IMG = 2;

	/**
	 * Image stored in Bitmap received from Camera
	 */
	private Bitmap mCameraBitmap = null;

	/**
	 * Image stored in Bitmap received from Gallery
	 */
	private Bitmap mGalleryBitmap = null;

	/**
	 * Image stored in byte[] array
	 */
	private byte[] mCameraInput = null;

	/**
	 * Image stored in byte[] array
	 */
	private byte[] mGalleryInput = null;

	/**
	 * Uri with appended Id associated with Id of row clicked in MainActivity
	 */
	private Uri mUriSelectedWithId;

	/**
	 * String with appended Email associated with Id of row clicked in MainActivity
	 */
	private String mEmailToSupplier;

	/**
	 * String with appended Name associated with Id of row clicked in MainActivity
	 */
	private String mNameProduct;

	/**
	 * Identifies a particular Loader being used in this component
	 */
	private static final int URL_LOADER = 1;

	/**
	 * Visability mOrder Checker
	 */
	private int visabilityCheck = 1;

	/**
	 * Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
	 */
	private boolean mInventoryHasChanged = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		// ButterKnife == findViewById() - performing the action for @BindViews
		ButterKnife.bind(DetailActivity.this);

		// Setup OnTouchListeners on all the input fields, so we can determine if the user
		// has touched or modified them. This will let us know if there are unsaved changes
		// or not, if the user tries to leave the editor without saving.
		mImageView.setOnTouchListener(mTouchListener);
		mProductName.setOnTouchListener(mTouchListener);
		mSupplierEmail.setOnTouchListener(mTouchListener);
		mRemoveOne.setOnTouchListener(mTouchListener);
		mAddOne.setOnTouchListener(mTouchListener);
		mQuantity.setOnTouchListener(mTouchListener);
		mPrice.setOnTouchListener(mTouchListener);

		//Receiving Object details to set up each child of ListView
		Intent intent = getIntent();
		// Get data
		mUriSelectedWithId = intent.getData();

		// Entered : EDIT MODE
		if (mUriSelectedWithId != null) {
			setTitle(R.string.edit_item);

			// Get data If exist (while clicking on existing entry in ListView)
			Bundle extras = intent.getExtras();
			mEmailToSupplier = extras.getString("emailToSupplierString");
			mNameProduct = extras.getString("nameProduct");

			if (visabilityCheck != 1) {
				mOrderMore.setVisibility(View.VISIBLE);
				visabilityCheck = 1;
			}

			/**
			 * Initializes the CursorLoader. The URL_LOADER value is eventually passed
			 * to onCreateLoader().
			 *
			 * Prepare the loader.  Either re-connect with an existing one,
			 * or start a new one.
			 */
			getLoaderManager().initLoader(URL_LOADER, null, this);

		} else {
			// Entered : ADD MODE
			setTitle(R.string.add_item);
			if (visabilityCheck == 1) {
				mOrderMore.setVisibility(View.GONE);
				visabilityCheck = 0;
			}
			// While in ADD mode we donnot need to display Delete option for current Item
			invalidateOptionsMenu();


		}

		// onClickListener to open DialogBox with types of image resource
		mImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddSourceDialog();
			}
		});

		// While opening "Add item" via Floating Button set the default value to 0;
		mQuantity.setText(String.valueOf("0"));
		// Changing the quantity value via buttons
		mAddOne.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String productQuantityBeforeAdd = mQuantity.getText().toString().toLowerCase().trim();
				int productQuantityBeforeAddInt = Integer.parseInt(productQuantityBeforeAdd);
				if (productQuantityBeforeAddInt <= 149) {
					productQuantityBeforeAddInt++;
				} else {
					productQuantityBeforeAddInt = 150;
					Toast.makeText(DetailActivity.this, getString(R.string.max_value), Toast.LENGTH_SHORT).show();
				}
				mQuantity.setText(String.valueOf(productQuantityBeforeAddInt));
			}
		});
		// Changing the quantity value via buttons
		mRemoveOne.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String productQuantityBeforeRemove = mQuantity.getText().toString().toLowerCase().trim();
				int productQuantityBeforeRemoveInt = Integer.parseInt(productQuantityBeforeRemove);
				if (productQuantityBeforeRemoveInt > 0) {
					productQuantityBeforeRemoveInt--;
				} else {
					productQuantityBeforeRemoveInt = 0;
					Toast.makeText(DetailActivity.this, getString(R.string.min_value), Toast.LENGTH_SHORT).show();
				}
				mQuantity.setText(String.valueOf(productQuantityBeforeRemoveInt));
			}
		});

		/**
		 * Button to OrderMore : EDIT MODE
		 * intent implicit to email app
		 */
		mOrderMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
						"mailto", mEmailToSupplier, null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, mNameProduct);
				emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body));
				startActivity(Intent.createChooser(emailIntent, "Send email..."));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu options from the res/menu/menu_editor.xml file.
		// This adds menu items to the app bar.
		getMenuInflater().inflate(R.menu.menu_editor, menu);
		return true;
	}

	//This is called right before the menu is shown,
	//every time it is shown. You can use this method to efficiently enable/disable
	// items or otherwise dynamically modify the contents.
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// If this is a new pet, hide the "Delete" menu item.
		if (mUriSelectedWithId == null) {
			MenuItem menuItem = menu.findItem(R.id.action_delete);
			menuItem.setVisible(false);
		}
		//You must return true for the menu to be displayed; if you return false it will not be shown.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// User clicked on a menu option in the app bar overflow menu
		switch (item.getItemId()) {
			// Respond to a click on the "Save" menu option
			case R.id.action_save:
				// Save the user entry
				saveItem();
				return true;
			// Respond to a click on the "Delete" menu option
			case R.id.action_delete:
				// Delete the entry Available : EDIT ACTIVITY
				if (mUriSelectedWithId != null) {
					showConfirmationDialog();
				}
				return true;
			// Respond to a click on the "Up" arrow button in the app bar
			case android.R.id.home:
				// Navigate back to parent activity (CatalogActivity)
				// Back arrow on left side of screen
				if (!mInventoryHasChanged) {
					NavUtils.navigateUpFromSameTask(this);
				} else {
					// Otherwise if there are unsaved changes, setup a dialog to warn the user.
					// Create a click listener to handle the user confirming that
					// changes should be discarded.
					showConfirmationSaveDialog();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void deleteSingleItem() {
		// Defines a variable to contain the number of rows deleted
		int rowsDeleted = 0;

		// Deletes whole row as per URI
		rowsDeleted = getContentResolver().delete(
				mUriSelectedWithId,   // the user dictionary content URI
				null,                    // the column to select on
				null                      // the value to compare to
		);

		// Display Toast message if succesfully or failed
		if (rowsDeleted == 0) {
			Toast.makeText(DetailActivity.this, getString(R.string.problem_delete), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(DetailActivity.this, getString(R.string.row_deleted), Toast.LENGTH_SHORT).show();
		}
	}

	// Method called via action-save button in Top menu
	// Method call ContentResolver to save data in SQL via ContentProider
	public void saveItem() {

		// Defines a new Uri object that receives the result of the insertion
		Uri newUri;

		// image in SQL is stored in byte [] array
		// change Bitmap to byte[] array
		if (mGalleryBitmap != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			mGalleryBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
			mGalleryInput = stream.toByteArray();

		} else if (mCameraBitmap != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			mCameraBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
			mCameraInput = stream.toByteArray();
		}

		String productName = mProductName.getText().toString().toLowerCase().trim();
		String productEmail = mSupplierEmail.getText().toString().toLowerCase().trim();
		String productQuantity = mQuantity.getText().toString().toLowerCase().trim();
		if (productQuantity.equals("")) {
			productQuantity = "-1";
		}

		//Change from String to Integer Object
		Integer productQuantityInt = Integer.parseInt(productQuantity);

		String productPrice = mPrice.getText().toString().toLowerCase().trim();
		if (productPrice.equals("")) {
			productPrice = "-1";
		}
		//Change from String to Integer Object
		Integer productPriceInt = Integer.parseInt(productPrice);

		/**
		 * If mUriSelectedWithId do not has value, means User entered the ADD MODE
		 * required method : insert()
		 */
		if (mUriSelectedWithId == null) {
			// Create a ContentValues object where column names are the keys,
			// and pet attributes from the editor are the values.
			ContentValues contentValues = new ContentValues();
			if (mCameraInput != null) {
				contentValues.put(InventoryEntry.PRODUCT_IMAGE, mCameraInput);
			} else if (mGalleryInput != null) {
				contentValues.put(InventoryEntry.PRODUCT_IMAGE, mGalleryInput);
			} else {
				byte[] emptyArray = null;
				contentValues.put(InventoryEntry.PRODUCT_IMAGE, emptyArray);
			}

			contentValues.put(InventoryEntry.SUPPLIER_EMAIL, productEmail);
			contentValues.put(InventoryEntry.PRODUCT_NAME, productName);
			contentValues.put(InventoryEntry.PRODUCT_QUANTITY, productQuantityInt);
			contentValues.put(InventoryEntry.PRODUCT_PRICE, productPriceInt);


			// Insert a new row for pet in the database, returning the ID of that new row.
			newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, contentValues);

			// Show a toast message depending on whether or not the insertion was successful
			if (newUri == null) {
				// If the new content URI is null, then there was an error with insertion.
				Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
						Toast.LENGTH_SHORT).show();
			} else {
				// get from URI - the row ID
				long parseID = ContentUris.parseId(newUri);
				// Otherwise, the insertion was successful and we can display a toast.
				Toast.makeText(this, getString(R.string.editor_insert_pet_successful) + parseID,
						Toast.LENGTH_SHORT).show();
				// Succesfully inserted data so we can: finish this activity & go back to previous
				finish();
			}
			/**
			 * If mUriSelectedWithId DO has value, means User entered the EDIT MODE
			 * required method : update()
			 */
		} else {

			// Create a ContentValues with value, where columns names are the keys;
			ContentValues updatesValues = new ContentValues();
			if (mCameraInput != null) {
				updatesValues.put(InventoryEntry.PRODUCT_IMAGE, mCameraInput);
			} else if (mGalleryInput != null) {
				updatesValues.put(InventoryEntry.PRODUCT_IMAGE, mGalleryInput);
			} else {
				byte[] existingArray = checkIfExist();
				updatesValues.put(InventoryEntry.PRODUCT_IMAGE, existingArray);
			}
			updatesValues.put(InventoryEntry.SUPPLIER_EMAIL, productEmail);
			updatesValues.put(InventoryEntry.PRODUCT_NAME, productName);
			updatesValues.put(InventoryEntry.PRODUCT_QUANTITY, productQuantityInt);
			updatesValues.put(InventoryEntry.PRODUCT_PRICE, productPriceInt);

			// Defines a variable to contain the number of updated rows
			int rowsUpdated = 0;

			rowsUpdated = getContentResolver().update(
					mUriSelectedWithId,                      // the user dictionary content URI
					updatesValues,                          // the columns to update
					null,                                   // the column to select on
					null);                                  // the value to compare to

			// Show a toast message depending on whether or not the insertion was successful
			if (rowsUpdated == 0) {
				// If rowsUpdated == 0, then there was no update
				Toast.makeText(this, getString(R.string.upss), Toast.LENGTH_SHORT).show();
			} else {
				// Otherwise, the insertion was successful and we can display a toast.
				Toast.makeText(this, "Rows updated: " + rowsUpdated, Toast.LENGTH_SHORT).show();
				// Succesfully updated data so we can: finish this activity & go back to previous
				finish();
			}

		}
	}

	/**
	 * Show a dialog that ask the user to select the image source
	 * <p>
	 * CODE BELOW IS ASSOCIATED WITH SELECTING / MAKING Image && Saving it.
	 */
	private void showAddSourceDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
		builder.setMessage(R.string.image_resource);
		// Add the buttons
		builder.setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Intent to make the photo by camera
				takePhotoByCameraIntent();
				// close the dialog.
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.device, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Intent to selec photo from gallery
				selectImageFromGalleryIntent();
				// close the dialog.
				dialog.dismiss();
			}
		});
		// Create and show the AlertDialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

	}

	// function that invokes an intent to capture a photo.
	private void takePhotoByCameraIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	// function that allows user to select image from Gallery
	private void selectImageFromGalleryIntent() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
	}

	// The Android Camera application encodes the photo in the return
	// Intent delivered to onActivityResult() as a small Bitmap in the extras, under the key "data".
	// The following code retrieves this image and displays it in an ImageView.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Receiving photo from Camera
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			Bitmap cameraBitmap = (Bitmap) extras.get("data");

			// resize bitmap
			mCameraBitmap = Bitmap.createScaledBitmap(cameraBitmap, 100, 100, false);
			//mCameraBitmap.recycle();
			mImageView.setImageBitmap(mCameraBitmap);

			// Receiving photo from Gallery
		} else if (resultCode == RESULT_OK) {
			try {
				Uri imageUri = data.getData();
				InputStream imageStream = getContentResolver().openInputStream(imageUri);
				Bitmap galleryBitmap = BitmapFactory.decodeStream(imageStream);

				// resize bitmap
				mGalleryBitmap = createScaledBitmap(galleryBitmap, 100, 100, false);
				//mGalleryBitmap.recycle();
				mImageView.setImageBitmap(mGalleryBitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(DetailActivity.this, getString(R.string.upss), Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(DetailActivity.this, getString(R.string.image_not_selected), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Initialized via LoaderManager
	 * <p>
	 * CODE BELOW IS ASSOCIATED WITH EDIT existing ITEM
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// The columns of choice
		String[] projection = {
				InventoryEntry._ID,
				InventoryEntry.PRODUCT_IMAGE,
				InventoryEntry.PRODUCT_NAME,
				InventoryEntry.PRODUCT_QUANTITY,
				InventoryEntry.PRODUCT_PRICE,
				InventoryEntry.SUPPLIER_EMAIL
		};

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		// CursorLoader query ContentResolver, which query ContentProvider
		CursorLoader cursorLoader = new CursorLoader(DetailActivity.this,
				mUriSelectedWithId,
				projection,
				null,
				null,
				null);

		// returns cursorLoader to onLoadFinished method and change the cursorloader into Cursor
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		// Read the data from the row at pos 0'th
		// Originally the position in @ -1 (row with column's name)
		data.moveToPosition(0);

		// Taking the column ID
		int imageColumnIndex = data.getColumnIndexOrThrow(InventoryEntry.PRODUCT_IMAGE);
		int emailColumnIndex = data.getColumnIndexOrThrow(InventoryEntry.SUPPLIER_EMAIL);
		int nameColumnIndex = data.getColumnIndexOrThrow(InventoryEntry.PRODUCT_NAME);
		int quantityColumnIndex = data.getColumnIndexOrThrow(InventoryEntry.PRODUCT_QUANTITY);
		int priceColumnIndex = data.getColumnIndexOrThrow(InventoryEntry.PRODUCT_PRICE);

		// Receiving the rest of resources:
		String nameInput = data.getString(nameColumnIndex);
		String emailInput = data.getString(emailColumnIndex);
		String quantityInput = data.getString(quantityColumnIndex);
		// If quantityInput == "", than display "N/A" in list_item
		if (quantityInput.equals("")) {
			quantityInput = "0";
		}
		String priceInput = data.getString(priceColumnIndex);
		// If priceInput == "", than display "N/A" in list_item
		if (priceInput.equals("")) {
			priceInput = "0";
		}

		// Receiving resources associated with column index and row of interest
		// Image in SQLite is stored as byte array
		if (data.getBlob(imageColumnIndex) != null) {
			byte[] imageInput = data.getBlob(imageColumnIndex);
			Bitmap bmp = BitmapFactory.decodeByteArray(imageInput, 0, imageInput.length);

			// Setting the value from cursor to the list_item.xml
			mImageView.setImageBitmap(bmp);
		} else {
			// If there was no photo added, add default
			mImageView.setImageResource(R.drawable.add_image);
		}
		mProductName.setText(nameInput);
		mQuantity.setText(quantityInput);
		mPrice.setText(priceInput);
		mSupplierEmail.setText(emailInput);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mImageView.setImageResource(R.drawable.add_image);
		mProductName.setText("");
		mQuantity.setText("");
		mPrice.setText("");
		mSupplierEmail.setText("");
	}

	/**
	 * Show a dialog that ask the user to confirm deleting
	 */
	private void showConfirmationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
		builder.setMessage(R.string.delete_row);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Intent to delete all table
				deleteSingleItem();
				// close the dialog.
				dialog.dismiss();
				// finish this activity & go back to previous
				finish();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// close the dialog.
				dialog.dismiss();
			}
		});
		// Create and show the AlertDialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

	}

	/**
	 * OnTouchListener that listens for any user touches on a View, implying that they are modifying
	 * the view, and we change the mInventoryHasChanged boolean to true.
	 */
	private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			mInventoryHasChanged = true;
			//True if the listener has consumed the event, false otherwise.
			return false;
		}
	};

	/**
	 * Show a dialog that ask the user to confirm exit without saving changes
	 */
	private void showConfirmationSaveDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
		builder.setMessage(R.string.confirm_exit);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked "Discard" button, close the current activity.
				Toast.makeText(DetailActivity.this, getString(R.string.without_saving), Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// close the dialog && continue editing/adding Item
				dialog.dismiss();
			}
		});
		// Create and show the AlertDialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

	}

	// returning byte[] array of existing image
	// Available : EDIT mode
	private byte[] checkIfExist() {

		// The columns of choice
		String[] projection = {
				InventoryEntry._ID,
				InventoryEntry.PRODUCT_IMAGE,
		};

		Cursor cursor = getContentResolver().query(mUriSelectedWithId, projection, null, null, null);

		// Cursor returns the columns header and the row of interest
		// I am interested in row below column headers
		int position = cursor.getPosition();
		Log.e("Detail Activity", "Original cursor position: " + position);
		// move the cursor to the row with data
		cursor.moveToNext();
		int positionSecond = cursor.getPosition();
		Log.e("Detail Activity", "Cursor after movement in position: " + positionSecond);
		// return the byte array of existing image
		byte[] byteArrayCheck = cursor.getBlob(cursor.getColumnIndexOrThrow(InventoryEntry.PRODUCT_IMAGE));
		return byteArrayCheck;
	}
}
