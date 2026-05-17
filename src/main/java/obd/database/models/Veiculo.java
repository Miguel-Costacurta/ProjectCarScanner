package obd.database.models;

import java.time.LocalDateTime;

public class Veiculo {
    private int id;
    private String apelido;
    private String modelo;
    private String marca;
    private int ano;
    private String motor;
    private String vin;
    private LocalDateTime dataCriacao;

    public Veiculo(){}

    public Veiculo(String apelido, String marca, String modelo
                    ,int ano, String motor, String vin){
        this.ano = ano;
        this.apelido = apelido;
        this.marca = marca;
        this.modelo = modelo;
        this.motor = motor;
        this.vin = vin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getMotor() {
        return motor;
    }

    public void setMotor(String motor) {
        this.motor = motor;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public String toString(){
        return apelido + " - " + marca + " " + modelo + " " + ano;
    }
}
