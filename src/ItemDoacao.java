import java.math.BigDecimal;

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
