package com.prakriti.customprovider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText edtName, edtClass;
    private String studentName, studentClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtName = findViewById(R.id.edtName);
        edtClass = findViewById(R.id.edtClass);
    }

    public void addStudentRecord(View view) {
        // Add new student record
        if(edtName.getText().toString().equals("") || edtClass.getText().toString().equals("")) {
            Toast.makeText(this, R.string.empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        studentName = edtName.getText().toString();
        studentClass = edtClass.getText().toString();

        ContentValues values = new ContentValues();
        values.put(DataProvider.NAME, studentName);
        values.put(DataProvider.CLASS, studentClass);

        Uri uri = getContentResolver().insert(DataProvider.CONTENT_URI, values);
        Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();

        edtName.setText("");
        edtClass.setText("");
    }

    public void retrieveStudentRecords(View view) {
        // Retrieve all student records
        String dataURL = "content://com.prakriti.customprovider.DataProvider";
        Cursor cursor = getContentResolver().query(Uri.parse(dataURL), null, null, null, "name");

        if (cursor.moveToFirst()) {
            do{
                Toast.makeText(this,
                        cursor.getString(cursor.getColumnIndex(DataProvider._ID)) +
                                ", " +  cursor.getString(cursor.getColumnIndex(DataProvider.NAME)) +
                                ", " + cursor.getString(cursor.getColumnIndex(DataProvider.CLASS)),
                        Toast.LENGTH_SHORT).show();
            } while (cursor.moveToNext());
        }
    }


}