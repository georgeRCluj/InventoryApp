package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.Data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;
    ListView productListView;
    FloatingActionButton fab;
    View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUiComponents();
        setListenerOnAddProductButton();
        setCursorAdapterAndSetListenerOnListItems();
        initializeLoaderManager();
    }

    private void initializeUiComponents() {
        setContentView(R.layout.activity_inventory);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        productListView = (ListView) findViewById(R.id.text_view_product);
        emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);
    }

    private void setListenerOnAddProductButton() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, ProductDetailsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setCursorAdapterAndSetListenerOnListItems() {
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(InventoryActivity.this, ProductDetailsActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });
    }

    private void initializeLoaderManager() {
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    private void insertDummyProduct() {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Oranges");
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 5);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 3);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, "Oranges Supplier Inc.");

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyProduct();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.d("InventoryActivity", rowsDeleted + " rows deleted from products database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(this,               // Parent activity context
                ProductEntry.CONTENT_URI,           // Provider content URI to query
                projection,                         // Columns to include in the resulting Cursor
                null,                               // No selection clause
                null,                               // No selection arguments
                null);                              // Default sort order for the returned rows
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the (@link ProductCursorAdapter) with this new cursor containing product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}