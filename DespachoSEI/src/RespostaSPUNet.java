import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.time.Duration;
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
public class RespostaSPUNet extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private DespachoServico despachoServico;

	public RespostaSPUNet(String tituloJanela, Connection conexao) {
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
		JButton botaoSair = new JButton("Sair");
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
							incluirDadosSPUNet(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), chkExibirNavegador.isSelected(), navegador);
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
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void incluirDadosSPUNet(JTextArea logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws Exception {
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
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SPUNET));
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
        WebElement weUsuario = driver.findElement(By.id("username"));
        waitUntil.until(ExpectedConditions.elementToBeClickable(weUsuario));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = driver.findElement(By.id("password"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));
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

        TimeUnit.SECONDS.sleep(1);
        
        MyUtils.appendLogArea(logArea, "Esperando o carregamento da página principal...");
        
        WebElement carregando = null;
        do {
        	try {
        		carregando = MyUtils.encontrarElemento(wait5, By.xpath("//p[text() = 'Carregando...']"));
        	} catch (Exception e) {
        		carregando = null;
        	}
        } while (carregando != null);
        
        MyUtils.appendLogArea(logArea, "Iniciando a sequência para entrar na tela de cadastro...");
        
        // clica na aba de ofícios
        WebElement btnMenuAplicacao = MyUtils.encontrarElemento(wait15, By.xpath("//button[@aria-label='Menu da Aplicação']"));
        waitUntil.until(ExpectedConditions.elementToBeClickable(btnMenuAplicacao));
        btnMenuAplicacao.click();

        WebElement btnGeoinformacao = MyUtils.encontrarElemento(wait15, By.xpath("//button[./div[contains(text(), 'GEOINFORMAÇÃO')]]"));
        btnGeoinformacao.click();

        WebElement btnGeoinformaçãoCadastrar = MyUtils.encontrarElemento(wait15, By.xpath("//a[@href = '#/geometadados/cadastrar']"));
        passarMouse.moveToElement(btnGeoinformaçãoCadastrar).click().build().perform();
        // btnGeoinformaçãoCadastrar.click();
//        String pastaDespachosSalvos = despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS);
//
//        // inicia o loop para leitura dos arquivos do diretório
//        for (File arquivo : obterArquivos(pastaDespachosSalvos)) {
//        	delayInSeconds(1);
//
//	        WebElement infCarregando = null;
//	        do {
//		        infCarregando = encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
//	        } while (infCarregando != null && infCarregando.isDisplayed());
//
//        	String numeroProcesso = arquivo.getName().toLowerCase().replace(".pdf", "");
//
//	        appendLogArea(logArea, "Nº do Processo: " + numeroProcesso + " - Arquivo: " + arquivo.getAbsolutePath());
//
//	        // clica no botão de filtro
//	        WebElement cbcProcessoJudicial = encontrarElemento(wait5, By.xpath("//div[./span[text() = 'Processo Judicial']]"));
//	        delayInSeconds(1);
//	        passarMouse.moveToElement(cbcProcessoJudicial).click().build().perform();
//	
//	        WebElement btnExpandirMenu = encontrarElemento(wait5, By.xpath("//div[./span[text() = 'Processo Judicial']]/div"));
//	        delayInSeconds(1);
//	        btnExpandirMenu.click();
//	
//	        WebElement divFiltro = encontrarElemento(wait5, By.xpath("//div[./a/span[text() = 'Filtros']]"));
//	        delayInSeconds(1);
//
//	        infCarregando = null;
//	        do {
//		        infCarregando = encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
//	        } while (infCarregando != null && infCarregando.isDisplayed());
//
//	        passarMouse.moveToElement(divFiltro).click().build().perform();
//	        WebElement iptPesquisar = encontrarElemento(wait5, By.xpath("//div[not(contains(@style, 'hidden'))]//input[@type = 'text' and @role = 'textbox' and @data-errorqtip = '' and not(@style)]"));
//	        Thread.sleep(500);
//	        iptPesquisar.clear();
//	        iptPesquisar.sendKeys(numeroProcesso);
//	
//	        delayInSeconds(2);
//
//	        infCarregando = null;
//	        do {
//		        infCarregando = encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
//	        } while (infCarregando != null && infCarregando.isDisplayed());
//		        
//	        // após retorno da pesquisa, buscar tabela "//table[contains(@id, 'gridview')]"
//	        List<WebElement> linhasRetornadas = encontrarElementos(wait15, By.xpath("//table[contains(@id, 'gridview')]/tbody/tr"));
//
//			if (linhasRetornadas.size() == 1) {
//				WebElement divLinhaResultado = linhasRetornadas.iterator().next().findElement(By.xpath("./td[1]/div"));
//				passarMouse.moveToElement(divLinhaResultado).click().build().perform();
//				passarMouse.contextClick(divLinhaResultado).perform();
//			} else {
//				if (linhasRetornadas.size() == 0) {
//					appendLogArea(logArea, "O processo " + numeroProcesso + " não foi encontrado.");
//				} else {
//					appendLogArea(logArea, "Foram encontrados " + linhasRetornadas.size() + " registros para o processo " + numeroProcesso + ". A resposta a este processo deverá ser feita manualmente.");
//				}
//				continue;
//			}
//	
//			delayInSeconds(1);
//			
//			// clicar no botão responder
//			WebElement divResponder = encontrarElemento(wait5, By.xpath("//div[./a/span[text() = 'Responder']]"));
//			passarMouse.moveToElement(divResponder).click().build().perform();
//
//			delayInSeconds(1);
//
//			// clicar no botão de upload de arquivos
//			WebElement btnUploadArquivo = encontrarElemento(wait5, By.id("button_browse-button"));
//			passarMouse.moveToElement(btnUploadArquivo).perform();
//	
//			WebElement inpUploadArquivo = encontrarElemento(wait5, By.xpath("//input[@type = 'file']"));
//			inpUploadArquivo.sendKeys(arquivo.getAbsolutePath());
//			
//			WebElement btnConfirmarUpload = encontrarElemento(wait5, By.id("button_upload"));
//			passarMouse.moveToElement(btnConfirmarUpload).click().build().perform();
//
//			WebElement infUploadCompleto = null;
//			
//			do {
//				infUploadCompleto = encontrarElemento(wait5, By.xpath("//tbody/tr/td[7]/div[text() = '100%']"));
//			} while (infUploadCompleto == null);
//
//			delayInSeconds(1);
//
//			WebElement btnFechar = encontrarElemento(wait5, By.xpath("//a[.//span[contains(text(), 'Fechar')]]"));
//			passarMouse.moveToElement(btnFechar).click().build().perform();
//
//	        infCarregando = null;
//	        do {
//		        infCarregando = encontrarElemento(wait5, By.xpath("//div[text() = 'Carregando...']"));
//	        } while (infCarregando != null && infCarregando.isDisplayed());
//			
//			// mover o arquivo
//	        criarDiretorioBackup(pastaDespachosSalvos);
//			arquivo.renameTo(new File(pastaDespachosSalvos + "\\bkp\\" + arquivo.getName()));
//        }

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
//        driver.quit();
	}
}
