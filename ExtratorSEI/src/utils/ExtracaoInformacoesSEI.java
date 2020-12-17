package utils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import framework.utils.MyUtils;
import models.ProcessoAndamento;

public class ExtracaoInformacoesSEI {

	private Connection conexao;
	
	private WebDriver driver;
	
	private Wait<WebDriver> wait;
	
	private boolean carregarMarcadores;
	
	private JTextArea logArea;

	public ExtracaoInformacoesSEI(Connection conexao, WebDriver driver, boolean carregarMarcadores, JTextArea logArea) {
		this.conexao = conexao;
		this.driver = driver;
		this.carregarMarcadores = carregarMarcadores;
		this.logArea = logArea;
        wait = new FluentWait<WebDriver>(this.driver)
        		.withTimeout(Duration.ofSeconds(15))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);
	}

	public void extrairInformacoesProcesso(int ultimoSequencialGravado, ProcessoAndamento paProcesso, String unidadeSelecionada, ProcessoAndamento ultimoAndamentoGravado) throws Exception {
        String descricaoProcesso = "";
    	
        // clicar em consultar processo para buscar a descrição do processo
        driver.switchTo().frame(ExtratorSEIUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));

        if (ultimoSequencialGravado == 0) {
            WebElement consultarProcesso = null;
            try {
	            consultarProcesso = ExtratorSEIUtils.encontrarElemento(wait, By.xpath("//img[@title = 'Consultar/Alterar Processo' or @title = 'Consultar Processo']"));
            } catch (Exception e) {
            	e.printStackTrace();
            }

            if (consultarProcesso != null) {
            	consultarProcesso.click();

	            WebElement weDescricaoProcesso = ExtratorSEIUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtDescricao']"));
	            descricaoProcesso = weDescricaoProcesso.getAttribute("value").replace("'", "");
            }
        }

        // consultar o histórico de marcadores
        if (carregarMarcadores) {
            WebElement gerenciarMarcador = null;
            try {
	            gerenciarMarcador = ExtratorSEIUtils.encontrarElemento(wait, By.xpath("//img[@title = 'Gerenciar Marcador']"));
            } catch (Exception e) {
            	MyUtils.appendLogArea(logArea, "Não foi encontrado o botão de gerenciamento de marcadores...");
            	e.printStackTrace();
            }

            // clicar no botão de marcador e buscar os dados na tabela aberta
            if (gerenciarMarcador != null) {
            	gerenciarMarcador.click();

            	// lê a tabela de dados
            	TimeUnit.SECONDS.sleep(1);

            	WebElement tabelaHistorico = null;

            	try {
	            	tabelaHistorico = driver.findElement(By.xpath("//table/tbody"));
            	} catch (Exception e) {
            		MyUtils.appendLogArea(logArea, "Não foi encontrada a tabela de histórico de marcadores...");
            		e.printStackTrace();
            	}

	            if (tabelaHistorico != null) {
	            	int ultimoSeqMarcador = obterUltimoSequencialMarcador(conexao, paProcesso, unidadeSelecionada);
	            	List<WebElement> linhasTabelaHistorico = tabelaHistorico.findElements(By.xpath("./tr"));
	            	String titTabela = tabelaHistorico.findElement(By.xpath("//table/caption")).getText();
	            	titTabela = titTabela.substring(titTabela.indexOf("(") + 1);
	            	titTabela = titTabela.substring(0, titTabela.indexOf(" registro"));
	            	int seqMarcador = Integer.parseInt(titTabela);

	            	boolean cabecTabHist = true;
	            	for (WebElement linhaTabelaHistorico : linhasTabelaHistorico) {
	            		if (cabecTabHist) {
	            			cabecTabHist = false;
	            			continue;
	            		}

	            		if (seqMarcador == ultimoSeqMarcador) break;

	            		String dataHoraMarcador = linhaTabelaHistorico.findElement(By.xpath("./td[1]")).getText();
	            		WebElement usuarioMarcador = linhaTabelaHistorico.findElement(By.xpath("./td[2]/a"));
	            		String cpfUsuarioMarcador = usuarioMarcador.getText();
	            		String nomeUsuarioMarcador = usuarioMarcador.getAttribute("title");
	            		String tipoMarcador = linhaTabelaHistorico.findElement(By.xpath("./td[3]")).getText();
	            		String textoMarcador = linhaTabelaHistorico.findElement(By.xpath("./td[4]")).getText();
	            		
	            		MyUtils.appendLogArea(logArea, (seqMarcador) + ": Marcador --> Data Hora: " + dataHoraMarcador + " - Usuário: " + cpfUsuarioMarcador + "/" + nomeUsuarioMarcador + " - Tipo: " + tipoMarcador + " - Texto: " + textoMarcador);
	            		gravarMarcador(conexao, paProcesso, seqMarcador--, dataHoraMarcador, cpfUsuarioMarcador, nomeUsuarioMarcador, tipoMarcador, textoMarcador, unidadeSelecionada);
	            	}
	            }
            }
        }

        driver.switchTo().defaultContent();
        driver.switchTo().frame(ExtratorSEIUtils.encontrarElemento(wait, By.id("ifrArvore")));
        
        // clicar em consultar andamento
        WebElement consultarAndamento = ExtratorSEIUtils.encontrarElemento(wait, By.xpath("//a[@onclick = 'consultarAndamento();']"));
        
    	consultarAndamento.click();
    	consultarAndamento.click();

        driver.switchTo().defaultContent();
        driver.switchTo().frame(ExtratorSEIUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));
        
        List<WebElement> verHistoricoCompleto = driver.findElements(By.xpath("//a[contains(text(), 'Ver histórico completo')]"));
        if (verHistoricoCompleto.size() > 0) {
        	verHistoricoCompleto.iterator().next().click();
        }

        int sequencial = 0;
        int primeiroSequencialLido = 0;
        boolean refazerFluxoProcesso = false;
        String qtRegistros = "";
        boolean indicadorAndamentoAlterado = false;

        while (true) {
        	TimeUnit.SECONDS.sleep(1);

            WebElement tabela = ExtratorSEIUtils.encontrarElemento(wait, By.xpath("//table/tbody"));

            // na lista de andamentos, buscar os registros que interessam ao controle
        	if (sequencial == 0) {
            	qtRegistros = tabela.findElement(By.xpath("//table/caption")).getText();
            	qtRegistros = qtRegistros.substring(qtRegistros.indexOf("(") + 1);
            	qtRegistros = qtRegistros.substring(0, qtRegistros.indexOf(" registro"));
            	primeiroSequencialLido = Integer.parseInt(qtRegistros);
            	sequencial = primeiroSequencialLido;
        	}
            List<WebElement> linhasTabela = tabela.findElements(By.xpath("./tr"));

            boolean cabecalho = true;
            for (WebElement linhaTabela : linhasTabela) {
            	if (cabecalho) {
            		cabecalho = false;
            	} else {
            		if (sequencial > ultimoSequencialGravado || sequencial == ultimoAndamentoGravado.getSequencial()) {
	            		String andamentoDataHora = linhaTabela.findElement(By.xpath("./td[1]")).getText();
	            		String andamentoUnidade = linhaTabela.findElement(By.xpath("./td[2]")).getText();
	            		String andamentoDescricao = linhaTabela.findElement(By.xpath("./td[4]")).getText().replace("'", "");
	            		WebElement andamentoUsuario = linhaTabela.findElement(By.xpath("./td[3]"));
	            		String andamentoUsuarioCPF = andamentoUsuario.getText();

	            		// verifica se o andamento lido é igual ao último andamento gravado e verifica se eles são iguais; se não forem, grava a informação; em qualquer caso, termina a leitura
	            		if (sequencial == ultimoAndamentoGravado.getSequencial()) {
	            			if (!ultimoAndamentoGravado.getDataHora().equalsIgnoreCase(paProcesso.dataFormatada(andamentoDataHora))
        					 || !ultimoAndamentoGravado.getDescricao().equalsIgnoreCase(andamentoDescricao)
        					 || !ultimoAndamentoGravado.getUnidade().equalsIgnoreCase(andamentoUnidade)
        					 || !ultimoAndamentoGravado.getUsuario().equalsIgnoreCase(andamentoUsuarioCPF)) {
	            				MyUtils.appendLogArea(logArea, "*** Este processo está com o andamento alterado em relação à última leitura feita. Isto pode indicar que ele foi excluído e incluído novamente com o mesmo número");
	            				indicadorAndamentoAlterado = true;
	            				atualizarIndicadorAndamentoAlterado(conexao, paProcesso.getNumeroProcesso());
		            			
		            			break;
	            			}
	            		}

            			if (sequencial > ultimoSequencialGravado) {
		            		if (andamentoDescricao.toLowerCase().contains(unidadeSelecionada.toLowerCase()) || andamentoUnidade.equalsIgnoreCase(unidadeSelecionada) || andamentoDescricao.toLowerCase().startsWith("assinado documento ") || andamentoDescricao.toLowerCase().replace(" ", "").startsWith("tema:")) {
			            		String andamentoUsuarioNome = andamentoUsuario.findElement(By.xpath("./a")).getAttribute("title");

			            		List<String> listaAnexos = new ArrayList<String>();
			            		
		            			// se for envio de correspondência eletrônica, abre o e-mail para buscar os documentos enviados
			            		if (andamentoDescricao.toLowerCase().startsWith("envio de correspondência eletrônica")) {
			            			WebElement linkParaEmail = linhaTabela.findElement(By.xpath("./td[4]")).findElement(By.xpath("./a"));
			            			String janelaAndamentos = driver.getWindowHandle();
			            			linkParaEmail.click();
			            			// navega até a janela de e-mail
			            			for (String tituloJanela : driver.getWindowHandles()) {
			            				driver.switchTo().window(tituloJanela);
			            			}

			            			// obtém os documentos enviados
			    		            List<WebElement> nomesAnexos = driver.findElements(By.xpath("//a"));
			    		            
			    		            for (WebElement nomeAnexo : nomesAnexos) {
			    		            	listaAnexos.add(nomeAnexo.getText());
			    		            }
			            			
			            			// retorna para a janela de andamentos
			    		            driver.close();
			            			driver.switchTo().window(janelaAndamentos);
			        	            driver.switchTo().frame(ExtratorSEIUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));
			            		}

			            		refazerFluxoProcesso = true;
			            		ProcessoAndamento andamento = new ProcessoAndamento(null, paProcesso.getNumeroProcesso(), andamentoDataHora, sequencial, andamentoUnidade, andamentoUsuarioCPF, andamentoDescricao);
		            			gravarAndamento(conexao, andamento, descricaoProcesso, unidadeSelecionada, andamentoUsuarioNome, listaAnexos);

		            			MyUtils.appendLogArea(logArea, "Processando " + sequencial + " de " + primeiroSequencialLido + " até " + (ultimoSequencialGravado + 1) + " - Data: " + andamentoDataHora + " - Unidade: " + andamentoUnidade + " - Descrição: " + andamentoDescricao);
		            		} else {
		            			MyUtils.appendLogArea(logArea, "Registro " + sequencial + " de " + primeiroSequencialLido + " até " + (ultimoSequencialGravado + 1) + " ignorado");
		            		}
            			}
            		} // fim do teste para verificar se processa o andamento ou não

            		sequencial --;
            		if ((sequencial <= ultimoSequencialGravado && sequencial < ultimoAndamentoGravado.getSequencial()) || sequencial == 0) {
            			break;
            		}
            	} // fim do teste de cabeçalho
            } // fim do loop de andamentos da página

            if (indicadorAndamentoAlterado || ((sequencial <= ultimoSequencialGravado && sequencial < ultimoAndamentoGravado.getSequencial()) || sequencial == 0)) {
            	break;
            }

	        List<WebElement> proximaPagina = driver.findElements(By.xpath("//a[@id = 'lnkInfraProximaPaginaSuperior']"));

	        if (proximaPagina.size() == 0) {
	        	break;
	        } else {
	        	proximaPagina.iterator().next().click();
	        }
        } // fim do loop de andamentos do processo (percorreu todas as páginas de andamentos)

        if (refazerFluxoProcesso && !indicadorAndamentoAlterado) {
        	gerarFluxoProcesso(conexao, paProcesso, unidadeSelecionada);
        }
        
        if (!qtRegistros.equals("") && sequencial == ultimoSequencialGravado && primeiroSequencialLido != ultimoSequencialGravado) {
        	atualizarUltimoAndamentoGravado(conexao, paProcesso, unidadeSelecionada, primeiroSequencialLido);
        }
    	
        driver.switchTo().defaultContent();
	}

	private int obterUltimoSequencialMarcador(Connection conexao, ProcessoAndamento andamento, String unidade) throws Exception {
		String sql = "select max(sequencial) as ultimosequencial from processomarcador "
				   + " where processoid = " + andamento.sqlBuscaProcesso()
				   + "   and unidadeid = " + andamento.sqlBuscaUnidade(unidade);
		int sequencial = 0;

		Statement consulta = conexao.createStatement();
		ResultSet rs = consulta.executeQuery(sql);
		while (rs.next()) {
			sequencial = rs.getInt("ultimosequencial");
		}

		consulta.close();
		return sequencial;
	}

	private static void gravarMarcador(Connection conexao, ProcessoAndamento andamento, int sequencial, String dataHora, String cpfUsuario, String nomeUsuario, String tipoMarcador, String texto, String unidade) throws Exception {
		gravarUsuario(conexao, cpfUsuario, nomeUsuario);
		gravarTipoMarcador(conexao, tipoMarcador);
		String[] informacoesPadronizadas = obterInformacoesPadronizadas(texto);

		String sql = "";
		sql += "insert into processomarcador (processoid, unidadeid, sequencial, datahora, usuarioid, tipomarcadorid, texto" + (informacoesPadronizadas == null ? "" : ", detalhe, municipio, gravidade, urgencia, tendencia") + ")";
		sql += "select " + andamento.sqlBuscaProcesso();
		sql += "     , " + andamento.sqlBuscaUnidade(unidade);
		sql += "     , " + sequencial;
		sql += "	 , '" + andamento.dataFormatada(dataHora) + "'";
		sql += "     , " + andamento.sqlBuscaUsuario(cpfUsuario);
		sql += "     , (select tipomarcadorid from tipomarcador where descricao = '" + tipoMarcador + "')";
		sql += "	 , '" + texto.replace("'", "") + "'";
		if (informacoesPadronizadas != null) {
			sql += " , '" + informacoesPadronizadas[4].replaceAll("'", "''") + "'";
			sql += " , '" + informacoesPadronizadas[3].replaceAll("'", "''") + "'";
			sql += " , " + informacoesPadronizadas[0];
			sql += " , " + informacoesPadronizadas[1];
			sql += " , " + informacoesPadronizadas[2];
		}
		sql += " where not exists (select 1 from processomarcador where processoid = " + andamento.sqlBuscaProcesso() + " and unidadeid = " + andamento.sqlBuscaUnidade(unidade) + " and sequencial = " + sequencial + ")";

		Statement cmd = conexao.createStatement();
		try {
			cmd.execute(sql);
		} catch (Exception e) {
			System.out.println(sql);
			throw e;
		}
		cmd.close();
	}

	private void atualizarIndicadorAndamentoAlterado(Connection conexao, String numeroProcesso) throws Exception {
		String sql = "";
		sql += "update processo ";
		sql += "   set indicadorandamentoalterado = true ";
		sql += " where numeroprocesso = '" + numeroProcesso + "' ";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();
	}

	private static void gravarAndamento(Connection conexao, ProcessoAndamento andamento, String descricaoProcesso, String unidadeSelecionada, String nomeUsuario, List<String> listaAnexos) throws Exception {
		String sqlProcesso = "";
		sqlProcesso += "insert into processo (numeroprocesso, ultimosequencial, descricao, indicadorandamentoalterado)";
		sqlProcesso += "select '" + andamento.getNumeroProcesso() + "'";
		sqlProcesso += "     , 0";
		sqlProcesso += "	 , '" + descricaoProcesso + "'";
		sqlProcesso += "     , false";
		sqlProcesso += " where not exists (select 1 from processo where numeroprocesso = '" + andamento.getNumeroProcesso() + "')";
		
		String sqlProcessoUnidade = "";
		sqlProcessoUnidade += "insert into processounidade (processoid, unidadeid, ultimosequencial)";
		sqlProcessoUnidade += "select " + andamento.sqlBuscaProcesso();
		sqlProcessoUnidade += "	 , " + andamento.sqlBuscaUnidade(unidadeSelecionada);
		sqlProcessoUnidade += "     , 0";
		sqlProcessoUnidade += " where not exists (select 1 from processounidade where processoid = " + andamento.sqlBuscaProcesso() + " and unidadeid = " + andamento.sqlBuscaUnidade(unidadeSelecionada) + ")";
		
		String sqlAndamento = "";
		sqlAndamento += "insert into processoandamento (numeroprocesso, datahora, sequencial, unidade, usuario, descricao, desconsiderar) ";
		sqlAndamento += "select '" + andamento.getNumeroProcesso() + "'";
		sqlAndamento += "	  , '" + andamento.dataFormatada() + "'";
		sqlAndamento += "     , " + andamento.getSequencial();
		sqlAndamento += "	  , '" + andamento.getUnidade() + "'";
		sqlAndamento += "	  , '" + andamento.getUsuario() + "'";
		sqlAndamento += "	  , '" + andamento.getDescricao() + "'";
		sqlAndamento += "	  , false ";
		sqlAndamento += " where not exists (select 1 from processoandamento where numeroprocesso = '" + andamento.getNumeroProcesso() + "' and sequencial = " + andamento.getSequencial() + ")";

		gravarUsuario(conexao, andamento.getUsuario(), nomeUsuario);

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sqlProcesso);
		inclusao.execute(sqlProcessoUnidade);
		inclusao.execute(sqlAndamento);
		inclusao.close();

		
		for (String nomeAnexo : listaAnexos) {
			gravarComplementoAndamento(inclusao, andamento, nomeAnexo);
		}
	}

	private void gerarFluxoProcesso(Connection conexao, ProcessoAndamento processo, String unidade) throws Exception {
		List<ProcessoAndamento> andamentos = obterAndamentosProcesso(conexao, processo.getNumeroProcesso(), unidade);
		
		ProcessoAndamento registroInicio = null;
		String atribuidoPara = null;
		
		excluiTramitesDocumentos(conexao, processo, unidade);
		
		for (ProcessoAndamento andamento : andamentos) {
			// verifica se o processo entrou na unidade
			if (registroInicio == null && isProcessoEntrouUnidade(andamento, unidade)) {
				registroInicio = andamento.clone();
				gravarInicioTramite(conexao, andamento, unidade);
				continue;
			} else {
				if (registroInicio != null && isProcessoRecebidoUnidade(andamento, unidade, registroInicio)) {
					gravarRecebimentoTramite(conexao, andamento, unidade);
					registroInicio = andamento.clone();
					continue;
				}
			}

			// verifica se o processo foi enviado a outra unidade
			if (andamento.getDescricao().equalsIgnoreCase("Processo remetido pela unidade " + unidade)) {
				gravarFimTramite(conexao, andamento, unidade);
				if (!isProcessoConcluido(andamento, andamentos, unidade)) {
					gravarInicioTramite(conexao, andamento, unidade, registroInicio, atribuidoPara);
				}
				continue;
			}
			
			// verifica se o processo saiu da unidade
			if (registroInicio != null && isProcessoSaiuUnidade(andamento)) {
				gravarSaidaProcesso(conexao, andamento, unidade, registroInicio);
				registroInicio = null;
				atribuidoPara = null;
				continue;
			}
			
			// verifica se foi gerado algum documento
			if (andamento.getDescricao().toLowerCase().startsWith("gerado documento ")) {
				String[] dadosDocumento = obterDadosDocumento(andamento);
				gravarDocumentoGerado(conexao, andamento, unidade, dadosDocumento);
				continue;
			}
			
			// verifica se foi excluído algum documento
			if (isDocumentoExcluido(andamento)) {
				String numeroDocumentoSEI = andamento.getDescricao().split(" ")[andamento.getDescricao().toLowerCase().startsWith("cancelado documento") ? 2 : 3];
				gravarExclusaoDocumentoSEI(conexao, andamento, unidade, numeroDocumentoSEI);
				continue;
			}
			
			// verifica se o processo foi atribuído a alguém
			if (andamento.getDescricao().toLowerCase().startsWith("processo atribuído para ")) {
				atribuidoPara = andamento.getDescricao().substring(andamento.getDescricao().lastIndexOf(" ") + 1, andamento.getDescricao().length());
				gravarAtribuicaoProcesso(conexao, andamento, unidade, atribuidoPara);
				continue;
			}
			
			// verifica se foi retirada a atribuição do processo
			if (andamento.getDescricao().toLowerCase().equalsIgnoreCase("Removida atribuição do processo")) {
				atribuidoPara = null;
				gravarAtribuicaoProcesso(conexao, andamento, unidade, atribuidoPara);
				continue;
			}
		}
	}

	private static void atualizarUltimoAndamentoGravado(Connection conexao, ProcessoAndamento processo, String unidade, int primeiroSequencialLido) throws Exception {
		String sql = "";
		sql += "update processounidade ";
		sql += "   set ultimosequencial = " + primeiroSequencialLido;
		sql += " where processoid = " + processo.sqlBuscaProcesso();
		sql += "   and unidadeid = " + processo.sqlBuscaUnidade(unidade);

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();
	}

	private static void gravarUsuario(Connection conexao, String cpfUsuario, String nomeUsuario) throws Exception {
		String sql = "";
		sql += "insert into usuario (cpf, nome)";
		sql += "select '" + cpfUsuario + "'";
		sql += "	 , '" + nomeUsuario.replace("'", "") + "'";
		sql += " where not exists (select 1 from usuario where cpf = '" + cpfUsuario + "')";

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		cmd.close();
	}

	private static void gravarTipoMarcador(Connection conexao, String descricao) throws Exception {
		String sql = "";
		sql += "insert into tipomarcador (descricao)";
		sql += "select '" + descricao.replace("'", "") + "'";
		sql += " where not exists (select 1 from tipomarcador where descricao = '" + descricao + "')";

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		cmd.close();
	}

	private static void gravarComplementoAndamento(Statement cmd, ProcessoAndamento andamento, String nomeAnexo) throws Exception {
		String sql = "";
		sql += "insert into processoandamentocomplemento (numeroprocesso, sequencial, nomedocumento, numerodocumentosei) ";
		sql += "select '" + andamento.getNumeroProcesso() + "'";
		sql += "     , " + andamento.getSequencial();
		sql += "	 , '" + nomeAnexo + "'";
		sql += "	 , '" + extrairSequenciaNumerica(nomeAnexo) + "'";
		sql += " where not exists (select 1 from processoandamentocomplemento where numeroprocesso = '" + andamento.getNumeroProcesso() + "' and sequencial = " + andamento.getSequencial() + " and nomedocumento = '" + nomeAnexo + "')";

		cmd.execute(sql);
		cmd.close();
	}

	private static String[] obterInformacoesPadronizadas(String texto) {
		String[] campos = texto.split("\\|", -1);
		if (campos.length == 5) {
			campos[0] = extrairSequenciaNumerica(campos[0]);
			campos[1] = extrairSequenciaNumerica(campos[1]);
			campos[2] = extrairSequenciaNumerica(campos[2]);
			campos[3] = campos[3].trim();
			campos[4] = campos[4].trim();
			if (campos[0].equals("") || campos[1].equals("") || campos[2].equals("")) {
				campos = null;
			}
		} else {
			campos = null;
		}
		return campos;
	}

	private List<ProcessoAndamento> obterAndamentosProcesso(Connection conexao, String numeroProcesso, String unidade) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("select max(processoandamentoid) as processoandamentoid, numeroprocesso, datahora, max(sequencial) as sequencial, group_concat(distinct unidade) as unidade, usuario, descricao ");
		sql.append("  from processoandamento ");
		sql.append(" where numeroprocesso = '" + numeroProcesso + "' ");
		sql.append("   and (unidade = '" + unidade + "' ");
		sql.append("     or descricao like '%" + unidade + "%') ");
		sql.append("   and not desconsiderar ");
		sql.append(" group by numeroprocesso, datahora, usuario, descricao");
		sql.append(" order by sequencial ");
		
		Statement consulta = conexao.createStatement();
		ResultSet rs = consulta.executeQuery(sql.toString());

		List<ProcessoAndamento> retorno = new ArrayList<ProcessoAndamento>();
		
		while (rs.next()) {
			retorno.add(new ProcessoAndamento(rs.getInt("processoandamentoid"), rs.getString("numeroprocesso"), rs.getString("datahora"), rs.getInt("sequencial"), rs.getString("unidade"), rs.getString("usuario"), rs.getString("descricao")));
		}
		
		consulta.close();
		
		return retorno;
	}

	private void excluiTramitesDocumentos(Connection conexao, ProcessoAndamento processo, String unidade) throws SQLException {
		gravarExclusaoDocumentoSEI(conexao, processo, unidade, null);

		// exclui as destinações dos trâmites do processo
		String sql = "";
		sql += "delete from processotramiteenviado ";
		sql += " where processotramiteid in (select processotramiteid from processotramite ";
		sql += "							  where processoid = " + processo.sqlBuscaProcesso();
		sql += "							    and unidadeid = " + processo.sqlBuscaUnidade(unidade) + ")";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);

		// exclui os trâmites do processo
		sql = "";
		sql += "delete from processotramite ";
		sql += " where processoid = " + processo.sqlBuscaProcesso();
		sql += "   and unidadeid = " + processo.sqlBuscaUnidade(unidade);

		inclusao.execute(sql);
		inclusao.close();
	}

	private void gravarAtribuicaoProcesso(Connection conexao, ProcessoAndamento andamento, String unidade, String atribuidoPara) throws SQLException {
		// atribuir o processo ao usuário
		String sql = "";
		sql += "update processotramite ";
		sql += "   set usuarioidatribuido = " + andamento.sqlBuscaUsuario(atribuidoPara);
		sql += " where processoid = " + andamento.sqlBuscaProcesso();
		sql += "   and unidadeid = " + andamento.sqlBuscaUnidade(unidade);
		sql += "   and datafim is null ";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();
	}

	private void gravarExclusaoDocumentoSEI(Connection conexao, ProcessoAndamento andamento, String unidade, String numeroDocumentoSEI) throws SQLException {
		// exclui as assinturas do documento
		String sql = "";
		sql += "delete from processodocumentoassinado ";
		sql += " where processodocumentoid in (select processodocumentoid "
			+ "									 from processodocumento pd "
			+ "								    inner join processotramite pt on pd.processotramiteid = pt.processotramiteid "
			+ "								    where pt.processoid = " + andamento.sqlBuscaProcesso()
			+ "									  and pt.unidadeid = " + andamento.sqlBuscaUnidade(unidade);

		sql += (numeroDocumentoSEI == null ? "" : " and numerodocumentosei = '" + numeroDocumentoSEI + "'") + ")";
		
		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);

		// exclui os envios do documento
		sql = "";
		sql += "delete from processodocumentoenviado ";
		sql += " where processodocumentoid in (select processodocumentoid "
			+ "									 from processodocumento pd "
			+ "								    inner join processotramite pt on pd.processotramiteid = pt.processotramiteid "
			+ "								    where pt.processoid = " + andamento.sqlBuscaProcesso()
			+ "									  and pt.unidadeid = " + andamento.sqlBuscaUnidade(unidade);

		sql += (numeroDocumentoSEI == null ? "" : " and numerodocumentosei = '" + numeroDocumentoSEI + "'") + ")";
		
		inclusao.execute(sql);

		// excluir o documento gerado
		sql = "";
		sql += "delete from processodocumento ";
		sql += " where processotramiteid in (select processotramiteid from processotramite pt ";
		sql += "							  where pt.processoid = " + andamento.sqlBuscaProcesso();
		sql += "								and pt.unidadeid = " + andamento.sqlBuscaUnidade(unidade) + ") ";

		sql += numeroDocumentoSEI == null ? "" : " and numerodocumentosei = '" + numeroDocumentoSEI + "'";

		inclusao.execute(sql);
		inclusao.close();
	}

	private void gravarDocumentoGerado(Connection conexao, ProcessoAndamento andamento, String unidade, String[] dadosDocumento) throws SQLException {
		// grava o documento gerado
		String sql = "";
		sql += "insert into processodocumento (processotramiteid, datadocumento, numerodocumentosei, tipodocumento) values (";
		sql += "(select processotramiteid from processotramite where processoid = " + andamento.sqlBuscaProcesso() + " and unidadeid = " + andamento.sqlBuscaUnidade(unidade) + " and datafim is null), ";
		sql += "'" + andamento.getDataHora() + "', ";
		sql += "'" + dadosDocumento[0] + "', ";
		sql += "'" + dadosDocumento[1] + "') ";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);

		// insere as assinaturas do documento
		sql = "";
		sql += "insert into processodocumentoassinado (processodocumentoid, usuarioidassinado) ";
		sql += "select pd.processodocumentoid, ";
		sql += "       u.usuarioid ";
		sql += "  from processodocumento pd, processotramite pt, processoandamento pa, usuario u ";
		sql += " where pd.numerodocumentosei = '" + dadosDocumento[0] + "' ";
		sql += "   and pd.processotramiteid = pt.processotramiteid ";
		sql += "   and pt.processoid = " + andamento.sqlBuscaProcesso();
		sql += "   and pt.unidadeid = " + andamento.sqlBuscaUnidade(unidade);
		sql += "   and pa.descricao like 'assinado documento " + dadosDocumento[0] + "%' ";
		sql += "   and pa.numeroprocesso = '" + andamento.getNumeroProcesso() + "' ";
		sql += "   and not pa.desconsiderar ";
		sql += "   and u.cpf = substr(pa.descricao, -11) ";
		sql += "   and not exists (select 1 from processoandamento pa2 ";
		sql += "   	                where pa2.numeroprocesso = '" + andamento.getNumeroProcesso() + "' ";
		sql += "                      and pa2.descricao like 'cancelamento de assinatura do documento " + dadosDocumento[0] + "%' ";		
		sql += "                      and pa2.datahora >= pa.datahora ";
		sql += "                      and not pa2.desconsiderar) ";

		inclusao.execute(sql);
		
		// insere os envios do documento (quando por e-mail no SEI)
		sql =  "insert into processodocumentoenviado (processodocumentoid, datahoraenvio, usuarioidenvio, formaenvio) ";
		sql += "select distinct pd.processodocumentoid " + 
				"     , pa.datahora " + 
				"     , u.usuarioid " + 
				"     , 'SEI' " + 
				"  from processoandamentocomplemento pac " + 
				" inner join processodocumento pd using (numerodocumentosei) " + 
				" inner join processoandamento pa using (numeroprocesso, sequencial) " + 
				" inner join usuario u on pa.usuario = u.cpf " + 
				" where pd.numerodocumentosei = '" + dadosDocumento[0] + "' " +
				"   and not pa.desconsiderar ";

		inclusao.execute(sql);
		
		// insere os envios do documento (quando registrado no andamento: correio, spunet ou entrega em mãos)
		sql =  "insert into processodocumentoenviado (processodocumentoid, datahoraenvio, usuarioidenvio, formaenvio)" ; 
		sql += "select distinct pd.processodocumentoid" ; 
		sql += "     , pa.datahora" ;
		sql += "     , u.usuarioid" ; 
		sql += "     , case when descricao like 'correio%:%" + dadosDocumento[0] + "%' then 'Correio' else case when descricao like 'spunet%:%" + dadosDocumento[0] + "%' then 'SPUNET' else case when descricao like 'entrega%em%maos%:%" + dadosDocumento[0] + "%' or descricao like 'entrega%em%mãos%:%" + dadosDocumento[0] + "%' then 'Entrega em mãos' else '' end end end" ; 
		sql += "  from processoandamento pa" ;
		sql += " inner join processodocumento pd " ; 
		sql += " inner join usuario u on pa.usuario = u.cpf" ;
		sql += " where pd.numerodocumentosei = '" + dadosDocumento[0] + "'" ;
		sql += "   and (pa.descricao like 'correio%:%" + dadosDocumento[0] + "%' or pa.descricao like 'spunet%:%" + dadosDocumento[0] + "%' or pa.descricao like 'entrega%em%maos%:%" + dadosDocumento[0] + "%' or pa.descricao like 'entrega%em%mãos%:%" + dadosDocumento[0] + "%') ";
		sql += "   and not pa.desconsiderar ";

		inclusao.execute(sql);
		inclusao.close();
	}

	private void gravarSaidaProcesso(Connection conexao, ProcessoAndamento andamento, String unidade, ProcessoAndamento registroInicio) throws SQLException {
		// atualiza a saída do processo
		String sql = "";
		sql += "update processotramite ";
		sql += "   set datasaida = '" + andamento.getDataHora() + "' ";
		sql += "     , usuarioidsaida = coalesce(usuarioidsaida, case when '" + andamento.getDescricao() + "' = 'Conclusão do processo na unidade' then " + andamento.sqlBuscaUsuario() + " else usuarioidsaida end)";
		sql += "     , datafim = coalesce(datafim, '" + andamento.getDataHora() + "') ";
		sql += " where processoid = " + andamento.sqlBuscaProcesso();
		sql += "   and unidadeid = " + andamento.sqlBuscaUnidade(unidade);
		sql += "   and dataentrada = '" + registroInicio.getDataHora() + "' ";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();

		if (andamento.getDescricao().equalsIgnoreCase("Conclusão do processo na unidade")) {
			gravarDestinoSaida(conexao, andamento, unidade);
		}
	}

	private void gravarDestinoSaida(Connection conexao, ProcessoAndamento andamento, String unidade) throws SQLException {
		String sql;
		
		Statement inclusao = conexao.createStatement();
		
		for (String destinoSaida : andamento.unidades()) {
			sql = "";
			sql += "insert into processotramiteenviado (processotramiteid, destinosaida) ";
			sql += "select processotramiteid, '" + destinoSaida + "' ";
			sql += "  from processotramite ";
			sql += " where processoid = " + andamento.sqlBuscaProcesso();
			sql += "   and unidadeid = " + andamento.sqlBuscaUnidade(unidade);
			sql += "   and datafim = '" + andamento.getDataHora() + "'";

			inclusao.execute(sql);
		}
		
		inclusao.close();
	}

	private void gravarFimTramite(Connection conexao, ProcessoAndamento andamento, String unidade) throws SQLException {
		// atualiza o fim do trâmite
		String sql = "";
		sql += "update processotramite ";
		sql += "   set datafim = '" + andamento.getDataHora() + "' ";
		sql += "     , usuarioidsaida = " + andamento.sqlBuscaUsuario();
		sql += " where processoid = " + andamento.sqlBuscaProcesso();
		sql += "   and unidadeid = " + andamento.sqlBuscaUnidade(unidade);
		sql += "   and datafim is null ";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();

		// insere os destinos do processo
		gravarDestinoSaida(conexao, andamento, unidade);
	}

	private void gravarRecebimentoTramite(Connection conexao, ProcessoAndamento andamento, String unidade) throws SQLException {
		String sql = "";
		sql += "update processotramite ";
		sql += "   set dataentrada = '" + andamento.getDataHora() + "' ";
		sql += "     , usuarioidentrada = " + andamento.sqlBuscaUsuario();
		sql += "     , dataentrada = '" + andamento.getDataHora() + "' ";
		sql += " where processoid = " + andamento.sqlBuscaProcesso();
		sql += "   and unidadeid = " + andamento.sqlBuscaUnidade(unidade);
		sql += "   and datasaida is null ";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();
	}

	private void gravarInicioTramite(Connection conexao, ProcessoAndamento andamento, String unidade, ProcessoAndamento registroInicio, String usuarioAtribuido) throws SQLException {
		String sql = "";
		sql += "insert into processotramite (processoid, unidadeid, dataentrada, usuarioidentrada, datainicio, usuarioidatribuido) values (";
		sql += andamento.sqlBuscaProcesso() + ", ";
		sql += andamento.sqlBuscaUnidade(unidade) + ", ";
		sql += "'" + registroInicio.getDataHora() + "', ";
		sql += registroInicio.sqlBuscaUsuario() + ", ";
		sql += "'" + andamento.getDataHora() + "', ";
		sql += andamento.sqlBuscaUsuario(usuarioAtribuido) + ") ";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();
	}

	private void gravarInicioTramite(Connection conexao, ProcessoAndamento andamento, String unidade) throws SQLException {
		String sql = "";
		sql += "insert into processotramite (processoid, unidadeid, dataentrada, usuarioidentrada, datainicio) values (";
		sql += andamento.sqlBuscaProcesso() + ", ";
		sql += andamento.sqlBuscaUnidade(unidade) + ", ";
		sql += "'" + andamento.getDataHora() + "', ";
		sql += andamento.sqlBuscaUsuario() + ", ";
		sql += "'" + andamento.getDataHora() + "') ";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();
	}
	
	private static String extrairSequenciaNumerica(String nomeAnexo) {
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(nomeAnexo);
		if (matcher.find()) return matcher.group(1);
		else return "";
	}

	private boolean isDocumentoExcluido(ProcessoAndamento andamento) {
		if (andamento.getDescricao().toLowerCase().startsWith("exclusão do documento") || andamento.getDescricao().toLowerCase().startsWith("cancelado documento")) {
			return true;
		}
		return false;
	}

	private String[] obterDadosDocumento(ProcessoAndamento andamento) {
		String[] dadosDocumento = new String[] { null, null };
		
		// busca o número do documento no SEI
		dadosDocumento[0] = andamento.getDescricao().split(" ")[3];
		dadosDocumento[0] = dadosDocumento[0].replaceAll(",", "");

		// busca o primeiro parêntese para obter o tipo de documento
		int parentese = andamento.getDescricao().indexOf("(");
		if (parentese != -1) {
			dadosDocumento[1] = andamento.getDescricao().substring(parentese + 1, andamento.getDescricao().indexOf(")"));
		}
		
		return dadosDocumento;
	}
	
	private boolean isProcessoSaiuUnidade(ProcessoAndamento andamento) {
		if (andamento.getDescricao().equalsIgnoreCase("Conclusão automática de processo na unidade") || andamento.getDescricao().equalsIgnoreCase("Conclusão do processo na unidade")) {
			return true;
		}
		return false;
	}

	private boolean isProcessoConcluido(ProcessoAndamento andamento, List<ProcessoAndamento> andamentos, String unidade) {
		for (ProcessoAndamento a : andamentos) {
			if (a.getDataHora().equals(andamento.getDataHora()) &&
				a.getUnidade().contains(unidade) &&
				a.getDescricao().equalsIgnoreCase("Conclusão automática de processo na unidade")) {
				return true;
			}
		}
		return false;
	}

	private boolean isProcessoEntrouUnidade(ProcessoAndamento andamento, String unidade) {
		String andamentoDescricao = andamento.getDescricao();
		String andamentoUnidade = andamento.getUnidade(); 
		if (andamentoUnidade.equalsIgnoreCase(unidade) &&
			(andamentoDescricao.equalsIgnoreCase("Processo recebido na unidade") ||
			 andamentoDescricao.toLowerCase().startsWith("processo público gerado") ||
			 andamentoDescricao.toLowerCase().startsWith("processo restrito gerado") ||
			 andamentoDescricao.equalsIgnoreCase("Reabertura do processo na unidade") ||
			 andamentoDescricao.toLowerCase().startsWith("processo remetido pela unidade"))) {
			return true;
		}
		return false;
	}
	
	private boolean isProcessoRecebidoUnidade(ProcessoAndamento andamento, String unidade, ProcessoAndamento registroInicio) {
		if (andamento.getUnidade().equals(unidade) &&
			andamento.getDescricao().equalsIgnoreCase("Processo recebido na unidade") &&
			registroInicio.getDescricao().toLowerCase().startsWith("Processo remetido pela unidade")) {
			return true;
		}
		return false;
	}

	public int obterUltimoSequencialGravado(Connection conexao, ProcessoAndamento andamento, String unidade) throws Exception {
		String sql = "select ultimosequencial from processounidade "
				   + " where processoid = " + andamento.sqlBuscaProcesso()
				   + "   and unidadeid = " + andamento.sqlBuscaUnidade(unidade);
		int sequencial = 0;
		
		Statement consulta = conexao.createStatement();
		ResultSet rs = consulta.executeQuery(sql);
		while (rs.next()) {
			sequencial = rs.getInt("ultimosequencial");
		}
		
		consulta.close();
		
		return sequencial;
	}

	public ProcessoAndamento obterUltimoAndamentoGravado(Connection conexao, String numeroProcesso) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("select processoandamentoid, numeroprocesso, datahora, sequencial, unidade, usuario, descricao ");
		sql.append("  from processoandamento ");
		sql.append(" where numeroprocesso = '" + numeroProcesso + "' ");
		sql.append("  order by sequencial desc limit 1 ");
		
		Statement consulta = conexao.createStatement();
		ResultSet rs = consulta.executeQuery(sql.toString());

		ProcessoAndamento retorno = new ProcessoAndamento();
		retorno.setSequencial(0);
		
		while (rs.next()) {
			retorno = new ProcessoAndamento(rs.getInt("processoandamentoid"), rs.getString("numeroprocesso"), rs.getString("datahora"), rs.getInt("sequencial"), rs.getString("unidade"), rs.getString("usuario"), rs.getString("descricao"));
		}
		
		consulta.close();
		
		return retorno;
	}
}
