package lopez.armando.contactosapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
/*
Tarea como hacer tu propio content provaider
 */
public class MainActivity extends AppCompatActivity {

    private final int PICK_CONTACT = 1;
    private Uri contactoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1000:
                if (grantResults.length < 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    return;
                }else {
                    finish();
                }
                return;
        }
    }

    public void initSeleccionarContacto(View v){
        Intent i = new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(i,PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT){
            if(resultCode == RESULT_OK){
                contactoUri = data.getData();
                recibirContacto(contactoUri);
            }
        }
    }

    public void recibirContacto(Uri uri){
        TextView nombre = (TextView) findViewById(R.id.nombreContacto);
        TextView telefono = (TextView) findViewById(R.id.telefonoContacto);
        ImageView foto = (ImageView) findViewById(R.id.fotoContacto);
        TextView correo= (TextView) findViewById(R.id.correoContacto);
        nombre.setText(getNombre(uri));
        telefono.setText(getTelefono(uri));
        foto.setImageBitmap(getFoto(uri));
        correo.setText(getCorreo(uri));
    }

    private String getCorreo(Uri uri){
        String correo=null;
        String id=null;

        Cursor contactoCursor = getContentResolver().query(
                uri,
                new String[]{ContactsContract.Contacts._ID},
                null,
                null,
                null
        );

        if (contactoCursor.moveToFirst()){
            id = contactoCursor.getString(0);
        }
        contactoCursor.close();

        String selectionArg = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "  = ? AND " +
                ContactsContract.CommonDataKinds.Email.DATA + " = " +
                ContactsContract.CommonDataKinds.Email.DATA;

        Cursor telefonoCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS},
                selectionArg,
                new String[]{id},
                null
        );

        if (telefonoCursor.moveToFirst()){
            correo = telefonoCursor.getString(0);
        }
        telefonoCursor.close();

        return correo;
    }

    private String getNombre(Uri uri) {
        String nombre = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(
                uri,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                null,
                null,
                null
        );
        if (c.moveToFirst()){
            nombre = c.getString(0);
        }
        c.close();
        return nombre;
    }
    private String getTelefono(Uri uri){
        String id = null;
        String telefono = null;

        Cursor contactoCursor = getContentResolver().query(
                uri,
                new String[]{ContactsContract.Contacts._ID},
                null,
                null,
                null
        );

        if (contactoCursor.moveToFirst()){
            id = contactoCursor.getString(0);
        }
        contactoCursor.close();

        String selectionArg = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "  = ? AND " +
                ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

        Cursor telefonoCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                selectionArg,
                new String[]{id},
                null
        );

        if (telefonoCursor.moveToFirst()){
            telefono = telefonoCursor.getString(0);
        }
        telefonoCursor.close();

        return telefono;
    }

    private Bitmap getFoto(Uri uri){
        Bitmap foto = null;
        String id = null;
        Cursor contactoCursor = getContentResolver().query(
                uri,
                new String[]{ContactsContract.Contacts._ID},
                null,
                null,
                null
        );

        if (contactoCursor.moveToFirst()){
            id = contactoCursor.getString(0);
        }
        try {
            Uri contactoUri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    Long.parseLong(id)
            );
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                    getContentResolver(),
                    contactoUri
            );
            if (input != null){
                foto = BitmapFactory.decodeStream(input);
                input.close();
            }
        } catch (IOException ioe){

        }
        contactoCursor.close();
        return foto;
    }

}


