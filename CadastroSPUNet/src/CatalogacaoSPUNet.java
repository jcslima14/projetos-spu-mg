import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.swing.JButton;
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
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class CatalogacaoSPUNet extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private SPUNetServico cadastroServico;

	public CatalogacaoSPUNet(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		cadastroServico = new SPUNetServico(conexao);

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

		cbbNavegador.addItem("Chrome");
		cbbNavegador.addItem("Firefox");

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
		painelDados.add(botaoProcessar); 
		painelDados.add(new JPanel());

		SpringUtilities.makeGrid(painelDados,
                espacoEmDisco == null ? 4 : 5, 2, //rows, cols
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
		if (cbbNavegador.getSelectedItem().toString().equalsIgnoreCase("chrome")) {
			ChromeOptions opcoes = new ChromeOptions();
			System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
	        driver = new ChromeDriver(opcoes);
		} else {
			FirefoxOptions opcoes = new FirefoxOptions();
			opcoes.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
			System.setProperty("webdriver.gecko.driver", MyUtils.firefoxWebDriverPath());
			driver = new FirefoxDriver(opcoes);
		}

        // acessando o endereço
        driver.get("http://spunet.planejamento.gov.br");
        // Actions passarMouse = new Actions(driver);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Wait<WebDriver> wait15 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(15))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        Wait<WebDriver> wait5 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(5))
        		.pollingEvery(Duration.ofSeconds(1))
        		.ignoring(NoSuchElementException.class);

        WebDriverWait waitUntil = new WebDriverWait(driver, 10);

        TimeUnit.MILLISECONDS.sleep(1000);

        // Find the text input element by its name
        WebElement weUsuario = MyUtils.encontrarElemento(wait15, By.id("username"));
        waitUntil.until(ExpectedConditions.elementToBeClickable(weUsuario));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = MyUtils.encontrarElemento(wait15, By.id("password"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = MyUtils.encontrarElemento(wait15, By.xpath("//button[contains(text(), 'Acessar')]"));
        botaoAcessar.click();

        if (cbbNavegador.getSelectedItem().toString().equalsIgnoreCase("firefox")) {
        	MyUtils.acceptSecurityAlert(driver);
        }
        
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

        List<Geoinformacao> geos = cadastroServico.obterGeoinformacao(null, false, null);
        int cont = 0;

        // inicia o loop para leitura dos arquivos do diretório
        for (Geoinformacao geo : geos) {
            // acessa o endereço de cadastrar nova cartografia
            driver.get("http://spunet.planejamento.gov.br/#/geometadados/cadastrar");
            
	        MyUtils.esperarCarregamento(1000, wait5, "//p[contains(text(), 'Carregando')]");

	        MyUtils.appendLogArea(logArea, MyUtils.formatarData(new Date(),  "dd/MM/yyyy HH:mm:ss") + " - Processando registro " + (++cont) + " de " + geos.size() + ": título '" + geo.getIdentTituloProduto() + "'");

	        // seção de identificação
	        WebElement cbbFormatoProduto = MyUtils.encontrarElemento(wait15, By.name("idProdutoCdg"));
	        cbbFormatoProduto.sendKeys(Keys.ESCAPE);
	        cbbFormatoProduto.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement optFormatoProduto = MyUtils.encontrarElemento(wait15, By.xpath("//md-option[./div[text() = '" + geo.getIdentFormatoProdutoCDG() + "']]"));
	        js.executeScript("arguments[0].click();", optFormatoProduto);
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement cbbProduto = MyUtils.encontrarElemento(wait15, By.name("idTipoProduto"));
	        cbbProduto.click();

	        WebElement optProduto = MyUtils.encontrarElemento(wait15, By.xpath("//md-option[./div[text() = '" + geo.getIdentProdutoCDG() + "']]"));
	        js.executeScript("arguments[0].click();", optProduto);
	        TimeUnit.MILLISECONDS.sleep(1000);
	        try {
	        	optProduto.sendKeys(Keys.ESCAPE);
	        } catch (Exception e) {
	        }

	        WebElement optColecao = MyUtils.encontrarElemento(wait15, By.xpath("//md-radio-button[@name = 'radioTipo' and @aria-label = 'Não']"));
	        optColecao.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement txtTitulo = MyUtils.encontrarElemento(wait15, By.xpath("//input[@name = 'dsTituloProdCartografico']"));
	        txtTitulo.sendKeys(geo.getIdentTituloProduto());
	        
	        do {
	        	TimeUnit.MILLISECONDS.sleep(500);
	        } while (!txtTitulo.getAttribute("value").equals(geo.getIdentTituloProduto()));

	        WebElement txtDataCriacao = MyUtils.encontrarElemento(wait15, By.xpath("//md-datepicker[@id = 'metadadosDtCriacao']//input"));
	        txtDataCriacao.click();
	        txtDataCriacao.sendKeys(Keys.ESCAPE);
	        txtDataCriacao.sendKeys(geo.getIdentDataCriacao());
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement txtDataPublicacao = MyUtils.encontrarElemento(wait15, By.xpath("//md-datepicker[@id = 'metadadosDtPublicacao']//input"));
	        txtDataPublicacao.click();
	        txtDataPublicacao.sendKeys(Keys.ESCAPE);
	        txtDataPublicacao.sendKeys(geo.getIdentDataDigitalizacao());
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement txtResumo = MyUtils.encontrarElemento(wait15, By.xpath("//textarea[@name = 'dsResumo']"));
	        txtResumo.sendKeys(geo.getIdentResumo());
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement cbbStatus = MyUtils.encontrarElemento(wait15, By.xpath("//md-select[@id = 'idStatusProduto']"));
	        cbbStatus.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement optStatus = MyUtils.encontrarElemento(wait15, By.xpath("//md-option[./div[text() = '" + geo.getIdentStatus() + "']]"));
	        js.executeScript("arguments[0].click();", optStatus);
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement cbbInstituicao = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetIdentificacaoForm']//md-select[@name = 'coResponsavel']"));
	        cbbInstituicao.click();

	        WebElement optInstituicao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getIdentInstituicao() + "']]"));
	        js.executeScript("arguments[0].click();", optInstituicao);
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        try {
	        	optInstituicao.sendKeys(Keys.ESCAPE);
	        } catch (Exception e) {
	        }
	        
	        WebElement btnContinuar = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetIdentificacaoForm']//button[text() = 'CONTINUAR/GRAVAR']"));
	        btnContinuar.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        // seção de sistema de referência
	        WebElement cbbSistemaReferencia = MyUtils.encontrarElemento(wait15, By.name("coSistemaReferencia"));
	        cbbSistemaReferencia.click();

	        WebElement optSistemaReferencia = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getSisrefDatum() + "']]"));
	        js.executeScript("arguments[0].click();", optSistemaReferencia);
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        if (!geo.getSisrefDatum().equalsIgnoreCase("sem datum")) {
		        WebElement cbbProjecao = MyUtils.encontrarElemento(wait15, By.name("coProjecao"));
		        cbbProjecao.click();

		        WebElement optProjecao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getSisrefProjecao() + "']]"));
		        js.executeScript("arguments[0].click();", optProjecao);
		        TimeUnit.MILLISECONDS.sleep(500);

		        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");
	        }

	        if (!geo.getSisrefObservacao().equals("")) {
		        WebElement txtObservacao = MyUtils.encontrarElemento(wait15, By.xpath("//textarea[@ng-model = 'metadados.dssisrefobservacao']"));
		        txtObservacao.sendKeys(geo.getSisrefObservacao());
		        
		        do {
		        	TimeUnit.MILLISECONDS.sleep(500);
		        } while (!txtObservacao.getAttribute("value").equals(geo.getSisrefObservacao()));
	        }
	        
	        btnContinuar = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetSistemaReferenciaForm']//button[text() = 'CONTINUAR/GRAVAR']"));
	        btnContinuar.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");
	        
	        // seção de identificação do CDG
	        WebElement cbbTipoRepresentacaoEspacial = MyUtils.encontrarElemento(wait15, By.name("coRepresentacaoEspacial"));
	        cbbTipoRepresentacaoEspacial.click();

	        WebElement optTipoRepresentacaoEspacial = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getIdentcdgTipoReprEspacial() + "']]"));
	        js.executeScript("arguments[0].click();", optTipoRepresentacaoEspacial);
	        TimeUnit.MILLISECONDS.sleep(500);

	        if (!geo.escalaAjustada().contentEquals("")) {
		        WebElement optEscala = MyUtils.encontrarElemento(wait15, By.xpath("//div[contains(@ng-show, 'idRepresentacaoEspacial') and @aria-hidden = 'false']/md-radio-group[@name = 'radioDAU']/md-radio-button[@aria-label = 'Escala']"));
		        optEscala.click();
		        TimeUnit.MILLISECONDS.sleep(500);

		        WebElement cbbListaEscala = MyUtils.encontrarElemento(wait15, By.xpath("//div[contains(@ng-show, 'idRepresentacaoEspacial') and @aria-hidden = 'false']//md-select[@name = 'vlEscala']"));
		        // passarMouse.moveToElement(cbbListaEscala).perform();
		        js.executeScript("arguments[0].click();", cbbListaEscala);
		        TimeUnit.MILLISECONDS.sleep(500);

		        WebElement optListaEscala = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.escalaAjustada() + "']]"));
		        js.executeScript("arguments[0].click();", optListaEscala);
		        TimeUnit.MILLISECONDS.sleep(500);
	        } else {
		        WebElement txtObservacaoEscala = MyUtils.encontrarElemento(wait15, By.xpath("//textarea[@ng-model = 'metadados.dsidentcdgobservacao']"));
		        txtObservacaoEscala.sendKeys(geo.observacaoEscala());

		        do {
		        	TimeUnit.MILLISECONDS.sleep(500);
		        } while (!txtObservacaoEscala.getAttribute("value").equals(geo.observacaoEscala()));
	        }

	        WebElement cbbIdioma = MyUtils.encontrarElemento(wait15, By.name("coIdiomaIdCdg"));
	        // passarMouse.moveToElement(cbbIdioma).perform();
	        cbbIdioma.click();

	        WebElement optIdioma = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getIdentcdgIdioma() + "']]"));
	        js.executeScript("arguments[0].click();", optIdioma);
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement cbbCategoriaTematica = MyUtils.encontrarElemento(wait15, By.name("coCategoriaTematica"));
	        cbbCategoriaTematica.click();

	        WebElement optCategoriaTematica = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getIdentcdgCategoria() + "']]"));
	        js.executeScript("arguments[0].click();", optCategoriaTematica);
	        TimeUnit.MILLISECONDS.sleep(500);

	        int tentativas = 1;
	        
	        do {
		        WebElement cbbUF = MyUtils.encontrarElemento(wait15, By.name("geocodigoUf"));
		        cbbUF.click();

		        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,100)", "");
		        
		        WebElement optUF = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getIdentcdgUF() + "']]"));
		        // js.executeScript("arguments[0].click();", optUF);
		        // passarMouse.moveToElement(optUF).perform();
		        
		        if (optUF.getAttribute("selected") != null) {
		        	optUF.click();
		        }

		        TimeUnit.MILLISECONDS.sleep(500);
		        optUF.click();
		        TimeUnit.MILLISECONDS.sleep(1000);
	
		        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");
	
		        TimeUnit.MILLISECONDS.sleep(1000);
		        optUF.sendKeys(Keys.ESCAPE);
		        TimeUnit.MILLISECONDS.sleep(500);
	
		        WebElement cbbMunicipio = MyUtils.encontrarElemento(wait15, By.name("geocodigoMunicipioIdentificacaoCdg"));
		        // js.executeScript("arguments[0].click();", cbbMunicipio);
		        TimeUnit.MILLISECONDS.sleep(1000);
		        cbbMunicipio.click();
		        TimeUnit.MILLISECONDS.sleep(2000);
	
		        WebElement optMunicipio = null;
		        try {
		        	optMunicipio = MyUtils.encontrarElemento(wait5, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-optgroup/md-option[./div[text() = '" + geo.getIdentcdgMunicipio() + "']]"));
		        } catch (Exception e) {
		        	MyUtils.appendLogArea(logArea, "Falhou tentativa " + (tentativas++) + " de obter a lista de municípios");
		        	MyUtils.encontrarElemento(wait5, By.xpath("//body")).click();
		        	TimeUnit.MILLISECONDS.sleep(500);
		        	continue;
		        }

		        js.executeScript("arguments[0].click();", optMunicipio);
		        TimeUnit.MILLISECONDS.sleep(500);
		        
		        optMunicipio.sendKeys(Keys.ESCAPE);
		        TimeUnit.MILLISECONDS.sleep(500);

		        break;
	        } while (true);
	        
	        WebElement cbbDatum = MyUtils.encontrarElemento(wait15, By.xpath("//md-select[@name = 'coDatum']"));
	        cbbDatum.click();

	        WebElement optDatum = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getIdentcdgDatum() + "']]"));
	        js.executeScript("arguments[0].click();", optDatum);
	        TimeUnit.MILLISECONDS.sleep(500);

	        btnContinuar = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetIdentificacaoCdgForm']//button[text() = 'CONTINUAR/GRAVAR']"));
	        btnContinuar.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");
	        
	        // seção qualidade
	        WebElement cbbNivelHierarquico = MyUtils.encontrarElemento(wait15, By.xpath("//md-select[@name = 'coNivelHierarquico']"));
	        cbbNivelHierarquico.click();

	        WebElement optNivelHierarquico = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getQualidadeNivel() + "']]"));
	        js.executeScript("arguments[0].click();", optNivelHierarquico);
	        TimeUnit.MILLISECONDS.sleep(500);

	        WebElement txtLinhagem = MyUtils.encontrarElemento(wait15, By.name("dsLinhagem"));
	        txtLinhagem.sendKeys(geo.qualidadeLinhagemAjustada());

	        do {
	        	TimeUnit.MILLISECONDS.sleep(500);
	        } while (!txtLinhagem.getAttribute("value").equals(geo.qualidadeLinhagemAjustada()));

	        btnContinuar = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetQualidadeForm']//button[text() = 'CONTINUAR/GRAVAR']"));
	        btnContinuar.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");
	        TimeUnit.MILLISECONDS.sleep(1000);

	        // seção distribuição
	        WebElement cbbFormatoDistribuicao = MyUtils.encontrarElemento(wait15, By.name("coFormatoDistribuicao"));
	        cbbFormatoDistribuicao.click();

	        WebElement optFormatoDistribuicao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getDistribuicaoFormato() + "']]"));
	        js.executeScript("arguments[0].click();", optFormatoDistribuicao);
	        TimeUnit.MILLISECONDS.sleep(500);

	        cbbInstituicao = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetDistribuicaoForm']//md-select[@name = 'coResponsavel']"));
	        cbbInstituicao.click();

	        optInstituicao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getDistribuicaoInstituicao() + "']]"));
	        js.executeScript("arguments[0].click();", optInstituicao);
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        WebElement cbbFuncao = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetDistribuicaoForm']//md-select[@name = 'coFuncao']"));
	        cbbFuncao.click();

	        WebElement optFuncao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getDistribuicaoFuncao() + "']]"));
	        js.executeScript("arguments[0].click();", optFuncao);
	        TimeUnit.MILLISECONDS.sleep(500);

	        btnContinuar = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetDistribuicaoForm']//button[text() = 'CONTINUAR/GRAVAR']"));
	        btnContinuar.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        // seção metametadados
	        cbbIdioma = MyUtils.encontrarElemento(wait15, By.name("coIdiomaMetadados"));
	        cbbIdioma.click();

	        optIdioma = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getMetadadoIdioma() + "']]"));
	        js.executeScript("arguments[0].click();", optIdioma);
	        TimeUnit.MILLISECONDS.sleep(1000);

	        cbbInstituicao = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetMetadadosForm']//md-select[@name = 'coResponsavel']"));
	        cbbInstituicao.click();

	        optInstituicao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getMetadadoInstituicao() + "']]"));
	        js.executeScript("arguments[0].click();", optInstituicao);
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        cbbFuncao = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetMetadadosForm']//md-select[@name = 'coFuncao']"));
	        cbbFuncao.click();

	        optFuncao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getMetadadoFuncao() + "']]"));
	        js.executeScript("arguments[0].click();", optFuncao);
	        TimeUnit.MILLISECONDS.sleep(500);

	        btnContinuar = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetMetadadosForm']//button[text() = 'CONTINUAR/GRAVAR']"));
	        btnContinuar.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        // seção informações adicionais
	        WebElement cbbTipoArticulacao = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetInfoAdicionaisForm']//md-select[@name = 'coTipoDeArticulacao']"));
	        cbbTipoArticulacao.click();

	        WebElement optTipoArticulacao = MyUtils.encontrarElemento(wait15, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + geo.getInfadicTipoArticulacao() + "']]"));
	        js.executeScript("arguments[0].click();", optTipoArticulacao);
	        TimeUnit.MILLISECONDS.sleep(500);

	        if (!geo.getInfadicCamadaInf().trim().equals("")) {
		        WebElement optCamadaInformacao = MyUtils.encontrarElemento(wait15, By.xpath("//md-checkbox[./*[text() = '" + geo.getInfadicCamadaInf() + "']]"));
		        js.executeScript("arguments[0].click();", optCamadaInformacao);
		        TimeUnit.MILLISECONDS.sleep(500);
	        }

	        WebElement btnEncaminhar = MyUtils.encontrarElemento(wait15, By.xpath("//form[@name = 'cadastroMetInfoAdicionaisForm']//button[text() = 'ENCAMINHAR']"));
	        btnEncaminhar.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        WebElement btnOK = MyUtils.encontrarElemento(wait15, By.xpath("//button[text() = 'OK']"));
	        btnOK.click();
	        TimeUnit.MILLISECONDS.sleep(500);
	        
	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

	        String[] url = driver.getCurrentUrl().split("\\/");

	        geo.setCadastrado(true);
	        geo.setIdSPUNet(Integer.parseInt(url[url.length - 1]));
	        cadastroServico.gravarEntidade(geo);
        }

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
        driver.quit();
	}
}
