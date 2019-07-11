import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.time.Duration;
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

@SuppressWarnings("serial")
public class RecepcaoProcesso extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private DespachoServico despachoServico;
	private Connection conexao;

	public RecepcaoProcesso(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		this.despachoServico = new DespachoServico(conexao);
		
		cbbNavegador.addItem("Chrome");
		cbbNavegador.addItem("Firefox");
		cbbNavegador.setSelectedItem(despachoServico.obterConteudoParametro(Parametro.DEFAULT_BROWSER, "Firefox"));

		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usuário:");
		JTextField txtUsuario = new JTextField(15);
		lblUsuario.setLabelFor(txtUsuario);

		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15);
		lblSenha.setLabelFor(txtSenha);

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = new JButton("Processar"); 
		JButton botaoSair = new JButton("Sair");
		JCheckBox chkExibirNavegador = new JCheckBox("Exibir nevagador", true);

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
//		painelDados.add(painelArquivo);
//		painelDados.add(lblDiretorioDespachosSalvos);
		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(lblNavegador);
		painelDados.add(cbbNavegador);
		painelDados.add(chkExibirNavegador);
		painelDados.add(new JPanel());
		painelDados.add(botaoProcessar); 
		painelDados.add(botaoSair); 

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
							appendLogArea(logArea, "Erro ao processar a carga: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
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

		botaoSair.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				System.exit(0);
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
		appendLogArea(logArea, "Iniciando o navegador web...");
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
			System.setProperty("webdriver.chrome.driver", "./resources/chromedriver/chromedriver.exe");
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
			System.setProperty("webdriver.gecko.driver", "./resources/firefoxdriver/geckodriver.exe");
			driver = new FirefoxDriver(opcoes);
		}

        // acessando o endereço
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SAPIENS));
        Actions passarMouse = new Actions(driver);

        Wait<WebDriver> wait30 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(30))
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
        WebElement weSenha = driver.findElement(By.xpath("//input[@name = 'password']"));
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

        // clica na aba de ofícios
        WebElement abaOficios = MyUtils.encontrarElemento(wait5, By.xpath("//a[.//span[text() = 'Ofícios']]"));
        passarMouse.moveToElement(abaOficios).click().build().perform();
        Thread.sleep(2000);
        abaOficios.click();

        int pagina = 0;
        
        apagaPastaDeDownloads(pastaDeDownload);

        while (true) {
        	delayInSeconds(1);

	        WebElement infCarregando = null;
	        do {
		        infCarregando = MyUtils.encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
	        } while (infCarregando != null && infCarregando.isDisplayed());

	        delayInSeconds(2);

	        // obtem a lista de processos a ser lida
	        WebElement tabela = MyUtils.encontrarElemento(wait5, By.xpath("//table[.//*[text() = 'SOLICITAÇÃO DE SUBSÍDIOS' or text() = 'REITERAÇÃO DE SOLICITAÇÃO DE SUBSÍDIOS' or text() = 'COMPLEMENTAÇÃO DE SOLICITAÇÃO DE SUBSÍDIOS']]"));
	        List<WebElement> linhas = MyUtils.encontrarElementos(wait5, By.xpath("//table/tbody/tr[.//*[text() = 'SOLICITAÇÃO DE SUBSÍDIOS' or text() = 'REITERAÇÃO DE SOLICITAÇÃO DE SUBSÍDIOS' or text() = 'COMPLEMENTAÇÃO DE SOLICITAÇÃO DE SUBSÍDIOS']]"));
	        MyUtils.appendLogArea(logArea, "Página: " + ++pagina + " - Processos encontrados: " + linhas.size());

	        int nLinha = 0;
	        
	        for (WebElement linha : linhas) {
	        	boolean numeroProcessoInformado = true;
	        	WebElement txtProcessoJudicial = null;
	        	String numeroProcessoJudicial = null;
	        	do {
		        	txtProcessoJudicial = linha.findElement(By.xpath("./td[3]/div"));
		        	passarMouse.moveToElement(txtProcessoJudicial).perform();
		        	numeroProcessoJudicial = txtProcessoJudicial.getText();
		        	if (numeroProcessoJudicial.equals("")) {
		        		((JavascriptExecutor) tabela).executeScript("window.scrollBy(0,100)", "");
		        	}
	        	} while (numeroProcessoJudicial.equals(""));
	        	WebElement lnkProcessoJudicial = linha.findElement(By.xpath("./td[2]/*/a"));
	        	// se o número único de processo judicial não estiver preenchido, usa o NUP como chave
	        	if (numeroProcessoJudicial.trim().equals("")) {
	        		numeroProcessoJudicial = lnkProcessoJudicial.getText();
	        		numeroProcessoInformado = false;
	        	}
        		String especie = linha.findElement(By.xpath("./td[4]/div")).getText().trim();
        		String dataHora = linha.findElement(By.xpath("./td[7]/div")).getText();
	        	if (numeroProcessoJudicial.indexOf(" (") != -1) numeroProcessoJudicial = numeroProcessoJudicial.substring(0, numeroProcessoJudicial.indexOf(" ("));
	        	String numeroSemFormatacao = numeroProcessoJudicial.replace(".", "").replace("-", "").replace("/", "");
	        	MyUtils.appendLogArea(logArea, ++nLinha + ") Processo: " + numeroProcessoJudicial + " (" + numeroSemFormatacao + ")");
	        	// if (!lnkProcessoJudicial.getText().equals("00478.000807/2019-54")) continue;
	        	// clica no link para abrir a janela do processo

    			// verifica se o processo já foi recepcionado com esta data de movimentação; se já tiver sido, não precisa ser reprocessado
        		if (!processoJaRecebido(logArea, numeroProcessoJudicial, dataHora)) {
    				lnkProcessoJudicial.click();

		        	TimeUnit.SECONDS.sleep(1);
	
		        	String janelaAtual = driver.getWindowHandle();
		        	String janelaAberta = "";
	
		        	for (String janela : driver.getWindowHandles()) {
		        		janelaAberta = janela;
		        		driver.switchTo().window(janelaAberta);
		        	}
	
		        	txtProcessoJudicial = MyUtils.encontrarElemento(wait30, By.xpath("//td[.//*[contains(text(), '" + (numeroProcessoInformado ? "Número Único" : "NUP") + "')]]//following-sibling::td/div/a/b"));
		        	TimeUnit.SECONDS.sleep(2);
		        	
	        		boolean encontrouRemessaDocumentos = false;

		        	do {
		    	        do {
		    		        infCarregando = MyUtils.encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
		    	        } while (infCarregando != null && infCarregando.isDisplayed());

		    	        delayInSeconds(1);

			        	List<WebElement> regDocumentos = MyUtils.encontrarElementos(wait5, By.xpath("//fieldset[@id = 'dadosDocumentosFC']//table[contains(@class, 'x-grid-table')]/tbody/tr"));
			        	for (WebElement regDocumento : regDocumentos) {
			        		// busca os dados a serem registrados
			        		String movimento = regDocumento.findElement(By.xpath("./td[3]")).getText();
			        		String dataHoraDocumento = regDocumento.findElement(By.xpath("./td[2]")).getText();
	
			        		if (movimento.toUpperCase().startsWith(especie + " - REMESSA DE COMUNICAÇÃO (") && dataHoraDocumento.startsWith(dataHoraDocumento)) {
			        			encontrouRemessaDocumentos = true;
				        		List<WebElement> lnkArquivos = regDocumento.findElements(By.xpath("./td[4]/*/a"));
				        		boolean arquivosOk = false;
				        		String resultadoDownload = "";
		
				        		do {
					        		int quantArquivos = 0;
					        		int linkLido = 0;
			        				preparaPastaProcesso(pastaDeDownload, numeroSemFormatacao, dataHora);
					        		
					        		for (WebElement lnkArquivo : lnkArquivos) {
					        			linkLido ++;
					        			String nomeArquivo = lnkArquivo.getAttribute("data-qtip");
					        			String tipoArquivo = lnkArquivo.getText();
					        			MyUtils.appendLogArea(logArea, "Documento " + linkLido + "/" + lnkArquivos.size() + ": " + tipoArquivo + " (" + nomeArquivo + ")");
			
					        			if (nomeArquivo != null && (nomeArquivo.toLowerCase().endsWith(".pdf)") || nomeArquivo.toUpperCase().startsWith("OUTROS"))) {
					        				passarMouse.moveToElement(lnkArquivo).perform();
					        				boolean clicou = false;
					        				int tentativa = 1;
					        				int tentativas = 5;
					        				do {
						        				try {
						        					lnkArquivo.click();
						        					clicou = true;
						        					break;
						        				} catch (Exception e) {
						        					MyUtils.appendLogArea(logArea, "- A tentativa " + tentativa + "/" + tentativas + " de abrir o arquivo falhou...");
						        					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,75)", "");
						        				}
					        				} while (tentativa++ <= tentativas);
					        				if (clicou) {
						        				TimeUnit.SECONDS.sleep(3);
						        				if (driver.getWindowHandles().size() > 2) {
						        					driver.switchTo().window(driver.getWindowHandles().toArray()[2].toString());
						        					// se deu erro ao fechar a janela, indica que o Chrome fechou a aba que é rapidamente aberta quando se inicia o download
						        					try {
						        						driver.close();
						        						resultadoDownload += "- " + tipoArquivo + "(" + nomeArquivo + "): não é PDF \n";
									        			MyUtils.appendLogArea(logArea, "- O arquivo não é do tipo PDF...");
						        					} catch (Exception e) {
						        						quantArquivos ++;
						        					} finally {
							        					driver.switchTo().window(janelaAberta);
						        					}
						        				} else {
						        					resultadoDownload += "- " + tipoArquivo + "(" + nomeArquivo + "): download realizado \n";
							        				quantArquivos ++;
						        				}
					        				}
					        			} else {
					        				resultadoDownload += "- " + tipoArquivo + "(" + nomeArquivo + "): arquivo não elegível \n";
						        			MyUtils.appendLogArea(logArea, "- Arquivo não elegível para download (nome não possui extensão PDF e não é do tipo OUTROS)");
					        			}
					        		}
		
					        		// após clicar nos links, renomear os arquivos e atualizar as informações para processamento final dos arquivos
					        		arquivosOk = arquivosBaixadosERenomeados(logArea, quantArquivos, pastaDeDownload, numeroSemFormatacao, dataHora);
				        		} while (!arquivosOk);
	
				        		atualizarProcessoRecebido(numeroProcessoJudicial, dataHora, resultadoDownload);
	
				        		break;
			        		}
		        		}

		        		if (encontrouRemessaDocumentos) break;

				        try {
				        	WebElement btnProximaPagina = MyUtils.encontrarElemento(wait5, By.xpath("//fieldset[@id = 'dadosDocumentosFC']//a[@data-qtip = 'Próxima Página' and not(contains(@class, 'x-item-disabled'))]"));
				        	passarMouse.moveToElement(btnProximaPagina).click().build().perform();
				        } catch (Exception e) {
					        break;
				        }
		        	} while (!encontrouRemessaDocumentos);

		        	// se não encontrou a remessa de documentos, grava log com esta informação
		        	if (!encontrouRemessaDocumentos) {
		        		atualizarProcessoRecebido(numeroProcessoJudicial, dataHora, "Não foram encontrados os documentos da remessa do processo.");
		        	}
		        	
		        	driver.close();
		        	driver.switchTo().window(janelaAtual);
	        	}
	        }

	        try {
	        	WebElement btnProximaPagina = MyUtils.encontrarElemento(wait5, By.xpath("//a[@data-qtip = 'Próxima Página' and not(contains(@class, 'x-item-disabled'))]"));
	        	passarMouse.moveToElement(btnProximaPagina).click().build().perform();
	        } catch (Exception e) {
		        break;
	        }
        }
		
		appendLogArea(logArea, "Fim do processamento...");
        driver.quit();
	}

	private boolean processoJaRecebido(JTextArea logArea, String numeroProcessoJudicial, String dataHoraMovimentacao) throws Exception {
		List<ProcessoRecebido> processos = despachoServico.obterProcessoRecebido(numeroProcessoJudicial, null, MyUtils.formatarData(MyUtils.obterData(dataHoraMovimentacao, "dd-MM-yyyy HH:mm"), "yyyy-MM-dd HH:mm:ss"), false);
		if (processos == null || processos.isEmpty()) {
			MyUtils.appendLogArea(logArea, "Processo ainda não recebido...");
			return false;
		} else {
			return true;
		}
	}

	private void atualizarProcessoRecebido(String numeroProcessoJudicial, String dataHoraMovimentacao, String resultadoDownload) throws Exception {
		String dataHoraFormatada = MyUtils.formatarData(MyUtils.obterData(dataHoraMovimentacao, "dd-MM-yyyy HH:mm"), "yyyy-MM-dd HH:mm:ss");
		String sql = "";
		sql += "insert into processorecebido (numerounico, datahoramovimentacao, municipioid, arquivosprocessados, resultadodownload) ";
		sql += "select '" + numeroProcessoJudicial + "'";
		sql += "     , '" + dataHoraFormatada + "'";
		sql += "     , (select municipioid from processorecebido where numerounico = '" + numeroProcessoJudicial + "' order by datahoramovimentacao desc limit 1) ";
		sql += "	 , false ";
		sql += "     , " + (resultadoDownload.equals("") ? "null" : "'" + resultadoDownload.replace("'", "") + "'");
		sql += " where not exists (select 1 from processorecebido where numerounico = '" + numeroProcessoJudicial + "' and datahoramovimentacao = '" + dataHoraFormatada + "')";

		MyUtils.execute(conexao, sql);
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

	private void delayInSeconds(int tempo) throws Exception {
		TimeUnit.SECONDS.sleep(tempo);
	}

	private void appendLogArea(JTextArea logArea, String msg) {
		logArea.append(msg + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	private boolean arquivosBaixadosERenomeados(JTextArea logArea, int quantArquivos, String caminho, String numeroProcesso, String dataHora) throws Exception {
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

			if (totalSegundos++ % 5 == 0) MyUtils.appendLogArea(logArea, "- " + quantArquivosBaixados + "/" + quantArquivos + " já baixados...");
		} while (quantArquivosBaixados != quantArquivos && segundosDesdeUltimaAlteracao < 150);

		// se atingiu o tempo limite sem ter completado o download, retorna falso para que se tente baixar novamente os arquivos
		if (quantArquivosBaixados != quantArquivos) {
			MyUtils.appendLogArea(logArea, "Atingido o tempo limite para tentar baixar os arquivos. Será feita uma nova tentativa...");
			apagaPastaDeDownloads(caminho);
			return false;
		}

		MyUtils.appendLogArea(logArea, "Todos os arquivos já baixados. Renomeando-os e alocando-os às suas pastas...");
		quantArquivosBaixados = 0;

		for (File arquivoBaixado : pasta.listFiles(filtro)) {
			arquivoBaixado.renameTo(new File(pastaProcesso + "\\" + numeroProcesso + " (" + ++quantArquivosBaixados + ").pdf"));
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
