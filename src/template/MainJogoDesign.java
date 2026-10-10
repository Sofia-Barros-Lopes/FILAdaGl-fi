package template;

import br.com.davidbuzatto.jsge.sound.Music;
import br.com.davidbuzatto.jsge.sound.Sound;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gleyce Kelly e Sofia Barros
 */
public class MainJogoDesign extends javax.swing.JFrame {

    private Music musicaFundo;
    private Sound somClique;
    private Sound somAcerto;
    private Sound somErro;

    private GerenciadorFila gerenciadorFila;
    private int estagioPlanta = 0;

    private CardLayout cardLayout;
    private JPanel painelPrincipal;

    private JPanel painelMenu;
    private JPanel painelJogo;
    private JPanel painelSaibaMais;
    private JPanel painelFinal;

    private JPanel painelFilaVisual;
    private JLabel lblAbelha;
    private JPanel painelTopoAbelha;
    private JLabel lblIconeAcaoCentral;
    private JTextArea txtDialogoAbelha;
    private JPanel painelBotoesAcao;

    //acesso as imagens
    private Map<String, BufferedImage> imagens;

    public MainJogoDesign() {
        carregarRecursos();
        carregarSonsComBuscaInteligente();
        initComponentsCustomizados();
        configurarIconeJanela();
        iniciarJogo();
    }

    private void carregarRecursos() {
        imagens = new HashMap<>();
        String[] ficheiros = {
            "logoJogo.png", "paginaInicial.png", "fundo0.png", "fundo1.png",
            "fundo2.png", "fundo3.png", "fundo4.png", "fundo5.png", "fundoFinal.png",
            "abelhaEntrando.png", "abelhaAcerto.png", "abelhaErro.png", "abelhaFinal.png",
            "regadorIcon.png", "aduboIcon.png", "solIcon.png", "carinhoIcon.png", "polenIcon.png",
            "nectarIcon.png", "asasIcon.png", "enzimaIcon.png", "favoIcon.png", "ceraIcon.png", "saibaMaisPage.png"
        };

        for (String arq : ficheiros) {
            try {
                URL url = getClass().getResource("/img/" + arq);
                if (url != null) {
                    BufferedImage img = ImageIO.read(url);
                    String chave = arq.substring(0, arq.lastIndexOf('.'));
                    imagens.put(chave, img);
                }
            } catch (IOException e) {
                System.err.println("Erro ao carregar imagem: " + arq);
            }
        }
    }

    private void carregarSonsComBuscaInteligente() {
        String pathMusica = encontrarCaminhoAudio("musicaFundo.wav", "musics");
        String pathClique = encontrarCaminhoAudio("clique.wav", "sfx");
        String pathAcerto = encontrarCaminhoAudio("acerto.wav", "sfx");
        String pathErro = encontrarCaminhoAudio("erro.wav", "sfx");

        try {
            if (pathMusica != null) {
                musicaFundo = new Music(pathMusica);
                musicaFundo.play();
            }
            if (pathClique != null) {
                somClique = new Sound(pathClique);
            }
            if (pathAcerto != null) {
                somAcerto = new Sound(pathAcerto);
            }
            if (pathErro != null) {
                somErro = new Sound(pathErro);
            }
        } catch (Exception e) {
            System.err.println("Erro ao inicializar som JSGE: " + e.getMessage());
        }
    }

    private String encontrarCaminhoAudio(String nomeArquivo, String subpastaJsge) {
        String[] tentativas = {
            "resources/" + subpastaJsge + "/" + nomeArquivo,
            subpastaJsge + "/" + nomeArquivo,
            "audio/" + nomeArquivo,
            "src/audio/" + nomeArquivo,
            nomeArquivo
        };

        for (String t : tentativas) {
            if (new File(t).exists()) {
                return t;
            }
        }
        return null;
    }

