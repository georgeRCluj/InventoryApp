package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.Data.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {
    private Button saleButton;
    private TextView textViewProductName;
    private TextView textViewProductSummary;
    private int productID;
    private int productQuantity;
    private int productPrice;
    private String productName;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        initializeUiComponents(view);
        updateUiBasedOnDataFromCursor(cursor);
        updateQuantityOnSale(context, productID, productQuantity);
    }

    private void initializeUiComponents(View view) {
        textViewProductName = (TextView) view.findViewById(R.id.name);
        textViewProductSummary = (TextView) view.findViewById(R.id.summary);
        saleButton = (Button) view.findViewById(R.id.saleButtonId);
        saleButton.setFocusable(false);
    }

    private void updateUiBasedOnDataFromCursor(Cursor cursor) {
        // Find the columns of product attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

        // Extract properties from cursor
        productID = cursor.getInt(idColumnIndex);
        productName = cursor.getString(nameColumnIndex);
        productQuantity = cursor.getInt(quantityColumnIndex);
        productPrice = cursor.getInt(priceColumnIndex);

        // Populate fields with extracted properties
        textViewProductName.setText(productName);
        textViewProductSummary.setText(String.valueOf("Current quantity: " + productQuantity + " kg\nPrice: $" + productPrice));
    }

    private void updateQuantityOnSale(Context context, int productID, int productQuantity) {
        final Context contextInListener = context;
        final Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productID);
        final ContentValues values = new ContentValues();
        final int productQuantityDecreased = productQuantity - 1;
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantityDecreased);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productQuantityDecreased >= 0) {
                    int rowsAffected = contextInListener.getContentResolver().update(currentProductUri, values, null, null);
                    if (rowsAffected == 0) {
                        Toast.makeText(contextInListener, contextInListener.getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(contextInListener, contextInListener.getString(R.string.editor_update_negative_quantity), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}