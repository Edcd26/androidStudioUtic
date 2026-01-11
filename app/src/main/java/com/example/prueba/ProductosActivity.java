package com.example.prueba;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ProductosActivity extends AppCompatActivity {
    private Cursor fila;
    private ListView lista;
    private EditText aux_codigo, aux_descripcion;
    private AutoCompleteTextView sp_tipo, sp_u_medida;
    private Button registrar;
    private Integer idSeleccionado;

    private ArrayList<String> listaUMedidaDesc = new ArrayList<>();
    private ArrayList<Integer> listaUMedidaId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_productos);

        lista = findViewById(R.id.lista_productos);
        aux_codigo = findViewById(R.id.txt_codigo);
        sp_tipo = findViewById(R.id.sp_tipo_auto);
        sp_u_medida = findViewById(R.id.sp_u_medida_auto);
        aux_descripcion = findViewById(R.id.txt_descripcion);
        registrar = findViewById(R.id.btn_agregar);

        String[] opcionesTipo = {"Planteras", "Sustratos", "Ceramica", "Insectisidas"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesTipo);
        sp_tipo.setAdapter(adapterTipo);

        cargarSpinnerUMedida();
        cargaLista();

        aux_codigo.setEnabled(false);
        aux_descripcion.requestFocus();

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String listItem = (String) lista.getItemAtPosition(i);
                try {
                    idSeleccionado = Integer.parseInt(listItem.split(" - ")[0]);
                    aux_codigo.setText(String.valueOf(idSeleccionado));
                    Recuperar();
                } catch (Exception e) {
                    Toast.makeText(ProductosActivity.this, "Error al seleccionar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarSpinnerUMedida() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();

        listaUMedidaDesc.clear();
        listaUMedidaId.clear();

        try {
            Cursor cursor = db.rawQuery("SELECT id_u_medida, u_medida_descri FROM u_medida", null);
            while (cursor.moveToNext()) {
                listaUMedidaId.add(cursor.getInt(0));
                listaUMedidaDesc.add(cursor.getString(1));
            }
            cursor.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar U. Medidas", Toast.LENGTH_SHORT).show();
        }
        db.close();

        ArrayAdapter<String> adapterUM = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaUMedidaDesc);
        sp_u_medida.setAdapter(adapterUM);
    }

    public void cargaLista() {
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = miconexion.getWritableDatabase();

        try {
            fila = db.rawQuery("SELECT p.id_producto, p.prod_tipo, u.u_medida_descri, p.prod_descri " +
                    "FROM producto p INNER JOIN u_medida u ON p.id_u_medida = u.id_u_medida ORDER BY p.id_producto", null);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            while (fila.moveToNext()) {
                adapter.add(fila.getString(0) + " - " + fila.getString(1) + " - " + fila.getString(2) + " - " + fila.getString(3));
            }
            lista.setAdapter(adapter);
        } catch (Exception e) {
            // Error handling
        }
        db.close();
    }

    public void Recuperar() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            Cursor c = db.rawQuery("select prod_tipo, id_u_medida, prod_descri from producto where id_producto=" + codigo, null);

            if (c.moveToFirst()) {
                sp_tipo.setText(c.getString(0), false);

                int idUM = c.getInt(1);
                for (int i = 0; i < listaUMedidaId.size(); i++) {
                    if (listaUMedidaId.get(i) == idUM) {
                        sp_u_medida.setText(listaUMedidaDesc.get(i), false);
                        break;
                    }
                }

                aux_descripcion.setText(c.getString(2));
                registrar.setEnabled(false);
            } else {
                Toast.makeText(this, "No existe el producto", Toast.LENGTH_LONG).show();
            }
            c.close();
        }
        db.close();
    }

    public void Registrar(View view) {
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = miconexion.getWritableDatabase();

        String tipo = sp_tipo.getText().toString();
        String descUM = sp_u_medida.getText().toString();
        int idUM = -1;
        
        for (int i = 0; i < listaUMedidaDesc.size(); i++) {
            if (listaUMedidaDesc.get(i).equals(descUM)) {
                idUM = listaUMedidaId.get(i);
                break;
            }
        }

        String descri = aux_descripcion.getText().toString().toUpperCase();

        if (!descri.isEmpty() && idUM != -1 && !tipo.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("prod_tipo", tipo);
            registro.put("id_u_medida", idUM);
            registro.put("prod_descri", descri);

            long res = db.insert("producto", null, registro);
            if (res != -1) {
                Toast.makeText(this, "Producto registrado correctamente", Toast.LENGTH_LONG).show();
                Cancelar();
                cargaLista();
            } else {
                Toast.makeText(this, "Error al guardar producto", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Debe completar todos los campos", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void Modificar(View view) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();
        String tipo = sp_tipo.getText().toString();
        String descUM = sp_u_medida.getText().toString();
        int idUM = -1;

        for (int i = 0; i < listaUMedidaDesc.size(); i++) {
            if (listaUMedidaDesc.get(i).equals(descUM)) {
                idUM = listaUMedidaId.get(i);
                break;
            }
        }
        
        String descri = aux_descripcion.getText().toString().toUpperCase();

        if (!codigo.isEmpty() && !descri.isEmpty() && idUM != -1 && !tipo.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("prod_tipo", tipo);
            registro.put("id_u_medida", idUM);
            registro.put("prod_descri", descri);

            int cant = db.update("producto", registro, "id_producto=" + codigo, null);
            if (cant == 1) {
                Toast.makeText(this, "Producto modificado exitosamente", Toast.LENGTH_LONG).show();
                Cancelar();
                cargaLista();
                registrar.setEnabled(true);
            } else {
                Toast.makeText(this, "No se pudo modificar", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Seleccione un producto para modificar", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void Eliminar(View view) {
        final String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Desea eliminar este producto?");
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(ProductosActivity.this, "bd_pam3", null, 2);
                    SQLiteDatabase db = admin.getWritableDatabase();
                    db.delete("producto", "id_producto=" + codigo, null);
                    db.close();
                    Toast.makeText(ProductosActivity.this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    Cancelar();
                    registrar.setEnabled(true);
                    cargaLista();
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        } else {
            Toast.makeText(this, "Seleccione un producto para eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    public void llamaCancelar(View view) {
        Cancelar();
    }

    public void Cancelar() {
        aux_codigo.setText("");
        aux_descripcion.setText("");
        sp_tipo.setText("", false);
        sp_u_medida.setText("", false);
        registrar.setEnabled(true);
        aux_descripcion.requestFocus();
    }

    public void salir(View view) {
        finish();
    }
}