    private void configurarIconeJanela() {
        BufferedImage logo = imagens.get("logoJogo");
        if (logo != null) {
            List<Image> icones = new ArrayList<>();
            icones.add(logo.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            icones.add(logo.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            icones.add(logo.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
            setIconImages(icones);
        }
    }

    private void initComponentsCustomizados() {
        setTitle("Jogo da Abelha - Estrutura de Dados FILA (FIFO)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 720));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        painelPrincipal = new JPanel(cardLayout);

        painelMenu = criarPainelMenu();
        painelJogo = criarPainelJogo();
        painelSaibaMais = criarPainelSaibaMais();
        painelFinal = criarPainelFinal();

        painelPrincipal.add(painelMenu, "MENU");
        painelPrincipal.add(painelJogo, "JOGO");
        painelPrincipal.add(painelSaibaMais, "SAIBA_MAIS");
        painelPrincipal.add(painelFinal, "TELA_FINAL");

        setContentPane(painelPrincipal);
    }

    private JPanel criarPainelMenu() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage bg = imagens.get("paginaInicial");
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.gridx = 0;

        JPanel espacador = new JPanel();
        espacador.setOpaque(false);
        espacador.setPreferredSize(new Dimension(10, 180));

        JButton btnJogar = criarBotaoEstilizado("JOGAR", new Color(255, 215, 0));
        btnJogar.setPreferredSize(new Dimension(240, 50));
        btnJogar.addActionListener(e -> {
            tocarSom(somClique);
            iniciarJogo();
            cardLayout.show(painelPrincipal, "JOGO");
        });

        JButton btnSaibaMais = criarBotaoEstilizado("SAIBA MAIS", new Color(245, 245, 220));
        btnSaibaMais.setPreferredSize(new Dimension(240, 50));
        btnSaibaMais.addActionListener(e -> {
            tocarSom(somClique);
            cardLayout.show(painelPrincipal, "SAIBA_MAIS");
        });

        gbc.gridy = 0;
        panel.add(espacador, gbc);
        gbc.gridy = 1;
        panel.add(btnJogar, gbc);
        gbc.gridy = 2;
        panel.add(btnSaibaMais, gbc);

        return panel;
    }

    private JPanel criarPainelJogo() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage bg = imagens.get("fundo" + estagioPlanta);
                if (bg == null) {
                    bg = imagens.get("fundo0");
                }
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        JPanel topoContainer = new JPanel(new BorderLayout());
        topoContainer.setOpaque(false);

        painelFilaVisual = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        painelFilaVisual.setOpaque(true);
        painelFilaVisual.setBackground(new Color(255, 250, 230, 235));
        painelFilaVisual.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 180, 100)));
        topoContainer.add(painelFilaVisual, BorderLayout.NORTH);

        painelTopoAbelha = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        painelTopoAbelha.setOpaque(false);

        // Abelha maior: 220x220px
        lblAbelha = new JLabel();
        lblAbelha.setPreferredSize(new Dimension(220, 220));

