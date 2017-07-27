package com.example.android.inventoryapp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.Data.ProductContract.ProductEntry;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "inventory.db";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase productsDataBase) {
        final String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB NOT NULL);";

        productsDataBase.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
