package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

/**
 * Entidade raiz que representa um Evento.
 * Demonstra encapsulamento e proteção de invariantes.
 */
public class Evento {

    private Long id;
    private String titulo; // Mapeado para nome no DB
    private String descricao;
    private Periodo periodo;
    private int capacidadeMaxima;
    private boolean inscricoesAbertas;
    private boolean deletado;
    private boolean certificado;
    private int frequenciaMinima;
    private String criterioFrequenciaCertificado = "PERCENTUAL";
    private int frequenciaMinimaMinutos;
    private String modoCargaHorariaCertificado = "DINAMICA";
    private int cargaHorariaFixaMinutos;
    private String modalidade;
    private String status;
    private String fusoHorario;
    private java.time.LocalDateTime prazoCancelamento;

    public Evento(String titulo, String descricao, Periodo periodo, int capacidadeMaxima) {
        setTitulo(titulo);
        setDescricao(descricao);
        setPeriodo(periodo);
        setCapacidadeMaxima(capacidadeMaxima);
        this.inscricoesAbertas = false; // Eventos começam fechados por padrão
        this.deletado = false;
        this.certificado = false;
        this.frequenciaMinima = 75; // Default 75%
        this.modalidade = "PRESENCIAL";
        this.status = "RASCUNHO";
        this.fusoHorario = "America/Sao_Paulo";
        this.prazoCancelamento = null;
        this.frequenciaMinimaMinutos = 0;
        this.cargaHorariaFixaMinutos = 0;
    }
    
