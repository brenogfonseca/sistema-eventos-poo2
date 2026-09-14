//modelo do evento

public class Evento {
    private int id;
    private String nome;
    private boolean deletado;
    private int CapacidadeMaxima;
    private boolean certificado;
    private int frequencia;

    public Evento(int id, String nome, boolean deletado, boolean certificado, int frequencia) {
        this.id = id;
        this.nome = nome;
        this.deletado = deletado;
        this.certificado = certificado;
        this.frequencia = frequencia;
    }
    public Evento(Long id, String nome, boolean deletado, boolean certificado, Long frequencia) {
        setNome(nome);
        setDeletado(deletado);
        setCertificado(certificado);
        setFrequencia(frequencia);
        setId(id);
        this.id = id.intValue();   
        this.frequencia = frequencia.intValue();
        this.certificado = certificado;
        this.deletado = deletado;
        this.nome = nome;
    }

    private void setNome(String nome) {
        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Nome do evento não pode ser nulo ou vazio.");
        }
    }

    private void setId(Long id) {
        this.id = id.intValue();
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
        return nome;
    }

    public boolean isDeletado() {
        return deletado;
    }

    public int getCapacidadeMaxima() {
        return CapacidadeMaxima;
    }

    public boolean isCertificado() {
        return certificado;
    }

    public int getFrequencia() {
        return frequencia;
    }
}