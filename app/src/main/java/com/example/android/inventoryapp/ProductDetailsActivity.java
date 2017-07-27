package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.Data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mSupplierEditText;
    private static final int EXISTING_PET_LOADER = 0;
    private Uri mCurrentProductUri;
    private boolean mProductHasChanged = false;
    private Button selectProductImageButton;
    private Button quantityAddButton;
    private Button quantitySubtractButton;
    private Button orderMoreButton;
    private ImageView productImageContainer;
    private byte[] imageForDbSave;
    public static final int PHOTO_MY_GALLERY_INTENT_CODE = 5;
    private Uri pickedImage;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeCurrentProduct();
        initializeUiComponents();
        setListenersOnTextFieldsAndButtons();
    }

    private void initializeCurrentProduct() {
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
    }

    private void initializeUiComponents() {
        setContentView(R.layout.activity_editor);
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        selectProductImageButton = (Button) findViewById(R.id.selectImageButtonId);
        productImageContainer = (ImageView) findViewById(R.id.productImageId);
        quantityAddButton = (Button) findViewById(R.id.quantity_add_buttonId);
        quantitySubtractButton = (Button) findViewById(R.id.quantity_subtract_buttonId);
        orderMoreButton = (Button) findViewById(R.id.order_moreButtonId);

        if (mCurrentProductUri != null) {
            setTitle(getResources().getString(R.string.editProduct));
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        } else {
            setTitle(getResources().getString(R.string.addProduct));
            quantitySubtractButton.setVisibility(View.GONE);
            quantityAddButton.setVisibility(View.GONE);
            orderMoreButton.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }
    }

    private void setListenersOnTextFieldsAndButtons() {
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);

        selectProductImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMyGalleryActivity();
            }
        });

        quantityAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrSubtractQuantity("add");
            }
        });

        quantitySubtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrSubtractQuantity("subtract");
            }
        });

        orderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mSupplierEditText.getText().toString().trim(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.orderEmailSubject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.orderEmailBody));
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
    }

    private void addOrSubtractQuantity(String addOrSubtract) {
        int quantityInt = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        if (addOrSubtract.equals("add")) {
            quantityInt++;
        } else {
            if (quantityInt > 0) {
                quantityInt--;
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.editor_update_negative_quantity), Toast.LENGTH_SHORT).show();
            }
        }
        final ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityInt);
        int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
        if (rowsAffected == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
        }
        mQuantityEditText.setText(quantityInt + "");
    }

    private void startMyGalleryActivity() {
        Intent myGalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(myGalleryIntent, PHOTO_MY_GALLERY_INTENT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_MY_GALLERY_INTENT_CODE && resultCode == Activity.RESULT_OK) {
            pickedImage = data.getData();
            if (pickedImage != null) {
                showImageOnTheScreen();
                prepareImageFormatForSavingIntoDatabase();
            }
        }
    }

    private void showImageOnTheScreen() {
        productImageContainer.setVisibility(View.VISIBLE);

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(pickedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        productImageContainer.setImageBitmap(BitmapFactory.decodeFile(picturePath));
    }

    private void prepareImageFormatForSavingIntoDatabase() {
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImage);
            imageForDbSave = getBitmapAsByteArray(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        if (nameString.isEmpty() || quantityString.isEmpty() || priceString.isEmpty()
                || supplierString.isEmpty() || imageForDbSave == null) {
            Toast.makeText(this, getResources().getString(R.string.fieldsUncompleted), Toast.LENGTH_LONG).show();
            return;
        } else if (!hasEmailPattern(supplierString)) {
            Toast.makeText(this, getResources().getString(R.string.validEmailAdressMessage), Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageForDbSave);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean hasEmailPattern(String emailToCheck) {
        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        Matcher emailMatch = pattern.matcher(emailToCheck);
        if (emailMatch.matches()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(ProductDetailsActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(ProductDetailsActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER};
        //TODO: have an unexplained error when trying to load the image saved, below line is related
        //TODO: error message - "Failed to read row 0, column 0 from a CursorWindow which has 0 rows, 6 columns."
        //TODO: "Make sure the Cursor is initialized correctly before accessing data from it."
        //ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            //TODO: have an unexplained error when trying to load the image saved, below line is related
            //TODO: error message - "Failed to read row 0, column 0 from a CursorWindow which has 0 rows, 6 columns."
            //TODO: "Make sure the Cursor is initialized correctly before accessing data from it."
            // int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            //TODO: have an unexplained error when trying to load the image saved, below line is related
            //TODO: error message - "Failed to read row 0, column 0 from a CursorWindow which has 0 rows, 6 columns."
            //TODO: "Make sure the Cursor is initialized correctly before accessing data from it."
            //byte[] imageByteFormat = cursor.getBlob(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(quantity + "");
            mPriceEditText.setText(price + "");
            mSupplierEditText.setText(supplier);
            //TODO: have an unexplained error when trying to load the image saved, below line is related
            //TODO: error message - "Failed to read row 0, column 0 from a CursorWindow which has 0 rows, 6 columns."
            //TODO: "Make sure the Cursor is initialized correctly before accessing data from it."
            // productImageContainer.setImageBitmap(getImageFromByteToBitmap(imageByteFormat));
        }
    }

    //TODO: have an unexplained error when trying to load the image saved, below method is related
    //TODO: error message - "Failed to read row 0, column 0 from a CursorWindow which has 0 rows, 6 columns."
    //TODO: "Make sure the Cursor is initialized correctly before accessing data from it."
    private Bitmap getImageFromByteToBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
    }


}