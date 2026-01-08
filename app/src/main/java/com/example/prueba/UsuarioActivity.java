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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UsuarioActivity extends AppCompatActivity {
    private Cursor fila;
    private ListView lista;

    // Variables actualizadas: Quitamos aux_rol y aux_est porque ahora son Spinners
    private EditText aux_codigo, aux_nombre, aux_login, aux_pass;
    private Spinner txt_estado, txt_nivel;

    private Button registrar;
    private Integer idSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario);

        // --- 1. ENLAZAR CONTROLES ---
        lista = (ListView) findViewById(R.id.lista_articulos);
        aux_codigo = (EditText) findViewById(R.id.txt_codigo);
        aux_nombre = (EditText) findViewById(R.id.txt_nombre);
        aux_login = (EditText) findViewById(R.id.txt_usuario);
        aux_pass = (EditText) findViewById(R.id.txt_contrasena);

        // Enlazar los nuevos Spinners
        txt_estado = (Spinner) findViewById(R.id.txt_estado);
        txt_nivel = (Spinner) findViewById(R.id.txt_nivel);

        registrar = (Button) findViewById(R.id.btn_agregar);

        // --- 2. CONFIGURAR SPINNER ESTADO ---
        String[] opcionesEstado = {"ACTIVO", "INACTIVO"};
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcionesEstado);
        txt_estado.setAdapter(adapterEstado);

        // --- 3. CONFIGURAR SPINNER NIVEL (ROL) ---
        String[] opcionesNivel = {"ADMINISTRADOR", "COMPRA"};
        ArrayAdapter<String> adapterNivel = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcionesNivel);
        txt_nivel.setAdapter(adapterNivel);

        // Cargar lista inicial y configuraciones
        cargaLista();
        aux_codigo.setEnabled(false);
        aux_nombre.requestFocus();

        // --- 4. LISTENER PARA SELECCIONAR DE LA LISTA ---
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String listItem = (String) lista.getItemAtPosition(i);
                // Extraemos el ID del string "1 - Juan - ADMIN..."
                idSeleccionado = Integer.parseInt(listItem.split(" - ")[0]);
                aux_codigo.setText(String.valueOf(idSeleccionado));

                Recuperar();
            }
        });
    }

    // Metodo auxiliar para seleccionar texto en un Spinner
    @SuppressWarnings("unchecked")
    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }


    public void cargaLista() {
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase db = miconexion.getWritableDatabase();

        fila = db.rawQuery("SELECT cod_usu, usu_nombre, usu_rol, usu_estado, usu_login, usu_clave FROM usuario ORDER BY cod_usu", null);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        while (fila.moveToNext()) {
            adapter.add(fila.getString(0) + " - " + fila.getString(1) + " - " + fila.getString(2) + " - " + fila.getString(3) + " - " + fila.getString(4) + " - " + fila.getString(5));
        }
        lista.setAdapter(adapter);
        db.close(); // Buena práctica cerrar la conexión aquí también si no se usa fila fuera
    }

    public void Recuperar() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            Cursor fila = BaseDeDatos.rawQuery("select usu_nombre, usu_rol, usu_estado, usu_login, usu_clave from usuario where cod_usu=" + codigo, null);

            if (fila.moveToFirst()) {
                aux_nombre.setText(fila.getString(0));

                // AQUÍ LA MAGIA: Usamos el método auxiliar para mover los Spinners
                String rolRecuperado = fila.getString(1);   // usu_rol
                String estadoRecuperado = fila.getString(2); // usu_estado

                setSpinnerValue(txt_nivel, rolRecuperado);
                setSpinnerValue(txt_estado, estadoRecuperado);

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
    }

    public void Registrar(View view) {
        AdminSQLiteOpenHelper miconexion = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = miconexion.getWritableDatabase();

        String var_codigo = aux_codigo.getText().toString();
        String var_nom = aux_nombre.getText().toString().toUpperCase();

        // OBTENER VALORES DE LOS SPINNERS
        String var_rol = txt_nivel.getSelectedItem().toString().toUpperCase();
        String var_est = txt_estado.getSelectedItem().toString().toUpperCase();

        String var_login = aux_login.getText().toString().toUpperCase();
        String var_pass = aux_pass.getText().toString().toUpperCase();

        if (!var_nom.isEmpty() && !var_login.isEmpty() && !var_pass.isEmpty()) {
            // Validamos duplicados
            fila = BaseDeDatos.rawQuery("SELECT * FROM usuario WHERE usu_login = '" + var_login + "'", null);

            if (fila.getCount() > 0) {
                Toast.makeText(this, "El Login de usuario ya existe...", Toast.LENGTH_LONG).show();
            } else {
                ContentValues registro = new ContentValues();
                registro.put("usu_nombre", var_nom);
                registro.put("usu_rol", var_rol);
                registro.put("usu_estado", var_est);
                registro.put("usu_login", var_login);
                registro.put("usu_clave", var_pass);

                BaseDeDatos.insert("usuario", null, registro);

                BaseDeDatos.close();
                Cancelar();
                Toast.makeText(this, "Registro Guardado Correctamente", Toast.LENGTH_LONG).show();
                cargaLista();
            }
        } else {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_LONG).show();
        }
    }

    public void Modificar(View view) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String codigo = aux_codigo.getText().toString();
        String nombre = aux_nombre.getText().toString().toUpperCase();

        // OBTENER VALORES DE LOS SPINNERS
        String rol = txt_nivel.getSelectedItem().toString().toUpperCase();
        String estado = txt_estado.getSelectedItem().toString().toUpperCase();

        String login = aux_login.getText().toString().toUpperCase();
        String clave = aux_pass.getText().toString().toUpperCase();

        if (!codigo.isEmpty() && !nombre.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("usu_nombre", nombre);
            registro.put("usu_rol", rol);
            registro.put("usu_estado", estado);
            registro.put("usu_login", login);
            registro.put("usu_clave", clave);

            int cantidad = BaseDeDatos.update("usuario", registro, "cod_usu =" + codigo, null);
            BaseDeDatos.close();

            if (cantidad == 1) {
                Toast.makeText(this, "Registro Modificado Exitosamente", Toast.LENGTH_LONG).show();
                Cancelar();
                cargaLista();
                registrar.setEnabled(true);
            } else {
                Toast.makeText(this, "El Registro No se Actualizo", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Seleccione un Usuario para Editar", Toast.LENGTH_LONG).show();
        }
    }

    public void Eliminar(View view) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 1);
        final SQLiteDatabase BaseDeDatos = admin.getWritableDatabase(); // Hacemos final para usar dentro del listener

        final String codigo = aux_codigo.getText().toString();

        if (!codigo.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UsuarioActivity.this);
            builder.setMessage("¿Está seguro de eliminar el registro?");

            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int cantidad = BaseDeDatos.delete("usuario", "cod_usu =" + codigo, null);
                    // BaseDeDatos.close(); // Mejor cerrar al final del bloque o método

                    Cancelar();
                    registrar.setEnabled(true);
                    aux_nombre.requestFocus();

                    if (cantidad == 1) {
                        Toast.makeText(UsuarioActivity.this, "Registro Eliminado", Toast.LENGTH_LONG).show();
                        cargaLista();
                    }
                    // Cerramos la base de datos aquí si ya no se usa
                    // BaseDeDatos.close(); No podemos cerrarla aquí fácilmente por el scope, idealmente abrir y cerrar en el mismo hilo principal
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            Toast.makeText(UsuarioActivity.this, "Seleccione un Elemento", Toast.LENGTH_LONG).show();
        }
    }

    public void llamaCancelar(View view) {
        Cancelar();
    }

    public void Cancelar() {
        aux_codigo.setText("");
        aux_nombre.setText("");

        // Resetear Spinners a la primera opción
        if(txt_nivel.getAdapter() != null) txt_nivel.setSelection(0);
        if(txt_estado.getAdapter() != null) txt_estado.setSelection(0);

        aux_login.setText("");
        aux_pass.setText("");
        registrar.setEnabled(true);
        aux_codigo.setEnabled(false); // Mantener bloqueado el código
    }

    public void salir(View view) {
        finish();
    }
}
