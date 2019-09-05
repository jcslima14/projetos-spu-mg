import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.openqa.selenium.By;
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
public class RespostaProcesso extends JInternalFrame {

	private JFileChooser filSelecionarDiretorio = new JFileChooser();
	private JButton btnAbrirJanelaSelecaoDiretorio = new JButton("Selecionar diret�rio");
	private JLabel lblDiretorioDespachosSalvos = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblSelecionarDiretorio = new JLabel("Diret�rio:", JLabel.TRAILING) {{ setLabelFor(filSelecionarDiretorio); }};
	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private DespachoServico despachoServico;

	public RespostaProcesso(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		despachoServico = new DespachoServico(conexao);
		
		cbbNavegador.addItem("Chrome");
		cbbNavegador.addItem("Firefox");
		cbbNavegador.setSelectedItem(despachoServico.obterConteudoParametro(Parametro.DEFAULT_BROWSER, "Firefox"));

		lblDiretorioDespachosSalvos.setText(despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS));
		JPanel painelArquivo = new JPanel() {{ add(lblSelecionarDiretorio); add(btnAbrirJanelaSelecaoDiretorio); }};

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
		JButton botaoSair = new JButton("Sair");
		JCheckBox chkExibirNavegador = new JCheckBox("Exibir nevagador", true);

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(painelArquivo);
		painelDados.add(lblDiretorioDespachosSalvos);
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
                espacoEmDisco == null ? 6 : 7, 2, //rows, cols
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

