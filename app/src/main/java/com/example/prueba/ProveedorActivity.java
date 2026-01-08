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

public class ProveedorActivity extends AppCompatActivity {
    private Cursor fila;
    private ListView lista;
    private EditText aux_codigo, aux_razonsocial, aux_ruc, aux_tel, aux_direccion, aux_email;
    private Button registrar;
    private Integer idSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_proveedor);

        lista=(ListView)findViewById(R.id.lista_proveedores);
        aux_codigo=(EditText)findViewById(R.id.txt_codigo);
        aux_razonsocial=(EditText)findViewById(R.id.txt_razonsocial);
        aux_ruc=(EditText)findViewById(R.id.txt_ruc);
        aux_tel=(EditText)findViewById(R.id.txt_telefono);
        aux_direccion=(EditText)findViewById(R.id.txt_direccion);
        aux_email=(EditText)findViewById(R.id.txt_email);
        registrar=(Button)findViewById(R.id.btn_agregar);

        cargaLista();

        aux_codigo.setEnabled(false);
        aux_razonsocial.requestFocus();

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
        fila = db.rawQuery("SELECT cod_prov, prov_razonsocial, prov_ruc, prov_tel, prov_direccion, prov_email FROM proveedor ORDER BY cod_prov", null);

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
                    ("select prov_razonsocial, prov_ruc, prov_tel, prov_direccion, prov_email from proveedor where cod_prov=" + codigo, null);

            if (((Cursor) fila).moveToFirst()) {
                aux_razonsocial.setText(fila.getString(0));
                aux_ruc.setText(fila.getString(1));
                aux_tel.setText(fila.getString(2));
                aux_direccion.setText(fila.getString(3));
                aux_email.setText(fila.getString(4));
                registrar.setEnabled(false);
                BaseDeDatos.close();
            } else {
                Toast.makeText(this, "No existe El Usuario", Toast.LENGTH_LONG).show();
                BaseDeDatos.close();
            }
        } else {
            Toast.makeText(this, "Ingrese el Codigo para Buscar", Toast.LENGTH_LONG).show();
        }
    }



    public void Registrar(View view){
        //abre y conecta con la base de datos
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = miconexion.getWritableDatabase();

        //variables auxiliares
        String var_codigo = aux_codigo.getText().toString();
        String var_aux_razonsocial = aux_razonsocial.getText().toString().toUpperCase();
        String var_aux_ruc = aux_ruc.getText().toString().toUpperCase();
        String var_aux_tel = aux_tel.getText().toString().toUpperCase();
        String var_aux_direccion = aux_direccion.getText().toString().toUpperCase();
        String var_aux_email = aux_email.getText().toString().toUpperCase();

        if (!var_aux_razonsocial.isEmpty() && !var_aux_ruc.isEmpty() && !var_aux_tel.isEmpty() && !var_aux_direccion.isEmpty() && !var_aux_email.isEmpty()) // no esta vacio
        {
            fila = BaseDeDatos.rawQuery("SELECT * FROM proveedor WHERE prov_razonsocial= '" + var_aux_razonsocial + "' and prov_ruc='"+var_aux_ruc+"' and prov_tel='"+var_aux_tel+"' and prov_direccion='"+var_aux_direccion+"' and prov_email='"+var_aux_email+"' ", null);

            if (fila.getCount() > 0) // Descripcion ya esta registrada en la tabla
            {
                Toast.makeText(this, "El usuario ya existe...", Toast.LENGTH_LONG).show();
            } else //no registrado
            {
                //contenedor auxiliar
                ContentValues registro = new ContentValues();
                registro.put("prov_razonsocial", var_aux_razonsocial);
                registro.put("prov_ruc", var_aux_ruc);
                registro.put("prov_tel", var_aux_tel);
                registro.put("prov_direccion", var_aux_direccion);
                registro.put("prov_email", var_aux_email);
                //inserta en la tabla
                long usuario = BaseDeDatos.insert("proveedor", null, registro);

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
        String razonsocial = aux_razonsocial.getText().toString().toUpperCase();
        String ruc = aux_ruc.getText().toString().toUpperCase();
        String telefono = aux_tel.getText().toString().toUpperCase();
        String direccion = aux_direccion.getText().toString().toUpperCase();
        String email = aux_email.getText().toString().toUpperCase();

        if (!codigo.isEmpty() && !razonsocial.isEmpty())
        {
            fila = BaseDeDatos.rawQuery("SELECT * FROM proveedor WHERE prov_razonsocial = '" + razonsocial + "'", null);
            if (fila.getCount() > 0)
            {
                Toast.makeText(this, "Ya existe un registro con este nombre", Toast.LENGTH_LONG).show();
            } else
            {
                ContentValues registro = new ContentValues();
                registro.put("prov_razonsocial", razonsocial);
                registro.put("prov_ruc", ruc);
                registro.put("prov_tel", telefono);
                registro.put("prov_direccion", direccion);
                registro.put("prov_email", email);

                //modifica el registro
                int cantidad = BaseDeDatos.update("proveedor", registro,  "cod_prov =" + codigo, null);
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

        final String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProveedorActivity.this);
            builder.setMessage("Esta seguro de eliminar el registro");

            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProveedorActivity.this, "bd_pam3", null, 1);
                    SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

                    int cantidad = BaseDeDatos.delete("proveedor", "cod_prov =" + codigo, null);
                    BaseDeDatos.close();

                    Cancelar();
                    registrar.setEnabled(true);
                    aux_razonsocial.requestFocus();

                    if (cantidad == 1)
                    {
                        Toast.makeText(ProveedorActivity.this, "Registro Eliminado", Toast.LENGTH_LONG).show();
                        cargaLista();
                    } else {
                    }

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
            Toast.makeText(ProveedorActivity.this, "Seleccione un Elemento", Toast.LENGTH_LONG).show();
        }
    } // fin eliminar




    public void llamaCancelar(View view)
    {
        Cancelar();
    }

    public void Cancelar()
    {
        aux_codigo.setText("");
        aux_razonsocial.setText("");
        aux_ruc.setText("");
        aux_tel.setText("");
        aux_direccion.setText("");
        aux_email.setText("");
        registrar.setEnabled(true);
    }
    public void salir (View view)
    {
        ///
        finish();
    }

}
