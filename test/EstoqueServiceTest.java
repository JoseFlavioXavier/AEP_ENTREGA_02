import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstoqueServiceTest {
    private EstoqueService estoque;
    private Doador doador;
    private Instituicao creche;
    private Alimento arroz;
    private Alimento feijao;

    @BeforeEach
    void preparar() {
        estoque = new EstoqueService();
        doador = new Doador("Supermercado Esperança", "12.345.678/0001-99", "(43) 99999-9999",
                TipoDoador.PESSOA_JURIDICA);
        creche = new Instituicao("Creche Sol Nascente", "98.765.432/0001-10", "(43) 3333-0000",
                "Rua das Flores, 100", 80);
        arroz = new Alimento("Arroz", Categoria.GRAOS, UnidadeMedida.KG);
        feijao = new Alimento("Feijão", Categoria.GRAOS, UnidadeMedida.KG);
    }

    private Lote registrarLote(Alimento alimento, String quantidade, LocalDate validade) {
        Doacao doacao = new Doacao(doador);
        ItemDoacao item = doacao.adicionarItem(alimento, new BigDecimal(quantidade), validade);
        estoque.registrarDoacao(doacao);
        return item.getLote();
    }

    private void assertSaldo(String esperado, Lote lote) {
        assertEquals(0, new BigDecimal(esperado).compareTo(lote.getQuantidade()),
                "saldo esperado " + esperado + " mas foi " + lote.getQuantidade());
    }

    @Test
    @DisplayName("Doação válida cria lote com origem, validade e saldo")
    void doacaoValida() {
        LocalDate validade = LocalDate.now().plusMonths(6);
        Lote lote = registrarLote(arroz, "100", validade);
        assertSame(doador, lote.getOrigem());
        assertSame(arroz, lote.getAlimento());
        assertEquals(validade, lote.getValidade());
        assertSaldo("100", lote);
        assertEquals(1, estoque.listarPorValidade().size());
    }

    @Test
    @DisplayName("Doação sem itens não pode ser registrada")
    void doacaoSemItens() {
        assertThrows(ValidacaoException.class, () -> estoque.registrarDoacao(new Doacao(doador)));
        assertTrue(estoque.listarPorValidade().isEmpty());
    }

    @Test
    @DisplayName("Quantidade zero, negativa ou nula é bloqueada na doação")
    void doacaoComQuantidadeInvalida() {
        Doacao doacao = new Doacao(doador);
        LocalDate validade = LocalDate.now().plusDays(30);
        assertThrows(ValidacaoException.class, () -> doacao.adicionarItem(arroz, BigDecimal.ZERO, validade));
        assertThrows(ValidacaoException.class, () -> doacao.adicionarItem(arroz, new BigDecimal("-1"), validade));
        assertThrows(ValidacaoException.class, () -> doacao.adicionarItem(arroz, null, validade));
        assertTrue(doacao.getItens().isEmpty());
    }

    @Test
    @DisplayName("Doação sem alimento ou sem validade é bloqueada")
    void doacaoSemAlimentoOuValidade() {
        Doacao doacao = new Doacao(doador);
        assertThrows(ValidacaoException.class,
                () -> doacao.adicionarItem(null, BigDecimal.TEN, LocalDate.now().plusDays(5)));
        assertThrows(ValidacaoException.class, () -> doacao.adicionarItem(arroz, BigDecimal.TEN, null));
    }

    @Test
    @DisplayName("Doação com validade anterior à data da doação é bloqueada")
    void doacaoComLoteVencido() {
        Doacao doacao = new Doacao(doador);
        assertThrows(LoteVencidoException.class,
                () -> doacao.adicionarItem(arroz, BigDecimal.TEN, LocalDate.now().minusDays(1)));
        assertTrue(doacao.getItens().isEmpty());
    }

    @Test
    @DisplayName("Doação não pode ser registrada duas vezes")
    void doacaoRegistradaDuasVezes() {
        Doacao doacao = new Doacao(doador);
        doacao.adicionarItem(arroz, BigDecimal.TEN, LocalDate.now().plusDays(30));
        estoque.registrarDoacao(doacao);
        assertThrows(ValidacaoException.class, () -> estoque.registrarDoacao(doacao));
        assertEquals(1, estoque.listarPorValidade().size());
    }

    @Test
    @DisplayName("Doação registrada não aceita novos itens")
    void doacaoRegistradaNaoAceitaItens() {
        Doacao doacao = new Doacao(doador);
        doacao.adicionarItem(arroz, BigDecimal.TEN, LocalDate.now().plusDays(30));
        estoque.registrarDoacao(doacao);
        assertThrows(ValidacaoException.class,
                () -> doacao.adicionarItem(feijao, BigDecimal.TEN, LocalDate.now().plusDays(30)));
    }

    @Test
    @DisplayName("Distribuição válida reduz o saldo do lote (10 - 4 = 6)")
    void distribuicaoValida() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        Distribuicao distribuicao = new Distribuicao(creche);
        distribuicao.adicionarItem(lote, new BigDecimal("4"));
        estoque.distribuir(distribuicao);
        assertSaldo("6", lote);
        assertTrue(distribuicao.isConcluida());
        assertEquals(1, estoque.listarDistribuicoes().size());
    }

    @Test
    @DisplayName("Estoque insuficiente bloqueia a saída e mantém o saldo")
    void estoqueInsuficiente() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        Distribuicao distribuicao = new Distribuicao(creche);
        distribuicao.adicionarItem(lote, new BigDecimal("11"));
        assertThrows(EstoqueInsuficienteException.class, () -> estoque.distribuir(distribuicao));
        assertSaldo("10", lote);
        assertFalse(distribuicao.isConcluida());
        assertTrue(estoque.listarDistribuicoes().isEmpty());
    }

    @Test
    @DisplayName("Lote vencido na data da saída bloqueia a distribuição")
    void loteVencido() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusDays(10));
        Distribuicao distribuicao = new Distribuicao(creche, LocalDateTime.now().plusDays(11));
        distribuicao.adicionarItem(lote, new BigDecimal("2"));
        assertThrows(LoteVencidoException.class, () -> estoque.distribuir(distribuicao));
        assertSaldo("10", lote);
    }

    @Test
    @DisplayName("Lote que vence no dia da saída ainda pode ser distribuído")
    void loteVenceHoje() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now());
        Distribuicao distribuicao = new Distribuicao(creche);
        distribuicao.adicionarItem(lote, new BigDecimal("3"));
        estoque.distribuir(distribuicao);
        assertSaldo("7", lote);
    }

    @Test
    @DisplayName("Quantidade zero ou negativa na distribuição é bloqueada")
    void distribuicaoComQuantidadeInvalida() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        Distribuicao distribuicao = new Distribuicao(creche);
        assertThrows(ValidacaoException.class, () -> distribuicao.adicionarItem(lote, BigDecimal.ZERO));
        assertThrows(ValidacaoException.class, () -> distribuicao.adicionarItem(lote, new BigDecimal("-2")));
        assertThrows(ValidacaoException.class, () -> distribuicao.adicionarItem(null, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Distribuição exige instituição e pelo menos um item")
    void distribuicaoSemDestinoOuItens() {
        assertThrows(ValidacaoException.class, () -> new Distribuicao(null));
        assertThrows(ValidacaoException.class, () -> estoque.distribuir(new Distribuicao(creche)));
    }

    @Test
    @DisplayName("Falha em um item não baixa nenhum item da distribuição")
    void distribuicaoEAtomica() {
        Lote arrozLote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        Lote feijaoLote = registrarLote(feijao, "5", LocalDate.now().plusMonths(2));
        Distribuicao distribuicao = new Distribuicao(creche);
        distribuicao.adicionarItem(arrozLote, new BigDecimal("4"));
        distribuicao.adicionarItem(feijaoLote, new BigDecimal("9"));
        assertThrows(EstoqueInsuficienteException.class, () -> estoque.distribuir(distribuicao));
        assertSaldo("10", arrozLote);
        assertSaldo("5", feijaoLote);
    }

    @Test
    @DisplayName("Itens do mesmo lote são somados para validar o saldo")
    void itensDoMesmoLoteSomam() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        Distribuicao distribuicao = new Distribuicao(creche);
        distribuicao.adicionarItem(lote, new BigDecimal("6"));
        distribuicao.adicionarItem(lote, new BigDecimal("6"));
        assertThrows(EstoqueInsuficienteException.class, () -> estoque.distribuir(distribuicao));
        assertSaldo("10", lote);
    }

    @Test
    @DisplayName("Distribuição concluída não recebe itens nem é concluída de novo")
    void distribuicaoConcluidaFicaFechada() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        Distribuicao distribuicao = new Distribuicao(creche);
        distribuicao.adicionarItem(lote, new BigDecimal("4"));
        estoque.distribuir(distribuicao);
        assertThrows(ValidacaoException.class, () -> distribuicao.adicionarItem(lote, BigDecimal.ONE));
        assertThrows(ValidacaoException.class, () -> estoque.distribuir(distribuicao));
        assertSaldo("6", lote);
    }

    @Test
    @DisplayName("Lote fora do estoque não pode ser distribuído")
    void loteForaDoEstoque() {
        Lote solto = new Lote(arroz, doador, BigDecimal.TEN, LocalDate.now().plusDays(20));
        Distribuicao distribuicao = new Distribuicao(creche);
        distribuicao.adicionarItem(solto, BigDecimal.ONE);
        assertThrows(ValidacaoException.class, () -> estoque.distribuir(distribuicao));
        assertSaldo("10", solto);
    }

    @Test
    @DisplayName("Perda válida reduz o saldo do lote")
    void perdaValida() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        estoque.registrarPerda(new Perda(lote, new BigDecimal("3"), "Embalagem danificada"));
        assertSaldo("7", lote);
    }

    @Test
    @DisplayName("Perda maior que o saldo ou sem motivo é bloqueada")
    void perdaInvalida() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        assertThrows(EstoqueInsuficienteException.class,
                () -> estoque.registrarPerda(new Perda(lote, new BigDecimal("11"), "Chuva")));
        assertThrows(ValidacaoException.class, () -> new Perda(lote, BigDecimal.ONE, " "));
        assertThrows(ValidacaoException.class, () -> new Perda(lote, BigDecimal.ZERO, "Motivo"));
        assertSaldo("10", lote);
    }

    @Test
    @DisplayName("Perda não pode ser processada duas vezes")
    void perdaProcessadaDuasVezes() {
        Lote lote = registrarLote(arroz, "10", LocalDate.now().plusMonths(2));
        Perda perda = new Perda(lote, new BigDecimal("3"), "Embalagem danificada");
        estoque.registrarPerda(perda);
        assertThrows(ValidacaoException.class, () -> estoque.registrarPerda(perda));
        assertSaldo("7", lote);
    }

    @Test
    @DisplayName("Listagem por validade começa pelo lote que vence primeiro")
    void ordemPorValidade() {
        Lote longe = registrarLote(arroz, "10", LocalDate.now().plusMonths(6));
        Lote perto = registrarLote(feijao, "10", LocalDate.now().plusDays(5));
        List<Lote> ordenados = estoque.listarPorValidade();
        assertSame(perto, ordenados.get(0));
        assertSame(longe, ordenados.get(1));
    }

    @Test
    @DisplayName("Lotes vencidos e esgotados não aparecem como disponíveis")
    void listarDisponiveis() {
        Doacao antiga = new Doacao(doador, LocalDateTime.now().minusDays(30));
        ItemDoacao vencido = antiga.adicionarItem(arroz, BigDecimal.TEN, LocalDate.now().minusDays(5));
        estoque.registrarDoacao(antiga);
        Lote esgotado = registrarLote(feijao, "5", LocalDate.now().plusDays(10));
        Distribuicao saida = new Distribuicao(creche);
        saida.adicionarItem(esgotado, new BigDecimal("5"));
        estoque.distribuir(saida);
        Lote bom = registrarLote(arroz, "8", LocalDate.now().plusDays(20));

        List<Lote> disponiveis = estoque.listarDisponiveis(LocalDate.now());
        assertEquals(1, disponiveis.size());
        assertSame(bom, disponiveis.get(0));
        assertTrue(vencido.getLote().estaVencido(LocalDate.now()));
    }
}
