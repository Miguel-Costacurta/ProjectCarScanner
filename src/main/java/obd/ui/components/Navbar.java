package obd.ui.components;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import obd.ui.tabs.*;

import java.util.function.Consumer;

public class Navbar {
    private final Consumer<VBox> onAbaChanged;
    private final SensorsTab sensorsTab;
    private final DtcTab dtcTab;
    private final PidsTab pidsTab;
    private final VeiculoTab veiculoTab;
    private final ConfigTab configTab;
    private Label navBtnAtivo;

    public Navbar(Consumer<VBox> onAbaChanged, SensorsTab sensorsTab,
                  DtcTab dtcTab, PidsTab pidsTab,
                  VeiculoTab veiculoTab, ConfigTab configTab){
        this.onAbaChanged  = onAbaChanged;
        this.sensorsTab    = sensorsTab;
        this.dtcTab        = dtcTab;
        this.pidsTab       = pidsTab;
        this.veiculoTab    = veiculoTab;
        this.configTab     = configTab;
    }
    // ── navbar: botões de abas ────────────────────────────────
    public HBox buildNavbar() {
        HBox navbar = new HBox();
        navbar.setStyle(
                "-fx-background-color: #0f0f0f;" +
                        "-fx-border-color: transparent transparent #1a1a1a transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Label btnSensores = buildNavBtn("SENSORES");
        Label btnDtc      = buildNavBtn("DTC");
        Label btnPids     = buildNavBtn("PIDs ATIVOS");
        Label btnVeiculo  = buildNavBtn("VEÍCULO");
        Label btnConfig   = buildNavBtn("CONFIGURAÇÕES");

        // ação de cada botão
        btnSensores.setOnMouseClicked(e -> mostrarAba(sensorsTab.build(), btnSensores));
        btnDtc.setOnMouseClicked(     e -> mostrarAba(dtcTab.build(),      btnDtc));
        btnPids.setOnMouseClicked(    e -> mostrarAba(pidsTab.build(),      btnPids));
        btnVeiculo.setOnMouseClicked( e -> mostrarAba(veiculoTab.build(),   btnVeiculo));
        btnConfig.setOnMouseClicked(  e -> mostrarAba(configTab.build(),    btnConfig));

        navbar.getChildren().addAll(btnSensores, btnDtc, btnPids, btnVeiculo, btnConfig);

        // abre aba de sensores por padrão
        ativarNavBtn(btnSensores);

        return navbar;
    }

    // ── factory de botão de navegação ────────────────────────
    private Label buildNavBtn(String texto) {
        Label btn = new Label(texto);
        btn.getStyleClass().add("nav-btn");
        btn.setPadding(new Insets(10, 14, 10, 14));
        btn.setCursor(Cursor.HAND);
        return btn;
    }

    // ── troca a aba visível ───────────────────────────────────
    private void mostrarAba(VBox aba, Label navBtn) {
        onAbaChanged.accept(aba);

        // desativa todos os botões de nav
        if (navBtnAtivo != null) {
            navBtnAtivo.getStyleClass().remove("nav-btn-active");
        }
        ativarNavBtn(navBtn);
    }

    private void ativarNavBtn(Label navBtn) {
        navBtn.getStyleClass().add("nav-btn-active");
        navBtnAtivo = navBtn;
    }
}
