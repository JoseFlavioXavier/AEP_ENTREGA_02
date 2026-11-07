import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Lote {
    private final UUID id;
    private final Alimento alimento;
    private final Doador origem;
    private final LocalDate validade;
    private BigDecimal quantidade;

    Lote(Alimento alimento, Doador origem, BigDecimal quantidade, LocalDate validade) {
        if (alimento == null || origem == null || validade == null) {
            throw new ValidacaoException("Lote exige alimento, doador de origem e validade.");
        }
        exigirPositiva(quantidade);
        this.id = UUID.randomUUID();
        this.alimento = alimento;
        this.origem = origem;
        this.validade = validade;
        this.quantidade = quantidade;
    }

    public UUID getId() {
        return id;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public Doador getOrigem() {
        return origem;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public boolean estaVencido(LocalDate data) {
        return validade.isBefore(data);
    }

    public void validarRetirada(BigDecimal valor, LocalDate data) {
        exigirPositiva(valor);
        if (estaVencido(data)) {
            throw new LoteVencidoException(alimento.getNome(), validade);
        }
        if (valor.compareTo(quantidade) > 0) {
            throw new EstoqueInsuficienteException(alimento.getNome(), quantidade, valor);
        }
    }

    public void retirar(BigDecimal valor, LocalDate data) {
        validarRetirada(valor, data);
        quantidade = quantidade.subtract(valor);
    }

    public void baixar(BigDecimal valor) {
        exigirPositiva(valor);
        if (valor.compareTo(quantidade) > 0) {
            throw new EstoqueInsuficienteException(alimento.getNome(), quantidade, valor);
        }
        quantidade = quantidade.subtract(valor);
    }

    static void exigirPositiva(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new ValidacaoException("A quantidade deve ser positiva.");
        }
    }

    @Override
    public String toString() {
        return alimento.getNome() + " | saldo " + quantidade + " " + alimento.getUnidade()
                + " | validade " + validade + " | origem " + origem.getNome();
    }
}
