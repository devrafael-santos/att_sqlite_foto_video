package com.example.att_sqlite_foto_video;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String ESTADO_CAMINHO = "caminhoAtual";
    private static final String ESTADO_TIPO = "tipoAtual";

    private DatabaseHelper db;
    private MidiaAdapter adapter;
    private final List<Midia> listaMidias = new ArrayList<>();

    private TextView txtTitulo;
    private EditText edtBusca;
    private String filtroTipo = null;

    private File arquivoAtual;
    private String tipoAtual;

    private final ActivityResultLauncher<Uri> tirarFotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    sucesso -> tratarResultadoCaptura(Boolean.TRUE.equals(sucesso)));

    private final ActivityResultLauncher<Uri> gravarVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.CaptureVideo(),
                    sucesso -> tratarResultadoCaptura(Boolean.TRUE.equals(sucesso)));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        if (savedInstanceState != null) {
            String caminho = savedInstanceState.getString(ESTADO_CAMINHO);
            if (caminho != null) arquivoAtual = new File(caminho);
            tipoAtual = savedInstanceState.getString(ESTADO_TIPO);
        }

        db = new DatabaseHelper(this);

        Button btnFoto = findViewById(R.id.btnFoto);
        Button btnVideo = findViewById(R.id.btnVideo);
        Button btnTodas = findViewById(R.id.btnTodas);
        Button btnFiltroFotos = findViewById(R.id.btnFiltroFotos);
        Button btnFiltroVideos = findViewById(R.id.btnFiltroVideos);
        edtBusca = findViewById(R.id.edtBusca);
        txtTitulo = findViewById(R.id.txtTitulo);
        ListView listView = findViewById(R.id.listaMidias);
        TextView txtVazio = findViewById(R.id.txtVazio);

        adapter = new MidiaAdapter(this, listaMidias);
        listView.setAdapter(adapter);
        listView.setEmptyView(txtVazio);

        btnFoto.setOnClickListener(v -> iniciarCaptura(Midia.TIPO_FOTO));
        btnVideo.setOnClickListener(v -> iniciarCaptura(Midia.TIPO_VIDEO));

        btnTodas.setOnClickListener(v -> {
            filtroTipo = null;
            edtBusca.setText("");
            carregarLista();
        });

        btnFiltroFotos.setOnClickListener(v -> {
            filtroTipo = Midia.TIPO_FOTO;
            edtBusca.setText("");
            carregarLista();
        });

        btnFiltroVideos.setOnClickListener(v -> {
            filtroTipo = Midia.TIPO_VIDEO;
            edtBusca.setText("");
            carregarLista();
        });

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarLista();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) ->
                abrirMidia(listaMidias.get(position)));

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            mostrarOpcoes(listaMidias.get(position));
            return true;
        });

        carregarLista();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (arquivoAtual != null) outState.putString(ESTADO_CAMINHO, arquivoAtual.getAbsolutePath());
        outState.putString(ESTADO_TIPO, tipoAtual);
    }

    private void iniciarCaptura(String tipo) {
        try {
            tipoAtual = tipo;
            arquivoAtual = criarArquivo(tipo);
            Uri uri = obterUri(arquivoAtual);

            if (Midia.TIPO_FOTO.equals(tipo)) {
                tirarFotoLauncher.launch(uri);
            } else {
                gravarVideoLauncher.launch(uri);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nenhum app de câmera encontrado", Toast.LENGTH_LONG).show();
        }
    }

    private File criarArquivo(String tipo) {
        String carimbo = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        boolean foto = Midia.TIPO_FOTO.equals(tipo);
        File pasta = getExternalFilesDir(foto ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES);
        String nome = (foto ? "FOTO_" : "VIDEO_") + carimbo + (foto ? ".jpg" : ".mp4");
        return new File(pasta, nome);
    }

    private Uri obterUri(File arquivo) {
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);
    }

    private void tratarResultadoCaptura(boolean sucesso) {
        if (arquivoAtual == null) return;

        if (sucesso && arquivoAtual.exists() && arquivoAtual.length() > 0) {
            pedirDescricaoESalvar(arquivoAtual, tipoAtual);
        } else {
            if (arquivoAtual.exists()) arquivoAtual.delete();
            Toast.makeText(this, "Captura cancelada", Toast.LENGTH_SHORT).show();
        }
        arquivoAtual = null;
    }

    private void pedirDescricaoESalvar(File arquivo, String tipo) {
        EditText campo = new EditText(this);
        campo.setHint("Ex.: Quadro da aula");

        new AlertDialog.Builder(this)
                .setTitle("Descrição da " + (Midia.TIPO_FOTO.equals(tipo) ? "foto" : "vídeo"))
                .setView(campo)
                .setCancelable(false)
                .setPositiveButton("Salvar", (d, w) -> {
                    String data = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                    Midia midia = new Midia(tipo, arquivo.getAbsolutePath(),
                            campo.getText().toString().trim(), data);
                    long id = db.inserir(midia);
                    if (id != -1) {
                        Toast.makeText(this, "Salvo com id " + id, Toast.LENGTH_SHORT).show();
                    }
                    carregarLista();
                })
                .show();
    }

    private void carregarLista() {
        listaMidias.clear();
        String textoBusca = edtBusca.getText().toString().trim();

        if (!textoBusca.isEmpty()) {
            listaMidias.addAll(db.buscarPorDescricao(textoBusca));
        } else if (filtroTipo != null) {
            listaMidias.addAll(db.listarPorTipo(filtroTipo));
        } else {
            listaMidias.addAll(db.listarTodas());
        }

        txtTitulo.setText("Minhas mídias (" + db.contar() + ")");
        adapter.notifyDataSetChanged();
    }

    private void mostrarOpcoes(Midia midia) {
        String opcaoFavorito = midia.isFavorito() ? "Desfavoritar" : "Favoritar";
        String[] opcoes = {opcaoFavorito, "Editar descrição", "Excluir"};

        new AlertDialog.Builder(this)
                .setTitle("O que deseja fazer?")
                .setItems(opcoes, (d, qual) -> {
                    if (qual == 0) {
                        db.atualizarFavorito(midia.getId(), !midia.isFavorito());
                        carregarLista();
                    } else if (qual == 1) {
                        editarDescricao(midia);
                    } else {
                        confirmarExclusao(midia);
                    }
                })
                .show();
    }

    private void editarDescricao(Midia midia) {
        EditText campo = new EditText(this);
        campo.setText(midia.getDescricao());

        new AlertDialog.Builder(this)
                .setTitle("Editar descrição")
                .setView(campo)
                .setPositiveButton("Salvar", (d, w) -> {
                    db.atualizarDescricao(midia.getId(), campo.getText().toString().trim());
                    carregarLista();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarExclusao(Midia midia) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage("Excluir este registro e o arquivo?")
                .setPositiveButton("Excluir", (d, w) -> {
                    db.excluir(midia.getId());
                    File arquivo = new File(midia.getCaminho());
                    if (arquivo.exists()) arquivo.delete();
                    carregarLista();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirMidia(Midia midia) {
        File arquivo = new File(midia.getCaminho());
        if (!arquivo.exists()) {
            Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(obterUri(arquivo), midia.isFoto() ? "image/*" : "video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nenhum app para abrir este arquivo", Toast.LENGTH_SHORT).show();
        }
    }
}
