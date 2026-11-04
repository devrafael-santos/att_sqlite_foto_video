package com.example.att_sqlite_foto_video;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "midias.db";
    private static final int VERSAO_BANCO = 2;

    public static final String TABELA = "midias";
    public static final String COL_ID = "id";
    public static final String COL_TIPO = "tipo";
    public static final String COL_CAMINHO = "caminho";
    public static final String COL_DESCRICAO = "descricao";
    public static final String COL_DATA = "data_criacao";
    public static final String COL_FAVORITO = "favorito";

    private static final String SQL_CRIAR_TABELA =
            "CREATE TABLE " + TABELA + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TIPO + " TEXT NOT NULL, " +
                    COL_CAMINHO + " TEXT NOT NULL, " +
                    COL_DESCRICAO + " TEXT, " +
                    COL_DATA + " TEXT NOT NULL, " +
                    COL_FAVORITO + " INTEGER DEFAULT 0)";

    public DatabaseHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CRIAR_TABELA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versaoAntiga, int versaoNova) {
        if (versaoAntiga < 2) {
            db.execSQL("ALTER TABLE " + TABELA + " ADD COLUMN " + COL_FAVORITO + " INTEGER DEFAULT 0");
        }
    }

    public long inserir(Midia midia) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_TIPO, midia.getTipo());
        valores.put(COL_CAMINHO, midia.getCaminho());
        valores.put(COL_DESCRICAO, midia.getDescricao());
        valores.put(COL_DATA, midia.getDataCriacao());
        valores.put(COL_FAVORITO, midia.isFavorito() ? 1 : 0);
        return db.insert(TABELA, null, valores);
    }

    public List<Midia> listarTodas() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABELA, null, null, null, null, null, COL_ID + " DESC");
        return lerCursor(cursor);
    }

    public List<Midia> listarPorTipo(String tipo) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABELA, null, COL_TIPO + " = ?", new String[]{tipo}, null, null, COL_ID + " DESC");
        return lerCursor(cursor);
    }

    public List<Midia> buscarPorDescricao(String texto) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABELA, null, COL_DESCRICAO + " LIKE ?", new String[]{"%" + texto + "%"}, null, null, COL_ID + " DESC");
        return lerCursor(cursor);
    }

    private List<Midia> lerCursor(Cursor cursor) {
        List<Midia> lista = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO));
                String caminho = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAMINHO));
                String descricao = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRICAO));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA));
                boolean favorito = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FAVORITO)) == 1;
                lista.add(new Midia(id, tipo, caminho, descricao, data, favorito));
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    public long contar() {
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, TABELA);
    }

    public int atualizarDescricao(long id, String novaDescricao) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_DESCRICAO, novaDescricao);
        return db.update(TABELA, valores, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int atualizarFavorito(long id, boolean favorito) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_FAVORITO, favorito ? 1 : 0);
        return db.update(TABELA, valores, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int excluir(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABELA, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }
}

