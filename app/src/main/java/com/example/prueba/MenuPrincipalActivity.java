package com.example.prueba;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
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
    }

    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.nav_Producto)
        {
            startActivity(new Intent(this, ProductosActivity.class));
        }
        else if (id == R.id.nav_U_Medida)
        {
            startActivity(new Intent(this,U_MedidaActivity.class));
        }
        else if (id == R.id.nav_Categoria)
        {
            startActivity(new Intent(this,CategoriaActivity.class));
        }
        else if (id == R.id.nav_Proveedor)
        {
            startActivity(new Intent(this,ProveedorActivity.class));
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
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

}