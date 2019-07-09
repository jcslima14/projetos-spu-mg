public class TipoProcesso {
	private Integer tipoProcessoId;
	
	private String descricao;

	public TipoProcesso(Integer tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public TipoProcesso(Integer tipoProcessoId, String descricao) {
		this.tipoProcessoId = tipoProcessoId;
		this.descricao = descricao;
	}

	public Integer getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Integer tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
