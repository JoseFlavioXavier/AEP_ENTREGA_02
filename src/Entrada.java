import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class Entrada {
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Entrada() {
    }

    public static BigDecimal decimal(String texto, String campo) {
        exigirPreenchido(texto, campo);
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Valor inválido no campo " + campo + ".");
        }
    }

    public static int inteiro(String texto, String campo) {
        exigirPreenchido(texto, campo);
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Valor inválido no campo " + campo + ".");
        }
    }

    public static LocalDate data(String texto, String campo) {
        exigirPreenchido(texto, campo);
        try {
            return LocalDate.parse(texto.trim(), FORMATO);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Data inválida no campo " + campo + ". Use dd/mm/aaaa.");
        }
    }

    public static String formatar(LocalDate data) {
        return data.format(FORMATO);
    }

    private static void exigirPreenchido(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("Preencha o campo " + campo + ".");
        }
    }
}
