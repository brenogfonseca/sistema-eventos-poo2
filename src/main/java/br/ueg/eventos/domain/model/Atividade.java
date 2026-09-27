package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

import java.time.LocalDate;
import java.time.LocalTime;

public class Atividade {

    private Long id;
    private String nome;
    private Long eventoId;
    private Local local;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private LocalDate data;
    private boolean controlaVagas;
    private int vagas;
    private String tipo;
    private TipoFrequenciaEnum tipoFrequencia;
    private Trilha trilha;

    public Atividade(String nome, Long eventoId, Local local, LocalTime horaInicio, LocalTime horaFim,
                     LocalDate data, boolean controlaVagas, int vagas, String tipo, 
                     TipoFrequenciaEnum tipoFrequencia, Trilha trilha) {
        setNome(nome);
        this.eventoId = eventoId;
        this.local = local;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.data = data;
        this.controlaVagas = controlaVagas;
        this.vagas = vagas;
        this.tipo = tipo;
        this.tipoFrequencia = tipoFrequencia;
        this.trilha = trilha;
    }

    public Atividade(Long id, String nome, Long eventoId, Local local, LocalTime horaInicio, LocalTime horaFim,
                     LocalDate data, boolean controlaVagas, int vagas, String tipo, 
                     TipoFrequenciaEnum tipoFrequencia, Trilha trilha) {
        this(nome, eventoId, local, horaInicio, horaFim, data, controlaVagas, vagas, tipo, tipoFrequencia, trilha);
        this.id = id;
    }

    private void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome da atividade é obrigatório.");
        }
        this.nome = nome;
    }

    // Comportamento
    public void registrarInscricao() {
        if (this.controlaVagas) {
            if (this.vagas <= 0) {
                throw new RegraNegocioException("Não há vagas disponíveis para esta atividade.");
            }
            this.vagas--;
        }
    }

    public void cancelarInscricao() {
        if (this.controlaVagas) {
            this.vagas++;
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Long getEventoId() { return eventoId; }
    public Local getLocal() { return local; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public LocalDate getData() { return data; }
    public boolean isControlaVagas() { return controlaVagas; }
    public int getVagas() { return vagas; }
    public String getTipo() { return tipo; }
    public TipoFrequenciaEnum getTipoFrequencia() { return tipoFrequencia; }
    public Trilha getTrilha() { return trilha; }
}
