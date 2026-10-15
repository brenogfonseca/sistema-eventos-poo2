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

    public Evento(String titulo, String descricao, Periodo periodo, int capacidadeMaxima) {
        setTitulo(titulo);
        setDescricao(descricao);
        setPeriodo(periodo);
        setCapacidadeMaxima(capacidadeMaxima);
        this.inscricoesAbertas = false; // Eventos começam fechados por padrão
        this.deletado = false;
        this.certificado = false;
        this.frequenciaMinima = 75; // Default 75%
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
        this.frequenciaMinima = frequenciaMinima;
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
        this.certificado = true;
        this.frequenciaMinima = frequenciaMinima;
    }
    
    public void desabilitarCertificado() {
        this.certificado = false;
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
}