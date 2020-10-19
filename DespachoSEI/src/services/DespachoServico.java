package services;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.swing.JOptionPane;

import framework.components.MyComboBox;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Assinante;
import model.AssinanteTipoResposta;
import model.Destino;
import model.Municipio;
import model.MunicipioTipoResposta;
import model.Origem;
import model.Parametro;
import model.Solicitacao;
import model.SolicitacaoEnvio;
import model.SolicitacaoResposta;
import model.TipoImovel;
import model.TipoProcesso;
import model.TipoResposta;

public class DespachoServico {
	private EntityManager conexao;
	
	public DespachoServico(EntityManager conexao) {
		this.conexao = conexao;
	}

	public List<Solicitacao> obterSolicitacao(Integer solicitacaoId, Origem origem, TipoProcesso tipoProcesso, String numeroProcesso, String chaveBusca) throws Exception {
		return obterSolicitacao(solicitacaoId, origem, tipoProcesso, numeroProcesso, chaveBusca, null, null, null, null, null);
	}

	public List<Solicitacao> obterSolicitacao(Integer solicitacaoId, Origem origem, TipoProcesso tipoProcesso, String numeroProcesso, String chaveBusca, String autor, Municipio municipio, String cartorio, String endereco, String area) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select s from Solicitacao s ");
		sql.append("  join fetch s.origem o ");
		sql.append("  join fetch s.tipoProcesso tp ");
		sql.append("  left join fetch s.municipio m ");
		sql.append("  left join fetch m.municipioComarca mc ");
		sql.append("  left join fetch m.tipoResposta mtr ");
		sql.append("  left join fetch s.destino d ");
		sql.append("  left join fetch s.tipoImovel ti ");
		sql.append(" where 1 = 1");
		if (solicitacaoId != null) {
			sql.append(" and s.solicitacaoId = :solicitacaoId");
			parametros.put("solicitacaoId", solicitacaoId);
		} else {
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and o.origemId = :origemId");
				parametros.put("origemId", origem.getOrigemId());
			}
			if (tipoProcesso != null && tipoProcesso.getTipoProcessoId() != null) {
				sql.append(" and tp.tipoProcessoId = :tipoProcessoId");
				parametros.put("tipoProcessoId", tipoProcesso.getTipoProcessoId());
			}
			if (numeroProcesso != null) {
				sql.append(" and s.numeroProcesso = :numeroProcesso");
				parametros.put("numeroProcesso", numeroProcesso);
			}
			if (chaveBusca != null) {
				sql.append(" and s.chaveBusca = :chaveBusca");
				parametros.put("chaveBusca", chaveBusca);
			}
			if (municipio != null && municipio.getMunicipioId() != null) {
				sql.append(" and m.municipioId = :municipioId");
				parametros.put("municipioId", municipio.getMunicipioId());
			}
			if (autor != null) {
				sql.append(" and s.autor like :autor");
				parametros.put("autor", autor);
			}
			if (cartorio != null) {
				sql.append(" and coalesce(s.cartorio, '') like :cartorio");
				parametros.put("cartorio", cartorio);
			}
			if (endereco != null) {
				sql.append(" and coalesce(s.endereco, '') like :endereco");
				parametros.put("endereco", endereco);
			}
			if (area != null) {
				sql.append(" and coalesce(s.area, '') like :area");
				parametros.put("area", area);
			}
		}

		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}


	public List<SolicitacaoEnvio> obterSolicitacaoEnvio(Solicitacao solicitacao) throws Exception {
		return obterSolicitacaoEnvio(null, solicitacao, null, null, null, null, false);
	}

	public List<SolicitacaoEnvio> obterSolicitacaoEnvio(Integer solicitacaoEnvioId, Solicitacao solicitacao, Origem origem, String numeroProcesso, String dataHoraMovimentacao, Boolean arquivosProcessados, boolean somenteMunicipiosPreenchidos) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select se from SolicitacaoEnvio se ");
		sql.append("  join fetch se.solicitacao s ");
		sql.append("  join fetch s.origem o ");
		sql.append("  left join fetch s.municipio m ");
		sql.append(" where 1 = 1");
		if (solicitacaoEnvioId != null) {
			sql.append(" and se.solicitacaoEnvioId = :solicitacaoEnvioId");
			parametros.put("solicitacaoEnvioId", solicitacaoEnvioId);
		} else {
			if (solicitacao != null && solicitacao.getSolicitacaoId() != null) {
				sql.append(" and s.solicitacaoId = :solicitacaoId");
				parametros.put("solicitacaoId", solicitacao.getSolicitacaoId());
			}
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and o.origemId = :origemId");
				parametros.put("origemId", origem.getOrigemId());
			}
			if (numeroProcesso != null) {
				sql.append(" and s.numeroProcesso = :numeroProcesso");
				parametros.put("numeroProcesso", numeroProcesso);
			}
			if (dataHoraMovimentacao != null) {
				sql.append(" and se.dataHoraMovimentacao = :dataHoraMovimentacao");
				parametros.put("dataHoraMovimentacao", dataHoraMovimentacao);
			}
			if (arquivosProcessados != null) {
				sql.append(" and se.arquivosProcessados = :arquivosProcessados");
				parametros.put("arquivosProcessados", arquivosProcessados);
			}
			if (somenteMunicipiosPreenchidos) {
				sql.append(" and s.municipio is not null");
			}
		}

		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<SolicitacaoResposta> obterRespostasAGerar(Assinante assinante) throws Exception {
		List<SolicitacaoResposta> respostas = obterSolicitacaoResposta(null, null, false, null, assinante, null, null, true, false, false);
		return  respostas;
	}

	public List<SolicitacaoResposta> obterRespostasAImprimir(Boolean respostaImpressa, Boolean respostaNoBlocoAssinatura, Assinante assinante, Boolean tipoRespostaImprimirResposta, boolean pendentesImpressao, boolean pendentesRetiradaBloco) throws Exception {
		List<SolicitacaoResposta> respostas = obterSolicitacaoResposta(null, new Solicitacao() {{ setTipoProcesso(TipoProcesso.ELETRONICO); }}, respostaImpressa, respostaNoBlocoAssinatura, assinante, new TipoResposta() {{ setImprimirResposta(tipoRespostaImprimirResposta); }}, null, false, pendentesImpressao, pendentesRetiradaBloco);
		return  respostas;
	}

	public List<SolicitacaoResposta> obterSolicitacaoResposta(Integer solicitacaoRespostaId) throws Exception {
		return obterSolicitacaoResposta(solicitacaoRespostaId, null, null, null, null, null, null, false, false, false);
	}

	public List<SolicitacaoResposta> obterSolicitacaoResposta(Solicitacao solicitacao) throws Exception {
		return obterSolicitacaoResposta(null, solicitacao, null, null, null, null, null, false, false, false);
	}

	public List<SolicitacaoResposta> obterSolicitacaoResposta(Integer solicitacaoRespostaId, Solicitacao solicitacao, Boolean respostaImpressa, Boolean respostaNoBlocoAssinatura, Assinante assinante, 
			TipoResposta tipoResposta, String numeroDocumentoSEI, boolean pendentesGeracao, boolean pendentesImpressao, boolean pendentesRetiradaBlocoAssinatura) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select sr from SolicitacaoResposta sr ");
		sql.append("  join fetch sr.solicitacao s ");
		sql.append("  join fetch s.origem o ");
		sql.append("  join fetch s.tipoProcesso tp ");
		sql.append("  left join fetch s.municipio m ");
		sql.append("  left join fetch m.municipioComarca mc ");
		sql.append("  left join fetch s.destino d ");
		sql.append("  left join fetch s.tipoImovel ti ");
		sql.append("  left join fetch sr.tipoResposta tr ");
		sql.append("  left join fetch sr.assinante a ");
		sql.append("  left join fetch sr.assinanteSuperior sup ");
		sql.append(" where 1 = 1");
		if (solicitacaoRespostaId != null) {
			sql.append(" and sr.solicitacaoRespostaId = :solicitacaoRespostaId");
			parametros.put("solicitacaoRespostaId", solicitacaoRespostaId);
		} else {
			if (solicitacao != null) {
				if (solicitacao != null && solicitacao.getSolicitacaoId() != null) {
					sql.append(" and s.solicitacaoId = :solicitacaoId");
					parametros.put("solicitacaoId", solicitacao.getSolicitacaoId());
				} else {
					if (solicitacao.getOrigem() != null && solicitacao.getOrigem().getOrigemId() != null) {
						sql.append(" and o.origemId = :origemId");
						parametros.put("origemId", solicitacao.getOrigem().getOrigemId());
					}
					if (solicitacao.getTipoProcesso() != null && solicitacao.getTipoProcesso().getTipoProcessoId() != null) {
						sql.append(" and tp.tipoProcessoId = :tipoProcessoId");
						parametros.put("tipoProcessoId", solicitacao.getTipoProcesso().getTipoProcessoId());
					}
					if (solicitacao.getNumeroProcesso() != null) {
						sql.append(" and s.numeroProcesso = :numeroProcesso");
						parametros.put("numeroProcesso", solicitacao.getNumeroProcesso());
					}
				}
			}
			if (respostaImpressa != null) {
				sql.append(" and sr.respostaImpressa = :respostaImpressa");
				parametros.put("respostaImpressa", respostaImpressa);
			}
			if (respostaNoBlocoAssinatura != null) {
				sql.append(" and sr.respostaNoBlocoAssinatura = :respostaNoBlocoAssinatura");
				parametros.put("respostaNoBlocoAssinatura", respostaNoBlocoAssinatura);
			}
			if (assinante != null && assinante.getAssinanteId() != null) {
				sql.append(" and a.assinanteId = :assinanteId");
				parametros.put("assinanteId", assinante.getAssinanteId());
			}
			if (tipoResposta != null && tipoResposta.getImprimirResposta() != null) {
				sql.append(" and tr.imprimirResposta = :imprimirResposta");
				parametros.put("imprimirResposta", tipoResposta.getImprimirResposta());
			}
			if (numeroDocumentoSEI != null) {
				sql.append(" and sr.numeroDocumentoSEI = :numeroDocumentoSEI");
				parametros.put("numeroDocumentoSEI", numeroDocumentoSEI);
			}
			if (pendentesGeracao) {
				sql.append(" and coalesce(sr.numeroDocumentoSEI, '') = '' ");
				sql.append(" and sr.tipoResposta is not null ");
			}
			if (pendentesImpressao) {
				sql.append(" and coalesce(sr.numeroDocumentoSEI, '') <> '' ");
				sql.append(" and coalesce(sr.numeroProcessoSEI, '') <> '' ");
				sql.append(" and sr.dataHoraResposta is not null ");
			}
			if (pendentesRetiradaBlocoAssinatura) {
				sql.append(" and coalesce(sr.numeroDocumentoSEI, '') <> '' ");
				sql.append(" and coalesce(sr.numeroProcessoSEI, '') <> '' ");
				sql.append(" and sr.dataHoraImpressao is not null ");
			}
		}

		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<Origem> obterOrigem(Integer origemId, String descricao) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select o from Origem o ");
		sql.append(" where 1 = 1");
		if (origemId != null) {
			sql.append(" and origemId = :origemId");
			parametros.put("origemId", origemId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like :descricao");
				parametros.put("descricao", descricao);
			}
		}

		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<Parametro> obterParametro(Integer parametroId, String descricao) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select p from Parametro p where 1 = 1");
		if (parametroId != null) {
			sql.append(" and parametroId = :parametroId");
			parametros.put("parametroId", parametroId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like :descricao");
				parametros.put("descricao", descricao);
			}
		}

		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public String obterConteudoParametro(int parametroId) {
		return obterConteudoParametro(parametroId, null);
	}

	public String obterConteudoParametro(int parametroId, String valorDefault) {
		List<Parametro> parametros = null;

		try {
			parametros = obterParametro(parametroId, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		if (parametros == null || parametros.isEmpty()) {
			return valorDefault;
		} else {
			return MyUtils.entidade(parametros).getConteudo();
		}
	}

	public void salvarConteudoParametro(int parametroId, String conteudo) {
		Parametro parametro = null;
		try {
			parametro = MyUtils.entidade(obterParametro(parametroId, null));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter o parâmetro a ser gravado: \n\n" + e.getMessage());
			return;
		}

		if (parametro != null) {
			parametro.setConteudo(conteudo);
			JPAUtils.persistir(conexao, parametro);
		}
	}

	public List<AssinanteTipoResposta> obterAssinanteTipoResposta(Integer assinanteTipoRespostaId, Integer assinanteId, Integer tipoRespostaId) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select atr from AssinanteTipoResposta atr ");
		sql.append(" join fetch atr.assinante a ");
		sql.append(" join fetch atr.tipoResposta tr ");
		sql.append(" where 1 = 1");
		if (assinanteTipoRespostaId != null) {
			sql.append(" and atr.assinanteTipoRespostaId = :assinanteTipoRespostaId");
			parametros.put("assinanteTipoRespostaId", assinanteTipoRespostaId);
		} else {
			if (assinanteId != null) {
				sql.append(" and a.assinanteId = :assinanteId");
				parametros.put("assinanteId", assinanteId);
			}
			if (tipoRespostaId != null) {
				sql.append(" and tr.tipoRespostaId = :tipoRespostaId");
				parametros.put("tipoRespostaId", tipoRespostaId);
			}
		}

		sql.append(" order by a.nome, tr.descricao ");
		
		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<TipoProcesso> obterTipoProcesso(Integer tipoProcessoId, String descricao) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select tp from TipoProcesso tp where 1 = 1");
		if (tipoProcessoId != null) {
			sql.append(" and tipoProcessoId = :tipoProcessoId");
			parametros.put("tipoProcessoId", tipoProcessoId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like :descricao");
				parametros.put("descricao", descricao);
			}
		}

		sql.append(" order by descricao ");

		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<TipoImovel> obterTipoImovel(Integer tipoImovelId, String descricao) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select ti from TipoImovel ti where 1= 1");
		if (tipoImovelId != null) {
			sql.append(" and tipoImovelId = :tipoImovelId");
			parametros.put("tipoImovelId", tipoImovelId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like :descricao");
				parametros.put("descricao", descricao);
			}
		}

		sql.append(" order by descricao ");
		
		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<TipoResposta> obterTipoResposta(Integer tipoRespostaId, String descricao, Boolean gerarProcessoIndividual) throws Exception {
		return obterTipoResposta(tipoRespostaId, descricao, gerarProcessoIndividual, null, false);
	}

	public List<TipoResposta> obterTipoResposta(Integer tipoRespostaId, String descricao, Boolean gerarProcessoIndividual, Origem origem, boolean incluirSemOrigem) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select tr from TipoResposta tr ");
		sql.append("  left join fetch tr.origem o ");
		sql.append(" where 1 = 1");
		if (tipoRespostaId != null) {
			sql.append(" and tr.tipoRespostaId = :tipoRespostaId");
			parametros.put("tipoRespostaId", tipoRespostaId);
		} else {
			if (descricao != null) {
				sql.append(" and tr.descricao like :descricao");
				parametros.put("descricao", descricao);
			}
			if (gerarProcessoIndividual != null) {
				sql.append(" and tr.gerarProcessoIndividual = :gerarProcessoIndividual");
				parametros.put("gerarProcessoIndividual", gerarProcessoIndividual);
			}
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and (o.origemId = :origemId");
				parametros.put("origemId", origem.getOrigemId());
				if (incluirSemOrigem) sql.append(" or tr.origem is null");
				sql.append(")");
			}
		}

		sql.append(" order by tr.descricao ");
		
		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<Municipio> obterMunicipio(Integer municipioId, String nome) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select m from Municipio m ");
		sql.append("  left join fetch m.municipioComarca mc ");
		sql.append("  left join fetch m.destino d ");
		sql.append("  left join fetch m.tipoResposta tr ");
		sql.append(" where 1 = 1");
		if (municipioId != null) {
			sql.append(" and m.municipioId = :municipioId");
			parametros.put("municipioId", municipioId);
		} else {
			if (nome != null) {
				sql.append(" and m.nome like :nome");
				parametros.put("nome", nome);
			}
		}

		sql.append(" order by m.nome ");
		
		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<MunicipioTipoResposta> obterMunicipioTipoResposta(Integer municipioTipoRespostaId, Municipio municipio, Origem origem, TipoResposta tipoResposta) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select mtr from MunicipioTipoResposta mtr ");
		sql.append(" join fetch mtr.municipio m ");
		sql.append(" join fetch mtr.origem o ");
		sql.append(" join fetch mtr.tipoResposta tr ");
		sql.append(" where 1 = 1 ");
		if (municipioTipoRespostaId != null) {
			sql.append(" and mtr.municipioTipoRespostaId = :municipioTipoRespostaId");
			parametros.put("municipioTipoRespostaId", municipioTipoRespostaId);
		} else {
			if (municipio != null && municipio.getMunicipioId() != null) {
				sql.append(" and m.municipioId = :municipioId");
				parametros.put("municipioId", municipio.getMunicipioId());
			}
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and o.origemId = :origemId");
				parametros.put("origemId", origem.getOrigemId());
			}
			if (tipoResposta != null && tipoResposta.getTipoRespostaId() != null) {
				sql.append(" and tr.tipoRespostaId = :tipoRespostaId");
				parametros.put("tipoRespostaId", tipoResposta.getTipoRespostaId());
			}
		}

		sql.append(" order by m.nome, o.descricao ");
		
		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<Assinante> obterAssinante(Integer assinanteId, String nome, Boolean superior, Boolean ativo) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select a from Assinante a where 1 = 1");
		if (assinanteId != null) {
			sql.append(" and assinanteId = :assinanteId");
			parametros.put("assinanteId", assinanteId);
		} else {
			if (nome != null) {
				sql.append(" and nome like :nome");
				parametros.put("nome", nome);
			}
			if (superior != null) {
				sql.append(" and superior = :superior");
				parametros.put("superior", superior);
			}
			if (ativo != null) {
				sql.append(" and ativo = :ativo");
				parametros.put("ativo", ativo);
			}
		}

		sql.append(" order by nome");
		
		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public List<Destino> obterDestino(Integer destinoId, String descricao, String abreviacao, Boolean usarCartorio, String municipio, String orderBy) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select d from Destino d where 1 = 1");
		if (destinoId != null) {
			sql.append(" and destinoId = :destinoId");
			parametros.put("destinoId", destinoId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like :descricao");
				parametros.put("descricao", descricao);
			}

			if (abreviacao != null) {
				sql.append(" and abreviacao like :abreviacao");
				parametros.put("abreviacao", abreviacao);
			}

			if (usarCartorio != null) {
				sql.append(" and usarCartorio = :usarCartorio");
				parametros.put("usarCartorio", usarCartorio);
			}

			if (municipio != null) {
				sql.append(" and destinoId = (select destinoId from Municipio where nome = :municipio)");
				parametros.put("municipio", municipio);
			}
		}

		if (orderBy == null) orderBy = "descricao";
		sql.append(" order by " + orderBy);
		
		return JPAUtils.executeQuery(conexao, sql.toString(), parametros);
	}

	public void preencherOpcoesTipoResposta(MyComboBox cbbTipoResposta, List<TipoResposta> opcoesIniciais, Origem origem) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<TipoResposta>();
			opcoesIniciais.addAll(obterTipoResposta(null, null, null, origem, true));
			MyUtils.insereOpcoesComboBox(cbbTipoResposta, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Tipo de Resposta: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesAssinante(MyComboBox cbbAssinante, List<Assinante> opcoesIniciais, Boolean superior, Boolean ativo) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Assinante>();
			opcoesIniciais.addAll(obterAssinante(null, null, superior, ativo));
			MyUtils.insereOpcoesComboBox(cbbAssinante, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Assinante: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesMunicipio(MyComboBox cbbMunicipio, List<Municipio> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Municipio>();
			opcoesIniciais.addAll(obterMunicipio(null, null));
			MyUtils.insereOpcoesComboBox(cbbMunicipio, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Município: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesDestino(MyComboBox cbbDestino, List<Destino> opcoesIniciais, Origem origem) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Destino>();
			opcoesIniciais.addAll(obterDestino(null, null, null, origem == null ? null : (origem.getOrigemId().equals(Origem.SAPIENS_ID) ? false : true), null, "abreviacao"));
			MyUtils.insereOpcoesComboBox(cbbDestino, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Destino: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesTipoImovel(MyComboBox cbbTipoImovel, List<TipoImovel> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<TipoImovel>();
			opcoesIniciais.addAll(obterTipoImovel(null, null));
			MyUtils.insereOpcoesComboBox(cbbTipoImovel, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Tipo de Imóvel: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesTipoProcesso(MyComboBox cbbTipoProcesso, List<TipoProcesso> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<TipoProcesso>();
			opcoesIniciais.addAll(obterTipoProcesso(null, null));
			MyUtils.insereOpcoesComboBox(cbbTipoProcesso, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Tipo de Processo: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesOrigem(MyComboBox cbbOrigem, List<Origem> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Origem>();
			opcoesIniciais.addAll(obterOrigem(null, null));
			MyUtils.insereOpcoesComboBox(cbbOrigem, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Origem: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public Solicitacao salvarSolicitacao(Solicitacao solicitacao) throws Exception {
		return JPAUtils.persistir(conexao, solicitacao);
	}

	public SolicitacaoEnvio salvarSolicitacaoEnvio(SolicitacaoEnvio solicitacaoEnvio) throws Exception {
		return JPAUtils.persistir(conexao, solicitacaoEnvio);
	}

	public void salvarSolicitacaoResposta(SolicitacaoResposta resposta) throws Exception {
		JPAUtils.persistir(conexao, resposta);
	}

	public void selecionarRespostaPadraoPorMunicipio(MyComboBox cbbTipoResposta, Municipio municipio, Origem origem) throws Exception {
		MunicipioTipoResposta municipioTipoResposta = MyUtils.entidade(obterMunicipioTipoResposta(null, municipio == null ? new Municipio(0) : municipio, origem, null));
		TipoResposta tipoRespostaPadrao = null;

		if (municipioTipoResposta != null) {
			tipoRespostaPadrao = municipioTipoResposta.getTipoResposta();
		} else {
			if (municipio != null && municipio.getTipoResposta() != null) {
				tipoRespostaPadrao = municipio.getTipoResposta();
			}
		}
		if (tipoRespostaPadrao != null) {
			cbbTipoResposta.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoResposta, tipoRespostaPadrao.getTipoRespostaId(), null));
		}
	}

	public List<SolicitacaoResposta> obterSolicitacaoRespostaPendente(Solicitacao solicitacao) throws Exception {
		List<SolicitacaoResposta> respostas = obterSolicitacaoResposta(null, solicitacao, null, null, null, null, null, false, false, false);
		Iterator<SolicitacaoResposta> i = respostas.iterator();

		while (i.hasNext()) {
			if (!MyUtils.emptyStringIfNull(i.next().getDataHoraResposta()).equals("")) {
				i.remove();
			}
		}

		return respostas;
	}

	public void selecionarAssinantePadrao(MyComboBox cbbAssinante) {
		String assinantePadrao = MyUtils.obterConfiguracaoLocal("assinantepadrao", "0");
		if (!assinantePadrao.equals("0")) {
			cbbAssinante.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbAssinante, Integer.parseInt(assinantePadrao),  null));
		}
	}

	public void salvarAssinantePadrao(Integer assinanteId) {
		MyUtils.salvarConfiguracaoLocal("assinantepadrao", assinanteId.toString(), "Assinante padrão salvo com sucesso!");
	}
}
