package br.ueg.eventos.application.port.in.inscricao;

public interface CalcularFrequenciaCertificadoPort {
    Resultado executar(Long idInscricaoEvento, Long idUsuario);

    final class Resultado {
        public final boolean certificadoHabilitado;
        public final int frequenciaMinima;
        public final String criterioFrequencia;
        public final int frequenciaMinimaMinutos;
        public final String modoCargaHoraria;
        public final long cargaHorariaCertificadoMinutos;
        public final double percentualFrequencia;
        public final long minutosPresentes;
        public final long minutosTotais;
        public final boolean elegivel;

        public Resultado(boolean certificadoHabilitado, int frequenciaMinima,
                         String criterioFrequencia, int frequenciaMinimaMinutos,
                         String modoCargaHoraria, long cargaHorariaCertificadoMinutos,
                         double percentualFrequencia, long minutosPresentes,
                         long minutosTotais, boolean elegivel) {
            this.certificadoHabilitado = certificadoHabilitado;
            this.frequenciaMinima = frequenciaMinima;
            this.criterioFrequencia = criterioFrequencia;
            this.frequenciaMinimaMinutos = frequenciaMinimaMinutos;
            this.modoCargaHoraria = modoCargaHoraria;
            this.cargaHorariaCertificadoMinutos = cargaHorariaCertificadoMinutos;
            this.percentualFrequencia = percentualFrequencia;
            this.minutosPresentes = minutosPresentes;
            this.minutosTotais = minutosTotais;
            this.elegivel = elegivel;
        }
    }
}
