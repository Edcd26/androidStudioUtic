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

public class PresupuestoActivity extends AppCompatActivity {
    private ListView listaPresupuestos;
    private EditText txtFiltro;
    private ArrayList<String> presupuestosList = new ArrayList<>();
    
    private ArrayList<String> detalleTempDisplay = new ArrayList<>();
    private ArrayList<Integer> detalleTempProdId = new ArrayList<>();
    private ArrayList<Integer> detalleTempCant = new ArrayList<>();
    private ArrayList<Integer> detalleTempPrecio = new ArrayList<>();

    private HashMap<String, Integer> mapProveedores = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_presupuesto);

        listaPresupuestos = findViewById(R.id.lista_presupuestos);
        txtFiltro = findViewById(R.id.txt_filtro);

        actualizarLista("");

        txtFiltro.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                actualizarLista(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        listaPresupuestos.setOnItemClickListener((parent, view, position, id) -> {
            String item = presupuestosList.get(position);
            int idPresu = Integer.parseInt(item.split(" - ")[0].replace("Nro: ", "").trim());
            verDetallePresupuesto(idPresu);
        });
    }

    private void actualizarLista(String filtro) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        presupuestosList.clear();

        String sql = "SELECT pr.id_presupuesto, p.prov_razonsocial, pr.id_pedido, pr.estado, " +
                "(SELECT SUM(total) FROM detalle_presupuesto WHERE id_presupuesto = pr.id_presupuesto) as total_presu " +
                "FROM presupuestos pr INNER JOIN proveedor p ON pr.id_prov = p.cod_prov " +
                "WHERE pr.estado = 'COTIZADO' ";
        
        if (!filtro.isEmpty()) sql += " AND pr.id_presupuesto LIKE '%" + filtro + "%' ";
        sql += " ORDER BY pr.id_presupuesto DESC";

        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            presupuestosList.add("Nro: " + c.getString(0) + " - Prov. " + c.getString(1) + 
                           "\nTotal: " + c.getString(4) + " - Pedido #" + c.getString(2) + " - [" + c.getString(3) + "]"+
                    "\nVer Detalles");
        }
        c.close(); db.close();

        listaPresupuestos.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, presupuestosList) {
            @NonNull @Override public View getView(int pos, @Nullable View v, @NonNull ViewGroup parent) {
                View view = super.getView(pos, v, parent);
                TextView t1 = view.findViewById(android.R.id.text1);
                TextView t2 = view.findViewById(android.R.id.text2);
                String[] parts = getItem(pos).split("\n");
                if (parts.length > 1) {
                    t1.setText(parts[0]); t1.setTextSize(14); t1.setAlpha(0.9f);
                    t2.setText(parts[1]); t2.setTextSize(12); t2.setAlpha(0.6f);
                }
                return view;
            }
        });
    }

    public void abrirFormulario(View view) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        
        Cursor cPed = db.rawQuery("SELECT id_pedido FROM pedidos", null);
        if (cPed.getCount() == 0) {
            Toast.makeText(this, "Primero debe cargar un pedido", Toast.LENGTH_LONG).show();
            cPed.close(); db.close();
            return;
        }
        cPed.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.layout_dialog_presupuesto, null);
        builder.setView(v);
        AlertDialog dialog = builder.create();

        AutoCompleteTextView spProv = v.findViewById(R.id.sp_prov_auto);
        AutoCompleteTextView spPedido = v.findViewById(R.id.sp_pedido_auto);
        AutoCompleteTextView spProd = v.findViewById(R.id.sp_prod_auto);
        TextView tvDepoInfo = v.findViewById(R.id.tv_deposito_info);
        EditText etPrecio = v.findViewById(R.id.et_precio_presu);
        ListView lvDetalle = v.findViewById(R.id.lv_detalle_presu);
        Button btnAddPrecio = v.findViewById(R.id.btn_add_precio);
        
        cargarDatosForm(spProv, spPedido);

        detalleTempDisplay.clear(); detalleTempProdId.clear(); detalleTempCant.clear(); detalleTempPrecio.clear();
        
        ArrayAdapter<String> adapterDetalle = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, detalleTempDisplay) {
            @NonNull @Override public View getView(int pos, @Nullable View v, @NonNull ViewGroup parent) {
                View view = super.getView(pos, v, parent);
                TextView t1 = view.findViewById(android.R.id.text1);
                TextView t2 = view.findViewById(android.R.id.text2);
                String[] parts = getItem(pos).split("\n");
                if (parts.length > 2) {
                    t1.setText(parts[0]); t1.setTextSize(13); t1.setAlpha(0.9f);
                    t2.setText(parts[1] + "\n" + parts[2]);
                    t2.setTextSize(11); t2.setAlpha(0.7f);
                }
                return view;
            }
        };
        lvDetalle.setAdapter(adapterDetalle);

        spPedido.setOnItemClickListener((parent, view1, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            int idPed = Integer.parseInt(selected.replace("Pedido #", "").trim());
            
            // Buscar Deposito del pedido
            AdminSQLiteOpenHelper admin2 = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
            SQLiteDatabase db2 = admin2.getReadableDatabase();
            Cursor c = db2.rawQuery("SELECT d.deposito_descri FROM pedidos p INNER JOIN deposito d ON p.id_deposito = d.id_deposito WHERE p.id_pedido = " + idPed, null);
            if(c.moveToFirst()) tvDepoInfo.setText("Depósito: " + c.getString(0));
            c.close(); db2.close();
            
            cargarProductosDePedido(idPed, adapterDetalle, spProd);
        });

        btnAddPrecio.setOnClickListener(v1 -> {
            String prodS = spProd.getText().toString();
            String precioS = etPrecio.getText().toString();
            if(!prodS.isEmpty() && !precioS.isEmpty()){
                int precio = Integer.parseInt(precioS);
                for(int i=0; i<detalleTempDisplay.size(); i++){
                    if(detalleTempDisplay.get(i).contains(prodS)){
                        detalleTempPrecio.set(i, precio);
                        int cant = detalleTempCant.get(i);
                        detalleTempDisplay.set(i, "✅ " + prodS + "\nCant: " + cant + " - Prec Unit: " + precio + "\nTotal: " + (cant*precio));
                        adapterDetalle.notifyDataSetChanged();
                        etPrecio.setText(""); spProd.setText("");
                        break;
                    }
                }
            }
        });

        v.findViewById(R.id.btn_registrar_presu).setOnClickListener(v1 -> {
            String provS = spProv.getText().toString();
            String pedS = spPedido.getText().toString();
            if(!mapProveedores.containsKey(provS) || pedS.isEmpty() || detalleTempPrecio.contains(0)){
                Toast.makeText(this, "Debe cotizar todos los productos", Toast.LENGTH_SHORT).show();
                return;
            }
            int idPed = Integer.parseInt(pedS.replace("Pedido #", "").trim());
            if(guardarPresupuesto(mapProveedores.get(provS), idPed)){
                dialog.dismiss();
                actualizarLista("");
            }
        });

        v.findViewById(R.id.btn_cancelar_presu).setOnClickListener(v1 -> dialog.dismiss());
        dialog.show();
        db.close();
    }

    private void cargarProductosDePedido(int idPed, ArrayAdapter<String> adapter, AutoCompleteTextView spProd) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        detalleTempDisplay.clear(); detalleTempProdId.clear(); detalleTempCant.clear(); detalleTempPrecio.clear();
        ArrayList<String> prodsParaCombo = new ArrayList<>();
        
        Cursor c = db.rawQuery("SELECT dp.cod_producto, p.prod_descri, dp.cantidad FROM detalle_pedido dp " +
                "INNER JOIN producto p ON dp.cod_producto = p.id_producto WHERE dp.id_pedido = " + idPed, null);
        while(c.moveToNext()){
            detalleTempProdId.add(c.getInt(0));
            detalleTempCant.add(c.getInt(2));
            detalleTempPrecio.add(0);
            detalleTempDisplay.add("❌ " + c.getString(1) + "\nCant: " + c.getInt(2) + " - Prec Unit: PENDIENTE\nTotal: 0");
            prodsParaCombo.add(c.getString(1));
        }
        c.close(); db.close();
        adapter.notifyDataSetChanged();
        spProd.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, prodsParaCombo));
    }

    private void cargarDatosForm(AutoCompleteTextView spProv, AutoCompleteTextView spPed) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        mapProveedores.clear();
        Cursor c1 = db.rawQuery("SELECT cod_prov, prov_razonsocial FROM proveedor", null);
        ArrayList<String> provs = new ArrayList<>();
        while(c1.moveToNext()){
            provs.add(c1.getString(1));
            mapProveedores.put(c1.getString(1), c1.getInt(0));
        }
        c1.close();
        spProv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, provs));

        ArrayList<String> peds = new ArrayList<>();
        Cursor c2 = db.rawQuery("SELECT id_pedido FROM pedidos WHERE p_estado = 'PENDIENTE'", null);
        while(c2.moveToNext()) peds.add("Pedido #" + c2.getInt(0));
        c2.close();
        spPed.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, peds));
        db.close();
    }

    private boolean guardarPresupuesto(int idProv, int idPed) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getWritableDatabase();
        try {
            db.beginTransaction();
            SharedPreferences pref = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
            String login = pref.getString("user", "admin");
            Cursor cu = db.rawQuery("SELECT cod_usu FROM usuario WHERE usu_login='"+login+"'", null);
            int idUsu = 1; if(cu.moveToFirst()) idUsu = cu.getInt(0); cu.close();

            ContentValues p = new ContentValues();
            p.put("id_user", idUsu); p.put("estado", "COTIZADO");
            p.put("fecha", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
            p.put("id_prov", idProv); p.put("id_pedido", idPed);
            long idPr = db.insert("presupuestos", null, p);

            for (int i = 0; i < detalleTempProdId.size(); i++) {
                ContentValues d = new ContentValues();
                d.put("id_presupuesto", idPr); d.put("cod_producto", detalleTempProdId.get(i));
                d.put("cantidad", detalleTempCant.get(i)); d.put("precio", detalleTempPrecio.get(i));
                d.put("total", (long)detalleTempCant.get(i) * detalleTempPrecio.get(i));
                db.insert("detalle_presupuesto", null, d);
            }
            db.setTransactionSuccessful();
            Toast.makeText(this, "Presupuesto registrado", Toast.LENGTH_SHORT).show();
            return true;
        } finally { db.endTransaction(); db.close(); }
    }

    private void verDetallePresupuesto(int idPresu) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.layout_dialog_presupuesto, null);
        builder.setView(v);
        AlertDialog dialog = builder.create();

        v.findViewById(R.id.btn_registrar_presu).setVisibility(View.GONE);
        v.findViewById(R.id.layout_carga_precios).setVisibility(View.GONE);
        ((TextView)v.findViewById(R.id.tv_titulo_modal_presu)).setText("DETALLE PRESUPUESTO Nro: " + idPresu);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "bd_pam3", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT pr.fecha, p.prov_razonsocial, pr.id_pedido, d.deposito_descri FROM presupuestos pr " +
                "INNER JOIN proveedor p ON pr.id_prov = p.cod_prov " +
                "INNER JOIN pedidos ped ON pr.id_pedido = ped.id_pedido " +
                "INNER JOIN deposito d ON ped.id_deposito = d.id_deposito " +
                "WHERE pr.id_presupuesto=" + idPresu, null);
        if(c.moveToFirst()){
            ((AutoCompleteTextView)v.findViewById(R.id.sp_prov_auto)).setText(c.getString(1));
            ((AutoCompleteTextView)v.findViewById(R.id.sp_pedido_auto)).setText("Pedido #" + c.getInt(2));
            ((TextView)v.findViewById(R.id.tv_deposito_info)).setText("Depósito: " + c.getString(3));
        }
        c.close();

        ArrayList<String> det = new ArrayList<>();
        Cursor d = db.rawQuery("SELECT p.prod_descri, dp.cantidad, dp.precio, dp.total FROM detalle_presupuesto dp " +
                "INNER JOIN producto p ON dp.cod_producto = p.id_producto WHERE dp.id_presupuesto=" + idPresu, null);
        while(d.moveToNext()){
            det.add("📋 " + d.getString(0) + "\nCant: " + d.getInt(1) + " - Prec Unit: " + d.getInt(2) + "\nTotal: " + d.getInt(3));
        }
        d.close(); db.close();

        ListView lvDetalle = v.findViewById(R.id.lv_detalle_presu);
        lvDetalle.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, det) {
            @NonNull @Override public View getView(int pos, @Nullable View v, @NonNull ViewGroup parent) {
                View view = super.getView(pos, v, parent);
                TextView t1 = view.findViewById(android.R.id.text1);
                TextView t2 = view.findViewById(android.R.id.text2);
                String[] parts = getItem(pos).split("\n");
                if (parts.length > 2) {
                    t1.setText(parts[0]); t1.setTextSize(13); t1.setAlpha(0.9f);
                    t2.setText(parts[1] + "\n" + parts[2]);
                    t2.setTextSize(11); t2.setAlpha(0.7f);
                }
                return view;
            }
        });
        
        v.findViewById(R.id.btn_cancelar_presu).setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    public void salir(View view) { finish(); }
}