		botaoSair.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				System.exit(0);
			} 
		});

		btnAbrirJanelaSelecaoDiretorio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String diretorioPadrao = despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS);
				if (diretorioPadrao != null && !diretorioPadrao.trim().equals("")) {
					File dirPadrao = new File(diretorioPadrao);
					if (dirPadrao.exists()) {
						filSelecionarDiretorio.setCurrentDirectory(dirPadrao);
					}
				}
				filSelecionarDiretorio.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				filSelecionarDiretorio.setAcceptAllFileFilterUsed(false);
				int retorno = filSelecionarDiretorio.showOpenDialog(RespostaProcesso.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filSelecionarDiretorio.getSelectedFile().exists()) {
						lblDiretorioDespachosSalvos.setText(filSelecionarDiretorio.getSelectedFile().getAbsolutePath());
						if (diretorioPadrao == null || !diretorioPadrao.equals(filSelecionarDiretorio.getSelectedFile().getAbsolutePath())) {
							despachoServico.salvarConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS, filSelecionarDiretorio.getSelectedFile().getAbsolutePath());
						}
					}
				}
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

        // acessando o endere�o
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SAPIENS));
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

        // clica na aba de of�cios
        WebElement abaOficios = MyUtils.encontrarElemento(wait5, By.xpath("//a[.//span[text() = 'Of�cios']]"));
        passarMouse.moveToElement(abaOficios).click().build().perform();
        Thread.sleep(2000);
        abaOficios.click();
        String pastaDespachosSalvos = despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS);

        // inicia o loop para leitura dos arquivos do diret�rio
        for (File arquivo : obterArquivos(pastaDespachosSalvos)) {
        	TimeUnit.SECONDS.sleep(1);

	        WebElement infCarregando = null;
	        do {
		        infCarregando = MyUtils.encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
	        } while (infCarregando != null && infCarregando.isDisplayed());

        	String numeroProcesso = arquivo.getName().toLowerCase().replace(".pdf", "");

        	MyUtils.appendLogArea(logArea, "N� do Processo: " + numeroProcesso + " - Arquivo: " + arquivo.getAbsolutePath());

	        // clica no bot�o de filtro
	        WebElement cbcProcessoJudicial = MyUtils.encontrarElemento(wait5, By.xpath("//div[./span[text() = 'Processo Judicial']]"));
        	TimeUnit.SECONDS.sleep(1);
	        passarMouse.moveToElement(cbcProcessoJudicial).click().build().perform();
	
	        WebElement btnExpandirMenu = MyUtils.encontrarElemento(wait5, By.xpath("//div[./span[text() = 'Processo Judicial']]/div"));
        	TimeUnit.SECONDS.sleep(1);
	        btnExpandirMenu.click();
	
	        WebElement divFiltro = MyUtils.encontrarElemento(wait5, By.xpath("//div[./a/span[text() = 'Filtros']]"));
        	TimeUnit.SECONDS.sleep(1);

	        infCarregando = null;
	        do {
		        infCarregando = MyUtils.encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
	        } while (infCarregando != null && infCarregando.isDisplayed());

	        passarMouse.moveToElement(divFiltro).click().build().perform();
	        WebElement iptPesquisar = MyUtils.encontrarElemento(wait5, By.xpath("//div[not(contains(@style, 'hidden'))]//input[@type = 'text' and @role = 'textbox' and @data-errorqtip = '' and not(@style)]"));
	        Thread.sleep(500);
	        iptPesquisar.clear();
	        iptPesquisar.sendKeys(numeroProcesso);
	
        	TimeUnit.SECONDS.sleep(2);

	        infCarregando = null;
	        do {
		        infCarregando = MyUtils.encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
	        } while (infCarregando != null && infCarregando.isDisplayed());
		        
	        // ap�s retorno da pesquisa, buscar tabela "//table[contains(@id, 'gridview')]"
	        List<WebElement> linhasRetornadas = MyUtils.encontrarElementos(wait15, By.xpath("//table[contains(@id, 'gridview')]/tbody/tr"));

			if (linhasRetornadas.size() == 1) {
				WebElement divLinhaResultado = linhasRetornadas.iterator().next().findElement(By.xpath("./td[1]/div"));
				passarMouse.moveToElement(divLinhaResultado).click().build().perform();
				passarMouse.contextClick(divLinhaResultado).perform();
			} else {
				if (linhasRetornadas.size() == 0) {
					MyUtils.appendLogArea(logArea, "O processo " + numeroProcesso + " n�o foi encontrado.");
				} else {
					MyUtils.appendLogArea(logArea, "Foram encontrados " + linhasRetornadas.size() + " registros para o processo " + numeroProcesso + ". A resposta a este processo dever� ser feita manualmente.");
				}
				continue;
			}
	
        	TimeUnit.SECONDS.sleep(1);
			
			// clicar no bot�o responder
			WebElement divResponder = MyUtils.encontrarElemento(wait5, By.xpath("//div[./a/span[text() = 'Responder']]"));
			passarMouse.moveToElement(divResponder).click().build().perform();

        	TimeUnit.SECONDS.sleep(1);

			// clicar no bot�o de upload de arquivos
			WebElement btnUploadArquivo = MyUtils.encontrarElemento(wait5, By.id("button_browse-button"));
			passarMouse.moveToElement(btnUploadArquivo).perform();
	
			WebElement inpUploadArquivo = MyUtils.encontrarElemento(wait5, By.xpath("//input[@type = 'file']"));
			inpUploadArquivo.sendKeys(arquivo.getAbsolutePath());
			
			WebElement btnConfirmarUpload = MyUtils.encontrarElemento(wait5, By.id("button_upload"));
			passarMouse.moveToElement(btnConfirmarUpload).click().build().perform();

			WebElement infUploadCompleto = null;
			
			do {
				infUploadCompleto = MyUtils.encontrarElemento(wait5, By.xpath("//tbody/tr/td[7]/div[text() = '100%']"));
			} while (infUploadCompleto == null);

        	TimeUnit.SECONDS.sleep(1);

			WebElement btnFechar = MyUtils.encontrarElemento(wait5, By.xpath("//a[.//span[contains(text(), 'Fechar')]]"));
			passarMouse.moveToElement(btnFechar).click().build().perform();

	        infCarregando = null;
	        do {
		        infCarregando = MyUtils.encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
	        } while (infCarregando != null && infCarregando.isDisplayed());
			
			// mover o arquivo
	        criarDiretorioBackup(pastaDespachosSalvos);
			arquivo.renameTo(new File(pastaDespachosSalvos + "\\bkp\\" + arquivo.getName()));
        }
		
        MyUtils.appendLogArea(logArea, "Fim do processamento...");
        // driver.close();
        driver.quit();
	}

	private void criarDiretorioBackup(String caminho) {
		File diretorio = new File(caminho + "\\bkp");
		if (!diretorio.exists()) {
			diretorio.mkdir();
		}
	}

	private ArrayList<File> obterArquivos(String nomeDiretorio) {
		ArrayList<File> retorno = new ArrayList<File>();
		File diretorio = new File(nomeDiretorio);
		for (File arquivo : diretorio.listFiles()) {
			if (!arquivo.isDirectory() && arquivo.getName().toLowerCase().endsWith("pdf")) {
				retorno.add(arquivo);
			}
		}
		return retorno;
	}
}
