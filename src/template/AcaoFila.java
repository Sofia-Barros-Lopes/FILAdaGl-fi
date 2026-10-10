package template;

public enum AcaoFila {
    // Ações da Fase 1 (Planta)
    REGAR("Regar Planta", 1),
    ADUBAR("Adubar Solo", 1),
    SOL("Dar Sol", 1),
    CARINHO("Cantar / Carinho", 1),
    COLHER("Colher Pólen", 1),

    // Ações da Fase 2 (Produção de Mel)
    DEPOSITAR_NECTAR("Depositar Néctar", 2),
    BATER_ASAS("Bater Asas", 2),
    ADICIONAR_ENZIMAS("Adicionar Enzimas", 2),
    PREENCHER_FAVO("Preencher Favo", 2),
    SELAR_CERA("Selar com Cera", 2);

    private final String nome;
    private final int fase;

    AcaoFila(String nome, int fase) {
        this.nome = nome;
        this.fase = fase;
    }

    public String getNome() { return nome; }
    public int getFase() { return fase; }
}