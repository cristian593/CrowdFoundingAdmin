package com.guilcapi.crowdfoundingadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AdminActivity extends AppCompatActivity {

    private EditText adminID, adminNombre, adminContrasena;
    private Button btnCrear;
    private String idAdmin, nombreAdmin, contrasenaAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        adminID =findViewById(R.id.adminUsuario1);
        adminNombre =findViewById(R.id.adminNombre1);
        adminContrasena =findViewById(R.id.adminContrasena1);
        btnCrear =findViewById(R.id.btnCrear);

        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idAdmin = adminID.getText().toString();
                nombreAdmin = adminNombre.getText().toString();
                contrasenaAdmin = adminContrasena.getText().toString();
                if(idAdmin.length()>0 && nombreAdmin.length()>0 && contrasenaAdmin.length()>0){
                    CrearAdministrador(idAdmin, nombreAdmin, contrasenaAdmin);
                }
            }
        });
    }

    private void CrearAdministrador(final String idAdmin, final String nombreAdmin, final String contrasenaAdmin) {
        final DatabaseReference adminReferencia;
        adminReferencia = FirebaseDatabase.getInstance().getReference();
        adminReferencia.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!(dataSnapshot.child("Administrador").child(idAdmin).exists())){

                    //Creamos el nuevo usuario
                    HashMap<String, Object> adminsitrador = new HashMap<>();
                    adminsitrador.put("usuario",idAdmin);
                    adminsitrador.put("nombre",nombreAdmin);
                    adminsitrador.put("contrasena",contrasenaAdmin);

                    adminReferencia.child("Administrador").child(idAdmin).setValue(adminsitrador).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(AdminActivity.this, " Administrador agregado ",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else {
                                Toast.makeText(AdminActivity.this, " Error de red ",
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }else {
                    Toast.makeText(AdminActivity.this, " Usuario Ocupado con anterioridad",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
