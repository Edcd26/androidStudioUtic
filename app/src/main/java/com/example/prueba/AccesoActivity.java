package com.example.prueba;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccesoActivity extends AppCompatActivity {

    private EditText aux_login, aux_pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acceso);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        aux_login=this.findViewById(R.id.txt_usuario);
        aux_pass=this.findViewById(R.id.txt_pass);
    }

    public void salir(View view){
        finish();
    }

    public void limpiar(View view){
        aux_login.setText("");
        aux_pass.setText("");
        aux_login.requestFocus();
    }

    public void verificar(View view)
    {
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase BaseDeDatos = miconexion.getWritableDatabase();

        String usuario = aux_login.getText().toString();
        String clave   = aux_pass.getText().toString();

        if(!usuario.isEmpty() && !clave.isEmpty())
        {
            Cursor fila = BaseDeDatos.rawQuery("select cod_usu,usu_nombre,usu_rol,usu_estado from usuario where usu_login='"+ usuario +"' and usu_clave='"+clave+"'", null);

            if(fila.moveToFirst())
            {
                String estado = fila.getString(3);
                if (estado != null && estado.equalsIgnoreCase("INACTIVO")) {
                    Toast.makeText(this, "Usuario Bloqueado. Contacte con el Administrador.", Toast.LENGTH_LONG).show();
                    BaseDeDatos.close();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString("parametro_usu", fila.getString(1));
                bundle.putString("parametro_rol", fila.getString(2));
                bundle.putString("parametro_login", usuario);

                guardar_preferencias(fila.getString(1));
                
                Intent siguiente = new Intent(this, MenuPrincipalActivity.class);
                siguiente.putExtras(bundle);
                startActivity(siguiente);
                
                BaseDeDatos.close();
                finish();
            }else
            {
                Toast.makeText(this, "Usuario o Clave Incorrectos", Toast.LENGTH_LONG).show();
                BaseDeDatos.close();
            }
        }
        else
        {
            Toast.makeText(this, "Hay Campos Vacios, Verificar!!!", Toast.LENGTH_LONG).show();
        }
    }

    public void guardar_preferencias(String nombreCompleto){
        SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String prefe_usuario = aux_login.getText().toString();
        String prefe_clave   = aux_pass.getText().toString();

        editor.putString("user",prefe_usuario);
        editor.putString("pass",prefe_clave);
        editor.putString("nombre", nombreCompleto);
        editor.apply();
    }

    public void leer_preferencias(View view)
    {
        SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        String user = preferences.getString("user","");
        String pass = preferences.getString("pass","");

        aux_login.setText(user);
        aux_pass.setText(pass);
    }
}