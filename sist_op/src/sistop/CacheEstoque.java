package sistop;

import java.util.LinkedHashMap;
import java.util.Map;

// =========================================================================
// CLASSE: CacheEstoque
// Responsável por simular e gerenciar FIFO e LRU no cache de estoque.
// =========================================================================
public class CacheEstoque {

    private final int capacidade;

    private final LinkedHashMap<Integer, String> cacheLRU;
    private final LinkedHashMap<Integer, String> cacheFIFO;

    private int hitsLRU = 0;
    private int missesLRU = 0;

    private int hitsFIFO = 0;
    private int missesFIFO = 0;

    // Construtor
    public CacheEstoque(int capacidade) {

        if (capacidade <= 0) {
            throw new IllegalArgumentException(
                    "A capacidade do cache deve ser maior que zero."
            );
        }

        this.capacidade = capacidade;

        // LRU: true ativa a ordenação por acesso.
        // O elemento menos recentemente utilizado fica no início.
        this.cacheLRU = new LinkedHashMap<Integer, String>(
                capacidade, 0.75f, true
        ) {
            @Override
            protected boolean removeEldestEntry(
                    Map.Entry<Integer, String> eldest
            ) {
                return size() > CacheEstoque.this.capacidade;
            }
        };

        // FIFO: false mantém a ordem original de inserção.
        this.cacheFIFO = new LinkedHashMap<Integer, String>(
                capacidade, 0.75f, false
        ) {
            @Override
            protected boolean removeEldestEntry(
                    Map.Entry<Integer, String> eldest
            ) {
                return size() > CacheEstoque.this.capacidade;
            }
        };
    }

    // Método principal de consulta ao estoque.
    public ResultadoConsulta consultar(int idProduto, String politica) {

        if (politica == null ||
                (!politica.equalsIgnoreCase("LRU") &&
                        !politica.equalsIgnoreCase("FIFO"))) {

            throw new IllegalArgumentException(
                    "Política inválida. Utilize LRU ou FIFO."
            );
        }

        String nomeProduto = "Produto #" + idProduto;

        boolean hit;
        String estadoCache;

        if (politica.equalsIgnoreCase("LRU")) {

            // IMPORTANTE:
            // O get() atualiza a posição do elemento na ordem de acesso.
            // Isso é necessário para o funcionamento correto do LRU.
            String produtoEncontrado = cacheLRU.get(idProduto);

            if (produtoEncontrado != null) {

                hitsLRU++;
                hit = true;

            } else {

                missesLRU++;
                hit = false;

                cacheLRU.put(idProduto, nomeProduto);
            }

            estadoCache = cacheLRU.keySet().toString();

            return new ResultadoConsulta(
                    idProduto,
                    "LRU",
                    hit,
                    estadoCache,
                    hitsLRU,
                    missesLRU
            );

        } else {

            // FIFO: get() não altera a ordem de inserção.
            String produtoEncontrado = cacheFIFO.get(idProduto);

            if (produtoEncontrado != null) {

                hitsFIFO++;
                hit = true;

            } else {

                missesFIFO++;
                hit = false;

                cacheFIFO.put(idProduto, nomeProduto);
            }

            estadoCache = cacheFIFO.keySet().toString();

            return new ResultadoConsulta(
                    idProduto,
                    "FIFO",
                    hit,
                    estadoCache,
                    hitsFIFO,
                    missesFIFO
            );
        }
    }

    // Classe auxiliar para transportar os resultados da consulta.
    public static class ResultadoConsulta {

        public final int idProduto;
        public final String politica;
        public final boolean hit;
        public final String estadoCache;
        public final int hits;
        public final int misses;

        public ResultadoConsulta(
                int idProduto,
                String politica,
                boolean hit,
                String estadoCache,
                int hits,
                int misses
        ) {
            this.idProduto = idProduto;
            this.politica = politica;
            this.hit = hit;
            this.estadoCache = estadoCache;
            this.hits = hits;
            this.misses = misses;
        }
    }
}
