import java.math.BigDecimal;

/**
 * Item de uma Distribuicao (composição). Só a própria Distribuicao o cria.
 */
public class ItemDistribuicao {
    private final BigDecimal quantidade;
    private final Lote lote;

    ItemDistribuicao(BigDecimal quantidade, Lote lote) {
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
