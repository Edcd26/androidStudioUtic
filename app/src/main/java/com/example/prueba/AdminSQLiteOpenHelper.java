package com.example.prueba;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper
{
    public AdminSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
   {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase bd_pam3)
    {
        //SQLITE BASE DE DATOS NATIVA
        bd_pam3.execSQL("create table pedidos(id_pedido integer primary key autoincrement, id_producto integer, cod_usu integer, cantidad integer,ped_fecha date, p_estado text)");
        bd_pam3.execSQL("create table producto(id_producto integer primary key autoincrement, producto_descri text, producto_estado text)");

        // Usuario
        bd_pam3.execSQL("create table usuario(cod_usu integer primary key autoincrement, usu_nombre text, usu_rol text, usu_estado text, usu_login integer, usu_clave text)");
        bd_pam3.execSQL("insert into usuario values(1, 'DENIS CORREA', 'ADMINISTRADOR','ACTIVO', 'admin',123)");

        //Pais
        bd_pam3.execSQL("create table pais(id_pais integer primary key autoincrement, pais_descri text)");
        bd_pam3.execSQL("insert into pais values(1,'PARAGUAY')");

        // Proveedor
        bd_pam3.execSQL("create table proveedor(cod_prov integer primary key autoincrement, prov_razonsocial text, prov_ruc text, prov_tel text, prov_direccion integer, prov_email text)");
        bd_pam3.execSQL("insert into proveedor values(1,'INSTRUMAQ S.A.','80012523-1','0981336679','San Blas 276','instrumaq@gmail.com')");


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int il)
    {

    }
}
