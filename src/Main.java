import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("        SISTEMA BANCO DE ALIMENTOS");
        System.out.println("======================================\n");

        EstoqueService estoque = new EstoqueService();

        Doador doador = new Doador("Supermercado Esperança", "12.345.678/0001-99",
                "(43) 99999-9999", TipoDoador.PESSOA_JURIDICA);
        Instituicao creche = new Instituicao("Creche Sol Nascente", "98.765.432/0001-10",
                "(43) 3333-0000", "Rua das Flores, 100", 80);

        System.out.println("--- Polimorfismo: descricao() ---");
        List<Pessoa> pessoas = List.of(doador, creche);
        for (Pessoa p : pessoas) {
            System.out.println(p.descricao());
        }

        System.out.println("\n--- UC02: registrar doação ---");
        Alimento arroz = new Alimento("Arroz Tipo 1", Categoria.GRAOS, "kg");
        Doacao doacao = new Doacao(doador);
        ItemDoacao item = doacao.adicionarItem(arroz, new BigDecimal("100"), LocalDate.now().plusMonths(6));
        estoque.registrarDoacao(doacao);
        Lote lote = item.getLote();
        System.out.println("Lote criado: " + lote);

        System.out.println("\n--- UC04: distribuir 30 kg ---");
        Distribuicao d1 = new Distribuicao(creche);
        d1.adicionarItem(lote, new BigDecimal("30"));
        estoque.distribuir(d1);
        System.out.println("Saldo: " + lote.getQuantidade() + " kg");

        System.out.println("\n--- Perda de 5 kg (embalagem danificada) ---");
        estoque.registrarPerda(new Perda(lote, new BigDecimal("5"), "Embalagem danificada"));
        System.out.println("Saldo: " + lote.getQuantidade() + " kg");

        System.out.println("\n--- Erro esperado: estoque insuficiente (80 kg) ---");
        try {
            Distribuicao d2 = new Distribuicao(creche);
            d2.adicionarItem(lote, new BigDecimal("80"));
            estoque.distribuir(d2);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }
        System.out.println("Saldo preservado: " + lote.getQuantidade() + " kg");

        System.out.println("\n--- Erro esperado: lote vencido ---");
        try {
            Distribuicao d3 = new Distribuicao(creche, LocalDateTime.now().plusYears(1));
            d3.adicionarItem(lote, new BigDecimal("10"));
            estoque.distribuir(d3);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }

        System.out.println("\n--- Erro esperado: doação com quantidade inválida ---");
        try {
            new Doacao(doador).adicionarItem(arroz, BigDecimal.ZERO, LocalDate.now().plusDays(10));
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }

        System.out.println("\n--- UC03: estoque por validade ---");
        for (Lote l : estoque.listarPorValidade()) {
            System.out.println(l);
        }
        System.out.println("\n======================================");
    }
}
