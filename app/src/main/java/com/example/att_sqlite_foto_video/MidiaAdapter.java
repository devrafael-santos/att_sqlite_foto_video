package com.example.att_sqlite_foto_video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class MidiaAdapter extends ArrayAdapter<Midia> {

    public MidiaAdapter(Context context, List<Midia> midias) {
        super(context, 0, midias);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_midia, parent, false);
        }

        Midia midia = getItem(position);
        ImageView imgMiniatura = convertView.findViewById(R.id.imgMiniatura);
        TextView txtDescricao = convertView.findViewById(R.id.txtDescricao);
        TextView txtDetalhes = convertView.findViewById(R.id.txtDetalhes);

        if (midia == null) return convertView;

        String descricao = midia.getDescricao();
        String textoBase = (descricao == null || descricao.isEmpty()) ? "(sem descrição)" : descricao;
        if (midia.isFavorito()) {
            textoBase = "★ " + textoBase;
        }
        txtDescricao.setText(textoBase);
        txtDetalhes.setText(midia.getTipo() + " • " + midia.getDataCriacao());

        if (midia.isFoto()) {
            Bitmap miniatura = carregarMiniatura(midia.getCaminho(), 120);
            if (miniatura != null) {
                imgMiniatura.setImageBitmap(miniatura);
            } else {
                imgMiniatura.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            Bitmap frameVideo = carregarMiniaturaVideo(midia.getCaminho());
            if (frameVideo != null) {
                imgMiniatura.setImageBitmap(frameVideo);
            } else {
                imgMiniatura.setImageResource(android.R.drawable.ic_media_play);
            }
        }

        return convertView;
    }

    private Bitmap carregarMiniatura(String caminho, int tamanhoAlvo) {
        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(caminho, opcoes);

        if (opcoes.outWidth <= 0 || opcoes.outHeight <= 0) return null;

        int fator = 1;
        while (opcoes.outWidth / (fator * 2) >= tamanhoAlvo
                && opcoes.outHeight / (fator * 2) >= tamanhoAlvo) {
            fator *= 2;
        }

        opcoes.inJustDecodeBounds = false;
        opcoes.inSampleSize = fator;
        return BitmapFactory.decodeFile(caminho, opcoes);
    }

    private Bitmap carregarMiniaturaVideo(String caminho) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(caminho);
            return retriever.getFrameAtTime();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }
}

