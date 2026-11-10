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
        DoadorService doadores = new DoadorService(new InMemoryRepository<>(), estoque);
        InstituicaoService instituicoes = new InstituicaoService(new InMemoryRepository<>(), estoque);
        AlimentoService alimentos = new AlimentoService(new InMemoryRepository<>(), estoque);

        System.out.println("--- CRUD de doadores ---");
        Doador doador = doadores.cadastrar("Supermercado Esperança", "12.345.678/0001-99",
                "(43) 99999-9999", TipoDoador.PESSOA_JURIDICA);
        Doador padaria = doadores.cadastrar("Padaria Pão Quente", "11.222.333/0001-44",
                "(43) 98888-8888", TipoDoador.PESSOA_JURIDICA);
        System.out.println("Cadastrados: " + doadores.listar().size());

        try {
            doadores.cadastrar("Outro Mercado", "12345678000199", "(43) 97777-7777", TipoDoador.PESSOA_JURIDICA);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }

        try {
            doadores.atualizar(padaria.getId(), "Padaria Nova", "", "(43) 98888-8888", TipoDoador.PESSOA_JURIDICA);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }
        System.out.println("Dados preservados: " + padaria.getNome());

        doadores.atualizar(padaria.getId(), "Padaria Pão Quente Ltda", "11.222.333/0001-44",
                "(43) 98888-8888", TipoDoador.PESSOA_JURIDICA);
        System.out.println("Atualizado: " + doadores.buscar(padaria.getId()).getNome());

        try {
            doadores.excluir(padaria.getId(), false);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }
        doadores.excluir(padaria.getId(), true);
        System.out.println("Após exclusão: " + doadores.listar().size() + " doador(es)\n");

        System.out.println("--- CRUD de instituições ---");
        Instituicao creche = instituicoes.cadastrar("Creche Sol Nascente", "98.765.432/0001-10",
                "(43) 3333-0000", "Rua das Flores, 100", 80);
        try {
            instituicoes.atualizar(creche.getId(), "Creche Sol Nascente", "98.765.432/0001-10",
                    "(43) 3333-0000", "Rua das Flores, 100", 0);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }
        instituicoes.atualizar(creche.getId(), "Creche Sol Nascente", "98.765.432/0001-10",
                "(43) 3333-0000", "Rua das Flores, 100", 90);
        System.out.println("Capacidade atualizada: " + instituicoes.buscar(creche.getId()).getCapacidade() + "\n");

        System.out.println("--- CRUD de alimentos ---");
        Alimento arroz = alimentos.cadastrar("Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        try {
            alimentos.cadastrar("arroz tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }
        System.out.println("Cadastrados: " + alimentos.listar().size() + "\n");

        System.out.println("--- Polimorfismo: descricao() ---");
        List<Pessoa> pessoas = List.of(doador, creche);
        for (Pessoa p : pessoas) {
            System.out.println(p.descricao());
        }

        System.out.println("\n--- UC02: registrar doação ---");
        Doacao doacao = new Doacao(doador);
        ItemDoacao item = doacao.adicionarItem(arroz, new BigDecimal("100"), LocalDate.now().plusMonths(6));
        estoque.registrarDoacao(doacao);
        Lote lote = item.getLote();
        System.out.println("Lote criado: " + lote);

        System.out.println("\n--- Doador com lote não pode ser excluído ---");
        try {
            doadores.excluir(doador.getId(), true);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }

        System.out.println("\n--- Alimento com lote não pode ser excluído ---");
        try {
            alimentos.excluir(arroz.getId(), true);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }

        System.out.println("\n--- UC04: distribuir 30 kg ---");
        Distribuicao d1 = new Distribuicao(creche);
        d1.adicionarItem(lote, new BigDecimal("30"));
        estoque.distribuir(d1);
        System.out.println("Saldo: " + lote.getQuantidade() + " kg");

        System.out.println("\n--- Instituição com distribuição não pode ser excluída ---");
        try {
            instituicoes.excluir(creche.getId(), true);
        } catch (ValidacaoException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }

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
