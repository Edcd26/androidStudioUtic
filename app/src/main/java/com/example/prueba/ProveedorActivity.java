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

        lista = findViewById(R.id.lista_proveedores);
        aux_codigo = findViewById(R.id.txt_codigo);
        aux_razonsocial = findViewById(R.id.txt_razonsocial);
        aux_ruc = findViewById(R.id.txt_ruc);
        aux_tel = findViewById(R.id.txt_telefono);
        aux_direccion = findViewById(R.id.txt_direccion);
        aux_email = findViewById(R.id.txt_email);
        registrar = findViewById(R.id.btn_agregar);

        // Carga inicial usando versión 2
        cargaLista();

        aux_codigo.setEnabled(false);
        aux_razonsocial.requestFocus();

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String listItem = (String) lista.getItemAtPosition(i);
                try {
                    idSeleccionado = Integer.parseInt(listItem.split(" - ")[0]);
                    aux_codigo.setText(String.valueOf(idSeleccionado));
                    Recuperar();
                } catch (Exception e) {
                    Toast.makeText(ProveedorActivity.this, "Error al seleccionar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cargaLista() {
        // Actualizado a Versión 2
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = miconexion.getReadableDatabase();

        try {
            fila = db.rawQuery("SELECT cod_prov, prov_razonsocial, prov_ruc, prov_tel, prov_direccion, prov_email FROM proveedor ORDER BY cod_prov", null);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            while (fila.moveToNext()) {
                adapter.add(fila.getString(0) + " - " + fila.getString(1) + " - " + fila.getString(2) + " - " + fila.getString(3));
            }
            lista.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar proveedores", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    public void Recuperar() {
        // Actualizado a Versión 2
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();

        String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            Cursor c = db.rawQuery("select prov_razonsocial, prov_ruc, prov_tel, prov_direccion, prov_email from proveedor where cod_prov=" + codigo, null);

            if (c.moveToFirst()) {
                aux_razonsocial.setText(c.getString(0));
                aux_ruc.setText(c.getString(1));
                aux_tel.setText(c.getString(2));
                aux_direccion.setText(c.getString(3));
                aux_email.setText(c.getString(4));
                registrar.setEnabled(false);
            } else {
                Toast.makeText(this, "No existe el proveedor", Toast.LENGTH_LONG).show();
            }
            c.close();
        }
        db.close();
    }

    public void Registrar(View view) {
        // Actualizado a Versión 2
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = miconexion.getWritableDatabase();

        String rs = aux_razonsocial.getText().toString().toUpperCase();
        String ruc = aux_ruc.getText().toString().toUpperCase();
        String tel = aux_tel.getText().toString().toUpperCase();
        String dir = aux_direccion.getText().toString().toUpperCase();
        String em = aux_email.getText().toString().toUpperCase();

        if (!rs.isEmpty() && !ruc.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("prov_razonsocial", rs);
            registro.put("prov_ruc", ruc);
            registro.put("prov_tel", tel);
            registro.put("prov_direccion", dir);
            registro.put("prov_email", em);

            long res = db.insert("proveedor", null, registro);
            if (res != -1) {
                Toast.makeText(this, "Proveedor guardado", Toast.LENGTH_SHORT).show();
                Cancelar();
                cargaLista();
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Complete Razón Social y RUC", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void Modificar(View view) {
        // Actualizado a Versión 2
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();
        String rs = aux_razonsocial.getText().toString().toUpperCase();
        String ruc = aux_ruc.getText().toString().toUpperCase();
        String tel = aux_tel.getText().toString().toUpperCase();
        String dir = aux_direccion.getText().toString().toUpperCase();
        String em = aux_email.getText().toString().toUpperCase();

        if (!codigo.isEmpty() && !rs.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("prov_razonsocial", rs);
            registro.put("prov_ruc", ruc);
            registro.put("prov_tel", tel);
            registro.put("prov_direccion", dir);
            registro.put("prov_email", em);

            int cant = db.update("proveedor", registro, "cod_prov =" + codigo, null);
            if (cant == 1) {
                Toast.makeText(this, "Modificado exitosamente", Toast.LENGTH_SHORT).show();
                Cancelar();
                cargaLista();
                registrar.setEnabled(true);
            }
        }
        db.close();
    }

    public void Eliminar(View view) {
        final String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Desea eliminar este proveedor?");
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Actualizado a Versión 2
                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProveedorActivity.this, "bd_pam3", null, 2);
                    SQLiteDatabase db = admin.getWritableDatabase();
                    db.delete("proveedor", "cod_prov =" + codigo, null);
                    db.close();
                    Toast.makeText(ProveedorActivity.this, "Proveedor eliminado", Toast.LENGTH_SHORT).show();
                    Cancelar();
                    cargaLista();
                    registrar.setEnabled(true);
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        }
    }

    public void llamaCancelar(View view) {
        Cancelar();
    }

    public void Cancelar() {
        aux_codigo.setText("");
        aux_razonsocial.setText("");
        aux_ruc.setText("");
        aux_tel.setText("");
        aux_direccion.setText("");
        aux_email.setText("");
        registrar.setEnabled(true);
        aux_razonsocial.requestFocus();
    }

    public void salir(View view) {
        finish();
    }
}