//modelo do evento

public class Evento {
    private int id;
    private String Nome;
    private boolean deletado;
    private int CapacidadeMaxima;
    private int trilha;
    private int tipo;
    private boolean certificado;
    private int frequencia;

    public Evento(int id, String nome, boolean deletado, int trilha, int tipo, boolean certificado, int frequencia) {
        this.id = id;
        Nome = nome;
        this.deletado = deletado;
        this.trilha = trilha;
        this.tipo = tipo;
        this.certificado = certificado;
        this.frequencia = frequencia;
    }
    public Evento(Long id, String nome, boolean deletado, Long trilha, Long tipo, boolean certificado, Long frequencia) {
        setNome(nome);
        setDeletado(deletado);
        setTrilha(trilha);
        setTipo(tipo);
        setCertificado(certificado);
        setFrequencia(frequencia);
        setId(id);
        this.id = id.intValue();   
        this.trilha = trilha.intValue();
        this.tipo = tipo.intValue();
        this.frequencia = frequencia.intValue();
        this.certificado = certificado;
        this.deletado = deletado;
        this.Nome = nome;
    }

    private void setNome(String nome) {
        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Nome do evento não pode ser nulo ou vazio.");
        }
    }

    private void setId(Long id) {
        this.id = id.intValue();
    }

    private void setTrilha(Long trilha) {
        this.trilha = trilha.intValue();
    }

    private void setTipo(Long tipo) {
        this.tipo = tipo.intValue();
    }

    private void setCertificado(boolean certificado) {
        this.certificado = certificado;
    }

    private void setFrequencia(Long frequencia) {
        this.frequencia = frequencia.intValue();
    }

    private void setDeletado(boolean deletado) {
        this.deletado = deletado;
    } 
    
    private void setCapacidadeMaxima(int capacidadeMaxima) {
        this.CapacidadeMaxima = capacidadeMaxima;
        if(CapacidadeMaxima < 0){
            throw new IllegalArgumentException("Capacidade máxima não pode ser negativa.");
        }
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return Nome;
    }

    public boolean isDeletado() {
        return deletado;
    }

    public int getCapacidadeMaxima() {
        return CapacidadeMaxima;
    }

    public int getTrilha() {
        return trilha;
    }

    public int getTipo() {
        return tipo;
    }

    public boolean isCertificado() {
        return certificado;
    }

    public int getFrequencia() {
        return frequencia;
    }

    public void setCapacidadeMaxima(int capacidadeMaxima) {
        if (capacidadeMaxima < 0) {
            throw new IllegalArgumentException("Capacidade máxima não pode ser negativa.");
        }
        this.CapacidadeMaxima = capacidadeMaxima;
    }
}