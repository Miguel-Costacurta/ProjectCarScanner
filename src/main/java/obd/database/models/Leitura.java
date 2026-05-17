package obd.database.models;

import java.time.LocalDateTime;

public class Leitura {
    private int id;
    private int sessaoId;
    private LocalDateTime timestamp;
    private String pid;
    private double valor;

    public Leitura(){}

    public Leitura(int sessaoId, String pid, double valor){
        this.sessaoId = sessaoId;
        this.pid = pid;
        this.valor = valor;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSessaoId() {
        return sessaoId;
    }

    public void setSessaoId(int sessaoId) {
        this.sessaoId = sessaoId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
