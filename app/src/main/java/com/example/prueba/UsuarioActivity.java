package com.example.prueba;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UsuarioActivity extends AppCompatActivity {
    private Cursor fila;
    private ListView lista;
    private EditText aux_codigo, aux_nombre, aux_rol, aux_est, aux_login, aux_pass;
    private Button registrar;
    private Integer idSeleccionado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario);
        // asociar con las variables
        lista=(ListView)findViewById(R.id.lista_articulos);
        aux_codigo=(EditText)findViewById(R.id.txt_codigo);
        aux_nombre=(EditText)findViewById(R.id.txt_nombre);
        aux_rol=(EditText)findViewById(R.id.txt_nivel);
        aux_est=(EditText)findViewById(R.id.txt_estado);
        aux_login=(EditText)findViewById(R.id.txt_usuario);
        aux_pass=(EditText)findViewById(R.id.txt_contrasena);
        registrar=(Button)findViewById(R.id.btn_agregar);

        cargaLista();

        aux_codigo.setEnabled(false);
        aux_nombre.requestFocus();

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String listItem = (String) lista.getItemAtPosition(i);
                idSeleccionado = Integer.parseInt(listItem.split(" - ")[0]);

                //aux_codigo.setText(idSeleccionado);
                aux_codigo.setText(String.valueOf(idSeleccionado));

                Recuperar();
            }
        });
    }

    public void cargaLista() {
        //abre y conecta con la base de datos
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase db = miconexion.getWritableDatabase();

        //consulta para cargar el cursor
        fila = db.rawQuery("SELECT cod_usu, usu_nombre, usu_rol, usu_estado, usu_login, usu_clave FROM usuario ORDER BY cod_usu", null);

        //recorre el cursor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        while (fila.moveToNext())//recorre el cursor
        {
            //adapter.add(fila.getString(0) + " - " + fila.getString(1) + " - " + fila.getString(2) + " - " + fila.getString(3) + " - " + fila.getString(4) + " - " + fila.getInt(5));//carga el array
            adapter.add(fila.getString(0) + " - " + fila.getString(1) + " - " + fila.getString(2) + " - " + fila.getString(3) + " - " + fila.getString(4) + " - " + fila.getString(5)); // <--- getString es más seguro para contraseñas
        }
        lista.setAdapter(adapter);//vuelca el array en la lista
    }//lista


    public void Recuperar()
    {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            Cursor fila = BaseDeDatos.rawQuery
                    ("select usu_nombre, usu_rol, usu_estado, usu_login, usu_clave from usuario where cod_usu=" + codigo, null);

            if (((Cursor) fila).moveToFirst()) {
                aux_nombre.setText(fila.getString(0));
                aux_rol.setText(fila.getString(1));
                aux_est.setText(fila.getString(2));
                aux_login.setText(fila.getString(3));
                aux_pass.setText(fila.getString(4));
                registrar.setEnabled(false);
                BaseDeDatos.close();
            } else {
                Toast.makeText(this, "No existe El Usuario", Toast.LENGTH_LONG).show();
                BaseDeDatos.close();
            }
        } else {
            Toast.makeText(this, "Ingrese el Codigo para Buscar", Toast.LENGTH_LONG).show();
        }
    }//fin recuperacion

    public void Registrar(View view){
        //abre y conecta con la base de datos
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = miconexion.getWritableDatabase();

        //variables auxiliares
        String var_codigo = aux_codigo.getText().toString();
        String var_nom = aux_nombre.getText().toString().toUpperCase();
        String var_rol = aux_rol.getText().toString().toUpperCase();
        String var_est = aux_est.getText().toString().toUpperCase();
        String var_login = aux_login.getText().toString().toUpperCase();
        String var_pass = aux_pass.getText().toString().toUpperCase();

        if (!var_nom.isEmpty() && !var_rol.isEmpty() && !var_est.isEmpty() && !var_login.isEmpty() && !var_pass.isEmpty()) // no esta vacio
        {
            fila = BaseDeDatos.rawQuery("SELECT * FROM usuario WHERE usu_nombre= '" + var_nom + "' and usu_rol='"+var_rol+"' and usu_estado='"+var_est+"' and usu_login='"+var_login+"' and usu_clave='"+var_pass+"' ", null);

            if (fila.getCount() > 0) // Descripcion ya esta registrada en la tabla
            {
                Toast.makeText(this, "El usuario ya existe...", Toast.LENGTH_LONG).show();
            } else //no registrado
            {
                //contenedor auxiliar
                ContentValues registro = new ContentValues();
                registro.put("usu_nombre", var_nom);
                registro.put("usu_rol", var_rol);
                registro.put("usu_estado", var_est);
                registro.put("usu_login", var_login);
                registro.put("usu_clave", var_pass);
                //inserta en la tabla
                long usuario = BaseDeDatos.insert("usuario", null, registro);

                BaseDeDatos.close();
                Cancelar();
                Toast.makeText(this, "Registro Guardado Correctamente", Toast.LENGTH_LONG).show();
                cargaLista();
            }
        }
        else //esta vacio
        {
            Toast.makeText(this, "Ingrese la Descripcion", Toast.LENGTH_LONG).show();
        }
    }

    public void Modificar(View view)
    {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();
        String nombre = aux_nombre.getText().toString().toUpperCase();
        String rol = aux_rol.getText().toString().toUpperCase();
        String estado = aux_est.getText().toString().toUpperCase();
        String login = aux_login.getText().toString().toUpperCase();
        String clave = aux_pass.getText().toString().toUpperCase();

        if (!codigo.isEmpty() && !nombre.isEmpty())
        {
            fila = BaseDeDatos.rawQuery("SELECT * FROM usuario WHERE usu_nombre = '" + nombre + "'", null);
            if (fila.getCount() > 0)
            {
                Toast.makeText(this, "Ya existe un registro con este nombre", Toast.LENGTH_LONG).show();
            } else
            {
                ContentValues registro = new ContentValues();
                registro.put("usu_nombre", nombre);
                registro.put("usu_rol", rol);
                registro.put("usu_estado", estado);
                registro.put("usu_login", login);
                registro.put("usu_clave", clave);

                //modifica el registro
                int cantidad = BaseDeDatos.update("usuario", registro,  "cod_usu =" + codigo, null);
                BaseDeDatos.close();

                if (cantidad == 1)
                {
                    Toast.makeText(this, "Registro Modificado Exitosamente", Toast.LENGTH_LONG).show();
                    Cancelar();
                    cargaLista();
                    registrar.setEnabled(true);
                } else
                {
                    Toast.makeText(this, "El Registro No se Actualizo", Toast.LENGTH_LONG).show();
                }

            }
        }else
        {
            Toast.makeText(this, "Seleccione un Usuario para Editar", Toast.LENGTH_LONG).show();
        }
    }//fin modificar

    public void Eliminar(View view)
    {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        //final String codigo = aux_codigo.getText().toString();
        final String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(UsuarioActivity.this);
            builder.setMessage("Esta seguro de eliminar el registro");

            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    //if (!codigo.isEmpty()) {
                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(UsuarioActivity.this, "bd_pam3", null, 1);
                    SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

                    // fila = BaseDeDatos.rawQuery("SELECT * FROM extintor_cab WHERE cod_mar= " + codigo + "", null);
                    // if (fila.getCount() > 0) {
                    //  Toast.makeText(MarcasActivity.this, "El registro esta siendo usado en otra tabla", Toast.LENGTH_LONG).show();
                    //} else {
                    int cantidad = BaseDeDatos.delete("usuario", "cod_usu =" + codigo, null);
                    BaseDeDatos.close();

                    Cancelar();
                    registrar.setEnabled(true);
                    aux_nombre.requestFocus();

                    if (cantidad == 1)
                    {
                        Toast.makeText(UsuarioActivity.this, "Registro Eliminado", Toast.LENGTH_LONG).show();
                        cargaLista();
                    } else {
                    }
                    //}

                /*} else {
                    Toast.makeText(MarcasActivity.this, "Seleccione Elemento", Toast.LENGTH_LONG).show();
                }*/
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            Toast.makeText(UsuarioActivity.this, "Seleccione un Elemento", Toast.LENGTH_LONG).show();
        }
    } // fin eliminar


    public void llamaCancelar(View view)
    {
        Cancelar();
    }

    public void Cancelar()
    {
        aux_codigo.setText("");
        aux_nombre.setText("");
        aux_rol.setText("");
        aux_est.setText("");
        aux_login.setText("");
        aux_pass.setText("");
        registrar.setEnabled(true);
    }
    public void salir (View view)
    {
        ///
        finish();
    }

}