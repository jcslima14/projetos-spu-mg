import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
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
import model.Origem;
import model.Parametro;
import model.Solicitacao;
import model.TipoProcesso;

@SuppressWarnings("serial")
public class RespostaProcesso extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private DespachoServico despachoServico;

	public RespostaProcesso(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		despachoServico = new DespachoServico(conexao);
		
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
							responderProcessosSapiens(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), chkExibirNavegador.isSelected(), navegador);
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

	private void responderProcessosSapiens(JTextArea logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws Exception {
		Origem sapiens = MyUtils.entidade(despachoServico.obterOrigem(Origem.SAPIENS_ID, null));
        String pastaDespachosSalvos = MyUtils.emptyStringIfNull(despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS) + File.separator + sapiens.getDescricao());
        int tempoEsperaUpload = Integer.parseInt(despachoServico.obterConteudoParametro(Parametro.TEMPO_ESPERA));
        
        if (pastaDespachosSalvos.equals("") || !MyUtils.arquivoExiste(pastaDespachosSalvos)) {
        	JOptionPane.showMessageDialog(null, "A pasta onde devem estar gravados os arquivos PDF de resposta não está configurada ou não existe: " + pastaDespachosSalvos + ". \nConfigure a origem Sapiens (" + Origem.SAPIENS_ID + ") com o caminho para a pasta onde os arquivos PDF deve estar gravados.");
        	return;
        }

        MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		WebDriver driver = null;
		if (navegador.equalsIgnoreCase("chrome")) {
			ChromeOptions opcoes = new ChromeOptions();
			if (!exibirNavegador) {
				opcoes.setHeadless(true);
			}
			System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
	        driver = new ChromeDriver(opcoes);
		} else {
			FirefoxOptions opcoes = new FirefoxOptions();
			if (!exibirNavegador) {
				opcoes.setHeadless(true);
			}
			System.setProperty("webdriver.gecko.driver", MyUtils.firefoxWebDriverPath());
			driver = new FirefoxDriver(opcoes);
		}

        // acessando o endereço
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SAPIENS));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Actions passarMouse = new Actions(driver);

        Wait<WebDriver> wait15 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(15))
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

        Map<String, List<Object[]>> mapaArquivos = separarArquivosPorTipoFiltro(MyUtils.obterArquivos(pastaDespachosSalvos));
        
        // inicia o loop para leitura dos arquivos do diretório
        for (String tipoFiltro : mapaArquivos.keySet()) {
        	List<Object[]> listaArquivos = mapaArquivos.get(tipoFiltro);

            // clica na aba de ofícios
            WebElement abaOficios = MyUtils.encontrarElemento(wait15, By.xpath("//a[.//span[text() = 'Ofícios']]"));
            js.executeScript("arguments[0].scrollIntoView(true);", abaOficios);
            TimeUnit.MILLISECONDS.sleep(500);
            js.executeScript("arguments[0].click();", abaOficios);

        	for (Object[] objArquivo : listaArquivos) {
	        	MyUtils.esperarCarregamento(1000, wait5, "//div[text() = 'Carregando...']");

        		String chaveBusca = objArquivo[0].toString();
        		File arquivo = (File) objArquivo[1];

	        	String numeroProcesso = arquivo.getName().toLowerCase().replace(".pdf", "");

	        	MyUtils.appendLogArea(logArea, "Nº do Processo: " + numeroProcesso + " - Arquivo: " + arquivo.getAbsolutePath());

		        // clica no botão de filtro
		        WebElement cbcProcessoJudicial = MyUtils.encontrarElemento(wait5, By.xpath("//div[./span[text() = '" + tipoFiltro + "']]"));
	        	TimeUnit.SECONDS.sleep(1);
	            js.executeScript("arguments[0].scrollIntoView(true);", cbcProcessoJudicial);
	            js.executeScript("arguments[0].click();", cbcProcessoJudicial);
	            TimeUnit.MILLISECONDS.sleep(500);

		        WebElement btnExpandirMenu = MyUtils.encontrarElemento(wait5, By.xpath("//div[./span[text() = '" + tipoFiltro + "']]/div"));
	            js.executeScript("arguments[0].click();", btnExpandirMenu);
	            TimeUnit.MILLISECONDS.sleep(500);

		        WebElement divFiltro = MyUtils.encontrarElemento(wait5, By.xpath("//div[./a/span[text() = 'Filtros']]"));
	        	MyUtils.esperarCarregamento(1000, wait5, "//div[text() = 'Carregando...']");

		        passarMouse.moveToElement(divFiltro).build().perform();
		        WebElement iptPesquisar = MyUtils.encontrarElemento(wait5, By.xpath("//div[not(contains(@style, 'visibility: hidden')) and contains(@class, 'x-menu-plain')]//input[@type = 'text' and @role = 'textbox' and @data-errorqtip = '' and contains(@name, 'textfield')]"));
		        TimeUnit.MILLISECONDS.sleep(500);
		        iptPesquisar.clear();
		        iptPesquisar.sendKeys(chaveBusca);

		        boolean encontrado = false;
		        boolean inconsistente = false;
		        int qtOcorrencias = 0;

		        do {
		        	MyUtils.esperarCarregamento(2000, wait5, "//div[text() = 'Carregando...']");
		        	TimeUnit.SECONDS.sleep(1);

			        // após retorno da pesquisa, buscar tabela "//table[contains(@id, 'gridview')]"
			        List<WebElement> linhasRetornadas = MyUtils.encontrarElementos(wait15, By.xpath("//table[contains(@id, 'gridview')]/tbody/tr"));

			        // se não encontrou nenhum registro, sai do loop
					if (linhasRetornadas.size() == 0) {
						break;
					}

					encontrado = true;

					WebElement colID = linhasRetornadas.iterator().next().findElement(By.xpath("./td[1]/div"));
					WebElement colNUP = linhasRetornadas.iterator().next().findElement(By.xpath("./td[2]/div//a"));
					WebElement colNumeroProcessoJudicial = linhasRetornadas.iterator().next().findElement(By.xpath("./td[3]/div"));
					String nup = colNUP.getText().trim().replaceAll("\\D+", "");
					String numeroProcessoJudicial = colNumeroProcessoJudicial.getText().trim().replaceAll("\\D+", "");

					// se a linha retornada não corresponde à pesquisa, sai do loop
					if (tipoFiltro.equalsIgnoreCase("nup") && (!nup.startsWith(chaveBusca) || !colNUP.getAttribute("href").trim().toLowerCase().contains("nup=" + chaveBusca))) {
						MyUtils.appendLogArea(logArea, "O NUP retornado (" + nup + ") não corresponde à chave de busca pesquisada (" + chaveBusca + ")");
						inconsistente = true;
						break;
					}

					if (!numeroProcessoJudicial.startsWith(numeroProcesso)) {
						MyUtils.appendLogArea(logArea, "O Processo Judicial retornado (" + numeroProcessoJudicial + ") não corresponde ao processo contido no nome do arquivo (" + numeroProcesso + ")");
						inconsistente = true;
						break;
					}

					MyUtils.appendLogArea(logArea, "Respondendo ocorrência " + (++qtOcorrencias));

					passarMouse.moveToElement(colID).contextClick(colID).perform();
	
		        	TimeUnit.SECONDS.sleep(1);
	
					// clicar no botão responder
					WebElement divResponder = MyUtils.encontrarElemento(wait5, By.xpath("//div[./a/span[text() = 'Responder']]"));
					passarMouse.moveToElement(divResponder).click().build().perform();
		
		        	TimeUnit.SECONDS.sleep(1);
		
					// clicar no botão de upload de arquivos
					WebElement btnUploadArquivo = MyUtils.encontrarElemento(wait5, By.id("button_browse-button"));
					passarMouse.moveToElement(btnUploadArquivo).perform();
			
					WebElement inpUploadArquivo = MyUtils.encontrarElemento(wait5, By.xpath("//input[@type = 'file']"));
					inpUploadArquivo.sendKeys(arquivo.getAbsolutePath());
	
		        	TimeUnit.SECONDS.sleep(tempoEsperaUpload);
	
					WebElement btnConfirmarUpload = MyUtils.encontrarElemento(wait5, By.id("button_upload"));
					passarMouse.moveToElement(btnConfirmarUpload).click().build().perform();
	
					WebElement infUploadCompleto = null;
	
					do {
						infUploadCompleto = MyUtils.encontrarElemento(wait5, By.xpath("//tbody/tr/td[7]/div[text() = '100%']"));
					} while (infUploadCompleto == null);
	
		        	TimeUnit.SECONDS.sleep(1);
		
					WebElement btnFechar = MyUtils.encontrarElemento(wait5, By.xpath("//a[.//span[contains(text(), 'Fechar')]]"));
					passarMouse.moveToElement(btnFechar).click().build().perform();
					
					try {
						WebElement btnNao = MyUtils.encontrarElemento(wait5, By.xpath("//a[.//span[contains(@class, 'x-btn-inner') and text() = 'Não']]"));
						passarMouse.moveToElement(btnNao).click().build().perform();
					} catch (Exception e) {
					}

		        	MyUtils.esperarCarregamento(500, wait5, "//div[text() = 'Carregando...']");
		
					WebElement btnAtualizar = MyUtils.encontrarElemento(wait5, By.xpath("//a[@data-qtip = 'Atualizar']"));
					passarMouse.moveToElement(btnAtualizar).click().build().perform();

		        	MyUtils.esperarCarregamento(500, wait5, "//div[text() = 'Carregando...']");
		        } while (true);

		        if (!encontrado || inconsistente) {
		        	if (!encontrado) {
						MyUtils.appendLogArea(logArea, "O processo " + numeroProcesso + " não foi encontrado. Pesquisa pelo " + tipoFiltro + ": " + chaveBusca);
		        	}

		        	continue;
		        }
		        
				// mover o arquivo
	        	TimeUnit.MILLISECONDS.sleep(50);
		        MyUtils.criarDiretorioBackup(pastaDespachosSalvos);
		        String nomeArquivoBkp = pastaDespachosSalvos + File.separator + "bkp" + File.separator + arquivo.getName();
		        
		        MyUtils.renomearArquivo(arquivo.getAbsolutePath(), nomeArquivoBkp, 30, true);
	        }

        	// ao terminar o tipo de filtro, dar um refresh na página para limpar os filtros e reiniciar o processo para o segundo tipo de filtro
        	driver.navigate().refresh();
        }
		
        MyUtils.appendLogArea(logArea, "Fim do processamento...");
        // driver.close();
        driver.quit();
	}
	
	private Map<String, List<Object[]>> separarArquivosPorTipoFiltro(List<File> arquivos) throws Exception {
		Map<String, List<Object[]>> retorno = new LinkedHashMap<String, List<Object[]>>();

		for (File arquivo : arquivos) {
        	String numeroProcesso = arquivo.getName().toLowerCase().replace(".pdf", "");
        	String chaveBusca = numeroProcesso;

        	// tenta obter o número da chave de busca para o número do processo lido do nome do arquivo
        	Solicitacao solicitacao = MyUtils.entidade(despachoServico.obterSolicitacao(null, Origem.SAPIENS, TipoProcesso.ELETRONICO, numeroProcesso, null));

        	if (solicitacao != null && !solicitacao.getChaveBusca().trim().equals("")) {
        		chaveBusca = solicitacao.getChaveBusca().trim();
        	}

	        // determina o tipo de filtro de busca
        	String textoBotaoBusca = (chaveBusca.length() != 17 ? "Processo Judicial" : "NUP");
        	
        	if (retorno.get(textoBotaoBusca) == null) retorno.put(textoBotaoBusca, new ArrayList<Object[]>());
        	retorno.get(textoBotaoBusca).add(new Object[] { chaveBusca, arquivo });
		}

		return retorno;
	}
}
