package com.jailsonspeedway.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jailsonspeedway.instagram.R;
import com.jailsonspeedway.instagram.helper.ConfiguracaoFirebase;
import com.jailsonspeedway.instagram.helper.UsuarioFirebase;
import com.jailsonspeedway.instagram.model.Usuario;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;

    private DatabaseReference firebaseRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference seguidoresref;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //Configurações iniciais
        firebaseRef   = ConfiguracaoFirebase.getFirebase();
        usuariosRef   = firebaseRef.child("usuarios");
        seguidoresref = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

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


    private  void recuperaDadosUsuarioLogado(){

        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Recupera dados de usuário logado
                usuarioLogado = dataSnapshot.getValue(Usuario.class);

                //Verifica se usuário já está seguindo amigo selecionado
                verificaSegueUsuarioAmigo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void verificaSegueUsuarioAmigo(){
        DatabaseReference seguidorRef = seguidoresref.child(idUsuarioLogado).child(usuarioSelecionado.getId());
        /*addListenerForSingleValueEvent() verifica apenas uma vez, consulta apenas um única vez
         não precisa ficar ouvindo o banco de dados
         */
        seguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //Já seguindo
                    Log.i("DadosUsuario", "Seguindo");
                    habilitarBotaoSeguir(true);

                }else{
                    //Ainda não está seguindo
                    Log.i("DadosUsuario", "Seguir");
                    habilitarBotaoSeguir(false);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void habilitarBotaoSeguir(boolean segueUsuario){
        if(segueUsuario){
            buttonAcaoPerfil.setText("Seguindo");
        }else{
            buttonAcaoPerfil.setText("Seguir");

            //Adiciona evendo para seguir usuário
            buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Salvar Seguidor
                    salvarSeguidor(usuarioLogado, usuarioSelecionado);
                }
            });
        }
    }

    public void salvarSeguidor(Usuario uLogado, Usuario uAmigo){

        /*
        seguidores
            id_jailson
                id_seguindo
                    dados seguindo
        * */

        HashMap<String, Object>dadosAmigo = new HashMap<>();
        dadosAmigo.put("nome", uAmigo.getNome());
        dadosAmigo.put("caminhoFoto", uAmigo.getCaminhoFoto());

        DatabaseReference seguidorRef = seguidoresref.child(uLogado.getId()).child(uAmigo.getId());
        seguidorRef.setValue(dadosAmigo);

        //Alterar botao acao para seguindo
        buttonAcaoPerfil.setText("Seguindo");
        buttonAcaoPerfil.setOnClickListener(null);

        //Incrementar seguindo do usuário logado
        int seguindo = uLogado.getSeguindo() + 1;

        HashMap<String, Object>dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo", seguindo);

        DatabaseReference usuarioSeguindo = usuariosRef.child(uLogado.getId());
        usuarioSeguindo.updateChildren(dadosSeguindo);

        //Incrementar seguidores do amigo

        int seguidores = uAmigo.getSeguidores() + 1;

        HashMap<String, Object>dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores", seguidores);

        DatabaseReference usuarioSeguidores = usuariosRef.child(uAmigo.getId());
        usuarioSeguidores.updateChildren(dadosSeguidores);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Recupera dados do amigo selecionado
        recuperaDadosperfilAmigo();

        //Recupera dados de usuário logado
        recuperaDadosUsuarioLogado();
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
        buttonAcaoPerfil.setText("Carregando");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}