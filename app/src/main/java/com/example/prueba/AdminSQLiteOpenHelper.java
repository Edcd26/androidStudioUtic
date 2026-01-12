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
        // Usuario
        bd_pam3.execSQL("create table usuario(cod_usu integer primary key autoincrement, usu_nombre text, usu_rol text, usu_estado text, usu_login text, usu_clave text)");
        bd_pam3.execSQL("insert into usuario values(1, 'DENIS CORREA', 'ADMINISTRADOR','ACTIVO', 'admin', '123')");

        // Pais
        bd_pam3.execSQL("create table pais(id_pais integer primary key autoincrement, pais_descri text)");
        bd_pam3.execSQL("insert into pais values(1,'PARAGUAY')");

        // Proveedor
        bd_pam3.execSQL("create table proveedor(cod_prov integer primary key autoincrement, prov_razonsocial text, prov_ruc text, prov_tel text, prov_direccion text, prov_email text)");
        bd_pam3.execSQL("insert into proveedor values(1,'INSTRUMAQ S.A.','80012523-1','0981336679','San Blas 276','instrumaq@gmail.com')");

        // Unidad de medida
        bd_pam3.execSQL("create table u_medida(id_u_medida integer primary key autoincrement, u_medida_descri text)");
        bd_pam3.execSQL("insert into u_medida values(1,'ml')");
        bd_pam3.execSQL("insert into u_medida values(2,'cm')");
        bd_pam3.execSQL("insert into u_medida values(3,'u')");
        bd_pam3.execSQL("insert into u_medida values(4,'kg')");

        // Deposito
        bd_pam3.execSQL("create table deposito(id_deposito integer primary key autoincrement, deposito_descri text)");
        bd_pam3.execSQL("insert into deposito values(1,'Capiata Matriz')");
        bd_pam3.execSQL("insert into deposito values(2,'San Lorenzo')");

        // Tabla producto
        bd_pam3.execSQL("create table producto(id_producto integer primary key autoincrement, prod_tipo text, id_u_medida integer, prod_descri text)");
        bd_pam3.execSQL("insert into producto values(1, 'Planteras', 3, 'PLANTERA PLASTICA')");

        // Tabla pedidos (Encabezado)
        bd_pam3.execSQL("create table pedidos(id_pedido integer primary key autoincrement, id_user integer, p_estado text, ped_fecha date, id_deposito integer)");
        
        // Tabla detalle_pedido
        bd_pam3.execSQL("create table detalle_pedido(id_detalle integer primary key autoincrement, id_pedido integer, cod_producto integer, cantidad integer)");

        // Tabla presupuestos (Encabezado)
        bd_pam3.execSQL("create table presupuestos(id_presupuesto integer primary key autoincrement, id_user integer, estado text, fecha date, id_prov integer, id_pedido integer)");

        // Tabla detalle_presupuesto
        bd_pam3.execSQL("create table detalle_presupuesto(id_detalle integer primary key autoincrement, id_presupuesto integer, cod_producto integer, cantidad integer, precio integer, total integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int il)
    {
        sqLiteDatabase.execSQL("drop table if exists usuario");
        sqLiteDatabase.execSQL("drop table if exists producto");
        sqLiteDatabase.execSQL("drop table if exists pedidos");
        sqLiteDatabase.execSQL("drop table if exists detalle_pedido");
        sqLiteDatabase.execSQL("drop table if exists presupuestos");
        sqLiteDatabase.execSQL("drop table if exists detalle_presupuesto");
        sqLiteDatabase.execSQL("drop table if exists pais");
        sqLiteDatabase.execSQL("drop table if exists proveedor");
        sqLiteDatabase.execSQL("drop table if exists u_medida");
        sqLiteDatabase.execSQL("drop table if exists deposito");
        onCreate(sqLiteDatabase);
    }
}
