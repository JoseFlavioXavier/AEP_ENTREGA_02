
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// =========================================================================
// CLASSE: Main
// Interface gráfica e integração entre GalpaoManager e CacheEstoque.
// =========================================================================
public class Main extends JFrame {

    private final GalpaoManager galpaoManager = new GalpaoManager();
    private final CacheEstoque cacheEstoque = new CacheEstoque(4);

    // Componentes do galpão.
    private JTextArea txtLogGalpao;

    private JLabel lblStatusRecepcao;
    private JLabel lblStatusTriagem;
    private JLabel lblStatusExpedicao;

    private JLabel lblEstoqueRecepcao;
    private JLabel lblEstoqueTriados;
    private JLabel lblEstoqueExpedidos;

    // Componentes do cache.
    private JTextArea txtCacheLog;
    private JComboBox<String> cbPoliticaCache;
    private JTextField txtIdProdutoConsulta;

    // =========================================================================
    // CONSTRUTOR
    // =========================================================================
    public Main() {

        setTitle("Simulação S.O. & Galpão Logístico - Projeto Integrado");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab(
                "Simulação Galpão (Sincronização & Threads)",
                criarPainelGalpao()
        );

        tabbedPane.addTab(
                "Cache de Estoque (LRU vs FIFO)",
                criarPainelCache()
        );

        add(tabbedPane);

        configurarListenersGalpaoManager();
    }

