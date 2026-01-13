package com.example.prueba;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;


public class MenuPrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Mostrar nombre de usuario logueado
        TextView tvUserLog = findViewById(R.id.tv_usuario_logueado);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && tvUserLog != null) {
            String nombre = bundle.getString("parametro_usu");
            String login = bundle.getString("parametro_login");
            tvUserLog.setText(nombre + " (" + login + ")");
        }
    }

    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.nav_Producto)
        {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null && "ADMINISTRADOR".equals(bundle.getString("parametro_rol")))
            {
                startActivity(new Intent(this, ProductosActivity.class));
            }
            else
            {
                Toast.makeText(this, "No tiene Privilegios para utilizar esta Opcion..", Toast.LENGTH_LONG).show();
            }
        }
        else if (id == R.id.nav_U_Medida)
        {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null && "ADMINISTRADOR".equals(bundle.getString("parametro_rol")))
            {
                startActivity(new Intent(this,U_MedidaActivity.class));
            }
            else
            {
                Toast.makeText(this, "No tiene Privilegios para utilizar esta Opcion..", Toast.LENGTH_LONG).show();
            }

        }
        else if (id == R.id.nav_Deposito)
        {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null && "ADMINISTRADOR".equals(bundle.getString("parametro_rol")))
            {
                startActivity(new Intent(this, DepositoActivity.class));
            }
            else
            {
                Toast.makeText(this, "No tiene Privilegios para utilizar esta Opcion..", Toast.LENGTH_LONG).show();
            }
        }
        else if (id == R.id.nav_Proveedor)
        {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null && "ADMINISTRADOR".equals(bundle.getString("parametro_rol")))
            {
                startActivity(new Intent(this,ProveedorActivity.class));
            }
            else
            {
                Toast.makeText(this, "No tiene Privilegios para utilizar esta Opcion..", Toast.LENGTH_LONG).show();
            }
        }
        else if (id == R.id.nav_Pedido)
        {
            startActivity(new Intent(this,PedidoActivity.class));
        }
        else if (id == R.id.nav_Presupuesto)
        {
            startActivity(new Intent(this,PresupuestoActivity.class));
        }
        else if (id == R.id.nav_Usuario)
        {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null && "ADMINISTRADOR".equals(bundle.getString("parametro_rol")))
            {
                startActivity(new Intent(this, UsuarioActivity.class));
            }
            else
            {
                Toast.makeText(this, "No tiene Privilegios para utilizar esta Opcion..", Toast.LENGTH_LONG).show();
            }
        }
        else if (id == R.id.nav_salir)
        {
            Intent intent = new Intent(this, AccesoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_about, null);
        builder.setView(dialogView);

        TextView tvInfo = dialogView.findViewById(R.id.tv_about_info);
        tvInfo.setText("Esta Aplicacion es un propotipo Mobile del Sistema de Gestion elaborado por Elvio Denis Correa (0981336679) para la materia del 3er año Programacion de Aplicaciones Moviles - UTIC 2026.\n\n" +
                "Vivero Siempre Verde - Insumos para jardinería\n" +
                "El Vivero Siempre verde es un negocio familiar que ofrece Insumos para " +
                "jardinería en general, desde sustratos para cactus y suculentas hasta planteras y " +
                "piedras decorativas para paisajismos de Jardín. La empresa fue creada a mediados " +
                "del 2019 y actualmente se asienta como una pequeña distribuidora para los viveros de " +
                "la zona y un lugar elegido por muchos clientes del país, el negocio actualmente se " +
                "encuentra en amplio crecimiento por lo que precisan de un sistema de gestión " +
                "informático a modo de lograr una mayor organización y promover el crecimiento de la " +
                "misma.\n\n" +
                "Capiatá Ruta 1 km 19/2 - (0981) 904 414\n" +
                "siempreverde@gmail.com\nhttps://www.instagram.com/siempre_verde/");

        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }

}
