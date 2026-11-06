import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Baixa de um lote por perda (dano, vencimento etc.), para os indicadores de perdas.
 */
public class Perda {
    private final UUID id;
    private final Lote lote;
    private final BigDecimal quantidade;
    private final String motivo;
    private final LocalDate data;
    private boolean processada;

    public Perda(Lote lote, BigDecimal quantidade, String motivo) {
        if (lote == null) {
            throw new ValidacaoException("A perda exige um lote.");
        }
        Lote.exigirPositiva(quantidade);
        if (motivo == null || motivo.isBlank()) {
            throw new ValidacaoException("O motivo da perda é obrigatório.");
        }
        this.id = UUID.randomUUID();
        this.lote = lote;
        this.quantidade = quantidade;
        this.motivo = motivo.trim();
        this.data = LocalDate.now();
    }

    public void processar() {
        if (processada) {
            throw new ValidacaoException("Esta perda já foi processada.");
        }
        lote.baixar(quantidade);
        processada = true;
    }

    public UUID getId() {
        return id;
    }

    public Lote getLote() {
        return lote;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDate getData() {
        return data;
    }

    public boolean isProcessada() {
        return processada;
    }
}
