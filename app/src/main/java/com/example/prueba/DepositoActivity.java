package com.example.prueba;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

public class DepositoActivity extends AppCompatActivity {
    private Cursor fila;
    private ListView lista;
    private EditText aux_codigo, aux_descripcion;
    private Button registrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deposito);
        lista = findViewById(R.id.lista_articulos);
        aux_codigo = findViewById(R.id.txt_codigo);
        aux_descripcion = findViewById(R.id.txt_descripcion);
        registrar = findViewById(R.id.btn_agregar);

        // Carga inicial usando versión 2
        cargaLista();

        aux_codigo.setEnabled(false);
        aux_codigo.setBackgroundColor(Color.LTGRAY); // Visual feedback for disabled field
        aux_descripcion.requestFocus();

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String listItem = (String) lista.getItemAtPosition(i);
                String id = listItem.split(" - ")[0];
                aux_codigo.setText(id);
                Recuperar();
            }
        });
    }

    public void cargaLista() {
        // Actualizado a versión 2
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = miconexion.getReadableDatabase();

        try {
            fila = db.rawQuery("SELECT id_deposito, deposito_descri FROM deposito ORDER BY id_deposito", null);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            while (fila.moveToNext()) {
                adapter.add(fila.getString(0) + " - " + fila.getString(1));
            }
            lista.setAdapter(adapter);
        } finally {
            db.close();
        }
    }

    public void Recuperar() {
        // Actualizado a versión 2
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase BaseDeDatos = admin.getReadableDatabase();
        String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            Cursor filaReg = BaseDeDatos.rawQuery("SELECT deposito_descri FROM deposito WHERE id_deposito=" + codigo, null);

            if (filaReg.moveToFirst()) {
                aux_descripcion.setText(filaReg.getString(0));
                registrar.setEnabled(false);
            } else {
                Toast.makeText(this, "No existe el registro", Toast.LENGTH_SHORT).show();
            }
            filaReg.close();
            BaseDeDatos.close();
        }
    }

    public void Registrar(View view) {
        // Actualizado a versión 2
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase BaseDeDatos = miconexion.getWritableDatabase();

        String descri = aux_descripcion.getText().toString().toUpperCase().trim();

        if (!descri.isEmpty()) {
            Cursor c = BaseDeDatos.rawQuery("SELECT * FROM deposito WHERE deposito_descri = '" + descri + "'", null);
            if (c.getCount() > 0) {
                Toast.makeText(this, "Este deposito ya existe", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues registro = new ContentValues();
                registro.put("deposito_descri", descri);
                BaseDeDatos.insert("deposito", null, registro);

                Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show();
                Cancelar();
                cargaLista();
            }
            c.close();
        } else {
            Toast.makeText(this, "Ingrese la descripción", Toast.LENGTH_SHORT).show();
        }
        BaseDeDatos.close();
    }

    public void Modificar(View view) {
        // Actualizado a versión 2
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();
        String descri = aux_descripcion.getText().toString().toUpperCase().trim();

        if (!codigo.isEmpty() && !descri.isEmpty()) {
            // Validar descripcion unica (excepto el actual)
            Cursor c = BaseDeDatos.rawQuery("SELECT * FROM deposito WHERE deposito_descri = '" + descri + "' AND id_deposito != " + codigo, null);
            if (c.getCount() > 0) {
                Toast.makeText(this, "Ya existe otro deposito con esta descripción", Toast.LENGTH_SHORT).show();
                c.close();
                BaseDeDatos.close();
                return;
            }
            c.close();

            ContentValues registro = new ContentValues();
            registro.put("deposito_descri", descri);

            int cant = BaseDeDatos.update("deposito", registro, "id_deposito=" + codigo, null);
            if (cant == 1) {
                Toast.makeText(this, "Modificado correctamente", Toast.LENGTH_SHORT).show();
                Cancelar();
                cargaLista();
            }
        }
        BaseDeDatos.close();
    }
    // SELECT id_deposito, deposito_descri FROM deposito ORDER BY id_deposito
    public void Eliminar(View view) {
        final String codigo = aux_codigo.getText().toString();
        if (!codigo.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar")
                    .setMessage("¿Desea eliminar este registro?")
                    .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                        // Actualizado a versión 2
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
                        SQLiteDatabase db = admin.getWritableDatabase();
                        db.delete("deposito", "id_deposito=" + codigo, null);
                        db.close();
                        Cancelar();
                        cargaLista();
                        Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    public void llamaCancelar(View view) {
        Cancelar();
    }

    public void Cancelar() {
        aux_codigo.setText("");
        aux_descripcion.setText("");
        registrar.setEnabled(true);
        aux_descripcion.requestFocus();
    }

    public void salir(View view) {
        finish();
    }
}