    // =========================================================================
    // PAINEL 1: GALPÃO
    // =========================================================================
    private JPanel criarPainelGalpao() {

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Botões de controle.
        JPanel painelControle = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10)
        );

        JButton btnIniciar = new JButton("Iniciar Simulação");
        JButton btnPausar = new JButton("Pausar / Parar");
        JButton btnLimparLog = new JButton("Limpar Log");

        painelControle.add(btnIniciar);
        painelControle.add(btnPausar);
        painelControle.add(btnLimparLog);

        painel.add(painelControle, BorderLayout.NORTH);

        // Painel central.
        JPanel painelCentro = new JPanel(
                new GridLayout(1, 2, 10, 10)
        );

        // Monitor de threads.
        JPanel painelStatus = new JPanel();

        painelStatus.setLayout(
                new BoxLayout(painelStatus, BoxLayout.Y_AXIS)
        );

        painelStatus.setBorder(
                BorderFactory.createTitledBorder(
                        "Monitor de Threads (Estados S.O.)"
                )
        );

        lblStatusRecepcao = new JLabel(
                "Thread Recepção: PARADA (S)"
        );

        lblStatusTriagem = new JLabel(
                "Thread Triagem: PARADA (S)"
        );

        lblStatusExpedicao = new JLabel(
                "Thread Expedição: PARADA (S)"
        );

        lblEstoqueRecepcao = new JLabel(
                "Caixas na Recepção: 0"
        );

        lblEstoqueTriados = new JLabel(
                "Caixas Triadas: 0"
        );

        lblEstoqueExpedidos = new JLabel(
                "Caixas Expedidas: 0"
        );

        painelStatus.add(lblStatusRecepcao);
        painelStatus.add(Box.createVerticalStrut(10));

        painelStatus.add(lblStatusTriagem);
        painelStatus.add(Box.createVerticalStrut(10));

        painelStatus.add(lblStatusExpedicao);
        painelStatus.add(Box.createVerticalStrut(20));

        painelStatus.add(lblEstoqueRecepcao);
        painelStatus.add(lblEstoqueTriados);
        painelStatus.add(lblEstoqueExpedidos);

        painelCentro.add(painelStatus);

        // Log de execução.
        txtLogGalpao = new JTextArea();

        txtLogGalpao.setEditable(false);
        txtLogGalpao.setFont(
                new Font("Monospaced", Font.PLAIN, 12)
        );

        JScrollPane scrollLog = new JScrollPane(txtLogGalpao);

        scrollLog.setBorder(
                BorderFactory.createTitledBorder(
                        "Log de Execução do Galpão"
                )
        );

        painelCentro.add(scrollLog);

        painel.add(painelCentro, BorderLayout.CENTER);

        // Ações dos botões.
        btnIniciar.addActionListener(e -> galpaoManager.iniciar());

        btnPausar.addActionListener(e -> galpaoManager.parar());

        btnLimparLog.addActionListener(
                e -> txtLogGalpao.setText("")
        );

        return painel;
    }

    // =========================================================================
    // LISTENERS DO GALPÃO
    // =========================================================================
    private void configurarListenersGalpaoManager() {

        galpaoManager.setLogListener(
                new GalpaoManager.LogListener() {

                    @Override
                    public void onLog(String mensagem) {

                        SwingUtilities.invokeLater(() -> {

                            txtLogGalpao.append(mensagem + "\n");

                            txtLogGalpao.setCaretPosition(
                                    txtLogGalpao.getDocument().getLength()
                            );
                        });
                    }

                    @Override
                    public void onAtualizacaoEstado(
                            int recepcao,
                            int triados,
                            int expedidos
                    ) {

                        SwingUtilities.invokeLater(() -> {

                            lblEstoqueRecepcao.setText(
                                    "Caixas na Recepção: " + recepcao
                            );

                            lblEstoqueTriados.setText(
                                    "Caixas Triadas: " + triados
                            );

                            lblEstoqueExpedidos.setText(
                                    "Caixas Expedidas: " + expedidos
                            );
                        });
                    }

                    @Override
                    public void onEstadoThread(
                            String threadNome,
                            String estado
                    ) {

                        SwingUtilities.invokeLater(() -> {

                            if (threadNome.equals("Recepção")) {

                                lblStatusRecepcao.setText(
                                        "Thread Recepção: " + estado
                                );

                            } else if (threadNome.equals("Triagem")) {

                                lblStatusTriagem.setText(
                                        "Thread Triagem: " + estado
                                );

                            } else if (threadNome.equals("Expedição")) {

                                lblStatusExpedicao.setText(
                                        "Thread Expedição: " + estado
                                );
                            }
                        });
                    }
                }
        );
    }

    // =========================================================================
    // PAINEL 2: CACHE DE ESTOQUE
    // =========================================================================
    private JPanel criarPainelCache() {

        JPanel painel = new JPanel(new BorderLayout(10, 10));

        painel.setBorder(
                new EmptyBorder(10, 10, 10, 10)
        );

        // Controles superiores.
        JPanel painelTopo = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 15, 10)
        );

        painelTopo.add(new JLabel("Política de Cache:"));

        cbPoliticaCache = new JComboBox<>(
                new String[]{"FIFO", "LRU"}
        );

        painelTopo.add(cbPoliticaCache);

        painelTopo.add(new JLabel("ID do Produto:"));

        txtIdProdutoConsulta = new JTextField(5);

        painelTopo.add(txtIdProdutoConsulta);

        JButton btnConsultar = new JButton("Consultar Produto");

        painelTopo.add(btnConsultar);

        JButton btnPopularPredefinido = new JButton(
                "Simular Acesso Sequencial (1,2,3,4,5,1,2...)"
        );

        painelTopo.add(btnPopularPredefinido);

        painel.add(painelTopo, BorderLayout.NORTH);

        // Área de resultados.
        txtCacheLog = new JTextArea();

        txtCacheLog.setEditable(false);

        txtCacheLog.setFont(
                new Font("Monospaced", Font.PLAIN, 13)
        );

        JScrollPane scrollCache = new JScrollPane(txtCacheLog);

        scrollCache.setBorder(
                BorderFactory.createTitledBorder(
                        "Simulador de Substituição de Páginas (Hit/Miss)"
                )
        );

        painel.add(scrollCache, BorderLayout.CENTER);

        // Consulta manual.
        btnConsultar.addActionListener(e -> {

            try {

                int id = Integer.parseInt(
                        txtIdProdutoConsulta.getText().trim()
                );

                String politica = (
                        String
                        ) cbPoliticaCache.getSelectedItem();

                CacheEstoque.ResultadoConsulta res =
                        cacheEstoque.consultar(id, politica);

                atualizarLogCache(res);

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Por favor, digite um ID numérico válido.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Simulação sequencial.
        btnPopularPredefinido.addActionListener(e -> {

            String politica = (
                    String
                    ) cbPoliticaCache.getSelectedItem();

            int[] sequenciaAcessos = {
                    1, 2, 3, 4, 5, 1, 2, 6, 3, 2
            };

            txtCacheLog.append(
                    "--- Iniciando Sequência de Teste para "
                            + politica + " ---\n"
            );

            for (int id : sequenciaAcessos) {

                CacheEstoque.ResultadoConsulta res =
                        cacheEstoque.consultar(id, politica);

                atualizarLogCache(res);
            }

            txtCacheLog.append(
                    "--------------------------------------------------\n"
            );
        });

        return painel;
    }

    // =========================================================================
    // ATUALIZAÇÃO DO LOG DO CACHE
    // =========================================================================
    private void atualizarLogCache(
            CacheEstoque.ResultadoConsulta res
    ) {

        String statusStr = res.hit
                ? "HIT (Em Memória)"
                : "MISS (Não está no cache)";

        txtCacheLog.append(
                String.format(
                        "[%s] Produto ID: %d | Status: %s | Cache Atual: %s | Hits: %d, Misses: %d\n",
                        res.politica,
                        res.idProduto,
                        statusStr,
                        res.estadoCache,
                        res.hits,
                        res.misses
                )
        );

        txtCacheLog.setCaretPosition(
                txtCacheLog.getDocument().getLength()
        );
    }

    // =========================================================================
    // MÉTODO MAIN
    // =========================================================================
    public static void main(String[] args) {

        try {

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {

            new Main().setVisible(true);

        });
    }
}
