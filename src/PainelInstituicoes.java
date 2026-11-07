import java.util.List;
import javax.swing.JTextField;

public class PainelInstituicoes extends PainelCrud<Instituicao> {
    private final InstituicaoService servico;
    private final JTextField nome = new JTextField(20);
    private final JTextField documento = new JTextField(20);
    private final JTextField contato = new JTextField(20);
    private final JTextField endereco = new JTextField(20);
    private final JTextField capacidade = new JTextField(20);

    public PainelInstituicoes(InstituicaoService servico) {
        super("Instituição", "Nome", "Documento", "Contato", "Endereço", "Capacidade");
        this.servico = servico;
        adicionarCampo("Nome", nome);
        adicionarCampo("Documento", documento);
        adicionarCampo("Contato", contato);
        adicionarCampo("Endereço", endereco);
        adicionarCampo("Capacidade", capacidade);
        recarregar();
    }

    @Override
    protected List<Instituicao> listarRegistros() {
        return servico.listar();
    }

    @Override
    protected Object[] linha(Instituicao i) {
        return new Object[] {i.getNome(), i.getDocumento(), i.getContato(), i.getEndereco(), i.getCapacidade()};
    }

    @Override
    protected void preencher(Instituicao i) {
        nome.setText(i.getNome());
        documento.setText(i.getDocumento());
        contato.setText(i.getContato());
        endereco.setText(i.getEndereco());
        capacidade.setText(String.valueOf(i.getCapacidade()));
    }

    @Override
    protected void limparCampos() {
        nome.setText("");
        documento.setText("");
        contato.setText("");
        endereco.setText("");
        capacidade.setText("");
    }

    @Override
    protected void criar() {
        servico.cadastrar(nome.getText(), documento.getText(), contato.getText(), endereco.getText(),
                Entrada.inteiro(capacidade.getText(), "Capacidade"));
    }

    @Override
    protected void alterar(Instituicao i) {
        servico.atualizar(i.getId(), nome.getText(), documento.getText(), contato.getText(), endereco.getText(),
                Entrada.inteiro(capacidade.getText(), "Capacidade"));
    }

    @Override
    protected void remover(Instituicao i) {
        servico.excluir(i.getId(), true);
    }
}
