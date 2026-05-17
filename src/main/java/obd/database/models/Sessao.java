package obd.database.models;

import java.time.LocalDateTime;

public class Sessao {
    private int id;
    private int veiculoId;
    private String descricao;
    private LocalDateTime inicio;
    private LocalDateTime fim;

    public Sessao() {}

    public Sessao(int veiculoId, String descricao){
        this.veiculoId = veiculoId;
        this.descricao = descricao;
        this.inicio = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(int veiculoId) {
        this.veiculoId = veiculoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    @Override
    public String toString(){
        return descricao + " - " + inicio;
    }
}
