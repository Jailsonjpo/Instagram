package com.jailsonspeedway.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jailsonspeedway.instagram.R;
import com.jailsonspeedway.instagram.helper.ConfiguracaoFirebase;
import com.jailsonspeedway.instagram.model.Usuario;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private TextView textPublicacoes, textSeguidores, textSeguindo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //Configurações iniciais
        usuariosRef = ConfiguracaoFirebase.getFirebase().child("usuarios");

        //Inicializar componentes
        inicializarComponentes();

        //Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Perfil");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //Recuperar usuario selecionado
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioSelecionado");

            //Configura nome do usuário na toolbar
            getSupportActionBar().setTitle(usuarioSelecionado.getNome());

            //Recuparar foto do usuário
            String caminhofoto = usuarioSelecionado.getCaminhoFoto();

            if(caminhofoto != null){

                //Converter caminhoFoto que é uma String para um Objeto do tipo Uri
                Uri url = Uri.parse(caminhofoto);
                Glide.with(PerfilAmigoActivity.this).load(url).into(imagePerfil);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperaDadosperfilAmigo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }

    private void recuperaDadosperfilAmigo(){

        usuarioAmigoRef = usuariosRef.child(usuarioSelecionado.getId());
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                String postagens = String.valueOf(usuario.getPostagens());
                String seguindo = String.valueOf(usuario.getSeguindo());
                String seguidores = String.valueOf(usuario.getSeguidores());

                //Configura valores recuperados
                textPublicacoes.setText(postagens);
                textSeguidores.setText(seguidores);
                textSeguindo.setText(seguindo);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void inicializarComponentes(){
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        imagePerfil      = findViewById(R.id.imagePerfil);
        textPublicacoes  = findViewById(R.id.textPublicacoes);
        textSeguidores   = findViewById(R.id.textSeguidores);
        textSeguindo     = findViewById(R.id.textSeguindo);
        buttonAcaoPerfil.setText("Seguir");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}