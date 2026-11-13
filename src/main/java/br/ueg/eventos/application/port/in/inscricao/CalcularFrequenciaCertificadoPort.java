package br.ueg.eventos.application.port.in.inscricao;

public interface CalcularFrequenciaCertificadoPort {
    Resultado executar(Long idInscricaoEvento, Long idUsuario);

    final class Resultado {
        public final boolean certificadoHabilitado;
        public final int frequenciaMinima;
        public final double percentualFrequencia;
        public final long minutosPresentes;
        public final long minutosTotais;
        public final boolean elegivel;

        public Resultado(boolean certificadoHabilitado, int frequenciaMinima,
                         double percentualFrequencia, long minutosPresentes,
                         long minutosTotais, boolean elegivel) {
            this.certificadoHabilitado = certificadoHabilitado;
            this.frequenciaMinima = frequenciaMinima;
            this.percentualFrequencia = percentualFrequencia;
            this.minutosPresentes = minutosPresentes;
            this.minutosTotais = minutosTotais;
            this.elegivel = elegivel;
        }
    }
}
