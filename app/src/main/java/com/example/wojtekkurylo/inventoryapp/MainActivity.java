package com.example.wojtekkurylo.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.wojtekkurylo.inventoryapp.data.InventoryContract;
import com.example.wojtekkurylo.inventoryapp.data.InventoryDBHelper;
import com.example.wojtekkurylo.inventoryapp.data.InventoryContract.InventoryEntry;

import java.net.MalformedURLException;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	// LoaderManager allows to perform action in background - without freezing app
	/**
	 * Instance of InventoryDBHelper Class, which initiate onCreate SQL database
	 */
	private InventoryDBHelper mInventoryDBHelper;

	/**
	 * Instance of InventoryCursorAdapter Class (subclass of CursorAdapter)
	 */
	private InventoryCursorAdapter mInventoryCursorAdapter;

	/**
	 * Identifies a particular Loader being used in this component
	 */
	private static final int URL_LOADER = 0;

	/**
	 * Returned Cursor on completion onLoadFinish() method
	 */
	private Cursor mCursor;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Calling DBHelper constructor to create SQLite database or check if exist
		mInventoryDBHelper = new InventoryDBHelper(MainActivity.this);

		// Connect the Floating button and perform action while clicked
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Fire up next activity
				Intent intent = new Intent(MainActivity.this, DetailActivity.class);
				startActivity(intent);
			}
		});

		// Find the ListView which will be populated with the Items data
		final ListView listView = (ListView) findViewById(R.id.list);

		// Find and set empty view on the ListView, so that it only shows when the list has 0 items.
		View emptyView = findViewById(R.id.empty_view);
		listView.setEmptyView(emptyView);

		// Setup an Adapter to create a list item for each row of pet data in the Cursor.
		// null will be replaced by Cursor Object while onLoadFinished completed;
		mInventoryCursorAdapter = new InventoryCursorAdapter(MainActivity.this, null);

		// Attach the adapter to the ListView.
		listView.setAdapter(mInventoryCursorAdapter);

		// On Click the single List Item go to Detail Activity of that product
		// && send uri associated with clicked List Item
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Get the email to supplier from the clicked item in list view
				String emailToSupplierString = mCursor.getString(mCursor.getColumnIndexOrThrow(InventoryEntry.SUPPLIER_EMAIL));
				String nameProduct = mCursor.getString(mCursor.getColumnIndexOrThrow(InventoryEntry.PRODUCT_NAME));
				// add to "general" Uri the ID of row selected.
				// Android automatically add "/"
				// content://com.example.android.pets/pets/ID
				Uri uriSelectedId = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

				// Fire up next activity && sending to EditorActivity Object details
				Intent intent = new Intent(MainActivity.this, DetailActivity.class);

				Bundle extras = new Bundle();
				extras.putString("nameProduct", nameProduct);
				extras.putString("emailToSupplierString", emailToSupplierString);
				// insert data
				intent.setData(uriSelectedId);
				intent.putExtras(extras);

				startActivity(intent);

			}
		});

		/**
		 * Initializes the CursorLoader. The URL_LOADER value is eventually passed
		 * to onCreateLoader().
		 *
		 * Prepare the loader.  Either re-connect with an existing one,
		 * or start a new one.
		 */
		getLoaderManager().initLoader(URL_LOADER, null, this);
	}

	// As per Android Lifecycle, after EditorActivity finish() this activity starts from onStart() method
	@Override
	protected void onStart() {
		super.onStart();

		getLoaderManager().restartLoader(URL_LOADER, null, this);
	}

	@Override
	// This is called when a new Loader needs to be created.
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
		CursorLoader cursorLoader = new CursorLoader(MainActivity.this,
				InventoryEntry.CONTENT_URI,
				projection,
				null,
				null,
				null);

		// returns cursorLoader to onLoadFinished method and change the cursorloader into Cursor
		return cursorLoader;
	}

	@Override
	// This method is called when a previously created loader has finished its load.
	// This method is guaranteed to be called prior to the release of the last data that was supplied for this loader.
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mCursor = data;
		// Swap the new cursor in.  (The framework will take care of closing the
		// old cursor once we return.)
		mInventoryCursorAdapter.swapCursor(mCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed.  We need to make sure we are no
		// longer using it.
		mInventoryCursorAdapter.swapCursor(null);
	}

	public void deleteAll() {

		// Defines a variable to contain the number of rows deleted
		int rowsDeleted = 0;

		// Deletes whole row as per URI
		rowsDeleted = getContentResolver().delete(
				InventoryEntry.CONTENT_URI,    // the user dictionary content URI
				null,                            // the column to select on
				null                            // the value to compare to
		);

		// Display Toast message if succesfully or failed
		if (rowsDeleted == 0) {
			Toast.makeText(MainActivity.this, "Problem with deleting table", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MainActivity.this, "Table deleted", Toast.LENGTH_SHORT).show();
		}


	}

	/**
	 * CODE BELOW IS ASSOCIATED WITH CREATING MENU
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu options from the res/menu/menu_editor.xml file.
		// This adds menu items to the app bar.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// User clicked on a menu option in the app bar overflow menu
		switch (item.getItemId()) {
			// Respond to a click on the "Delete" menu option
			case R.id.action_delete:
				// Delete the entrys
				showConfirmationDialog();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Show a dialog that ask the user to confirm deleting
	 */
	private void showConfirmationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage(R.string.delete_all);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Intent to delete all table
				deleteAll();
				// close the dialog.
				dialog.dismiss();
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


}
