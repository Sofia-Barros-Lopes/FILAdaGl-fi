# 🐝 Jogo da Abelha

Projeto desenvolvido em **Java (Swing)** focado na aplicação prática da estrutura de dados **Fila (Queue)**.

---

## 📌 Sobre o Projeto

O jogo utiliza conceitos de interface gráfica e lógica de programação para demonstrar o funcionamento de uma fila (conceito FIFO - *First In, First Out*).

### 👥 Autoria e Créditos
* **Alunas:** Sofia Barros e Gleyce Kelly
* **Disciplina / Orientação:** Prof. Dr. David Buzatto

---

## 🛠️ Estrutura do Código

A arquitetura do projeto está organizada no pacote `src/template/`:

* **`MainJogoDesign.java`**: Interface gráfica principal do jogo (Java Swing) e controle de interações com o usuário.
* **`GerenciadorFila.java`**: Implementação e controle da estrutura de dados Fila.
* **`AcaoFila.java`**: Classe que representa os elementos e ações manipuladas na fila.
* **`EstadoJogo.java`**: Gerenciamento do fluxo de estados, pontuação e transições de fases do jogo.
* **`GerenciadorAudio.java`**: Módulo responsável pela reprodução e controle da música de fundo em *loop*.

---

## 🎨 Recursos e Funcionalidades

* **Embaralhamento Dinâmico:** Botões de ação aleatorizados nas Fases 1 e 2 (`Collections.shuffle()`).
* **Efeitos Sonoros & Música:** Integração de áudio via Java Sound API (`javax.sound.sampled`).
* **Recursos Visuais:** Pasta `resources/` contendo imagens e o áudio de fundo (`musicaFundo.wav`).
