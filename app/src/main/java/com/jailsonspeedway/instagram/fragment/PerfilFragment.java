package com.jailsonspeedway.instagram.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jailsonspeedway.instagram.R;
import com.jailsonspeedway.instagram.activity.EditarPerfilActivity;
import com.jailsonspeedway.instagram.activity.PerfilAmigoActivity;
import com.jailsonspeedway.instagram.adapter.AdapterGrid;
import com.jailsonspeedway.instagram.helper.ConfiguracaoFirebase;
import com.jailsonspeedway.instagram.helper.UsuarioFirebase;
import com.jailsonspeedway.instagram.model.Postagem;
import com.jailsonspeedway.instagram.model.Usuario;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PerfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PerfilFragment extends Fragment {

    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    public GridView gridViewPerfil;
    private AdapterGrid adapterGrid;

    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private Button buttonAcaoPerfil;
    private Usuario usuarioLogado;

    private DatabaseReference firebaseRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private ValueEventListener valueEventListenerPerfil;
    private DatabaseReference postagensUsuarioRef;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PerfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PerfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PerfilFragment newInstance(String param1, String param2) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        //Configurações iniciais
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef   = ConfiguracaoFirebase.getFirebase();
        usuariosRef   = firebaseRef.child("usuarios");

        //Configurar referencia postagens usuario
        postagensUsuarioRef = ConfiguracaoFirebase.getFirebase().child("postagens").child(usuarioLogado.getId());


        //Configurações dos componentes
        inicializarComponentes(view);

        //Abre edição de perfil
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditarPerfilActivity.class);
                startActivity(i);
            }
        });

        //Inicializar image loader
        inicializarImageLoader();

        //Carrega as fotos das postagens de um usuário
        carregarFotosPostagem();

        return view;
    }

    public void carregarFotosPostagem(){

        //Recupera as fotos postadas pelo usuario
        postagensUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Configurar o tamanho do grid
                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                int tamanhoImagem = tamanhoGrid / 3;
                gridViewPerfil.setColumnWidth(tamanhoImagem);

                List<String> urlFotos = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    urlFotos.add(postagem.getCaminhoFoto());

                    //Log.i("postagem", "url: " + postagem.getCaminhoFoto());
                }

                //Configurar adapter
                adapterGrid = new AdapterGrid(getActivity(), R.layout.grid_postagem, urlFotos );
                gridViewPerfil.setAdapter(adapterGrid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void inicializarImageLoader(){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
                .memoryCache ( new LruMemoryCache( 2  *  1024  *  1024 ))
                .memoryCacheSize ( 2  *  1024  *  1024 )
                .diskCacheSize ( 50  *  1024  *  1024 )
                .diskCacheFileCount ( 100 )
                .diskCacheFileNameGenerator ( new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);
    }

    private void inicializarComponentes(View view){

        gridViewPerfil   = view.findViewById(R.id.gridViewPerfil);
        progressBar      = view.findViewById(R.id.progressBarPerfil);
        imagePerfil      = view.findViewById(R.id.imagePerfil);
        textPublicacoes  = view.findViewById(R.id.textPublicacoes);
        textSeguidores   = view.findViewById(R.id.textSeguidores);
        textSeguindo     = view.findViewById(R.id.textSeguindo);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);

    }

    private void recuperaDadosUsuarioLogado(){

        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId());
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                // String postagens = String.valueOf(usuario.getPostagens());
                String seguindo = String.valueOf(usuario.getSeguindo());
                String seguidores = String.valueOf(usuario.getSeguidores());

                //Configura valores recuperados
                // textPublicacoes.setText(postagens);
                textSeguidores.setText(seguidores);
                textSeguindo.setText(seguindo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void recuperarFotoUsuario(){

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuparar foto do usuário
        String caminhofoto = usuarioLogado.getCaminhoFoto();

        if(caminhofoto != null){

            //Converter caminhoFoto que é uma String para um Objeto do tipo Uri
            Uri url = Uri.parse(caminhofoto);
            Glide.with(getActivity()).load(url).into(imagePerfil);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        //Recupera dados do usuário logado
        recuperaDadosUsuarioLogado();

        //Recuperar foto usuario
        recuperarFotoUsuario();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioLogadoRef.removeEventListener(valueEventListenerPerfil);
    }
}