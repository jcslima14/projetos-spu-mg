import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.time.Duration;
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
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void incluirDadosSPUNet(JTextArea logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws Exception {
		Origem spunet = MyUtils.entidade(despachoServico.obterOrigem(Origem.SPUNET_ID, null));
        String pastaDespachosSalvos = MyUtils.emptyStringIfNull(despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS) + "\\" + spunet.getDescricao());
        if (pastaDespachosSalvos.equals("") || !MyUtils.arquivoExiste(pastaDespachosSalvos)) {
        	JOptionPane.showMessageDialog(null, "A pasta onde devem estar gravados os arquivos PDF de resposta não está configurada ou não existe: " + pastaDespachosSalvos + ". \nConfigure a origem SPUNet (" + Origem.SPUNET_ID + ") com o caminho para a pasta onde os arquivos PDF deve estar gravados.");
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
		MyUtils.appendLogArea(logArea, "Acessando o SPUNet...");
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SPUNET));
        Actions passarMouse = new Actions(driver);

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
		MyUtils.appendLogArea(logArea, "Informando credenciais...");

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

        // clica na aba de ofícios
//        WebElement btnMenuAplicacao = MyUtils.encontrarElemento(wait15, By.xpath("//button[@aria-label='Menu da Aplicação']"));
//        passarMouse.moveToElement(btnMenuAplicacao).perform();
//        waitUntil.until(ExpectedConditions.elementToBeClickable(btnMenuAplicacao));
//        btnMenuAplicacao.click();
//
//        WebElement btnServicos = MyUtils.encontrarElemento(wait15, By.xpath("//button[./div[contains(text(), 'SERVIÇOS (PORTAL SPU/MP)')]]"));
//        passarMouse.moveToElement(btnServicos).click().build().perform();
//
//        WebElement btnTriagem = MyUtils.encontrarElemento(wait15, By.xpath("//a[text() = 'Triagem']"));
//        passarMouse.moveToElement(btnTriagem).perform();
//        waitUntil.until(ExpectedConditions.elementToBeClickable(btnTriagem));
//        btnTriagem.click();

        driver.get("http://spunet.planejamento.gov.br/#/servicos/triagem");
        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");

        // inicia o loop para leitura dos arquivos do diretório
        for (File arquivo : MyUtils.obterArquivos(pastaDespachosSalvos)) {
	        String nomeArquivo = arquivo.getName().split("\\.")[0];
	        String[] dadosResposta = nomeArquivo.split("\\-");
        	String numeroAtendimento = dadosResposta[0];
        	String numeroDocumentoSEI = "0";
        	if (dadosResposta.length > 1) numeroDocumentoSEI = dadosResposta[1];

	        Solicitacao solicitacao = MyUtils.entidade(despachoServico.obterSolicitacao(null, Origem.SPUNET, TipoProcesso.ELETRONICO, null, numeroAtendimento));
	        SolicitacaoResposta resposta = null;
	        if (solicitacao == null) {
	        	MyUtils.appendLogArea(logArea, "Arquivo " + arquivo.getName() + ": não foi encontrada a solicitação para o nº de atendimento " + numeroAtendimento + ". A resposta não poderá ser feita automaticamente");
	        	continue;
	        } else {
	        	// busca a resposta referente ao arquivo lido
	        	resposta = MyUtils.entidade(despachoServico.obterSolicitacaoResposta(null, solicitacao, null, null, null, null, numeroDocumentoSEI, false, false, false));
	        }

	        if (resposta == null) {
	        	MyUtils.appendLogArea(logArea, "Arquivo " + arquivo.getName() + ": não foi encontrado o número do documento de resposta na base de dados. A resposta não poderá ser feita automaticamente");
	        	continue;
	        }

	        if (MyUtils.emptyStringIfNull(resposta.getTipoResposta().getRespostaSPUNet()).trim().equals("")) {
	        	MyUtils.appendLogArea(logArea, "Arquivo " + arquivo.getName() + "(" + solicitacao.getNumeroProcesso() + " / " + numeroAtendimento + "): o tipo de resposta não está configurado para qual tipo de resposta deve ser data no SPUNet. Configure a resposta para o SPUNet e tente novamente.");
	        	continue;
	        }

        	MyUtils.appendLogArea(logArea, "Nº do Processo: " + solicitacao.getNumeroProcesso() + " (Nº Atendimento: " + numeroAtendimento + ") - Arquivo: " + arquivo.getAbsolutePath());

	        WebElement txtNumeroAtendimento = MyUtils.encontrarElemento(wait15, By.xpath("//input[@ng-model = 'filtro.nuAtendimento']"));
	        txtNumeroAtendimento.clear();
	        txtNumeroAtendimento.sendKeys(numeroAtendimento);

	        WebElement btnPesquisar = MyUtils.encontrarElemento(wait15, By.xpath("//button[@aria-label = 'Pesquisar']"));
	        btnPesquisar.click();

	        MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]"); 

	        List<WebElement> linhasRetornadas = MyUtils.encontrarElementos(wait5, By.xpath("//table/tbody/tr"));
	        if (linhasRetornadas.size() == 1) {
	        	WebElement txtSituacao = linhasRetornadas.iterator().next().findElement(By.xpath("./td[6]"));
	        	if (!txtSituacao.getText().trim().equalsIgnoreCase("Em Análise Técnica")) {
	        		MyUtils.appendLogArea(logArea, "A solicitação não está em análise técnica e não pode ser respondida automaticamente");
	        		continue;
	        	}
	        	
	        	WebElement btnExpandirOpcoes = linhasRetornadas.iterator().next().findElement(By.xpath("//md-fab-trigger"));
		        js.executeScript("arguments[0].click();", btnExpandirOpcoes);
		        TimeUnit.MILLISECONDS.sleep(500);
	        	// passarMouse.moveToElement(btnExpandirOpcoes).click().build().perform();
	        	
	        	WebElement btnDetalhar = linhasRetornadas.iterator().next().findElement(By.xpath("//a[@ng-click = 'irParaDetalhar(item);']"));
		        js.executeScript("arguments[0].click();", btnDetalhar);
		        TimeUnit.MILLISECONDS.sleep(500);
//	            passarMouse.moveToElement(btnDetalhar).perform();
//	        	btnDetalhar.click();
	        	
	            MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]");
	            
	            WebElement optResposta = MyUtils.encontrarElemento(wait5, By.xpath("//md-radio-button[@aria-label = '" + resposta.getTipoResposta().getRespostaSPUNet() + "']"));
	            optResposta.click();
	            
	            if (!MyUtils.emptyStringIfNull(resposta.getTipoResposta().getComplementoSPUNet()).equals("")) {
		            WebElement txtInformacoesComplementares = MyUtils.encontrarElemento(wait5, By.xpath("//textarea[@ng-model = 'analiseRequerimento.justificativa']"));
		            txtInformacoesComplementares.sendKeys(MyUtils.emptyStringIfNull(resposta.getTipoResposta().getComplementoSPUNet()));
	            }

	            WebElement txtUpload = MyUtils.encontrarElemento(wait5, By.xpath("//input[@type = 'file']"));
	            
	            TimeUnit.MILLISECONDS.sleep(500);
	
	            js.executeScript("arguments[0].style.visibility = 'visible'; arguments[0].style.overflow = 'visible'; arguments[0].style.height = '1px'; arguments[0].style.width = '1px'; arguments[0].style.opacity = 1", txtUpload);
	
	            TimeUnit.MILLISECONDS.sleep(500);
	            
	            txtUpload.sendKeys(arquivo.getAbsolutePath());
	            
	            MyUtils.esperarCarregamento(2000, wait5, "//p[contains(text(), 'Carregando')]");

	            // busca o botão de enviar para clicar
	            WebElement btnEnviar = MyUtils.encontrarElemento(wait5, By.xpath("//button[text() = 'Enviar']"));
	            passarMouse.moveToElement(btnEnviar);
	            btnEnviar.click();

	            MyUtils.esperarCarregamento(2000, wait15, "//p[contains(text(), 'Carregando')]");

	            // botao para fechar a janela após clicar em enviar
	            WebElement btnFechar = MyUtils.encontrarElemento(wait5, By.xpath("//button[@ng-click='fechar()' and ./label[text() = 'fechar']]"));
	            passarMouse.moveToElement(btnFechar);
	            btnFechar.click();

	            MyUtils.esperarCarregamento(2000, wait5, "//p[contains(text(), 'Carregando')]");
	        } else {
	        	MyUtils.appendLogArea(logArea, "Foram retornadas " + linhasRetornadas.size() + " linhas ao pesquisar. Não será possível responder automaticamente.");
	        }

			// mover o arquivo
	        MyUtils.criarDiretorioBackup(pastaDespachosSalvos);
			arquivo.renameTo(new File(pastaDespachosSalvos + "\\bkp\\" + arquivo.getName()));
        }

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
        driver.quit();
	}
}
