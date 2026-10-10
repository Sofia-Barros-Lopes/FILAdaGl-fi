package template;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GerenciadorAudio {

    private static final float TAXA = 44100f;
    private static final AudioFormat FORMATO_SAIDA = new AudioFormat(TAXA, 16, 2, true, false);

    private static final int FRAMES_POR_BLOCO = 1024;
    private static final int BUFFER_LINHA_BYTES = 4096 * 4;
    private static final int MAX_EFEITOS_SIMULTANEOS = 16;

    private volatile float volumeMusica = 0.6f;
    private volatile float volumeEfeitos = 1.0f;

    private static class Som {
        final short[] amostras;
        Som(short[] amostras) { this.amostras = amostras; }
    }

    private static class Reproducao {
        final short[] amostras;
        int posicao = 0;
        Reproducao(short[] amostras) { this.amostras = amostras; }
    }

    private final Map<String, Som> cache = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Reproducao> pendentes = new ConcurrentLinkedQueue<>();
    private final List<Reproducao> ativos = new ArrayList<>(); // usada somente pela thread do mixer

    private volatile Som musica;
    private volatile String caminhoMusica;

    private volatile boolean rodando = false;
    private SourceDataLine linha;
    private Thread threadMixer;

    public GerenciadorAudio() {
        iniciarSaida();
    }

    public void precarregar(String... caminhos) {
        for (String caminho : caminhos) {
            carregar(caminho, false);
        }
    }

    public void tocarMusicaFundo(String caminho) {
        if (caminho.equals(caminhoMusica)) {
            return;
        }
        caminhoMusica = caminho;

        Thread t = new Thread(() -> {
            try {
                Som som = carregar(caminho, true);
                if (som == null) {
                    System.err.println("[Áudio] A música de fundo NÃO foi carregada (veja a mensagem acima).");
                    if (caminho.equals(caminhoMusica)) {
                        caminhoMusica = null;
                    }
                    return;
                }
                if (caminho.equals(caminhoMusica)) {
                    musica = som;
                    System.out.printf("[Áudio] Música de fundo pronta: %.1f s (%s)%n",
                            som.amostras.length / 2 / TAXA, caminho);
                    if (!rodando) {
                        System.err.println("[Áudio] ATENÇÃO: a saída de áudio não está aberta, então não haverá som.");
                    }
                }
            } catch (Throwable erro) {
                System.err.println("[Áudio] Falha ao carregar a música de fundo: " + erro);
                caminhoMusica = null;
            }
        }, "carregar-musica");
        t.setDaemon(true);
        t.start();
    }

    public void tocarEfeito(String caminho) {
        if (!rodando) {
            return;
        }
        Som som = cache.get(caminho);
        if (som != null) {
            pendentes.add(new Reproducao(som.amostras));
            return;
        }

        Thread t = new Thread(() -> {
            Som s = carregar(caminho, false);
            if (s != null) {
                pendentes.add(new Reproducao(s.amostras));
            }
        }, "carregar-efeito");
        t.setDaemon(true);
        t.start();
    }

    public void pararMusica() {
        musica = null;
        caminhoMusica = null;
    }

    public void setVolumeMusica(float volume) {
        this.volumeMusica = Math.max(0f, Math.min(1f, volume));
    }

    public void setVolumeEfeitos(float volume) {
        this.volumeEfeitos = Math.max(0f, Math.min(1f, volume));
    }

    public void encerrar() {
        rodando = false;
        if (threadMixer != null) {
            try {
                threadMixer.join(500);
            } catch (InterruptedException ignorado) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void iniciarSaida() {
        linha = abrirLinha();
        if (linha == null) {
            System.err.println("[Áudio] Nenhuma saída de áudio disponível: o jogo ficará sem som.");
            return;
        }
        linha.start();
        rodando = true;

        threadMixer = new Thread(this::loopMixer, "mixer-audio");
        threadMixer.setDaemon(true);
        threadMixer.setPriority(Thread.MAX_PRIORITY);
        threadMixer.start();
    }

    private SourceDataLine abrirLinha() {
        try {
            SourceDataLine l = AudioSystem.getSourceDataLine(FORMATO_SAIDA);
            l.open(FORMATO_SAIDA, BUFFER_LINHA_BYTES);
            System.out.println("[Áudio] Saída de áudio aberta (dispositivo padrão).");
            return l;
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            System.err.println("[Áudio] Dispositivo padrão indisponível: " + e.getMessage());
        }

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMATO_SAIDA);
        for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
            try {
                Mixer mixer = AudioSystem.getMixer(mi);
                if (!mixer.isLineSupported(info)) {
                    continue;
                }
                SourceDataLine l = (SourceDataLine) mixer.getLine(info);
                l.open(FORMATO_SAIDA, BUFFER_LINHA_BYTES);
                System.out.println("[Áudio] Saída de áudio aberta em: " + mi.getName());
                return l;
            } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) { }
        }
        return null;
    }

    private void loopMixer() {
        final int amostrasBloco = FRAMES_POR_BLOCO * 2;
        final int[] soma = new int[amostrasBloco];
        final byte[] saida = new byte[amostrasBloco * 2];

        Som musicaTocando = null;
        int posMusica = 0;
        boolean erroJaAvisado = false;

        try {
            while (rodando) {
                try {
                    Arrays.fill(soma, 0);

                    Som m = musica;
                    if (m != musicaTocando) { 
                        musicaTocando = m;
                        posMusica = 0;
                    }
                    if (m != null) {
                        short[] d = m.amostras;
                        int n = d.length;
                        float vol = volumeMusica;
                        int p = posMusica;
                        for (int i = 0; i < amostrasBloco; i++) {
                            soma[i] += (int) (d[p] * vol);
                            if (++p >= n) {
                                p = 0; //volta ao início
                            }
                        }
                        posMusica = p;
                    }

                    Reproducao nova;
                    while ((nova = pendentes.poll()) != null) {
                        if (ativos.size() < MAX_EFEITOS_SIMULTANEOS) {
                            ativos.add(nova);
                        }
                    }
                    float volE = volumeEfeitos;
                    for (Iterator<Reproducao> it = ativos.iterator(); it.hasNext(); ) {
                        Reproducao r = it.next();
                        short[] d = r.amostras;
                        int p = r.posicao;
                        int qtd = Math.min(d.length - p, amostrasBloco);
                        for (int i = 0; i < qtd; i++) {
                            soma[i] += (int) (d[p + i] * volE);
                        }
                        r.posicao += qtd;
                        if (r.posicao >= d.length) {
                            it.remove();
                        }
                    }

                    for (int i = 0; i < amostrasBloco; i++) {
                        int v = soma[i];
                        if (v > 32767) v = 32767;
                        else if (v < -32768) v = -32768;
                        saida[2 * i] = (byte) v;
                        saida[2 * i + 1] = (byte) (v >> 8);
                    }

                    linha.write(saida, 0, saida.length);
                } catch (RuntimeException erro) {
                    if (!erroJaAvisado) {
                        erroJaAvisado = true;
                        System.err.println("[Áudio] Erro no mixer (o áudio continua): " + erro);
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            linha.stop();
            linha.close();
        }
    }

    private Som carregar(String caminho, boolean ciclico) {
        Som existente = cache.get(caminho);
        if (existente != null) {
            return existente;
        }

        try (InputStream src = getClass().getResourceAsStream(caminho)) {
            if (src == null) {
                System.err.println("Arquivo de áudio não encontrado: " + caminho);
                return null;
            }

            try (AudioInputStream original = AudioSystem.getAudioInputStream(new BufferedInputStream(src))) {
                AudioFormat f = original.getFormat();
                int canais = f.getChannels();

                AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        f.getSampleRate(), 16, canais, canais * 2, f.getSampleRate(), false);

                try (AudioInputStream convertido = AudioSystem.getAudioInputStream(pcm, original)) {
                    byte[] bytes = convertido.readAllBytes();
                    short[] amostras = paraFormatoDeSaida(bytes, canais, f.getSampleRate(), ciclico);
                    if (amostras.length == 0) {
                        System.err.println("Áudio vazio: " + caminho);
                        return null;
                    }
                    Som som = new Som(amostras);
                    cache.put(caminho, som);
                    return som;
                }
            }
        } catch (UnsupportedAudioFileException | IOException | IllegalArgumentException e) {
            System.err.println("Erro ao carregar áudio '" + caminho + "': " + e.getMessage());
            return null;
        }
    }
    
    private static short[] paraFormatoDeSaida(byte[] b, int canais, float taxaOrigem, boolean ciclico) {
        int bytesPorFrame = canais * 2;
        int framesOrigem = b.length / bytesPorFrame;
        if (framesOrigem == 0) {
            return new short[0];
        }

        double passo = taxaOrigem / TAXA;
        int framesSaida = (int) (framesOrigem / passo);
        short[] saida = new short[framesSaida * 2];
        for (int i = 0; i < framesSaida; i++) {
            double pos = i * passo;
            int i0 = (int) pos;
            int i1 = ciclico ? (i0 + 1) % framesOrigem : Math.min(i0 + 1, framesOrigem - 1);
            double frac = pos - i0;

            int base0 = i0 * bytesPorFrame;
            int base1 = i1 * bytesPorFrame;
            int e0 = ler16(b, base0);
            int e1 = ler16(b, base1);
            int d0 = canais > 1 ? ler16(b, base0 + 2) : e0;
            int d1 = canais > 1 ? ler16(b, base1 + 2) : e1;

            saida[2 * i] = (short) Math.round(e0 + (e1 - e0) * frac);
            saida[2 * i + 1] = (short) Math.round(d0 + (d1 - d0) * frac);
        }
        return saida;
    }

    private static int ler16(byte[] b, int pos) {
        return (short) ((b[pos] & 0xFF) | (b[pos + 1] << 8));
    }
}