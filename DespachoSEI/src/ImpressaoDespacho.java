import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import framework.ComboBoxItem;
import framework.MyComboBox;
import framework.MyComboBoxModel;
import framework.MyLabel;
import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ImpressaoDespacho extends JInternalFrame {

	private Connection conexao;
	private MyComboBox cbbAssinante = new MyComboBox();
	private MyLabel lblAssinante = new MyLabel("Assinado por");
	private JTextField txtUsuario = new JTextField(15);
	private JLabel lblUsuario = new JLabel("Usuário:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15);
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private DespachoServico despachoServico;

	public ImpressaoDespacho(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		opcoesAssinante();

		painelDados.add(lblAssinante);
		painelDados.add(cbbAssinante);
		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 
		
		SpringUtilities.makeGrid(painelDados,
	            4, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.WEST);
		add(areaDeRolagem, BorderLayout.SOUTH);

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					logArea.setText("");
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								imprimirRespostaSEI(txtUsuario.getText(), new String(txtSenha.getPassword()), MyUtils.idItemSelecionado(cbbAssinante));;
							} catch (Exception e) {
								MyUtils.appendLogArea(logArea, "Erro ao imprimir as respostas do SEI: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
								e.printStackTrace();
							}
						}

						private String stackTraceToString(Exception e) {
							StringWriter sw = new StringWriter();
							e.printStackTrace(new PrintWriter(sw));
							return sw.toString();
						}
					}).start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	private void opcoesAssinante() {
		cbbAssinante.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbAssinante, "select assinanteid, nome from assinante where superior = false order by nome", Arrays.asList(new ComboBoxItem(0, null, "(Todos)")));
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void imprimirRespostaSEI(String usuario, String senha, Integer assinanteId) throws Exception {
        String pastaRespostasImpressas = despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS);
        String msgValidacao = validarPastas(pastaRespostasImpressas);
        if (!msgValidacao.equals("")) {
        	JOptionPane.showMessageDialog(null, msgValidacao);
        	return;
        }

		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		ChromeOptions opcoes = new ChromeOptions();
		opcoes.setExperimentalOption("prefs", new LinkedHashMap<String, Object>() {{ put("download.prompt_for_download", false); put("download.default_directory", pastaRespostasImpressas); }});
		System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
        WebDriver driver = new ChromeDriver(opcoes);

        // And now use this to visit Google
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));

        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(60))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        Wait<WebDriver> wait5 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(60))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        // Find the text input element by its name
        WebElement weUsuario = driver.findElement(By.id("txtUsuario"));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = driver.findElement(By.id("pwdSenha"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = driver.findElement(By.id("sbmLogin"));
        botaoAcessar.click();

        // verifica se foi aberto popup indesejado (fechar o popup)
        MyUtils.fecharPopup(driver);

        // selecionar a unidade default
        MyUtils.selecionarUnidade(driver, wait, despachoServico.obterConteudoParametro(Parametro.UNIDADE_PADRAO_SEI));

		Map<String, List<SolicitacaoResposta>> respostasAImprimir = obterRespostasAProcessar(1, assinanteId);
		for (String numeroProcessoSEI : respostasAImprimir.keySet()) {
			// pesquisa o número do processo
			driver.switchTo().defaultContent();
			WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait5, By.name("txtPesquisaRapida"));
			txtPesquisaRapida.clear();
			txtPesquisaRapida.sendKeys(numeroProcessoSEI);
			txtPesquisaRapida.sendKeys(Keys.ENTER);

			// clicar em gerar documentos
			driver.switchTo().frame("ifrVisualizacao");
			WebElement btnGerarPDF = MyUtils.encontrarElemento(wait5, By.xpath("//img[@alt = 'Gerar Arquivo PDF do Processo']"));
			btnGerarPDF.click();

			// encontra a quantidade de registros aptos a serem impressos
			String quantidadeRegistros = MyUtils.encontrarElemento(wait5, By.xpath("//table[@id = 'tblDocumentos']/caption")).getText();
			quantidadeRegistros = quantidadeRegistros.split("\\(")[1];
			quantidadeRegistros = quantidadeRegistros.replaceAll("\\D+", "");

			do {
				List<WebElement> linhasAptas = MyUtils.encontrarElementos(wait, By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[./input[@type = 'checkbox']]]"));
				if (linhasAptas != null && linhasAptas.size() == Integer.parseInt(quantidadeRegistros)) {
					break;
				} else {
					TimeUnit.SECONDS.sleep(1);
				}
			} while (true);

			// clicar em selecionar tudo (precisa clicar 2x, pois o primeiro click marca todos (que já estão marcados) e o segundo desmarca tudo)
			WebElement btnDesmarcarTudo = MyUtils.encontrarElemento(wait, By.xpath("//img[@title = 'Selecionar Tudo']"));
			btnDesmarcarTudo.click();
			TimeUnit.SECONDS.sleep(1);
			btnDesmarcarTudo.click();

			for (SolicitacaoResposta respostaAImprimir : respostasAImprimir.get(numeroProcessoSEI)) {
				String numeroProcesso = respostaAImprimir.getSolicitacao().getNumeroProcesso();
				String nomeArquivoFinal;
				if (respostaAImprimir.getSolicitacao().getOrigem().getOrigemId().equals(Origem.SPUNET_ID) && !respostaAImprimir.getSolicitacao().getChaveBusca().equals("")) {
					nomeArquivoFinal = respostaAImprimir.getSolicitacao().getChaveBusca() + "-" + respostaAImprimir.getNumeroDocumentoSEI();
				} else {
					nomeArquivoFinal = numeroProcesso;
				}
				String numeroDocumentoSEI = respostaAImprimir.getNumeroDocumentoSEI();

				MyUtils.appendLogArea(logArea, "Processo: " + numeroProcesso + " - Nº Documento SEI: " + numeroDocumentoSEI);
	
				// encontra e marca o checkbox do documento
				WebElement chkSelecionarDocumento = null;
				try {
					chkSelecionarDocumento = MyUtils.encontrarElemento(wait5, By.xpath("//tr[./*/a[text() = '" + numeroDocumentoSEI + "']]/*/input[@class = 'infraCheckbox']"));
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (chkSelecionarDocumento != null) {
					// trecho para verificar se o documento possui a quantidade de assinaturas necessárias
					String janelaAtual = driver.getWindowHandle();
	
					WebElement lnkDocumento = MyUtils.encontrarElemento(wait5, By.xpath("//a[text() = '" + numeroDocumentoSEI + "']"));
					lnkDocumento.click();
					
					for (String janelaAberta : driver.getWindowHandles()) {
						driver.switchTo().window(janelaAberta);
					}

					// espera encontrar o fim do documento para verificar se a quantidade de assinaturas está correta
					MyUtils.encontrarElemento(wait, By.xpath("//p[contains(text(), 'Processo nº " + numeroProcessoSEI + "')]"));
					
					List<WebElement> assinaturas = MyUtils.encontrarElementos(wait5, By.xpath("//p[contains(text(), 'Documento assinado eletronicamente por')]"));
	
					driver.close();
					driver.switchTo().window(janelaAtual);
					driver.switchTo().defaultContent();
					driver.switchTo().frame("ifrVisualizacao");
	
					int quantidadeAssinaturas = respostaAImprimir.getTipoResposta().getQuantidadeAssinaturas();
					
					if (assinaturas.size() != quantidadeAssinaturas) {
						MyUtils.appendLogArea(logArea, "Para ser impresso, o documento precisa de " + quantidadeAssinaturas + " assinaturas. Este documento possui " + assinaturas.size() + " assinaturas.");
					} else {
						chkSelecionarDocumento.click();
	
						// apaga arquivo com o nome do processo, caso já exista
						apagarArquivoProcesso(pastaRespostasImpressas, numeroProcessoSEI);
	
						// gera o arquivo no diretório de downloads
						WebElement btnGerarDocumento = MyUtils.encontrarElemento(wait5, By.name("btnGerar"));
						btnGerarDocumento.click();
	
						String nomeArquivoPasta = respostaAImprimir.getSolicitacao().getOrigem().getPastaPDFResposta() + "\\" + nomeArquivoFinal + ".pdf";
						renomearArquivoProcesso(pastaRespostasImpressas, numeroProcessoSEI, nomeArquivoPasta);
	
						// atualiza o indicativo de que o documento foi impresso
						atualizarRespostaImpressa(respostaAImprimir, nomeArquivoPasta);
	
						chkSelecionarDocumento = MyUtils.encontrarElemento(wait5, By.xpath("//tr[./*/a[text() = '" + numeroDocumentoSEI + "']]/*/input[@class = 'infraCheckbox']"));
						chkSelecionarDocumento.click();
					}
				} else {
					MyUtils.appendLogArea(logArea, "Documento não encontrado ou não habilitado para geração em PDF");
				}
			} // fim do loop de leitura das respostas de cada processo
		} // fim do loop de diferentes processos com documentos a serem impressos

		// início da retirada dos documentos do bloco de assinatura
		MyUtils.appendLogArea(logArea, "Preparando para retirar os documentos dos blocos de assinatura...");

		Map<String, List<SolicitacaoResposta>> respostasARetirar = obterRespostasAProcessar(2, assinanteId);
		for (String blocoAssinatura : respostasARetirar.keySet()) {
			driver.switchTo().defaultContent();
			WebElement btnControleProcessos = MyUtils.encontrarElemento(wait5, By.id("lnkControleProcessos"));
			btnControleProcessos.click();

			WebElement btnBlocosAssinatura = MyUtils.encontrarElemento(wait5, By.xpath("//a[text() = 'Blocos de Assinatura']"));
			btnBlocosAssinatura.click();

			WebElement lnkBlocoAssinatura = null;
			
			try {
				lnkBlocoAssinatura = MyUtils.encontrarElemento(wait5, By.xpath("//table[@summary = 'Tabela de Blocos.']/tbody/tr/td/a[text() = '" + blocoAssinatura + "']"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (lnkBlocoAssinatura == null) {
				MyUtils.appendLogArea(logArea, "*** Não foi possível encontrar o bloco de assinatura " + blocoAssinatura + ".");
				continue;
			}

			lnkBlocoAssinatura.click();
			List<SolicitacaoResposta> respostasRetiradas = new ArrayList<SolicitacaoResposta>();

			for (SolicitacaoResposta respostaARetirar : respostasARetirar.get(blocoAssinatura)) {
				WebElement chkSelecaoLinha = null;
				try {
					chkSelecaoLinha = MyUtils.encontrarElemento(wait5, By.xpath("//table[@summary = 'Tabela de Processos/Documentos.']/tbody/tr[.//*[text() = '" + respostaARetirar.getNumeroDocumentoSEI() + "']]/td/input"));
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (chkSelecaoLinha != null) {
					MyUtils.appendLogArea(logArea, "Marcando para retirada o documento " + respostaARetirar.getNumeroDocumentoSEI());
					chkSelecaoLinha.click();
					respostasRetiradas.add(respostaARetirar);
				}
			}
			
			if (respostasRetiradas.size() > 0) {
				MyUtils.appendLogArea(logArea, "Retirando os documentos marcados...");
				WebElement btnExcluir = MyUtils.encontrarElemento(wait5, By.id("btnExcluir"));
				btnExcluir.click();
				TimeUnit.MILLISECONDS.sleep(500);
				driver.switchTo().alert().accept();
				TimeUnit.SECONDS.sleep(2);

				MyUtils.encontrarElemento(wait5, By.id("btnFechar"));

				MyUtils.appendLogArea(logArea, "Atualizando a situação dos documentos retirados...");
				for (SolicitacaoResposta respostaRetirada : respostasRetiradas) {
					atualizarRespostaRetiradaBlocoAssinatura(respostaRetirada);
				}
			}
		}
		
		MyUtils.appendLogArea(logArea, "Fim do Processamento...");

        driver.close();
        driver.quit();
	}

	private void apagarArquivoProcesso(String diretorioDespachos, String numeroProcessoSEI) {
		File arquivo = new File(diretorioDespachos + "\\" + "SEI_" + numeroProcessoSEI.replace("/", "_").replace("-", "_") + ".pdf");
		if (arquivo.exists()) {
			arquivo.delete();
		}
	}

	private void renomearArquivoProcesso(String diretorioDespachos, String numeroProcessoSEI, String arquivoRenomeado) throws Exception {
		int vezes = 0;
		while (vezes++ < 15) {
			TimeUnit.SECONDS.sleep(1);
			File arquivo = new File(diretorioDespachos + "\\" + "SEI_" + numeroProcessoSEI.replace("/", "_").replace("-", "_") + ".pdf");
			if (arquivo.exists() && arquivo.length() > 0) {
				File novoArquivo = new File(arquivoRenomeado);
				if (novoArquivo.exists()) novoArquivo.delete();
				arquivo.renameTo(novoArquivo);
				break;
			}
		};
	}

	private void atualizarRespostaImpressa(SolicitacaoResposta resposta, String nomeArquivo) throws Exception {
		if (MyUtils.arquivoExiste(nomeArquivo)) {
			StringBuilder sql = new StringBuilder("");
			sql.append("update solicitacaoresposta "
					 + "   set respostaimpressa = true "
					 + "	 , datahoraimpressao = datetime('now', 'localtime') "
					 + " where solicitacaorespostaid = " + resposta.getSolicitacaoRespostaId());
	
			MyUtils.execute(conexao, sql.toString());
		}
	}

	private void atualizarRespostaRetiradaBlocoAssinatura(SolicitacaoResposta resposta) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update solicitacaoresposta "
				 + "   set respostanoblocoassinatura = false "
				 + " where solicitacaorespostaid = " + resposta.getSolicitacaoRespostaId());

		MyUtils.execute(conexao, sql.toString());
	}

	// método para obter documentos: tipos de filtro: 1 - respostas não impressas; 2 - documentos de resposta a serem retirados do bloco de assinatura
	private Map<String, List<SolicitacaoResposta>> obterRespostasAProcessar(int tipoFiltro, Integer assinanteId) throws Exception {
		Map<String, List<SolicitacaoResposta>> retorno = new LinkedHashMap<String, List<SolicitacaoResposta>>();
		Boolean respostaImpressa = null;
		Boolean respostaNoBlocoAssinatura = null;
		Assinante assinante = (assinanteId.equals(0) ? null : new Assinante(assinanteId));

		if (tipoFiltro == 1) {
			respostaImpressa = false;
		} else {
			respostaImpressa = true;
			respostaNoBlocoAssinatura = true;
		}
		
		List<SolicitacaoResposta> respostas = despachoServico.obterRespostasAImprimir(respostaImpressa, respostaNoBlocoAssinatura, assinante, true);

		for (SolicitacaoResposta resposta : respostas) {
			String chave = (tipoFiltro == 1 ? resposta.getNumeroProcessoSEI() : resposta.getBlocoAssinatura());
			if (retorno.get(chave) == null) retorno.put(chave, new ArrayList<SolicitacaoResposta>());
			retorno.get(chave).add(resposta);
		}

		return retorno;
	}
	
	private String validarPastas(String pastaDownload) throws Exception {
		String retorno = "";

		if (pastaDownload.equals("") || !MyUtils.arquivoExiste(pastaDownload)) {
			retorno += "A pasta de download dos arquivos não está configurada ou não existe: " + pastaDownload + "\n";
			retorno += "Altere o parâmetro " + Parametro.PASTA_DESPACHOS_SALVOS + " com o caminho para a pasta onde os arquivos devem ser gerados antes de serem transferidos para a pasta final.\n\n";
		}

		for (Origem origem : despachoServico.obterOrigem(null, null)) {
			String pastaOrigem = MyUtils.emptyStringIfNull(origem.getPastaPDFResposta());

			if (pastaOrigem.equals("") || !MyUtils.arquivoExiste(pastaOrigem)) {
				retorno += "A pasta onde serão gravados os PDFs da origem " + origem.getDescricao() + " não está configurada ou não existe: " + pastaOrigem + "\n";
				retorno += "Configure a origem indicando a pasta onde os arquivos devem ser armazenados após terem sido gerados.\n\n";
			}
		}

		return retorno;
	}
}
