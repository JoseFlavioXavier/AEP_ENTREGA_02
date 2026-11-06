import java.time.LocalDate;

/**
 * Lançada quando se tenta receber ou retirar alimento de um lote vencido (RN03).
 */
public class LoteVencidoException extends ValidacaoException {
    private static final long serialVersionUID = 1L;

    public LoteVencidoException(String alimento, LocalDate validade) {
        super("Lote vencido: " + alimento + " (validade " + validade + ").");
    }
}
