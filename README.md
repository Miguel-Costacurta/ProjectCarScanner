# 🚗 Scanner Automotivo OBD2

Um scanner automotivo desenvolvido em Java + JavaFX com suporte para conexão via cabo OBD2 USB/Serial e dispositivos ELM327 Bluetooth.

O sistema permite realizar leitura de sensores em tempo real, escanear PIDs suportados pelo veículo, visualizar e apagar códigos de falha (DTC), além de cadastrar veículos para organização dos diagnósticos.

---

## ✨ Funcionalidades

- 🔌 Conexão via:
  - Cabo OBD2 Serial/USB
  - ELM327 Bluetooth

- 📡 Leitura de sensores em tempo real
- 🔍 Escaneamento automático de PIDs suportados pelo veículo
- 🎛️ Ativação/desativação de sensores exibidos
- 🚘 Cadastro de veículos
- ⚠️ Leitura de códigos de falha (DTC)
- 🧹 Limpeza/apagamento de DTCs
- 🖥️ Interface gráfica feita com JavaFX

---

## 🛠️ Tecnologias Utilizadas

- Java
- JavaFX
- Maven
- jSerialComm
- CSS (estilização da interface)
  
---  

## ▶️ Como Executar

Pré-requisitos:
Java JDK 17+
Maven 3.8+

Clone o projeto
git clone https://github.com/seu-usuario/seu-repositorio.git

Entre na pasta
cd seu-repositorio

Execute o projeto
mvn javafx:run

---
🚘 Como Utilizar
Conecte o adaptador OBD2 ao veículo
Conecte via:
Cabo USB/Serial
ELM327 Bluetooth
Abra o sistema
Selecione a porta de comunicação
Realize o scan de PIDs suportados
Escolha os sensores que deseja visualizar
Utilize as funções de leitura/apagamento de DTC quando necessário
📸 Interface

<img width="799" height="560" alt="image" src="https://github.com/user-attachments/assets/8a0a5de6-9b8c-4931-b4df-daa67c784043" />

<img width="898" height="598" alt="image" src="https://github.com/user-attachments/assets/db9b92d3-93cf-4eaf-bcfa-f28c72a32a1c" />

<img width="899" height="597" alt="image" src="https://github.com/user-attachments/assets/beadf3fa-fe53-49b3-ab7e-4bedc6b75cad" />

<img width="897" height="595" alt="image" src="https://github.com/user-attachments/assets/ed386b1b-389d-4bc5-983a-cb61b3576414" />

<img width="895" height="592" alt="image" src="https://github.com/user-attachments/assets/85f5a935-44c4-41d4-bd56-d0619a3d2c5b" />

<img width="895" height="560" alt="image" src="https://github.com/user-attachments/assets/2ece2650-f949-4637-b6c9-3c1e25927c0c" />

Exemplo:

## 📁 Estrutura do Projeto

```text
src/
 ├── main/
 │    ├── java/
 │    │     ├── obd/
 │    │     │     ├── connection/
 │    │     │     │     ├── HomoObdConnection.java
 │    │     │     │     ├── IObdConnection.java
 │    │     │     │     └── ObdConnection.java
 │    │     │     │
 │    │     │     ├── core/
 │    │     │     │     ├── dtcs/
 │    │     │     │     │     ├── DtcDescription.java
 │    │     │     │     │     └── DtcReader.java
 │    │     │     │     │
 │    │     │     │     ├── pids/
 │    │     │     │     │     ├── PidConverter.java
 │    │     │     │     │     ├── EPidDescription.java
 │    │     │     │     │     └── PidScanner.java
 │    │     │     │     │
 │    │     │     │     └── sensors/
 │    │     │     │           ├── ObdReader.java
 │    │     │     │           └── ActiveSensor.java
 │    │     │     │
 │    │     │     └── ui/
 │    │     │           ├── components/
 │    │     │           │     ├── GraficoPanel.java
 │    │     │           │     ├── Navbar.java
 │    │     │           │     ├── SensorCard.java
 │    │     │           │     ├── SensorListItem.java
 │    │     │           │     ├── StatusBar.java
 │    │     │           │     └── Topbar.java
 │    │     │           │
 │    │     │           ├── tabs/
 │    │     │           │     ├── ConfigTab.java
 │    │     │           │     ├── DtcTab.java
 │    │     │           │     ├── PidsTab.java
 │    │     │           │     ├── SensorsTab.java
 │    │     │           │     └── VeiculoTab.java
 │    │     │           │
 │    │     │           └── windows/
 │    │     │                 ├── ConnectWindow.java
 │    │     │                 └── MainWindow.java
 │    │     │
 │    │     └── Main.java
 │    │
 │    └── resources/
 │          └── style.css
```
🧩 Organização dos Módulos
🔌 connection/
Responsável pela comunicação com dispositivos OBD2 e ELM327.

- IObdConnection.java → Interface de comunicação
- ObdConnection.java → Implementação principal
- HomoObdConnection.java → Simulação/mock de conexão OBD
  
⚙️ core/
Contém toda a lógica principal do scanner.

⚠️ dtcs/
Manipulação de códigos de falha (DTC).

- DtcReader.java → Leitura/apagamento de falhas
- DtcDescription.java → Descrição dos códigos DTC
  
📡 pids/
Gerenciamento e interpretação de PIDs OBD2.

- PidScanner.java → Scanner de PIDs suportados
- PidConverter.java → Conversão de valores recebidos
- EPidDescription.java → Enum/descritivo de sensores
  
📈 sensors/
Leitura de sensores em tempo real.

- ObdReader.java
- ActiveSensor.java
  
🖥️ ui/
Interface gráfica desenvolvida com JavaFX.

🧱 components/
Componentes reutilizáveis da interface.

📑 tabs/
Abas principais do sistema.

- Configurações
- Sensores
- PIDs
- DTCs
- Veículos
  
🪟 windows/
Janelas principais da aplicação.

- Tela de conexão
- Janela principal do scanner

## ⚠️ Compatibilidade

Compatível com veículos que possuem suporte ao protocolo OBD2.
---
## License

This project is licensed under the PolyForm Noncommercial License 1.0.0.

Commercial use is prohibited without explicit permission from the author.
---
## 👨‍💻 Autor

Desenvolvido por Miguel Falcão Costacurta
