import java.time.LocalDate;

public class LoteVencidoException extends ValidacaoException {
    private static final long serialVersionUID = 1L;

    public LoteVencidoException(String alimento, LocalDate validade) {
        super("Lote vencido: " + alimento + " (validade " + validade + ").");
    }
}
