package obd.core.pids;

import java.util.HashMap;
import java.util.Map;

public enum EPidDescription {

    PID_01("01", "Status dos monitores desde limpeza de DTCs"),
    PID_02("02", "DTC que ativou o MIL"),
    PID_03("03", "Status do sistema de combustível"),
    PID_04("04", "Carga calculada do motor"),
    PID_05("05", "Temperatura do líquido de arrefecimento"),
    PID_06("06", "STFT — trim curto banco 1"),
    PID_07("07", "LTFT — trim longo banco 1"),
    PID_08("08", "STFT — trim curto banco 2"),
    PID_09("09", "LTFT — trim longo banco 2"),
    PID_0A("0A", "Pressão do combustível"),
    PID_0B("0B", "Pressão absoluta do coletor de admissão (MAP)"),
    PID_0C("0C", "Rotação do motor (RPM)"),
    PID_0D("0D", "Velocidade do veículo"),
    PID_0E("0E", "Avanço de ignição"),
    PID_0F("0F", "Temperatura do ar de admissão (IAT)"),
    PID_10("10", "Vazão de ar (MAF)"),
    PID_11("11", "Posição do acelerador (TPS)"),
    PID_12("12", "Status do ar secundário comandado"),
    PID_13("13", "Sondas lambda presentes (2 bancos)"),
    PID_14("14", "Sonda O2 — banco 1 sensor 1"),
    PID_15("15", "Sonda O2 — banco 1 sensor 2"),
    PID_16("16", "Sonda O2 — banco 1 sensor 3"),
    PID_17("17", "Sonda O2 — banco 1 sensor 4"),
    PID_18("18", "Sonda O2 — banco 2 sensor 1"),
    PID_19("19", "Sonda O2 — banco 2 sensor 2"),
    PID_1A("1A", "Sonda O2 — banco 2 sensor 3"),
    PID_1B("1B", "Sonda O2 — banco 2 sensor 4"),
    PID_1C("1C", "Padrão OBD do veículo"),
    PID_1D("1D", "Sondas lambda presentes (4 bancos)"),
    PID_1E("1E", "Status entrada auxiliar"),
    PID_1F("1F", "Tempo de funcionamento desde partida"),
    PID_20("20", "PIDs suportados 21–40"),
    PID_21("21", "Distância percorrida com MIL ativo"),
    PID_22("22", "Pressão do rail de combustível"),
    PID_23("23", "Pressão manométrica do rail"),
    PID_24("24", "Lambda / tensão O2 — banco 1 sensor 1"),
    PID_25("25", "Lambda / tensão O2 — banco 1 sensor 2"),
    PID_26("26", "Lambda / tensão O2 — banco 1 sensor 3"),
    PID_27("27", "Lambda / tensão O2 — banco 1 sensor 4"),
    PID_28("28", "Lambda / tensão O2 — banco 2 sensor 1"),
    PID_29("29", "Lambda / tensão O2 — banco 2 sensor 2"),
    PID_2A("2A", "Lambda / tensão O2 — banco 2 sensor 3"),
    PID_2B("2B", "Lambda / tensão O2 — banco 2 sensor 4"),
    PID_2C("2C", "EGR comandado"),
    PID_2D("2D", "Erro de EGR"),
    PID_2E("2E", "Purga evaporativa comandada"),
    PID_2F("2F", "Nível do tanque de combustível"),
    PID_30("30", "Ciclos de aquecimento desde limpeza de DTCs"),
    PID_31("31", "Distância percorrida desde limpeza de DTCs"),
    PID_32("32", "Pressão de vapor do sistema evaporativo"),
    PID_33("33", "Pressão barométrica absoluta"),
    PID_34("34", "Lambda / corrente O2 — banco 1 sensor 1"),
    PID_35("35", "Lambda / corrente O2 — banco 1 sensor 2"),
    PID_36("36", "Lambda / corrente O2 — banco 1 sensor 3"),
    PID_37("37", "Lambda / corrente O2 — banco 1 sensor 4"),
    PID_38("38", "Lambda / corrente O2 — banco 2 sensor 1"),
    PID_39("39", "Lambda / corrente O2 — banco 2 sensor 2"),
    PID_3A("3A", "Lambda / corrente O2 — banco 2 sensor 3"),
    PID_3B("3B", "Lambda / corrente O2 — banco 2 sensor 4"),
    PID_3C("3C", "Temperatura do catalisador — banco 1 sensor 1"),
    PID_3D("3D", "Temperatura do catalisador — banco 2 sensor 1"),
    PID_3E("3E", "Temperatura do catalisador — banco 1 sensor 2"),
    PID_3F("3F", "Temperatura do catalisador — banco 2 sensor 2"),
    PID_40("40", "PIDs suportados 41–60"),
    PID_41("41", "Status dos monitores neste ciclo de condução"),
    PID_42("42", "Tensão do módulo de controle"),
    PID_43("43", "Carga absoluta"),
    PID_44("44", "Relação ar/combustível comandada"),
    PID_45("45", "Posição relativa do acelerador"),
    PID_46("46", "Temperatura do ar ambiente"),
    PID_47("47", "Posição absoluta do acelerador B"),
    PID_48("48", "Posição absoluta do acelerador C"),
    PID_49("49", "Posição do pedal do acelerador D"),
    PID_4A("4A", "Posição do pedal do acelerador E"),
    PID_4B("4B", "Posição do pedal do acelerador F"),
    PID_4C("4C", "Atuador do acelerador comandado"),
    PID_4D("4D", "Tempo com MIL ativo"),
    PID_4E("4E", "Tempo desde limpeza de DTCs"),
    PID_51("51", "Tipo de combustível"),
    PID_52("52", "Percentual de etanol no combustível"),
    PID_59("59", "Pressão absoluta do rail de combustível"),
    PID_5A("5A", "Posição relativa do pedal do acelerador"),
    PID_5B("5B", "Carga restante da bateria híbrida"),
    PID_5C("5C", "Temperatura do óleo do motor"),
    PID_5D("5D", "Tempo de injeção de combustível"),
    PID_5E("5E", "Taxa de consumo de combustível");

    private final String codigo;
    private final String descricao;

    private static final Map<String, EPidDescription> lookup = new HashMap<>();

    static {
        for (EPidDescription p : values()) {
            lookup.put(p.codigo.toUpperCase(), p);
        }
    }

    EPidDescription(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo()    { return codigo; }
    public String getDescricao() { return descricao; }

    public static String getDescricao(String pid) {
        EPidDescription p = lookup.get(pid.toUpperCase());
        return p != null ? p.descricao : "Descrição não disponível";
    }
}

