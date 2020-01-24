package com.guilcapi.crowdfoundingadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdministracionActivity extends AppCompatActivity {
    private String usuarioRegistrado;

    private EditText nombreProducto, descripcionProducto, emailProducto, telefonoProducto;
    private Button btnimagen, btnArchivo, btnPublicar;
    private ImageView productoImg;
    private String productoNombre, productoDescr, productoEmail, productoTelefono;
    private Intent intent;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private String ID;
    private Uri imageUri, archivoUri;

    //La carpeta contenedora en storage
    private StorageReference ProductImageRef;
    //La rama que contendra los productos
    private DatabaseReference ProductsRef;
    //Para los permisos de la galeria
    private static final int galleryPick = 1;
    private static final int galleryPick2 = 2;

    private String productRandomKey,    downloadImageUrl, downloadArchivoUrl;
    private String  saveCurrentDate, saveCurrentTime;

    private static final int PHOTO_PRODUCT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administracion);

        getSupportActionBar().setTitle("Agregar Producto");

        ProductImageRef = FirebaseStorage.getInstance().getReference().child("Products");
        ProductsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        usuarioRegistrado = getIntent().getExtras().getString("USER");
        Toast.makeText(AdministracionActivity.this, usuarioRegistrado,
                Toast.LENGTH_LONG).show();

        nombreProducto = findViewById(R.id.nombreProducto);
        descripcionProducto = findViewById(R.id.descripcionProducto);
        emailProducto = findViewById(R.id.emailProducto);
        telefonoProducto = findViewById(R.id.telefonoProducto);
        btnimagen = findViewById(R.id.btnImagen);
        btnArchivo = findViewById(R.id.btnArchivo);
        btnPublicar = findViewById(R.id.btnPublicar);
        productoImg = findViewById(R.id.posterProducto);

        btnimagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent, "Selecciona una Foto"), PHOTO_PRODUCT);*/
                OpenGallery();

            }
        });
        btnArchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDocuments();
            }
        });
        btnPublicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();

            }
        });


    }

    private void OpenDocuments() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("*/*");
        startActivityForResult(galleryIntent, galleryPick2);
    }


    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data!=null){
            imageUri = data.getData();
            productoImg.setImageURI(imageUri);

        }
        if (requestCode == galleryPick2 && resultCode == RESULT_OK && data!=null){
            archivoUri = data.getData();
            Toast.makeText(this, ":)",Toast.LENGTH_SHORT).show();

        }
    }

    private void ValidateProductData() {

        productoNombre = nombreProducto.getText().toString();
        productoDescr = descripcionProducto.getText().toString();
        productoEmail = emailProducto.getText().toString();
        productoTelefono = telefonoProducto.getText().toString();

        //validamos la imagen
        if(imageUri == null){
            Toast.makeText(this, "La imagen es obligatoria....",Toast.LENGTH_SHORT).show();
        }else if(archivoUri == null){
            Toast.makeText(this, "No ha seleccionado ningun archivo....",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(productoNombre)){
            Toast.makeText(this, "El Nombre es Obligatorii....",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(productoDescr)){
            Toast.makeText(this, "La descripcion es Obligatoria....",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(productoEmail)){
            Toast.makeText(this, "El correo es Obligatorio....",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(productoTelefono)){
            Toast.makeText(this, "El telefono es Obligatorio....",Toast.LENGTH_SHORT).show();
        }
        else{
            StoreProductInformation();
        }
    }

    private void StoreProductInformation() {
        //Creamos un key unico para el nuevo elemnto  formado de la fecha u la hora
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;

        //Guardamos la imagen en Cloud Storage
        final StorageReference filePath = ProductImageRef.child(imageUri.getLastPathSegment() + productRandomKey+".jpg");
        //Guardamos el archivo en Cloud Storage
        final StorageReference filePath2 = ProductImageRef.child(archivoUri.getLastPathSegment() + productRandomKey+".pdf");

        final UploadTask uploadTask = filePath.putFile(imageUri);
        final UploadTask uploadTask1 = filePath2.putFile(archivoUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message  = e.toString();
                Toast.makeText(AdministracionActivity.this, "Error: "+message, Toast.LENGTH_LONG).show();
           
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdministracionActivity.this, "La imagen se subio satisfactoriamente..", Toast.LENGTH_LONG).show();

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        Toast.makeText(AdministracionActivity.this, ":)", Toast.LENGTH_LONG).show();

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){

                            downloadImageUrl = task.getResult().toString();

                            Toast.makeText(AdministracionActivity.this, "Got the Product Image Url Successfully...", Toast.LENGTH_LONG).show();

                            //Guaradamos el producto
                           // SaveProductInfoToDatabase();
                        }

                    }
                });
            }
        });

        uploadTask1.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message  = e.toString();
                Toast.makeText(AdministracionActivity.this, "Error: "+message, Toast.LENGTH_LONG).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdministracionActivity.this, "El Archivo se subio satisfactoriamente..", Toast.LENGTH_LONG).show();

                Task<Uri> urlTask = uploadTask1.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        Toast.makeText(AdministracionActivity.this, ":)", Toast.LENGTH_LONG).show();

                        downloadArchivoUrl = filePath2.getDownloadUrl().toString();
                        return filePath2.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){

                            downloadArchivoUrl = task.getResult().toString();

                            Toast.makeText(AdministracionActivity.this, "Got the Product Archivo Url Successfully...", Toast.LENGTH_LONG).show();

                            //Guaradamos el producto
                            SaveProductInfoToDatabase();
                        }

                    }
                });
            }
        });


    }

    private void SaveProductInfoToDatabase() {

        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", productoDescr);
        productMap.put("image", downloadImageUrl);
        productMap.put("archivo", downloadArchivoUrl);
        productMap.put("email", productoEmail);
        productMap.put("telefono", productoTelefono);
        productMap.put("pname", productoNombre);

        ProductsRef.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(AdministracionActivity.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(AdministracionActivity.this, "Product is Added Successfully...", Toast.LENGTH_LONG).show();
                        }else {
                            String messaje = task.getException().toString();
                            Toast.makeText(AdministracionActivity.this, "Error: "+messaje, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    }



