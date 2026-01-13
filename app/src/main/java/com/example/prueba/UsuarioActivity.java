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

public class UsuarioActivity extends AppCompatActivity {
    private Cursor fila;
    private ListView lista;
    private EditText aux_codigo, aux_nombre, aux_login, aux_pass;
    private AutoCompleteTextView txt_estado, txt_nivel;
    private Button registrar;
    private Integer idSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario);

        // --- 1. ENLAZAR CONTROLES ---
        lista = findViewById(R.id.lista_articulos);
        aux_codigo = findViewById(R.id.txt_codigo);
        aux_nombre = findViewById(R.id.txt_nombre);
        aux_login = findViewById(R.id.txt_usuario);
        aux_pass = findViewById(R.id.txt_contrasena);
        txt_estado = findViewById(R.id.txt_estado_auto);
        txt_nivel = findViewById(R.id.txt_nivel_auto);
        registrar = findViewById(R.id.btn_agregar);

        // --- 2. CONFIGURAR DROPDOWN ESTADO ---
        String[] opcionesEstado = {"ACTIVO", "INACTIVO"};
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesEstado);
        txt_estado.setAdapter(adapterEstado);

        // --- 3. CONFIGURAR DROPDOWN NIVEL (ROL) ---
        String[] opcionesNivel = {"ADMINISTRADOR", "COMPRA"};
        ArrayAdapter<String> adapterNivel = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesNivel);
        txt_nivel.setAdapter(adapterNivel);

        // Cargar lista inicial (Versión 2)
        cargaLista();

        aux_codigo.setEnabled(false);
        aux_nombre.requestFocus();

        // --- 4. LISTENER PARA SELECCIONAR DE LA LISTA ---
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String listItem = (String) lista.getItemAtPosition(i);
                try {
                    idSeleccionado = Integer.parseInt(listItem.split(" - ")[0]);
                    aux_codigo.setText(String.valueOf(idSeleccionado));
                    Recuperar();
                } catch (Exception e) {
                    Toast.makeText(UsuarioActivity.this, "Error al seleccionar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cargaLista() {
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = miconexion.getReadableDatabase();

        try {
            fila = db.rawQuery("SELECT cod_usu, usu_nombre, usu_rol, usu_estado, usu_login, usu_clave FROM usuario ORDER BY cod_usu", null);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            while (fila.moveToNext()) {
                adapter.add(fila.getString(0) + " - " + fila.getString(1) + " - " + fila.getString(2) + " - " + fila.getString(3));
            }
            lista.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar lista", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    public void Recuperar() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();

        String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            Cursor c = db.rawQuery("select usu_nombre, usu_rol, usu_estado, usu_login, usu_clave from usuario where cod_usu=" + codigo, null);

            if (c.moveToFirst()) {
                aux_nombre.setText(c.getString(0));
                txt_nivel.setText(c.getString(1), false);
                txt_estado.setText(c.getString(2), false);
                aux_login.setText(c.getString(3));
                aux_pass.setText(c.getString(4));
                registrar.setEnabled(false);
            } else {
                Toast.makeText(this, "No existe el usuario", Toast.LENGTH_SHORT).show();
            }
            c.close();
        }
        db.close();
    }

    public void Registrar(View view) {
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = miconexion.getWritableDatabase();

        String var_nom = aux_nombre.getText().toString().toUpperCase();
        String var_rol = txt_nivel.getText().toString().toUpperCase();
        String var_est = txt_estado.getText().toString().toUpperCase();
        String var_login = aux_login.getText().toString();
        String var_pass = aux_pass.getText().toString();

        if (!var_nom.isEmpty() && !var_login.isEmpty() && !var_pass.isEmpty() && !var_rol.isEmpty() && !var_est.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("usu_nombre", var_nom);
            registro.put("usu_rol", var_rol);
            registro.put("usu_estado", var_est);
            registro.put("usu_login", var_login);
            registro.put("usu_clave", var_pass);

            long res = db.insert("usuario", null, registro);
            if (res != -1) {
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                Cancelar();
                cargaLista();
            } else {
                Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void Modificar(View view) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();
        String nombre = aux_nombre.getText().toString().toUpperCase();
        String rol = txt_nivel.getText().toString().toUpperCase();
        String estado = txt_estado.getText().toString().toUpperCase();
        String login = aux_login.getText().toString();
        String clave = aux_pass.getText().toString();

        if (!codigo.isEmpty() && !nombre.isEmpty() && !rol.isEmpty() && !estado.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("usu_nombre", nombre);
            registro.put("usu_rol", rol);
            registro.put("usu_estado", estado);
            registro.put("usu_login", login);
            registro.put("usu_clave", clave);

            int cantidad = db.update("usuario", registro, "cod_usu =" + codigo, null);
            if (cantidad == 1) {
                Toast.makeText(this, "Usuario modificado", Toast.LENGTH_SHORT).show();
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
            builder.setMessage("¿Desea eliminar este usuario?");
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(UsuarioActivity.this, "bd_pam3", null, 2);
                    SQLiteDatabase db = admin.getWritableDatabase();
                    db.delete("usuario", "cod_usu=" + codigo, null);
                    db.close();
                    Toast.makeText(UsuarioActivity.this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    Cancelar();
                    cargaLista();
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
        aux_nombre.setText("");
        aux_login.setText("");
        aux_pass.setText("");
        txt_nivel.setText("", false);
        txt_estado.setText("", false);
        registrar.setEnabled(true);
        aux_nombre.requestFocus();
    }

    public void salir(View view) {
        finish();
    }
}