    // Construtor para reconstrução via repositório
    public Evento(Long id, String titulo, String descricao, Periodo periodo, int capacidadeMaxima, 
                  boolean inscricoesAbertas, boolean deletado, boolean certificado, int frequenciaMinima) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.periodo = periodo;
        this.capacidadeMaxima = capacidadeMaxima;
        this.inscricoesAbertas = inscricoesAbertas;
        this.deletado = deletado;
        this.certificado = certificado;
        validarFrequenciaMinima(frequenciaMinima);
        this.frequenciaMinima = frequenciaMinima;
        this.modalidade = "PRESENCIAL";
        this.status = "PUBLICADO";
        this.fusoHorario = "America/Sao_Paulo";
    }

    public Evento(Long id, String titulo, String descricao, Periodo periodo, int capacidadeMaxima,
                  boolean inscricoesAbertas, boolean deletado, boolean certificado, int frequenciaMinima,
                  String modalidade, String status, String fusoHorario) {
        this(id, titulo, descricao, periodo, capacidadeMaxima, inscricoesAbertas, deletado,
                certificado, frequenciaMinima, modalidade, status, fusoHorario, null);
    }

    public Evento(Long id, String titulo, String descricao, Periodo periodo, int capacidadeMaxima,
                  boolean inscricoesAbertas, boolean deletado, boolean certificado, int frequenciaMinima,
                  String modalidade, String status, String fusoHorario,
                  java.time.LocalDateTime prazoCancelamento) {
        this(id, titulo, descricao, periodo, capacidadeMaxima, inscricoesAbertas,
                deletado, certificado, frequenciaMinima);
        configurarPublicacao(modalidade, status, fusoHorario);
        configurarPrazoCancelamento(prazoCancelamento);
    }

    public Evento(Long id, String titulo, String descricao, Periodo periodo, int capacidadeMaxima,
                  boolean inscricoesAbertas, boolean deletado, boolean certificado, int frequenciaMinima,
                  String modalidade, String status, String fusoHorario, java.time.LocalDateTime prazoCancelamento,
                  String criterioFrequenciaCertificado, int frequenciaMinimaMinutos,
                  String modoCargaHorariaCertificado, int cargaHorariaFixaMinutos) {
        this(id, titulo, descricao, periodo, capacidadeMaxima, inscricoesAbertas, deletado, certificado,
                frequenciaMinima, modalidade, status, fusoHorario, prazoCancelamento);
        configurarPoliticaCertificado(criterioFrequenciaCertificado, frequenciaMinima,
                frequenciaMinimaMinutos, modoCargaHorariaCertificado, cargaHorariaFixaMinutos);
        this.certificado = certificado;
    }

    // Regras de negócio protegidas nos "Setters" privados (ou públicos quando fizer sentido)
    private void setTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new RegraNegocioException("O título do evento é obrigatório.");
        }
        this.titulo = titulo;
    }

    private void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    private void setPeriodo(Periodo periodo) {
        if (periodo == null) {
            throw new RegraNegocioException("O evento deve possuir um período de realização.");
        }
        this.periodo = periodo;
    }

    private void setCapacidadeMaxima(int capacidadeMaxima) {
        if (capacidadeMaxima <= 0) {
            throw new RegraNegocioException("A capacidade máxima deve ser maior que zero.");
        }
        this.capacidadeMaxima = capacidadeMaxima;
    }

    // =========================================================================
    // COMPORTAMENTOS DO DOMÍNIO (ENCAPSULAMENTO E PROTEÇÃO DE INVARIANTES)
    // =========================================================================
    // NOTA DIDÁTICA SOBRE O DESIGN DO DOMÍNIO:
    // Em vez de expor "setters" públicos soltos (ex: setTitulo, setPeriodo),
    // o domínio expõe métodos de intenção de negócio. Isso impede que a entidade
    // fique em estado inconsistente ou inválido no meio da execução.
    // =========================================================================

    public void abrirInscricoes() {
        this.inscricoesAbertas = true;
    }

    public void fecharInscricoes() {
        this.inscricoesAbertas = false;
    }

    public void habilitarCertificado(int frequenciaMinima) {
        configurarPoliticaCertificado("PERCENTUAL", frequenciaMinima, 0, "DINAMICA", 0);
    }

    public void configurarPoliticaCertificado(String criterio, int minimoPercentual,
                                              int minimoMinutos, String modoCarga,
                                              int cargaFixaMinutos) {
        String criterioNormalizado = criterio == null ? "" : criterio.trim().toUpperCase();
        String modoNormalizado = modoCarga == null ? "" : modoCarga.trim().toUpperCase();
        if (!java.util.Set.of("PERCENTUAL", "HORAS").contains(criterioNormalizado))
            throw new RegraNegocioException("O critério do certificado deve ser PERCENTUAL ou HORAS.");
        if (!java.util.Set.of("FIXA", "DINAMICA").contains(modoNormalizado))
            throw new RegraNegocioException("A carga horária do certificado deve ser FIXA ou DINAMICA.");
        validarFrequenciaMinima(minimoPercentual);
        if ("HORAS".equals(criterioNormalizado) && minimoMinutos <= 0)
            throw new RegraNegocioException("Informe a quantidade mínima de horas de presença.");
        if ("FIXA".equals(modoNormalizado) && cargaFixaMinutos <= 0)
            throw new RegraNegocioException("Informe a carga horária fixa do certificado.");
        if (minimoMinutos < 0 || cargaFixaMinutos < 0)
            throw new RegraNegocioException("As cargas horárias não podem ser negativas.");
        this.certificado = true;
        this.criterioFrequenciaCertificado = criterioNormalizado;
        this.frequenciaMinima = minimoPercentual;
        this.frequenciaMinimaMinutos = minimoMinutos;
        this.modoCargaHorariaCertificado = modoNormalizado;
        this.cargaHorariaFixaMinutos = cargaFixaMinutos;
    }
    
    public void desabilitarCertificado() {
        this.certificado = false;
    }

    public void configurarPublicacao(String modalidade, String status, String fusoHorario) {
        String modalidadeNormalizada = modalidade == null || modalidade.isBlank()
                ? "PRESENCIAL" : modalidade.trim().toUpperCase();
        String statusNormalizado = status == null || status.isBlank()
                ? "RASCUNHO" : status.trim().toUpperCase();
        if (!java.util.Set.of("PRESENCIAL", "ONLINE", "HIBRIDO").contains(modalidadeNormalizada)) {
            throw new RegraNegocioException("Modalidade deve ser PRESENCIAL, ONLINE ou HIBRIDO.");
        }
        if (!java.util.Set.of("RASCUNHO", "PUBLICADO", "CANCELADO", "ENCERRADO").contains(statusNormalizado)) {
            throw new RegraNegocioException("Estado deve ser RASCUNHO, PUBLICADO, CANCELADO ou ENCERRADO.");
        }
        if (fusoHorario == null || fusoHorario.isBlank()) {
            throw new RegraNegocioException("O fuso horário do evento é obrigatório.");
        }
        try {
            java.time.ZoneId.of(fusoHorario.trim());
        } catch (java.time.DateTimeException e) {
            throw new RegraNegocioException("Informe um fuso horário IANA válido.");
        }
        this.modalidade = modalidadeNormalizada;
        this.status = statusNormalizado;
        this.fusoHorario = fusoHorario.trim();
        if (!"PUBLICADO".equals(statusNormalizado)) {
            fecharInscricoes();
        }
    }

    public void configurarPrazoCancelamento(java.time.LocalDateTime prazo) {
        if (prazo != null && prazo.isAfter(periodo.getInicio())) {
            throw new RegraNegocioException("O prazo de cancelamento deve ocorrer até o início do evento.");
        }
        this.prazoCancelamento = prazo;
    }

    private void validarFrequenciaMinima(int frequenciaMinima) {
        if (frequenciaMinima != 50 && frequenciaMinima != 75 && frequenciaMinima != 100) {
            throw new RegraNegocioException("A frequência mínima para certificado deve ser 50%, 75% ou 100%.");
        }
    }
    
    /**
     * Atualização integral dos dados cadastrais do evento.
     * 
     * MOTIVO / POR QUE FOI ADICIONADO:
     * Para atender ao caso de uso de alteração de eventos (AlterarEventoService) sem quebrar o
     * encapsulamento nem expor setters públicos soltos. Todas as propriedades passam
     * obrigatoriamente pelas validações de invariantes privadas (setTitulo, setDescricao,
     * setPeriodo, setCapacidadeMaxima).
     * 
     * POR QUE NÃO COLOCAMOS O 'usuarioExecutor' AQUI DENTRO DO EVENTO?
     * Pelo princípio de Responsabilidade Única (SRP) e baixo acoplamento: a entidade Evento
     * não conhece quem é seu organizador nem a lista de usuários do sistema. Essa amarração
     * de segurança é delegada à classe associativa 'VinculoOrganizador' e orquestrada na camada
     * de aplicação (AlterarEventoService).
     */
    public void atualizarDados(String titulo, String descricao, Periodo periodo, int capacidadeMaxima) {
        setTitulo(titulo);
        setDescricao(descricao);
        setPeriodo(periodo);
        setCapacidadeMaxima(capacidadeMaxima);
    }

    /**
     * Marca o evento como deletado (exclusão lógica).
     * 
     * MOTIVO / POR QUE UTILIZAMOS EXCLUSÃO LÓGICA (SOFT DELETE):
     * Em sistemas corporativos com histórico, não removemos fisicamente a linha do evento
     * imediatamente porque ele pode ter inscrições, questionários ou frequências já emitidas.
     * Este método apenas altera o invariante interno do evento para 'deletado = true'.
     */
    public void deletar() {
        this.deletado = true;
    }


    // Getters
    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public boolean isInscricoesAbertas() {
        return inscricoesAbertas;
    }

    public boolean isDeletado() {
        return deletado;
    }

    public boolean isCertificado() {
        return certificado;
    }

    public int getFrequenciaMinima() {
        return frequenciaMinima;
    }

    public String getCriterioFrequenciaCertificado() { return criterioFrequenciaCertificado; }
    public int getFrequenciaMinimaMinutos() { return frequenciaMinimaMinutos; }
    public String getModoCargaHorariaCertificado() { return modoCargaHorariaCertificado; }
    public int getCargaHorariaFixaMinutos() { return cargaHorariaFixaMinutos; }

    public String getModalidade() { return modalidade; }

    public String getStatus() { return status; }

    public String getFusoHorario() { return fusoHorario; }

    public java.time.LocalDateTime getPrazoCancelamento() { return prazoCancelamento; }

    public boolean isPublicado() { return "PUBLICADO".equals(status); }
}
