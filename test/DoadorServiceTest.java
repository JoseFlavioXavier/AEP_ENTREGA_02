import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DoadorServiceTest {
    private EstoqueService estoque;
    private DoadorService servico;

    @BeforeEach
    void preparar() {
        estoque = new EstoqueService();
        servico = new DoadorService(new InMemoryRepository<>(), estoque);
    }

    private Doador cadastrarPadrao() {
        return servico.cadastrar("Supermercado Esperança", "12.345.678/0001-99",
                "(43) 99999-9999", TipoDoador.PESSOA_JURIDICA);
    }

    @Test
    @DisplayName("Cadastro válido aparece na listagem")
    void cadastroValido() {
        Doador doador = cadastrarPadrao();
        assertEquals(1, servico.listar().size());
        assertEquals("Supermercado Esperança", servico.buscar(doador.getId()).getNome());
    }

    @Test
    @DisplayName("Nome, documento, contato e tipo são obrigatórios")
    void camposObrigatorios() {
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar(" ", "123", "contato", TipoDoador.PESSOA_FISICA));
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("Nome", "", "contato", TipoDoador.PESSOA_FISICA));
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("Nome", "123", null, TipoDoador.PESSOA_FISICA));
        assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("Nome", "123", "contato", null));
        assertTrue(servico.listar().isEmpty());
    }

    @Test
    @DisplayName("Documento repetido é bloqueado mesmo com pontuação diferente")
    void documentoDuplicado() {
        cadastrarPadrao();
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> servico.cadastrar("Outro Mercado", "12345678000199", "(43) 97777-7777",
                        TipoDoador.PESSOA_JURIDICA));
        assertEquals("Já existe um doador com este documento.", erro.getMessage());
        assertEquals(1, servico.listar().size());
    }

    @Test
    @DisplayName("Atualização válida altera os dados e mantém o identificador")
    void atualizacaoValida() {
        Doador doador = cadastrarPadrao();
        UUID id = doador.getId();
        servico.atualizar(id, "Supermercado Nova Esperança", "12.345.678/0001-99",
                "(43) 90000-0000", TipoDoador.PESSOA_JURIDICA);
        assertEquals("Supermercado Nova Esperança", servico.buscar(id).getNome());
        assertEquals(id, servico.buscar(id).getId());
    }

    @Test
    @DisplayName("Atualização inválida preserva os dados anteriores")
    void atualizacaoInvalidaPreservaDados() {
        Doador doador = cadastrarPadrao();
        assertThrows(ValidacaoException.class,
                () -> servico.atualizar(doador.getId(), "Novo Nome", "", "(43) 90000-0000",
                        TipoDoador.PESSOA_JURIDICA));
        assertEquals("Supermercado Esperança", doador.getNome());
        assertEquals("(43) 99999-9999", doador.getContato());
    }

    @Test
    @DisplayName("Atualizar para o documento de outro doador é bloqueado")
    void atualizarParaDocumentoDeOutro() {
        cadastrarPadrao();
        Doador segundo = servico.cadastrar("Padaria Pão Quente", "11.222.333/0001-44",
                "(43) 98888-8888", TipoDoador.PESSOA_JURIDICA);
        assertThrows(ValidacaoException.class,
                () -> servico.atualizar(segundo.getId(), "Padaria Pão Quente", "12.345.678/0001-99",
                        "(43) 98888-8888", TipoDoador.PESSOA_JURIDICA));
        assertEquals("11.222.333/0001-44", segundo.getDocumento());
    }

    @Test
    @DisplayName("Exclusão exige confirmação")
    void exclusaoExigeConfirmacao() {
        Doador doador = cadastrarPadrao();
        assertThrows(ValidacaoException.class, () -> servico.excluir(doador.getId(), false));
        assertEquals(1, servico.listar().size());
    }

    @Test
    @DisplayName("Exclusão confirmada remove o doador")
    void exclusaoConfirmada() {
        Doador doador = cadastrarPadrao();
        servico.excluir(doador.getId(), true);
        assertTrue(servico.listar().isEmpty());
        assertThrows(ValidacaoException.class, () -> servico.buscar(doador.getId()));
    }

    @Test
    @DisplayName("Doador com lote no estoque não pode ser excluído")
    void doadorComLoteNaoEExcluido() {
        Doador doador = cadastrarPadrao();
        Doacao doacao = new Doacao(doador);
        doacao.adicionarItem(new Alimento("Arroz", Categoria.GRAOS, "kg"), new BigDecimal("10"),
                LocalDate.now().plusMonths(3));
        estoque.registrarDoacao(doacao);
        assertThrows(ValidacaoException.class, () -> servico.excluir(doador.getId(), true));
        assertEquals(1, servico.listar().size());
    }
}
