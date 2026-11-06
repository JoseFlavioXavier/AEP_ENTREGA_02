import java.math.BigDecimal;

/**
 * Item de uma Doacao (composição). Só a própria Doacao o cria.
 */
public class ItemDoacao {
    private final BigDecimal quantidade;
    private final Lote lote;

    ItemDoacao(BigDecimal quantidade, Lote lote) {
        this.quantidade = quantidade;
        this.lote = lote;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public Lote getLote() {
        return lote;
    }
}
