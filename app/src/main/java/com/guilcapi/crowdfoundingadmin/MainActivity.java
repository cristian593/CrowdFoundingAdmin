package com.guilcapi.crowdfoundingadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText adminNombre, adminContrasena;
    private Button adminIngresar, adminCrear;
    private Intent intent;
    private String usuario, contrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adminNombre = findViewById(R.id.adminNombre);
        adminContrasena = findViewById(R.id.adminContrasena);
        adminIngresar = findViewById(R.id.btnIngresar);
        adminCrear = findViewById(R.id.btnCrear);

        adminIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               usuario = adminNombre.getText().toString();
               contrasena = adminContrasena.getText().toString();
                if(usuario.length()<1 && contrasena.length()<1){
                    Toast.makeText(MainActivity.this, "Ingrese los datos solicitados",Toast.LENGTH_SHORT).show();
                }else {
                    Ingresar(usuario, contrasena);
                }

            }
        });

        adminCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, AdminActivity.class);
                startActivity(intent);
            }
        });


    }

    private void Ingresar(final String usuario, final String contrasena) {
        final DatabaseReference adminRef;
        adminRef = FirebaseDatabase.getInstance().getReference();
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Administrador").child(usuario).exists()){
                    String contrasena2 =dataSnapshot.child("Administrador").child(usuario).child("contrasena").getValue().toString();
                    if(contrasena2.equals(contrasena)){
                        intent = new Intent(MainActivity.this, AdministracionActivity.class);
                        intent.putExtra("USER",usuario);
                        startActivity(intent);
                    }else {
                        Toast.makeText(MainActivity.this, "Contraseña equivocada ggggg",Toast.LENGTH_SHORT).show();
                        adminContrasena.setText("");
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Usuario no encontrado :(",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
