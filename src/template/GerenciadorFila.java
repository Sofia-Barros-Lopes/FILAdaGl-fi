package template;

import java.util.LinkedList;
import java.util.Queue;

public class GerenciadorFila {

    private Queue<AcaoFila> filaAcoes;
    private int faseAtual = 1;

    public GerenciadorFila() {
        filaAcoes = new LinkedList<>();
    }

    public void inicializarFilaFase1() {
        faseAtual = 1;
        filaAcoes.clear();
        filaAcoes.add(AcaoFila.REGAR);
        filaAcoes.add(AcaoFila.ADUBAR);
        filaAcoes.add(AcaoFila.SOL);
        filaAcoes.add(AcaoFila.CARINHO);
        filaAcoes.add(AcaoFila.COLHER);
    }

    public void inicializarFilaFase2() {
        faseAtual = 2;
        filaAcoes.clear();
        filaAcoes.add(AcaoFila.DEPOSITAR_NECTAR);
        filaAcoes.add(AcaoFila.BATER_ASAS);
        filaAcoes.add(AcaoFila.ADICIONAR_ENZIMAS);
        filaAcoes.add(AcaoFila.PREENCHER_FAVO);
        filaAcoes.add(AcaoFila.SELAR_CERA);
    }

    public boolean processarAcao(AcaoFila acaoClicada) {
        if (!filaAcoes.isEmpty() && filaAcoes.peek() == acaoClicada) {
            filaAcoes.poll(); // Remove da frente da fila (FIFO)
            return true;
        }
        return false;
    }

    public Queue<AcaoFila> getFilaAcoes() { return filaAcoes; }
    public AcaoFila getProximaAcao() { return filaAcoes.peek(); }
    public boolean estaVazia() { return filaAcoes.isEmpty(); }
    public int getFaseAtual() { return faseAtual; }
    
    public void inicializarFila() {
        inicializarFilaFase1();
    }
}