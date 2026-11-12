package sistop;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

// =========================================================================
// CLASSE: GalpaoManager
// Gerencia as threads de Recepção, Triagem e Expedição.
// Utiliza Mutex e Semáforos para sincronização.
// =========================================================================
public class GalpaoManager {

    // Mutex para proteger o estoque compartilhado.
    private final ReentrantLock mutexEstoque = new ReentrantLock();

    // Semáforos para controle de recursos.
    private final Semaphore semapTriagem = new Semaphore(2);
    private final Semaphore semapExpedicao = new Semaphore(1);

    // Controle de execução.
    private volatile boolean rodando = false;

    // Estoques do galpão.
    private int estoqueRecepcao = 0;
    private int estoqueTriados = 0;
    private int estoqueExpedidos = 0;

    // Threads.
    private Thread tRecepcao;
    private Thread tTriagem;
    private Thread tExpedicao;

    // Interface para comunicação com a interface gráfica.
    public interface LogListener {

        void onLog(String mensagem);

        void onAtualizacaoEstado(
                int recepcao,
                int triados,
                int expedidos
        );

        void onEstadoThread(String threadNome, String estado);
    }

    private volatile LogListener logListener;

    public void setLogListener(LogListener listener) {
        this.logListener = listener;
    }

    // =========================================================================
    // INICIAR SIMULAÇÃO
    // =========================================================================
    public synchronized void iniciar() {

        if (rodando) {
            return;
        }

        // Evita iniciar novas threads enquanto as anteriores ainda encerram.
        if ((tRecepcao != null && tRecepcao.isAlive()) ||
                (tTriagem != null && tTriagem.isAlive()) ||
                (tExpedicao != null && tExpedicao.isAlive())) {
            return;
        }

        rodando = true;

        // ---------------------------------------------------------------------
        // THREAD 1: RECEPÇÃO
        // ---------------------------------------------------------------------
        tRecepcao = new Thread(() -> {

            notificarEstado("Recepção", "EXECUTANDO (R)");

            while (rodando) {

                try {

                    // Adquire o Mutex.
                    mutexEstoque.lock();

                    try {

                        estoqueRecepcao += 5;

                        notificarLog(
                                "[RECEPÇÃO] Descarregando caixas... Total na Recepção: "
                                        + estoqueRecepcao
                        );

                        notificarContadores();

                    } finally {

                        // Libera o Mutex após a seção crítica.
                        mutexEstoque.unlock();
                    }

                    Thread.sleep(2000);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;
                }
            }

            notificarEstado("Recepção", "DORMINDO / PARADA (S)");

        }, "Thread-Recepcao");

        // ---------------------------------------------------------------------
        // THREAD 2: TRIAGEM
        // ---------------------------------------------------------------------
        tTriagem = new Thread(() -> {

            notificarEstado("Triagem", "EXECUTANDO (R)");

            while (rodando) {

                boolean semaforoAdquirido = false;
                boolean mutexAdquirido = false;

                try {

                    // Adquire uma permissão do semáforo.
                    semapTriagem.acquire();
                    semaforoAdquirido = true;

                    // Adquire o Mutex.
                    mutexEstoque.lock();
                    mutexAdquirido = true;

                    if (estoqueRecepcao >= 3) {

                        estoqueRecepcao -= 3;
                        estoqueTriados += 3;

                        notificarLog(
                                "[TRIAGEM] Processando caixas (Mutex + Semáforo)... Triadas: "
                                        + estoqueTriados
                        );

                        notificarContadores();
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;

                } finally {

                    // Libera somente os recursos efetivamente adquiridos.
                    if (mutexAdquirido) {
                        mutexEstoque.unlock();
                    }

                    if (semaforoAdquirido) {
                        semapTriagem.release();
                    }
                }

                try {

                    Thread.sleep(3000);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;
                }
            }

            notificarEstado("Triagem", "DORMINDO / PARADA (S)");

        }, "Thread-Triagem");

        // ---------------------------------------------------------------------
        // THREAD 3: EXPEDIÇÃO
        // ---------------------------------------------------------------------
        tExpedicao = new Thread(() -> {

            notificarEstado("Expedição", "EXECUTANDO (R)");

            while (rodando) {

                boolean semaforoAdquirido = false;
                boolean mutexAdquirido = false;

                try {

                    // Adquire uma permissão do semáforo.
                    semapExpedicao.acquire();
                    semaforoAdquirido = true;

                    // Adquire o Mutex.
                    mutexEstoque.lock();
                    mutexAdquirido = true;

                    if (estoqueTriados >= 2) {

                        estoqueTriados -= 2;
                        estoqueExpedidos += 2;

                        notificarLog(
                                "[EXPEDIÇÃO] Carregando caminhão... Expedidas: "
                                        + estoqueExpedidos
                        );

                        notificarContadores();
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;

                } finally {

                    // Libera somente os recursos efetivamente adquiridos.
                    if (mutexAdquirido) {
                        mutexEstoque.unlock();
                    }

                    if (semaforoAdquirido) {
                        semapExpedicao.release();
                    }
                }

                try {

                    Thread.sleep(4000);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;
                }
            }

            notificarEstado("Expedição", "DORMINDO / PARADA (S)");

        }, "Thread-Expedicao");

        // Inicia as três threads.
        tRecepcao.start();
        tTriagem.start();
        tExpedicao.start();
    }

    // =========================================================================
    // PARAR SIMULAÇÃO
    // =========================================================================
    public synchronized void parar() {

        rodando = false;

        if (tRecepcao != null) {
            tRecepcao.interrupt();
        }

        if (tTriagem != null) {
            tTriagem.interrupt();
        }

        if (tExpedicao != null) {
            tExpedicao.interrupt();
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private void notificarLog(String msg) {

        LogListener listener = logListener;

        if (listener != null) {
            listener.onLog(msg);
        }
    }

    private void notificarContadores() {

        LogListener listener = logListener;

        if (listener != null) {

            listener.onAtualizacaoEstado(
                    estoqueRecepcao,
                    estoqueTriados,
                    estoqueExpedidos
            );
        }
    }

    private void notificarEstado(String nome, String estado) {

        LogListener listener = logListener;

        if (listener != null) {
            listener.onEstadoThread(nome, estado);
        }
    }
}
