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

    // METODO PARA VALIDAR EL USUARIO Y CONTRASEÑA (SE CREA)
    public void verificar(View view)
    {
        //se indica con que base de datos se va a trabajar
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = miconexion.getWritableDatabase();

        //variables auxiliares para realizar las comparaciones
        String usuario = aux_login.getText().toString();
        String clave   = aux_pass.getText().toString();

        //**validar si parametros estan vacios
        if(!usuario.isEmpty() && !clave.isEmpty())
        {
            //Cursor fila = BaseDeDatos.rawQuery("select cod_usu,usu_nombre from usuario where usu_login='"+ aux_login.getText().toString() +"' and usu_clave='"+clave+"'", null);
            Cursor fila = BaseDeDatos.rawQuery("select cod_usu,usu_nombre,usu_rol from usuario where usu_login='"+ usuario +"' and usu_clave='"+clave+"'", null);

            if(((Cursor) fila).moveToFirst())// encontro, coincide
            {
                Bundle bundle = new Bundle();
                //toma la columna 2 de la tabla de usuarios
                bundle.putString("parametro_usu",fila.getString(1).toString());
                bundle.putString("parametro_rol",fila.getString(2).toString());//nivel o rol

                //grabar las preferencias en el archivo xml de preferencias
                guardar_preferencias();
                //sale del acceso
                finish();
                //funcion para llamar a otra activity
                Intent siguiente = new Intent(this, UsuarioActivity.class); // AQUI DEBE IR EL NOMBRE DEL MENU PRINCIPAL EN VEZ DE AccesoActivity

                //ENVIA  el parametro
                siguiente.putExtras(bundle);
                //ejcuta el activity
                startActivity(siguiente); //metodo para levantar la otra activity- cambio de pantalla

                //cierra la base de datos
                BaseDeDatos.close();
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

    }// fin validar

    public void guardar_preferencias(){
        //se crea un archivo xml llamado credenciales para guardar los datos, y el objeto es preferences
        SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);

        // se habilita o abre el archivo para editar, se crea el objeto editor para ello
        SharedPreferences.Editor editor = preferences.edit();

        //variables auxiliares para las preferencias
        String prefe_usuario = aux_login.getText().toString();
        String prefe_clave   = aux_pass.getText().toString();

        // se graba los datos en el archivo xml credenciales
        // se utiliza putString para datos string, putInt para numericos, etc.
        editor.putString("user",prefe_usuario);
        editor.putString("pass",prefe_clave);

        //confirma la grabacion en el archivo xml de preferencias
        editor.commit();
        //aux_login.setText("");
    }
    //preferencias shared
    public void leer_preferencias(View view)
    {
        //se crea un archivo xml llamado credenciales para guardar los datos, y el objeto es preferences
        SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);

        String user = preferences.getString("user","No existe Informacion del login");
        String pass = preferences.getString("pass","No existe Informacion del Password");

        aux_login.setText(user);
        aux_pass.setText(pass);
    }

}