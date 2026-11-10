import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlimentoServiceTest {
    private EstoqueService estoque;
    private AlimentoService servico;

    @BeforeEach
    void preparar() {
        estoque = new EstoqueService();
        servico = new AlimentoService(new InMemoryRepository<>(), estoque);
    }

    @Test
    @DisplayName("Cadastro válido aparece na listagem")
    void cadastroValido() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        assertEquals(1, servico.listar().size());
        assertEquals(UnidadeMedida.KG, servico.buscar(arroz.getId()).getUnidade());
    }

    @Test
    @DisplayName("Nome, categoria e unidade são obrigatórios")
    void camposObrigatorios() {
        assertThrows(ValidacaoException.class, () -> servico.cadastrar("", Categoria.GRAOS, UnidadeMedida.KG));
        assertThrows(ValidacaoException.class, () -> servico.cadastrar("Arroz", null, UnidadeMedida.KG));
        assertThrows(ValidacaoException.class, () -> servico.cadastrar("Arroz", Categoria.GRAOS, null));
        assertTrue(servico.listar().isEmpty());
    }

    @Test
    @DisplayName("Nome repetido é bloqueado sem diferenciar maiúsculas")
    void nomeDuplicado() {
        servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("arroz tipo 1", Categoria.GRAOS, UnidadeMedida.KG));
        assertEquals(1, servico.listar().size());
    }

    @Test
    @DisplayName("Atualizar mantendo o próprio nome é permitido")
    void atualizarMantendoNome() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        servico.atualizar(arroz.getId(), "Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.UN);
        assertEquals(UnidadeMedida.UN, arroz.getUnidade());
    }

    @Test
    @DisplayName("Atualização inválida preserva os dados anteriores")
    void atualizacaoInvalidaPreservaDados() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        assertThrows(ValidacaoException.class,
                () -> servico.atualizar(arroz.getId(), "Arroz Integral", null, UnidadeMedida.KG));
        assertEquals("Arroz Tipo 1", arroz.getNome());
        assertEquals(Categoria.GRAOS, arroz.getCategoria());
    }

    @Test
    @DisplayName("Exclusão exige confirmação e remove quando confirmada")
    void exclusao() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        assertThrows(ValidacaoException.class, () -> servico.excluir(arroz.getId(), false));
        servico.excluir(arroz.getId(), true);
        assertTrue(servico.listar().isEmpty());
    }

    @Test
    @DisplayName("Alimento com lote no estoque não pode mudar de unidade de medida")
    void alimentoComLoteNaoMudaDeUnidade() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        servico.atualizar(arroz.getId(), "Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.G);
        assertEquals(UnidadeMedida.G, arroz.getUnidade());
        servico.atualizar(arroz.getId(), "Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        Doador doador = new Doador("Supermercado", "12.345.678/0001-99", "contato", TipoDoador.PESSOA_JURIDICA);
        Doacao doacao = new Doacao(doador);
        doacao.adicionarItem(arroz, new BigDecimal("10"), LocalDate.now().plusMonths(3));
        estoque.registrarDoacao(doacao);
        assertThrows(ValidacaoException.class,
                () -> servico.atualizar(arroz.getId(), "Arroz Integral", Categoria.GRAOS, UnidadeMedida.G));
        assertEquals(UnidadeMedida.KG, arroz.getUnidade());
        assertEquals("Arroz Tipo 1", arroz.getNome());
        servico.atualizar(arroz.getId(), "Arroz Integral", Categoria.GRAOS, UnidadeMedida.KG);
        assertEquals("Arroz Integral", arroz.getNome());
    }

    @Test
    @DisplayName("Alimento com lote no estoque não pode ser excluído")
    void alimentoComLoteNaoEExcluido() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, UnidadeMedida.KG);
        Doador doador = new Doador("Supermercado", "12.345.678/0001-99", "contato", TipoDoador.PESSOA_JURIDICA);
        Doacao doacao = new Doacao(doador);
        doacao.adicionarItem(arroz, new BigDecimal("10"), LocalDate.now().plusMonths(3));
        estoque.registrarDoacao(doacao);
        assertThrows(ValidacaoException.class, () -> servico.excluir(arroz.getId(), true));
        assertEquals(1, servico.listar().size());
    }
}
