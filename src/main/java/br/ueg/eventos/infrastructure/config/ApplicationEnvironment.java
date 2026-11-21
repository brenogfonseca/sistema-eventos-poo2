package br.ueg.eventos.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reads process environment first and then a local, git-ignored .env file. */
public final class ApplicationEnvironment {
    private static final Map<String, String> DOT_ENV = carregarArquivo();

    private ApplicationEnvironment() { }

    public static String get(String chave) {
        String valor = System.getenv(chave);
        if (valor != null && !valor.isBlank()) return valor.trim();
        valor = DOT_ENV.get(chave);
        return valor == null || valor.isBlank() ? null : valor;
    }

    public static String getOrDefault(String chave, String padrao) {
        String valor = get(chave);
        return valor == null ? padrao : valor;
    }

    private static Map<String, String> carregarArquivo() {
        Path arquivo = Path.of(System.getProperty("user.dir"), ".env");
        if (!Files.isRegularFile(arquivo)) return Map.of();
        Map<String, String> valores = new LinkedHashMap<>();
        try {
            for (String linha : Files.readAllLines(arquivo)) {
                String conteudo = linha.trim();
                if (conteudo.isEmpty() || conteudo.startsWith("#")) continue;
                if (conteudo.startsWith("export ")) conteudo = conteudo.substring(7).trim();
                int separador = conteudo.indexOf('=');
                if (separador <= 0) continue;
                String chave = conteudo.substring(0, separador).trim();
                String valor = conteudo.substring(separador + 1).trim();
                if (valor.length() >= 2) {
                    char primeiro = valor.charAt(0);
                    char ultimo = valor.charAt(valor.length() - 1);
                    if ((primeiro == '"' && ultimo == '"') || (primeiro == '\'' && ultimo == '\'')) {
                        valor = valor.substring(1, valor.length() - 1);
                    }
                }
                if (!chave.isEmpty()) valores.put(chave, valor);
            }
            return Map.copyOf(valores);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo .env local.", e);
        }
    }
}
