package com.example.att_sqlite_foto_video;

public class Midia {

    public static final String TIPO_FOTO = "FOTO";
    public static final String TIPO_VIDEO = "VIDEO";

    private long id;
    private String tipo;
    private String caminho;
    private String descricao;
    private String dataCriacao;
    private boolean favorito;

    public Midia(String tipo, String caminho, String descricao, String dataCriacao) {
        this.tipo = tipo;
        this.caminho = caminho;
        this.descricao = descricao;
        this.dataCriacao = dataCriacao;
        this.favorito = false;
    }

    public Midia(long id, String tipo, String caminho, String descricao, String dataCriacao) {
        this(tipo, caminho, descricao, dataCriacao);
        this.id = id;
    }

    public Midia(long id, String tipo, String caminho, String descricao, String dataCriacao, boolean favorito) {
        this(id, tipo, caminho, descricao, dataCriacao);
        this.favorito = favorito;
    }

    public boolean isFoto() {
        return TIPO_FOTO.equals(tipo);
    }

    public long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getCaminho() { return caminho; }
    public String getDescricao() { return descricao; }
    public String getDataCriacao() { return dataCriacao; }
    public boolean isFavorito() { return favorito; }

    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setFavorito(boolean favorito) { this.favorito = favorito; }
}
