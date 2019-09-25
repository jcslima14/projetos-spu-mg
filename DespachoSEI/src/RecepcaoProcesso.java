import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class RecepcaoProcesso extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private DespachoServico despachoServico;

	public RecepcaoProcesso(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.despachoServico = new DespachoServico(conexao);
		
		cbbNavegador.addItem("Chrome");
		cbbNavegador.addItem("Firefox");
		cbbNavegador.setSelectedItem(despachoServico.obterConteudoParametro(Parametro.DEFAULT_BROWSER, "Firefox"));

		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usu�rio:");
		JTextField txtUsuario = new JTextField(15);
		lblUsuario.setLabelFor(txtUsuario);

		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15);
		lblSenha.setLabelFor(txtSenha);

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = new JButton("Processar"); 
		JCheckBox chkExibirNavegador = new JCheckBox("Exibir nevagador", true);

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}

		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(lblNavegador);
		painelDados.add(cbbNavegador);
		painelDados.add(chkExibirNavegador);
		painelDados.add(new JPanel());
		painelDados.add(botaoProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
                espacoEmDisco == null ? 5 : 6, 2, //rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad
		
		add(painelDados, BorderLayout.WEST);
		JTextArea logArea = new JTextArea(30, 100);
		JScrollPane areaDeRolagem = new JScrollPane(logArea);
		add(areaDeRolagem, BorderLayout.SOUTH);

		botaoProcessar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				logArea.setText("");
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							String navegador = cbbNavegador.getSelectedItem().toString();
							despachoServico.salvarConteudoParametro(Parametro.DEFAULT_BROWSER, navegador);
							recepcionarProcessosSapiens(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), chkExibirNavegador.isSelected(), navegador);
						} catch (Exception e) {
							MyUtils.appendLogArea(logArea, "Erro ao processar a carga: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
							e.printStackTrace();
						}
					}

					private String stackTraceToString(Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						return sw.toString();
					}
				}).start();
			} 
		}); 
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void recepcionarProcessosSapiens(JTextArea logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws Exception {
		String pastaDeDownload = despachoServico.obterParametro(1, null).iterator().next().getConteudo();
		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		WebDriver driver = null;
		if (navegador.equalsIgnoreCase("chrome")) {
			ChromeOptions opcoes = new ChromeOptions();
			opcoes.setExperimentalOption("prefs", new LinkedHashMap<String, Object>() {{ 
				put("download.prompt_for_download", false); 
				put("download.default_directory", pastaDeDownload); 
				put("pdfjs.disabled", true); 
				put("plugins.always_open_pdf_externally", true);
				}});
			opcoes.addArguments("--disable-extensions");
			if (!exibirNavegador) {
				opcoes.setHeadless(true);
			}
			System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
	        driver = new ChromeDriver(opcoes);
		} else {
			FirefoxOptions opcoes = new FirefoxOptions();
			// FirefoxProfile perfil = new FirefoxProfile();
			opcoes.addPreference("browser.download.folderList", 2);
			opcoes.addPreference("browser.download.dir", pastaDeDownload);
			opcoes.addPreference("browser.download.useDownloadDir", true);
			opcoes.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf");
			opcoes.addPreference("browser.link.open_newwindow", 3);
			opcoes.addPreference("pdfjs.disabled", true);  // disable the built-in PDF viewer
			opcoes.addPreference("pdfjs.previousHandler.alwaysAskBeforeHandling", true);
			opcoes.addPreference("pdfjs.previousHandler.preferredAction", 4);
			opcoes.addPreference("pdfjs.enabledCache.state", false);
			if (!exibirNavegador) {
				opcoes.setHeadless(true);
			}
			System.setProperty("webdriver.gecko.driver", MyUtils.firefoxWebDriverPath());
			driver = new FirefoxDriver(opcoes);
		}

        // acessando o endere�o
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SAPIENS));
        Actions passarMouse = new Actions(driver);

        Wait<WebDriver> wait60 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(60))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        Wait<WebDriver> wait5 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(5))
        		.pollingEvery(Duration.ofSeconds(1))
        		.ignoring(NoSuchElementException.class);

        WebDriverWait waitUntil = new WebDriverWait(driver, 10);
        
        // Find the text input element by its name
        WebElement weUsuario = driver.findElement(By.xpath("//input[@name = 'username']"));
        waitUntil.until(ExpectedConditions.elementToBeClickable(weUsuario));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = driver.findElement(By.name("password"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = driver.findElement(By.xpath("//span[text() = 'Entrar']"));
        botaoAcessar.click();
        
        // verifica se foi aberto popup indesejado (fechar o popup)
        String primeiraJanela = "";
        for (String tituloJanela : driver.getWindowHandles()) {
        	if (!primeiraJanela.equalsIgnoreCase("")) {
        		driver.switchTo().window(tituloJanela);
        		driver.close();
        	} else {
        		primeiraJanela = tituloJanela;
        	}
        }

        driver.switchTo().window(primeiraJanela);

        // clica na aba de of�cios
        WebElement abaOficios = MyUtils.encontrarElemento(wait5, By.xpath("//a[.//span[text() = 'Of�cios']]"));
        passarMouse.moveToElement(abaOficios).click().build().perform();
        Thread.sleep(2000);
        abaOficios.click();
        StringBuilder mensagemNaoEncontrados = new StringBuilder("");

        int pagina = 0;
        
        apagaPastaDeDownloads(pastaDeDownload);

        while (true) {
    		MyUtils.esperarCarregamento(1000, wait5, "//div[text() = 'Carregando...']");

    		TimeUnit.SECONDS.sleep(2);

	        // obtem a lista de processos a ser lida
	        WebElement tabela = MyUtils.encontrarElemento(wait5, By.xpath("//div[@id = 'comunicacaoGrid-body']//table"));
	        List<WebElement> linhas = MyUtils.encontrarElementos(wait5, By.xpath("//div[@id = 'comunicacaoGrid-body']//table/tbody/tr[.//*[text() = 'SOLICITA��O DE SUBS�DIOS' or text() = 'REITERA��O DE SOLICITA��O DE SUBS�DIOS' or text() = 'COMPLEMENTA��O DE SOLICITA��O DE SUBS�DIOS']]"));
	        MyUtils.appendLogArea(logArea, "P�gina: " + ++pagina + " - Processos encontrados: " + linhas.size());

	        int nLinha = 0;

	        for (WebElement linha : linhas) {
	        	WebElement txtProcessoJudicial = null;
	        	String numeroProcessoJudicial = null;
	        	String chaveBusca = "";
	        	do {
		        	txtProcessoJudicial = linha.findElement(By.xpath("./td[3]/div"));
		        	passarMouse.moveToElement(txtProcessoJudicial).perform();
		        	numeroProcessoJudicial = txtProcessoJudicial.getText();
		        	if (numeroProcessoJudicial.equals("")) {
		        		((JavascriptExecutor) tabela).executeScript("window.scrollBy(0,100)", "");
		        	}
	        	} while (numeroProcessoJudicial.equals(""));
	        	WebElement lnkNumeroUnicoProcesso = null;
	        	try {
	        		lnkNumeroUnicoProcesso = linha.findElement(By.xpath("./td[2]//i[@data-qtip = 'Processo']//following-sibling::a"));
	        		chaveBusca = lnkNumeroUnicoProcesso.getText().trim();
	        	} catch (Exception e) {
	        		System.out.println("N� �nico do processo n�o encontrado");
	        	}
	        	// se o n�mero �nico de processo judicial n�o estiver preenchido, usa o NUP como chave
	        	if (numeroProcessoJudicial.trim().equals("")) {
	        		numeroProcessoJudicial = chaveBusca;
	        	}
        		String especie = linha.findElement(By.xpath("./td[4]/div")).getText().trim();
        		String dataHora = linha.findElement(By.xpath("./td[7]/div")).getText();
        		numeroProcessoJudicial = numeroProcessoJudicial.split("\\(")[0].trim();
	        	String numeroSemFormatacao = numeroProcessoJudicial.replaceAll("\\D+", "");
	        	chaveBusca = chaveBusca.replaceAll("\\D+", "");
	        	MyUtils.appendLogArea(logArea, ++nLinha + ") Processo: " + numeroProcessoJudicial + " (" + numeroSemFormatacao + ") - " + chaveBusca + " - " + especie);
	        	// if (!chaveBusca.equals("00476001410201908")) continue;

    			// verifica se o processo j� foi recepcionado com esta data de movimenta��o; se j� tiver sido, n�o precisa ser reprocessado
        		if (!processoJaRecebido(logArea, numeroSemFormatacao, dataHora)) {
    				lnkNumeroUnicoProcesso.click();

		        	TimeUnit.SECONDS.sleep(1);
	
		        	String janelaAtual = driver.getWindowHandle();
		        	String janelaAberta = "";
	
		        	for (String janela : driver.getWindowHandles()) {
		        		janelaAberta = janela;
		        		driver.switchTo().window(janelaAberta);
		        	}

		        	// busca o nome do autor da solicita��o
		        	String autor = null;
		        	try {
		        		autor = MyUtils.encontrarElemento(wait60, By.xpath("//div[@id = 'dadosInteressadosFC-innerCt']//table[contains(@class, 'x-grid-table')]/tbody/tr[./td/div[text() = 'REQUERENTE (P�LO ATIVO)']]/td[2]")).getText();
		        	} catch (Exception e) {
		        		autor = "";
		        	}

	        		boolean encontrouRemessaDocumentos = false;
	        		boolean isComplementacao = especie.startsWith("COMPLEMENTA��O");
	        		boolean encontrouComplementacao = !isComplementacao;

	        		List<String> juntadas = new ArrayList<String>();
	        		Integer seqAnterior = -1;

		        	do {
		        		MyUtils.esperarCarregamento(100, wait5, "//div[text() = 'Carregando...']");

		        		TimeUnit.SECONDS.sleep(1);

			        	List<WebElement> regDocumentos = MyUtils.encontrarElementos(wait5, By.xpath("//fieldset[@id = 'dadosDocumentosFC']//table[contains(@class, 'x-grid-table')]/tbody/tr"));
			        	for (WebElement regDocumento : regDocumentos) {
			        		// busca os dados a serem registrados
			        		String movimento = regDocumento.findElement(By.xpath("./td[3]")).getText();
			        		String dataHoraDocumento = regDocumento.findElement(By.xpath("./td[2]")).getText();
			        		Integer seqDocInicial = Integer.parseInt(regDocumento.findElement(By.xpath("./td[1]")).getText().split(" ")[0]);
			        		Integer seqDocFinal = null;
			        		if (seqAnterior.equals(-1)) {
			        			seqDocFinal = seqDocInicial.intValue() + regDocumento.findElements(By.xpath("./td[4]//i[@class = 'icon-link']")).size();
			        		} else {
			        			seqDocFinal = seqAnterior.intValue() - 1;
			        		}

			        		seqAnterior = seqDocInicial;
			        		
			        		// busca os sequencias da remessa de comunica��o referente � esp�cie da solicita��o e a remessa seguinte de solicita��o de subs�dios ou reitera��o
			        		if ((movimento.toUpperCase().startsWith(especie + " - REMESSA DE COMUNICA��O (") && dataHoraDocumento.startsWith(dataHoraDocumento))
			        				|| (isComplementacao 
			        						&& encontrouRemessaDocumentos 
			        						&& (movimento.toUpperCase().startsWith("SOLICITA��O DE SUBS�DIOS - REMESSA DE COMUNICA��O") || movimento.toUpperCase().startsWith("REITERA��O DE SOLICITA��O DE SUBS�DIOS - REMESSA DE COMUNICA��O")))) {
			        			// adiciona a faixa de documentos encontrados aos que devem ser baixados
			        			juntadas.add(seqDocInicial.toString() + "-" + seqDocFinal.toString());
			        			if (movimento.toUpperCase().startsWith(especie + " - REMESSA DE COMUNICA��O (") && dataHoraDocumento.startsWith(dataHoraDocumento)) {
			        				encontrouRemessaDocumentos = true;
			        			} else {
			        				encontrouComplementacao = true;
			        			}
			        		}

			        		if (encontrouRemessaDocumentos && encontrouComplementacao) break;
			        	} // fim do loop das linhas de documentos

		        		if (encontrouRemessaDocumentos && encontrouComplementacao) break;

		        		// se ainda existem p�ginas a ler, busca a pr�xima p�gina
				        try {
				        	WebElement btnProximaPagina = MyUtils.encontrarElemento(wait5, By.xpath("//fieldset[@id = 'dadosDocumentosFC']//a[@data-qtip = 'Pr�xima P�gina' and not(contains(@class, 'x-item-disabled'))]"));
				        	passarMouse.moveToElement(btnProximaPagina).click().build().perform();
				        } catch (Exception e) {
					        break;
				        }
		        	} while (!encontrouRemessaDocumentos || !encontrouComplementacao);

		        	if (encontrouRemessaDocumentos && encontrouComplementacao) {
		        		// inicia o download dos documentos
        				preparaPastaProcesso(pastaDeDownload, numeroSemFormatacao, dataHora);

	        			// clica no �cone de download de arquivos
	        			WebElement btnDownloadDocumentos = MyUtils.encontrarElemento(wait5, By.xpath("//a[@data-qtip = 'Download dos Documentos']"));
	        			passarMouse.moveToElement(btnDownloadDocumentos	).click().build().perform();

	        			// encontra o objeto de marcar download parcial
	        			WebElement chkParcial = MyUtils.encontrarElemento(wait5, By.xpath("//div[contains(@id, 'edicaodownloadwindow')]//input[@type = 'button' and @role = 'checkbox']"));
	        			chkParcial.click();

	        			TimeUnit.SECONDS.sleep(1);

	        			// preenche o campo de quais documentos imprimir
	        			WebElement txtDocsAImprimir = MyUtils.encontrarElemento(wait5, By.xpath("//input[@name = 'parcial']"));

	        			txtDocsAImprimir.sendKeys(String.join(",", juntadas));

		        		boolean arquivosOk = false;
		        		String resultadoDownload = "";

		        		do {
		        			// clica no bot�o para gerar o PDF
		        			WebElement btnGerar = MyUtils.encontrarElemento(wait5, By.xpath("//a[./span/span/span[text() = 'Gerar']]"));
		        			passarMouse.moveToElement(btnGerar).click().build().perform();

			        		// ap�s clicar nos links, renomear os arquivos e atualizar as informa��es para processamento final dos arquivos
			        		arquivosOk = arquivosBaixadosERenomeados(logArea, 1, pastaDeDownload, numeroSemFormatacao, dataHora, isComplementacao);
		        		} while (!arquivosOk);

	        			// clica no bot�o fechar
	        			WebElement btnFechar = MyUtils.encontrarElemento(wait5, By.xpath("//a[./span/span/span[text() = 'Fechar']]"));
	        			passarMouse.moveToElement(btnFechar).click().build().perform();

		        		receberProcessoSapiens(numeroSemFormatacao, chaveBusca, autor, dataHora, resultadoDownload);
	        		} else {
			        	// se n�o encontrou a remessa ou a complementa��o documentos, grava log com esta informa��o
	        			mensagemNaoEncontrados.append("Processo: " + numeroProcessoJudicial + (!encontrouRemessaDocumentos ? " - Remessa n�o encontrada" : "") + (!encontrouComplementacao ? " - Complementa��o n�o encontrada" : "") + " - Data/Hora: " + dataHora + "\n");
	        		}

		        	driver.close();
		        	driver.switchTo().window(janelaAtual);
	        	}
	        }
	        
	        try {
	        	WebElement btnProximaPagina = MyUtils.encontrarElemento(wait5, By.xpath("//a[@data-qtip = 'Pr�xima P�gina' and not(contains(@class, 'x-item-disabled'))]"));
	        	passarMouse.moveToElement(btnProximaPagina).click().build().perform();
	        } catch (Exception e) {
		        break;
	        }
        }

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
        driver.quit();

        if (!mensagemNaoEncontrados.toString().equals("")) {
        	JOptionPane.showMessageDialog(null, "ATEN��O: para um ou mais processos n�o foi poss�vel localizar e baixar os documentos. \n Isso pode acontecer porque a internet est� lenta ou algum outro problema fora da escopo do rob�. \n Em geral, basta apenas executar novamente o processamento para que os documenetos sejam lidos. \n\n" + mensagemNaoEncontrados.toString());
        }
	}

	private boolean processoJaRecebido(JTextArea logArea, String numeroProcesso, String dataHoraMovimentacao) throws Exception {
		List<SolicitacaoEnvio> processos = despachoServico.obterSolicitacaoEnvio(null, null, Origem.SAPIENS, numeroProcesso, MyUtils.formatarData(MyUtils.obterData(dataHoraMovimentacao, "dd-MM-yyyy HH:mm"), "yyyy-MM-dd HH:mm:ss"), null, false);
		if (processos == null || processos.isEmpty()) {
			MyUtils.appendLogArea(logArea, "Processo ainda n�o recebido...");
			return false;
		} else {
			return true;
		}
	}

	private void receberProcessoSapiens(String numeroProcesso, String chaveBusca, String autor, String dataHoraMovimentacao, String resultadoDownload) throws Exception {
		String dataHoraFormatada = MyUtils.formatarData(MyUtils.obterData(dataHoraMovimentacao, "dd-MM-yyyy HH:mm"), "yyyy-MM-dd HH:mm:ss");
		Solicitacao solicitacao = MyUtils.entidade(despachoServico.obterSolicitacao(null, Origem.SAPIENS, TipoProcesso.ELETRONICO, numeroProcesso, null));

		// se a solicita��o j� existe, atualiza o nome do autor; se n�o, cria uma nova solicita��o que ser� gravada
		if (solicitacao != null) {
			solicitacao.setAutor(autor);
		} else {
			solicitacao = new Solicitacao(Origem.SAPIENS, TipoProcesso.ELETRONICO, numeroProcesso, chaveBusca, autor);
		}

		solicitacao = despachoServico.salvarSolicitacao(solicitacao);
		despachoServico.salvarSolicitacaoEnvio(new SolicitacaoEnvio(null, solicitacao, dataHoraFormatada, resultadoDownload, false, null));
	}

	private void apagaPastaDeDownloads(String caminho) {
		File pasta = new File(caminho);
		for (File arquivo : pasta.listFiles()) {
			if (!arquivo.isDirectory()) arquivo.delete();
		}
	}
	
	private void preparaPastaProcesso(String caminho, String processo, String dataHora) throws Exception {
		File diretorio = new File(caminho + "\\" + processo + " (" + MyUtils.formatarData(MyUtils.obterData(dataHora, "dd-MM-yyyy HH:mm"), "yyyyMMdd_HHmm") + ")");
		if (!diretorio.exists()) {
			diretorio.mkdir();
		} else {
			for (File arquivo : diretorio.listFiles()) {
				arquivo.delete();
			}
		}
	}

	private boolean arquivosBaixadosERenomeados(JTextArea logArea, int quantArquivos, String caminho, String numeroProcesso, String dataHora, boolean isComplementacao) throws Exception {
		File pasta = null;
		String pastaProcesso = caminho + "\\" + numeroProcesso + " (" + MyUtils.formatarData(MyUtils.obterData(dataHora, "dd-MM-yyyy HH:mm"), "yyyyMMdd_HHmm") + ")";
		FilenameFilter filtro = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File arquivo = new File(dir, name);
				return (name != null && (name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith("application_pdf")) && arquivo.length() > 0);
			}
		};

		int quantArquivosBaixados = 0;
		int totalSegundos = 0;
		MyUtils.appendLogArea(logArea, "Aguardando o download dos arquivos...");

		String ultimaChave = "";
		int segundosDesdeUltimaAlteracao = 0;

		do {
			segundosDesdeUltimaAlteracao++;
			TimeUnit.SECONDS.sleep(1);
			pasta = new File(caminho);
			quantArquivosBaixados = pasta.listFiles(filtro).length;

			String chaveAtual = chaveArquivos(caminho);

			if (!chaveAtual.equals(ultimaChave)) {
				ultimaChave = chaveAtual;
				segundosDesdeUltimaAlteracao = 0;
			}

			if (totalSegundos++ % 5 == 0) MyUtils.appendLogArea(logArea, "- " + quantArquivosBaixados + "/" + quantArquivos + " j� baixados...");
		} while (quantArquivosBaixados != quantArquivos && segundosDesdeUltimaAlteracao < 150);

		// se atingiu o tempo limite sem ter completado o download, retorna falso para que se tente baixar novamente os arquivos
		if (quantArquivosBaixados != quantArquivos) {
			MyUtils.appendLogArea(logArea, "Atingido o tempo limite para tentar baixar os arquivos. Ser� feita uma nova tentativa...");
			apagaPastaDeDownloads(caminho);
			return false;
		}

		MyUtils.appendLogArea(logArea, "Todos os arquivos j� baixados. Renomeando-os e alocando-os �s suas pastas...");
		quantArquivosBaixados = 0;

		for (File arquivoBaixado : pasta.listFiles(filtro)) {
			arquivoBaixado.renameTo(new File(pastaProcesso + "\\" + numeroProcesso + (isComplementacao ? " --- COMPLEMENTA��O ---" : "") + " (" + ++quantArquivosBaixados + ").pdf"));
		}

		return true;
	}
	
	private String chaveArquivos(String caminho) {
		String retorno = "";
		File pasta = new File(caminho);
		File[] arquivos = pasta.listFiles();
		Arrays.sort(arquivos, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (File arquivo : arquivos) {
			if (!arquivo.isDirectory()) {
				retorno = retorno + arquivo.getName() + "-" + arquivo.length() + ";";
			}
		}
		
		return retorno;
	}
}
