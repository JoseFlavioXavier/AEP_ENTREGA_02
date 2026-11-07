import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InstituicaoServiceTest {
    private EstoqueService estoque;
    private InstituicaoService servico;

    @BeforeEach
    void preparar() {
        estoque = new EstoqueService();
        servico = new InstituicaoService(new InMemoryRepository<>(), estoque);
    }

    private Instituicao cadastrarPadrao() {
        return servico.cadastrar("Creche Sol Nascente", "98.765.432/0001-10", "(43) 3333-0000",
                "Rua das Flores, 100", 80);
    }

    @Test
    @DisplayName("Cadastro válido aparece na listagem")
    void cadastroValido() {
        Instituicao creche = cadastrarPadrao();
        assertEquals(1, servico.listar().size());
        assertEquals(80, servico.buscar(creche.getId()).getCapacidade());
    }

    @Test
    @DisplayName("Capacidade e endereço inválidos bloqueiam o cadastro")
    void dadosInvalidos() {
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("Creche", "111", "contato", "Rua A", 0));
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("Creche", "111", "contato", " ", 10));
        assertTrue(servico.listar().isEmpty());
    }

    @Test
    @DisplayName("Documento repetido é bloqueado")
    void documentoDuplicado() {
        cadastrarPadrao();
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("Outra Creche", "98765432000110", "contato", "Rua B", 20));
    }

    @Test
    @DisplayName("Atualização inválida preserva os dados anteriores")
    void atualizacaoInvalidaPreservaDados() {
        Instituicao creche = cadastrarPadrao();
        assertThrows(ValidacaoException.class,
                () -> servico.atualizar(creche.getId(), "Novo Nome", "98.765.432/0001-10",
                        "(43) 3333-0000", "Rua Nova, 1", -5));
        assertEquals("Creche Sol Nascente", creche.getNome());
        assertEquals("Rua das Flores, 100", creche.getEndereco());
        assertEquals(80, creche.getCapacidade());
    }

    @Test
    @DisplayName("Exclusão exige confirmação e remove quando confirmada")
    void exclusao() {
        Instituicao creche = cadastrarPadrao();
        assertThrows(ValidacaoException.class, () -> servico.excluir(creche.getId(), false));
        assertEquals(1, servico.listar().size());
        servico.excluir(creche.getId(), true);
        assertTrue(servico.listar().isEmpty());
    }

    @Test
    @DisplayName("Instituição com distribuição registrada não pode ser excluída")
    void instituicaoComDistribuicaoNaoEExcluida() {
        Instituicao creche = cadastrarPadrao();
        Doador doador = new Doador("Supermercado", "12.345.678/0001-99", "contato", TipoDoador.PESSOA_JURIDICA);
        Doacao doacao = new Doacao(doador);
        ItemDoacao item = doacao.adicionarItem(new Alimento("Arroz", Categoria.GRAOS, "kg"),
                new BigDecimal("10"), LocalDate.now().plusMonths(3));
        estoque.registrarDoacao(doacao);
        Distribuicao distribuicao = new Distribuicao(creche);
        distribuicao.adicionarItem(item.getLote(), new BigDecimal("4"));
        estoque.distribuir(distribuicao);
        assertThrows(ValidacaoException.class, () -> servico.excluir(creche.getId(), true));
        assertEquals(1, servico.listar().size());
    }
}
