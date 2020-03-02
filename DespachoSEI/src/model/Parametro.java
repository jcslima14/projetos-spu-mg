package model;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "parametro")
public class Parametro {
	public static int PASTA_DOWNLOAD_SAPIENS = 1;
	public static int PASTA_DESTINO_PROCESSOS_SAPIENS = 2;
	public static int ENDERECO_SEI = 3;
	public static int ENDERECO_SAPIENS = 4;
	public static int PASTA_DESPACHOS_SALVOS = 5;
	public static int PASTA_PLANILHA_IMPORTACAO = 6;
	public static int DEFAULT_BROWSER = 7;
	public static int ENDERECO_SPUNET = 8;
	public static int PASTA_ARQUIVOS_PROCESSOS_INDIVIDUAIS = 9;
	public static int UNIDADE_PADRAO_SEI = 10;
	public static int BAIXAR_TODO_PROCESSO_SAPIENS = 11;
	public static int TEMPO_ESPERA = 12;
	public static int RECEBER_PROCESSO_SEM_ARQUIVO = 13;

	@SuppressWarnings("serial")
	public static Map<Integer, String[]> DESCRICOES = new LinkedHashMap<Integer, String[]>() {{ 
		put(PASTA_DOWNLOAD_SAPIENS, new String[] { "Pasta de download dos arquivos do Sapiens", "" });
		put(PASTA_DESTINO_PROCESSOS_SAPIENS, new String[] { "Pasta de destino dos processos do Sapiens (após identificação dos municípios)", "" });
		put(ENDERECO_SEI, new String[] { "Endereço do SEI", "https://sei.fazenda.gov.br/sei/inicializar.php" });
		put(ENDERECO_SAPIENS, new String[] { "Endereço do Sapiens", "https://sapiens.agu.gov.br/login" });
		put(PASTA_DESPACHOS_SALVOS, new String[] { "Pasta de respostas impressas em PDF", "" });
		put(PASTA_PLANILHA_IMPORTACAO, new String[] { "Pasta da planilha de importação", "" });
		put(DEFAULT_BROWSER, new String[] { "Navegador padrão", "Chrome" });
		put(ENDERECO_SPUNET, new String[] { "Endereço do SPUNet", "http://spunet.planejamento.gov.br" });
		put(PASTA_ARQUIVOS_PROCESSOS_INDIVIDUAIS, new String[] { "Pasta de arquivos para anexar aos processos individuais", "" });
		put(UNIDADE_PADRAO_SEI, new String[] { "Unidade padrão ao acessar o SEI", "SPU-MG-NUSUC" });
		put(BAIXAR_TODO_PROCESSO_SAPIENS, new String[] { "Baixar todo o processo do Sapiens", "Não" });
		put(TEMPO_ESPERA, new String[] { "Tempo de espera para verificação (em segundos)", "0" });
		put(RECEBER_PROCESSO_SEM_ARQUIVO, new String[] { "Receber processo do Sapiens sem arquivo baixado por excesso de tentativas", "Não" });
	}};

	public static String obterDescricao(int parametroId) {
		return DESCRICOES.get(parametroId)[0];
	}

	public static String obterValorDefault(int parametroId) {
		return DESCRICOES.get(parametroId)[1];
	}

	@Id
	private Integer parametroId;

	private String descricao;

	private String conteudo;

	private Boolean ativo;

	public Parametro() {
	}
	
	public Parametro(Integer parametroId, String descricao, String conteudo, Boolean ativo) {
		this.parametroId = parametroId;
		this.descricao = descricao;
		this.conteudo = conteudo;
		this.ativo = ativo;
	}

	public Integer getParametroId() {
		return parametroId;
	}

	public void setParametroId(Integer parametroId) {
		this.parametroId = parametroId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public String getAtivoAsString() {
		if (getAtivo() == null) {
			return "";
		} else if (getAtivo()) {
			return "Sim";
		} else {
			return "Não";
		}
	}
}
