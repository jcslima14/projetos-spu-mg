
public class ProcessoAndamento implements Cloneable {
	private Integer processoAndamentoId;
	
	private String numeroProcesso;
	
	private String dataHora;
	
	private Integer sequencial;
	
	private String unidade;
	
	private String usuario;
	
	private String descricao;

	public ProcessoAndamento() {
	}
	
	public ProcessoAndamento(Integer processoAndamentoId, String numeroProcesso, String dataHora, Integer sequencial, String unidade, String usuario, String descricao) {
		setProcessoAndamentoId(processoAndamentoId);
		setNumeroProcesso(numeroProcesso);
		setDataHora(dataHora);
		setSequencial(sequencial);
		setUnidade(unidade);
		setUsuario(usuario);
		setDescricao(descricao);
	}
	
	public Integer getProcessoAndamentoId() {
		return processoAndamentoId;
	}

	public void setProcessoAndamentoId(Integer processoAndamentoId) {
		this.processoAndamentoId = processoAndamentoId;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getDataHora() {
		return dataHora;
	}

	public void setDataHora(String dataHora) {
		this.dataHora = dataHora;
	}

	public Integer getSequencial() {
		return sequencial;
	}

	public void setSequencial(Integer sequencial) {
		this.sequencial = sequencial;
	}

	public String getUnidade() {
		return unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String dataFormatada(String dataHora) {
		if (dataHora != null) {
			return dataHora.substring(6, 10).concat("-").concat(dataHora.substring(3, 5)).concat("-").concat(dataHora.substring(0, 2)).concat(dataHora.substring(10, 16));
		} else {
			return null;
		}
	}
	
	public String dataFormatada() {
		return dataFormatada(getDataHora());
	}

	public String[] unidades() {
		if (getUnidade() != null) {
			return getUnidade().split(",");
		} else {
			return null;
		}
	}

	public String sqlBuscaProcesso() {
		if (getNumeroProcesso() != null) {
			return "(select processoid from processo where numeroprocesso = '" + getNumeroProcesso() + "')";
		} else {
			return null;
		}
	}

	public String sqlBuscaUsuario(String usuario) {
		if (usuario != null) {
			return "(select usuarioid from usuario where cpf = '" + usuario + "')";
		} else {
			return "null";
		}
	}

	public String sqlBuscaUnidade(String unidade) {
		if (unidade != null) {
			return "(select unidadeid from unidade where nome = '" + unidade + "')";
		} else {
			return null;
		}
	}

	public String sqlBuscaUsuario() {
		return sqlBuscaUsuario(getUsuario());
	}
	
	@Override
	protected ProcessoAndamento clone() {
		ProcessoAndamento clone = null;
		try {
			clone = (ProcessoAndamento) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}
}