        txtDialogoAbelha = new JTextArea(4, 28);
        txtDialogoAbelha.setFont(new Font("SansSerif", Font.BOLD, 15));
        txtDialogoAbelha.setEditable(false);
        txtDialogoAbelha.setLineWrap(true);
        txtDialogoAbelha.setWrapStyleWord(true);
        txtDialogoAbelha.setBackground(new Color(255, 255, 255, 235));
        txtDialogoAbelha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 80, 0), 2),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        painelTopoAbelha.add(lblAbelha);
        painelTopoAbelha.add(txtDialogoAbelha);
        topoContainer.add(painelTopoAbelha, BorderLayout.CENTER);

        panel.add(topoContainer, BorderLayout.NORTH);

        JPanel painelCentroVazio = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        painelCentroVazio.setOpaque(false);

        lblIconeAcaoCentral = new JLabel();
        lblIconeAcaoCentral.setPreferredSize(new Dimension(300, 300));
        painelCentroVazio.add(lblIconeAcaoCentral);

        panel.add(painelCentroVazio, BorderLayout.CENTER);

        painelBotoesAcao = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 15));
        painelBotoesAcao.setOpaque(false);
        panel.add(painelBotoesAcao, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelSaibaMais() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage bg = imagens.get("saibaMaisPage");
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    setBackground(new Color(245, 240, 225));
                }
            }
        };

        JButton btnVoltar = criarBotaoEstilizado("VOLTAR", new Color(255, 215, 0));
        btnVoltar.setPreferredSize(new Dimension(180, 50));
        btnVoltar.addActionListener(e -> {
            tocarSom(somClique);
            cardLayout.show(painelPrincipal, "MENU");
        });

        JPanel painelSul = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelSul.setOpaque(false);
        painelSul.setBorder(BorderFactory.createEmptyBorder(10, 10, 25, 10));
        painelSul.add(btnVoltar);

        panel.add(painelSul, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelFinal() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage bg = imagens.get("fundoFinal");
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        JPanel painelBotoesFinal = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        painelBotoesFinal.setOpaque(false);
        painelBotoesFinal.setBorder(BorderFactory.createEmptyBorder(0, 10, 40, 10));

        JButton btnReiniciar = criarBotaoEstilizado("Jogar Novamente", new Color(144, 238, 144));
        btnReiniciar.setPreferredSize(new Dimension(200, 52));
        btnReiniciar.addActionListener(e -> {
            tocarSom(somClique);
            iniciarJogo();
            cardLayout.show(painelPrincipal, "JOGO");
        });

        JButton btnSaibaMais = criarBotaoEstilizado("Saiba Mais", new Color(255, 235, 150));
        btnSaibaMais.setPreferredSize(new Dimension(200, 52));
        btnSaibaMais.addActionListener(e -> {
            tocarSom(somClique);
            cardLayout.show(painelPrincipal, "SAIBA_MAIS");
        });

        JButton btnMenu = criarBotaoEstilizado("Menu Inicial", new Color(220, 220, 220));
        btnMenu.setPreferredSize(new Dimension(200, 52));
        btnMenu.addActionListener(e -> {
            tocarSom(somClique);
            cardLayout.show(painelPrincipal, "MENU");
        });

        painelBotoesFinal.add(btnReiniciar);
        painelBotoesFinal.add(btnSaibaMais);
        painelBotoesFinal.add(btnMenu);

        panel.add(painelBotoesFinal, BorderLayout.SOUTH);

        return panel;
    }

    private JButton criarBotaoEstilizado(String texto, Color corFundo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setBackground(corFundo);
        btn.setForeground(new Color(30, 30, 30));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corFundo.darker(), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return btn;
    }

    private Color getCorPorAcao(AcaoFila acao) {
        switch (acao) {
            case REGAR:
                return new Color(175, 220, 245);
            case ADUBAR:
                return new Color(225, 205, 175);
            case SOL:
                return new Color(255, 235, 150);
            case CARINHO:
                return new Color(255, 200, 220);
            case COLHER:
                return new Color(255, 215, 0);

            case DEPOSITAR_NECTAR:
                return new Color(255, 210, 120);
            case BATER_ASAS:
                return new Color(210, 235, 255);
            case ADICIONAR_ENZIMAS:
                return new Color(235, 200, 255);
            case PREENCHER_FAVO:
                return new Color(255, 190, 70);
            case SELAR_CERA:
                return new Color(245, 225, 140);
            default:
                return Color.LIGHT_GRAY;
        }
    }

    private String getChaveIconePorAcao(AcaoFila acao) {
        switch (acao) {
            case REGAR:
                return "regadorIcon";
            case ADUBAR:
                return "aduboIcon";
            case SOL:
                return "solIcon";
            case CARINHO:
                return "carinhoIcon";
            case COLHER:
                return "polenIcon";
            case DEPOSITAR_NECTAR:
                return "nectarIcon";
            case BATER_ASAS:
                return "asasIcon";
            case ADICIONAR_ENZIMAS:
                return "enzimaIcon";
            case PREENCHER_FAVO:
                return "favoIcon";
            case SELAR_CERA:
                return "ceraIcon";
            default:
                return "";
        }
    }

    private String getExplicacaoAcao(AcaoFila acao) {
        switch (acao) {
            case REGAR:
                return "A água hidrata a flor e ajuda na absorção dos nutrientes do solo!";
            case ADUBAR:
                return "O adubo fornece minerais essenciais para a planta crescer forte e saudável!";
            case SOL:
                return "A luz do sol permite a fotossíntese para gerar energia!";
            case CARINHO:
                return "O cuidado constante garante o bom desenvolvimento da flor!";
            case COLHER:
                return "Coletamos o néctar e o pólen da flor para levar até à colmeia!";
            case DEPOSITAR_NECTAR:
                return "Passo 1: As abelhas operárias depositam o néctar fresco colhido diretamente nos favos!";
            case BATER_ASAS:
                return "Passo 2: Batemos as asas rapidamente para desidratar o néctar e reduzir o excesso de água!";
            case ADICIONAR_ENZIMAS:
                return "Passo 3: Misturamos enzimas medicinais que transformam os açúcares do néctar em mel puro!";
            case PREENCHER_FAVO:
                return "Passo 4: O mel espesso e maduro é armazenado com precisão nas células sextavadas!";
            case SELAR_CERA:
                return "Passo 5: Fechamos o favo com uma fina camada de cera para conservar e proteger o mel!";
            default:
                return "Ação executada com sucesso!";
        }
    }

    private void iniciarJogo() {
        gerenciadorFila = new GerenciadorFila();
        gerenciadorFila.inicializarFilaFase1();
        estagioPlanta = 0;
        reconstruirBotoesAcao();
        atualizarInterfaceJogo("abelhaEntrando", "Observa a FILA no topo!\nClica na ação que está na frente da fila.");
        if (lblIconeAcaoCentral != null) {
            lblIconeAcaoCentral.setIcon(null);
        }
    }

    private void reconstruirBotoesAcao() {
        painelBotoesAcao.removeAll();

        int fase = gerenciadorFila.getFaseAtual();

        List<AcaoFila> acoesFase = new ArrayList<>();
        for (AcaoFila acao : AcaoFila.values()) {
            if (acao.getFase() == fase) {
                acoesFase.add(acao);
            }
        }

        Collections.shuffle(acoesFase);

        for (AcaoFila acao : acoesFase) {
            Color corBotao = getCorPorAcao(acao);
            JButton btn = criarBotaoEstilizado(acao.getNome(), corBotao);

            btn.setPreferredSize(new Dimension(210, 65));

            String chaveIcone = getChaveIconePorAcao(acao);
            BufferedImage imgIcone = imagens.get(chaveIcone);
            if (imgIcone != null) {
                btn.setIcon(new ImageIcon(imgIcone.getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
                btn.setIconTextGap(10);
            }

            btn.addActionListener(e -> executarJogada(acao));
            painelBotoesAcao.add(btn);
        }

        painelBotoesAcao.revalidate();
        painelBotoesAcao.repaint();
    }

    private void executarJogada(AcaoFila acao) {
        boolean acertou = gerenciadorFila.processarAcao(acao);

        if (acertou) {
            tocarSom(somAcerto);
            String explicacao = getExplicacaoAcao(acao);
            
            if (gerenciadorFila.getFaseAtual() == 2) {
                String chaveIcone = getChaveIconePorAcao(acao);
                BufferedImage imgIcone = imagens.get(chaveIcone);
                if (imgIcone != null) {
                    lblIconeAcaoCentral.setIcon(new ImageIcon(imgIcone.getScaledInstance(280, 280, Image.SCALE_SMOOTH)));
                }
            } else {
                lblIconeAcaoCentral.setIcon(null);
            }

            if (gerenciadorFila.estaVazia()) {
                if (gerenciadorFila.getFaseAtual() == 1) {
                    estagioPlanta = 5;
                    gerenciadorFila.inicializarFilaFase2();
                    reconstruirBotoesAcao();
                    atualizarInterfaceJogo("abelhaAcerto", "Flor cuidada!\n" + explicacao + "\n\nAgora vamos produzir o mel na colmeia!");
                } else {
                    cardLayout.show(painelPrincipal, "TELA_FINAL");
                }
            } else {
                if (gerenciadorFila.getFaseAtual() == 1) {
                    estagioPlanta = Math.min(estagioPlanta + 1, 4);
                }
                atualizarInterfaceJogo("abelhaAcerto", explicacao);
            }
        } else {
            tocarSom(somErro);
            AcaoFila esperada = gerenciadorFila.getProximaAcao();
            atualizarInterfaceJogo("abelhaErro", "Atenção ao FIFO!\nA ação correta no início da fila é: '" + esperada.getNome() + "'!");
        }
    }

    private void atualizarInterfaceJogo(String spriteAbelha, String mensagem) {
        txtDialogoAbelha.setText(mensagem);

        BufferedImage imgAbelha = imagens.get(spriteAbelha);
        if (imgAbelha != null) {
            int tamanhoAbelha;
            if (gerenciadorFila != null && gerenciadorFila.getFaseAtual() == 2) {
                painelTopoAbelha.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
                tamanhoAbelha = 270;
            } else {
                painelTopoAbelha.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
                tamanhoAbelha = 220;
            }

            if (imgAbelha != null) {
                lblAbelha.setIcon(new ImageIcon(imgAbelha.getScaledInstance(tamanhoAbelha, tamanhoAbelha, Image.SCALE_SMOOTH)));
            }
        }

        painelFilaVisual.removeAll();

        JLabel lblTituloFila = new JLabel("FILA (FIFO): ");
        lblTituloFila.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTituloFila.setForeground(new Color(100, 60, 0));
        painelFilaVisual.add(lblTituloFila);

        List<AcaoFila> acoes = new ArrayList<>(gerenciadorFila.getFilaAcoes());
        for (int i = 0; i < acoes.size(); i++) {
            AcaoFila a = acoes.get(i);

            JLabel itemLabel = new JLabel(a.getNome());
            itemLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            itemLabel.setOpaque(true);
            itemLabel.setBackground(getCorPorAcao(a));
            itemLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(getCorPorAcao(a).darker(), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));

            String chaveIcone = getChaveIconePorAcao(a);
            BufferedImage imgIcone = imagens.get(chaveIcone);
            if (imgIcone != null) {
                itemLabel.setIcon(new ImageIcon(imgIcone.getScaledInstance(34, 34, Image.SCALE_SMOOTH)));
                itemLabel.setIconTextGap(6);
            }

            painelFilaVisual.add(itemLabel);

            if (i < acoes.size() - 1) {
                JLabel seta = new JLabel("➔");
                seta.setFont(new Font("SansSerif", Font.BOLD, 18));
                seta.setForeground(new Color(120, 90, 30));
                painelFilaVisual.add(seta);
            }
        }

        if (acoes.isEmpty()) {
            JLabel lblVazia = new JLabel("[ FILA VAZIA ]");
            lblVazia.setFont(new Font("SansSerif", Font.BOLD, 16));
            lblVazia.setForeground(Color.GRAY);
            painelFilaVisual.add(lblVazia);
        }

        painelFilaVisual.revalidate();
        painelFilaVisual.repaint();
        painelJogo.repaint();
    }

    private void tocarSom(Sound som) {
        if (som != null) {
            try {
                som.play();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro LookAndFeel: " + ex.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> new MainJogoDesign().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
