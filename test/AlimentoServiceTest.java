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
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, "kg");
        assertEquals(1, servico.listar().size());
        assertEquals("kg", servico.buscar(arroz.getId()).getUnidade());
    }

    @Test
    @DisplayName("Nome, categoria e unidade são obrigatórios")
    void camposObrigatorios() {
        assertThrows(ValidacaoException.class, () -> servico.cadastrar("", Categoria.GRAOS, "kg"));
        assertThrows(ValidacaoException.class, () -> servico.cadastrar("Arroz", null, "kg"));
        assertThrows(ValidacaoException.class, () -> servico.cadastrar("Arroz", Categoria.GRAOS, " "));
        assertTrue(servico.listar().isEmpty());
    }

    @Test
    @DisplayName("Nome repetido é bloqueado sem diferenciar maiúsculas")
    void nomeDuplicado() {
        servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, "kg");
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("arroz tipo 1", Categoria.GRAOS, "kg"));
        assertEquals(1, servico.listar().size());
    }

    @Test
    @DisplayName("Atualizar mantendo o próprio nome é permitido")
    void atualizarMantendoNome() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, "kg");
        servico.atualizar(arroz.getId(), "Arroz Tipo 1", Categoria.GRAOS, "saco");
        assertEquals("saco", arroz.getUnidade());
    }

    @Test
    @DisplayName("Atualização inválida preserva os dados anteriores")
    void atualizacaoInvalidaPreservaDados() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, "kg");
        assertThrows(ValidacaoException.class,
                () -> servico.atualizar(arroz.getId(), "Arroz Integral", null, "kg"));
        assertEquals("Arroz Tipo 1", arroz.getNome());
        assertEquals(Categoria.GRAOS, arroz.getCategoria());
    }

    @Test
    @DisplayName("Exclusão exige confirmação e remove quando confirmada")
    void exclusao() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, "kg");
        assertThrows(ValidacaoException.class, () -> servico.excluir(arroz.getId(), false));
        servico.excluir(arroz.getId(), true);
        assertTrue(servico.listar().isEmpty());
    }

    @Test
    @DisplayName("Alimento com lote no estoque não pode ser excluído")
    void alimentoComLoteNaoEExcluido() {
        Alimento arroz = servico.cadastrar("Arroz Tipo 1", Categoria.GRAOS, "kg");
        Doador doador = new Doador("Supermercado", "12.345.678/0001-99", "contato", TipoDoador.PESSOA_JURIDICA);
        Doacao doacao = new Doacao(doador);
        doacao.adicionarItem(arroz, new BigDecimal("10"), LocalDate.now().plusMonths(3));
        estoque.registrarDoacao(doacao);
        assertThrows(ValidacaoException.class, () -> servico.excluir(arroz.getId(), true));
        assertEquals(1, servico.listar().size());
    }
}
