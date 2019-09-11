import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JButton;
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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class CatalogacaoSPUNet extends JInternalFrame {

	private SPUNetServico cadastroServico;

	public CatalogacaoSPUNet(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		cadastroServico = new SPUNetServico(conexao);

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

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(botaoProcessar); 
		painelDados.add(new JPanel());

		SpringUtilities.makeGrid(painelDados,
                espacoEmDisco == null ? 3 : 4, 2, //rows, cols
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
							incluirDadosSPUNet(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()));
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

	private void incluirDadosSPUNet(JTextArea logArea, String usuario, String senha) throws Exception {
		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		WebDriver driver = null;
		System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
        driver = new ChromeDriver();

        // acessando o endere�o
        driver.get("http://spunet.planejamento.gov.br");
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

        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]"); 
        
        // clica na aba de of�cios
        WebElement btnMenuAplicacao = MyUtils.encontrarElemento(wait15, By.xpath("//button[@aria-label='Menu da Aplica��o']"));
        passarMouse.moveToElement(btnMenuAplicacao).perform();
        waitUntil.until(ExpectedConditions.elementToBeClickable(btnMenuAplicacao));
        btnMenuAplicacao.click();

        WebElement btnServicos = MyUtils.encontrarElemento(wait15, By.xpath("//button[./div[contains(text(), 'GEOINFORMA��O')]]"));
        passarMouse.moveToElement(btnServicos).click().build().perform();

        WebElement btnTriagem = MyUtils.encontrarElemento(wait15, By.xpath("//a[@href = '#/geometadados/cadastrar']"));
        passarMouse.moveToElement(btnTriagem).perform();
        waitUntil.until(ExpectedConditions.elementToBeClickable(btnTriagem));
        btnTriagem.click();

        List<Geoinformacao> geos = cadastroServico.obterGeoinformacao(null, false, null);
        int cont = 0;
        
        // inicia o loop para leitura dos arquivos do diret�rio
        for (Geoinformacao geo : geos) {
	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        MyUtils.appendLogArea(logArea, "Processando registro " + (++cont) + " de " + geos.size() + ": t�tullo '" + geo.getIdentTituloProduto() + "'");

	        WebElement cbbFormatoProduto = MyUtils.encontrarElemento(wait15, By.xpath("//md-select[@aria-label = 'Formato do Produto de CDG']"));
	        cbbFormatoProduto.click();;

	        WebElement optFormatoProduto = MyUtils.encontrarElemento(wait15, By.xpath("//md-option[./div[text() = '" + geo.getIdentFormatoProdutoCDG() + "']]"));
	        optFormatoProduto.click();

	        WebElement cbbProduto = MyUtils.encontrarElemento(wait15, By.xpath("//md-select[@aria-label = 'Produto de CDG']"));
	        cbbProduto.click();;

	        WebElement optProduto = MyUtils.encontrarElemento(wait15, By.xpath("//md-option[./div[text() = '" + geo.getIdentProdutoCDG() + "']]"));
	        optProduto.click();

	        WebElement optColecao = MyUtils.encontrarElemento(wait15, By.xpath("//md-radio-button[@name = 'radioTipo' and @aria-label = 'N�o']"));
	        optColecao.click();

	        WebElement txtTitulo = MyUtils.encontrarElemento(wait15, By.xpath("//input[@name = 'dsTituloProdCartografico']"));
	        txtTitulo.sendKeys(geo.getIdentTituloProduto());

	        WebElement txtDataCriacao = MyUtils.encontrarElemento(wait15, By.xpath("//md-datepicker[@id = 'metadadosDtCriacao']//input"));
	        txtDataCriacao.sendKeys(geo.getIdentDataCriacao());

	        WebElement txtDataPublicacao = MyUtils.encontrarElemento(wait15, By.xpath("//md-datepicker[@id = 'metadadosDtPublicacao']//input"));
	        txtDataPublicacao.sendKeys(geo.getIdentDataDigitalizacao());

	        WebElement txtResumo = MyUtils.encontrarElemento(wait15, By.xpath("//textarea[@name = 'dsResumo']"));
	        txtResumo.sendKeys(geo.getIdentResumo());

	        WebElement cbbStatus = MyUtils.encontrarElemento(wait15, By.xpath("//md-select[@id = 'idStatusProduto']"));
	        cbbStatus.click();;

	        WebElement optStatus = MyUtils.encontrarElemento(wait15, By.xpath("//md-option[./div[text() = '" + geo.getIdentProdutoCDG() + "']]"));
	        optStatus.click();

	        WebElement cbbInstituicao = MyUtils.encontrarElemento(wait15, By.xpath("//md-select[@aria-label = 'Institui��o Respons�vel']"));
	        cbbInstituicao.click();;

	        WebElement optInstituicao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']//md-option[./div[text() = '" + geo.getIdentInstituicao() + "']]"));
	        optInstituicao.click();

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]"); 
        }

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
        driver.quit();
	}
}
