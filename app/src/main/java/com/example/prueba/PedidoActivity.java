package com.example.prueba;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PedidoActivity extends AppCompatActivity {
    private ListView listaPedidos;
    private EditText txtFiltro;
    private ArrayList<String> pedidosList = new ArrayList<>();
    
    // Listas temporales para el detalle del modal
    private ArrayList<String> detalleTempDisplay = new ArrayList<>();
    private ArrayList<Integer> detalleTempProdId = new ArrayList<>();
    private ArrayList<Integer> detalleTempCant = new ArrayList<>();

    // Mapas para obtener IDs a partir de descripciones
    private HashMap<String, Integer> mapDepositos = new HashMap<>();
    private HashMap<String, Integer> mapProductos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pedido);

        listaPedidos = findViewById(R.id.lista_pedidos);
        txtFiltro = findViewById(R.id.txt_filtro);

        actualizarListaPedidos("");

        // Filtro en tiempo real
        txtFiltro.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                actualizarListaPedidos(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Evento click para ver detalle
        listaPedidos.setOnItemClickListener((parent, view, position, id) -> {
            String item = pedidosList.get(position);
            int idPedido = Integer.parseInt(item.split(" - ")[0].replace("Pedido #", "").trim());
            verDetallePedido(idPedido);
        });
    }

    private void actualizarListaPedidos(String filtro) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        pedidosList.clear();

        String sql = "SELECT p.id_pedido, p.p_estado, d.deposito_descri " +
                "FROM pedidos p INNER JOIN deposito d ON p.id_deposito = d.id_deposito ";
        if (!filtro.isEmpty()) sql += " WHERE p.id_pedido LIKE '%" + filtro + "%' ";
        sql += " ORDER BY p.id_pedido DESC";

        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            pedidosList.add("Pedido #" + c.getString(0) + " - " + c.getString(1) + 
                           "\nDeposito: " + c.getString(2) + "\nVer detalles");
        }
        c.close(); db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, pedidosList) {
            @NonNull @Override public View getView(int pos, @Nullable View v, @NonNull ViewGroup parent) {
                View view = super.getView(pos, v, parent);
                TextView t1 = view.findViewById(android.R.id.text1);
                TextView t2 = view.findViewById(android.R.id.text2);
                String[] parts = getItem(pos).split("\n");
                if (parts.length > 2) {
                    t1.setText(parts[0]); t1.setTextSize(14); t1.setAlpha(0.9f);
                    t2.setText(parts[1] + "\n" + parts[2]);
                    t2.setTextSize(12); t2.setAlpha(0.6f);
                    // El "Ver detalles" se puede alinear a la derecha si se usara un layout custom, 
                    // pero con simple_list_item_2 trataremos de darle el formato solicitado.
                }
                return view;
            }
        };
        listaPedidos.setAdapter(adapter);
    }

    private void verDetallePedido(int idPedido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_pedido, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvTitulo = dialogView.findViewById(R.id.tv_titulo_modal);
        if (tvTitulo != null) tvTitulo.setText("DETALLE DE PEDIDO #" + idPedido);

        TextView tvUser = dialogView.findViewById(R.id.tv_usuario);
        TextView tvFecha = dialogView.findViewById(R.id.tv_fecha);
        AutoCompleteTextView spDeposito = dialogView.findViewById(R.id.sp_deposito_auto);
        
        dialogView.findViewById(R.id.sp_producto_auto).setEnabled(false);
        dialogView.findViewById(R.id.et_cantidad).setEnabled(false);
        dialogView.findViewById(R.id.btn_add_producto).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btn_registrar_final).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btn_limpiar_pedido).setVisibility(View.GONE);
        spDeposito.setEnabled(false);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        
        Cursor c = db.rawQuery("SELECT p.ped_fecha, u.usu_nombre, d.deposito_descri FROM pedidos p " +
                "INNER JOIN usuario u ON p.id_user = u.cod_usu " +
                "INNER JOIN deposito d ON p.id_deposito = d.id_deposito WHERE p.id_pedido=" + idPedido, null);
        
        if(c.moveToFirst()){
            tvFecha.setText(c.getString(0));
            tvUser.setText(c.getString(1));
            spDeposito.setText(c.getString(2));
        }
        c.close();

        ArrayList<String> det = new ArrayList<>();
        Cursor d = db.rawQuery("SELECT pr.prod_descri, dp.cantidad FROM detalle_pedido dp " +
                "INNER JOIN producto pr ON dp.cod_producto = pr.id_producto WHERE dp.id_pedido=" + idPedido, null);
        while(d.moveToNext()){
            det.add("📦 " + d.getString(0) + " - Cantidad: " + d.getString(1));
        }
        d.close(); db.close();
        
        ListView lvDetalle = dialogView.findViewById(R.id.lv_detalle_temporal);
        lvDetalle.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, det));

        dialogView.findViewById(R.id.btn_cancelar_pedido).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void abrirFormularioPedido(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_pedido, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvTitulo = dialogView.findViewById(R.id.tv_titulo_modal);
        if (tvTitulo != null) tvTitulo.setText("REGISTRAR NUEVO PEDIDO");

        TextView tvUser = dialogView.findViewById(R.id.tv_usuario);
        TextView tvFecha = dialogView.findViewById(R.id.tv_fecha);
        AutoCompleteTextView spDeposito = dialogView.findViewById(R.id.sp_deposito_auto);
        AutoCompleteTextView spProducto = dialogView.findViewById(R.id.sp_producto_auto);
        EditText etCantidad = dialogView.findViewById(R.id.et_cantidad);
        ListView lvDetalle = dialogView.findViewById(R.id.lv_detalle_temporal);
        Button btnAdd = dialogView.findViewById(R.id.btn_add_producto);
        Button btnRegistrar = dialogView.findViewById(R.id.btn_registrar_final);

        SharedPreferences pref = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        String login = pref.getString("user", "admin");
        tvUser.setText(login);
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvFecha.setText(fechaActual);

        cargarMapas();
        spDeposito.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(mapDepositos.keySet())));
        spProducto.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(mapProductos.keySet())));

        detalleTempDisplay.clear(); detalleTempProdId.clear(); detalleTempCant.clear();
        ArrayAdapter<String> adapterDetalle = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, detalleTempDisplay);
        lvDetalle.setAdapter(adapterDetalle);

        btnAdd.setOnClickListener(v -> {
            String prodDesc = spProducto.getText().toString();
            String cantS = etCantidad.getText().toString();
            if (mapProductos.containsKey(prodDesc) && !cantS.isEmpty()) {
                detalleTempDisplay.add("📦 " + prodDesc + " x " + cantS);
                detalleTempProdId.add(mapProductos.get(prodDesc));
                detalleTempCant.add(Integer.parseInt(cantS));
                adapterDetalle.notifyDataSetChanged();
                spProducto.setText(""); etCantidad.setText("");
            } else {
                Toast.makeText(this, "Datos de producto inválidos", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegistrar.setOnClickListener(v -> {
            String dep = spDeposito.getText().toString();
            if (!mapDepositos.containsKey(dep) || detalleTempProdId.isEmpty()) {
                Toast.makeText(this, "Complete depósito y productos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (guardarPedidoBD(mapDepositos.get(dep), login, fechaActual)) {
                dialog.dismiss();
                actualizarListaPedidos("");
            }
        });

        dialogView.findViewById(R.id.btn_cancelar_pedido).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_limpiar_pedido).setOnClickListener(v -> {
            detalleTempDisplay.clear(); detalleTempProdId.clear(); detalleTempCant.clear();
            adapterDetalle.notifyDataSetChanged();
        });

        dialog.show();
    }

    private void cargarMapas() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        mapDepositos.clear();
        Cursor c1 = db.rawQuery("SELECT id_deposito, deposito_descri FROM deposito", null);
        while (c1.moveToNext()) mapDepositos.put(c1.getString(1), c1.getInt(0));
        c1.close();
        mapProductos.clear();
        Cursor c2 = db.rawQuery("SELECT id_producto, prod_descri FROM producto", null);
        while (c2.moveToNext()) mapProductos.put(c2.getString(1), c2.getInt(0));
        c2.close(); db.close();
    }

    private boolean guardarPedidoBD(int idDep, String login, String fecha) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getWritableDatabase();
        try {
            db.beginTransaction();
            Cursor c = db.rawQuery("SELECT cod_usu FROM usuario WHERE usu_login='" + login + "'", null);
            int idUsu = 1; if (c.moveToFirst()) idUsu = c.getInt(0); c.close();

            ContentValues p = new ContentValues();
            p.put("id_user", idUsu); p.put("p_estado", "PENDIENTE"); p.put("ped_fecha", fecha); p.put("id_deposito", idDep);
            long idP = db.insert("pedidos", null, p);

            for (int i = 0; i < detalleTempProdId.size(); i++) {
                ContentValues d = new ContentValues();
                d.put("id_pedido", idP); d.put("cod_producto", detalleTempProdId.get(i)); d.put("cantidad", detalleTempCant.get(i));
                db.insert("detalle_pedido", null, d);
            }
            db.setTransactionSuccessful();
            Toast.makeText(this, "Pedido registrado exitosamente", Toast.LENGTH_SHORT).show();
            return true;
        } finally { db.endTransaction(); db.close(); }
    }

    public void salir(View view) { finish(); }
}
