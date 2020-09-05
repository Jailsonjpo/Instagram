package com.jailsonspeedway.instagram.model;

import com.google.firebase.database.DatabaseReference;
import com.jailsonspeedway.instagram.helper.ConfiguracaoFirebase;

import java.io.Serializable;

public class Postagem implements Serializable {

    /*
    Modelo de Postagem
    postagens
        <id_usuario>
            <id_postagem_firebase>
                descricao
                caminhoFoto
                idUsuario
    */

    private String id;
    private String idUsuario;
    private String descricao;
    private String caminhoFoto;

    public Postagem() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference postagemRef = firebaseRef.child("postagens");
        String idPostagem = postagemRef.push().getKey();
        setId(idPostagem);

    }

    public boolean salvar(){

        DatabaseReference firebaseRef  = ConfiguracaoFirebase.getFirebase();
        DatabaseReference postagensRef = firebaseRef.child("postagens").child(getIdUsuario()).child(getId());
        postagensRef.setValue(this);
        return true;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}
