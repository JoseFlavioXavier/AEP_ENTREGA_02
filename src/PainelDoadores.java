import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class PainelDoadores extends PainelCrud<Doador> {
    private final DoadorService servico;
    private final JTextField nome = new JTextField(20);
    private final JTextField documento = new JTextField(20);
    private final JTextField contato = new JTextField(20);
    private final JComboBox<TipoDoador> tipo = new JComboBox<>(TipoDoador.values());

    public PainelDoadores(DoadorService servico) {
        super("Doador", "Nome", "Documento", "Contato", "Tipo");
        this.servico = servico;
        Visual.rotulos(tipo);
        adicionarCampo("Nome", nome);
        adicionarCampo("Documento", documento);
        adicionarCampo("Contato", contato);
        adicionarCampo("Tipo", tipo);
        recarregar();
    }

    @Override
    protected List<Doador> listarRegistros() {
        return servico.listar();
    }

    @Override
    protected Object[] linha(Doador d) {
        return new Object[] {d.getNome(), d.getDocumento(), d.getContato(), d.getTipo().getDescricao()};
    }

    @Override
    protected void preencher(Doador d) {
        nome.setText(d.getNome());
        documento.setText(d.getDocumento());
        contato.setText(d.getContato());
        tipo.setSelectedItem(d.getTipo());
    }

    @Override
    protected void limparCampos() {
        nome.setText("");
        documento.setText("");
        contato.setText("");
        tipo.setSelectedIndex(0);
    }

    @Override
    protected void criar() {
        servico.cadastrar(nome.getText(), documento.getText(), contato.getText(),
                (TipoDoador) tipo.getSelectedItem());
    }

    @Override
    protected void alterar(Doador d) {
        servico.atualizar(d.getId(), nome.getText(), documento.getText(), contato.getText(),
                (TipoDoador) tipo.getSelectedItem());
    }

    @Override
    protected void remover(Doador d) {
        servico.excluir(d.getId(), true);
    }
}
