import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ExtracaoDadosCIDI extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private ImoveisCIDIServico cadastroServico;

	public ExtracaoDadosCIDI(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		cadastroServico = new ImoveisCIDIServico(conexao);

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
			opcoes.addArguments("--ignore-certificate-errors");
			System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
	        driver = new ChromeDriver(opcoes);
		} else {
			FirefoxOptions opcoes = new FirefoxOptions();
			opcoes.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
			System.setProperty("webdriver.gecko.driver", MyUtils.firefoxWebDriverPath());
			driver = new FirefoxDriver(opcoes);
		}

        // acessando o endereço
        driver.get("https://cidi.rffsa.gov.br/iv-login.php");

        Wait<WebDriver> wait15 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(60))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        WebDriverWait waitUntil = new WebDriverWait(driver, 10);

        TimeUnit.MILLISECONDS.sleep(1000);

        MyUtils.appendLogArea(logArea, "Informando credenciais de acesso ao site...");
        
        // Find the text input element by its name
        WebElement weUsuario = MyUtils.encontrarElemento(wait15, By.name("user"));
        waitUntil.until(ExpectedConditions.elementToBeClickable(weUsuario));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = MyUtils.encontrarElemento(wait15, By.name("psw"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = MyUtils.encontrarElemento(wait15, By.xpath("//input[@type = 'submit']"));
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

        MyUtils.appendLogArea(logArea, "Acessando a página de pesquisa de bens transferidos à SPU...");

        // acessa o endereço de consulta de imóveis transferidos para a SPU
        driver.get("https://cidi.rffsa.gov.br/iv-consultas-spu.php");

		// seleciona a opção de pesquisa desejada
		Select cbxOpcaoPesquisa = new Select(MyUtils.encontrarElemento(wait15, By.name("campo1")));
		cbxOpcaoPesquisa.selectByValue("SIG_UF_BP");

		WebElement txtCriterioPesquisa = MyUtils.encontrarElemento(wait15, By.name("string1"));
		txtCriterioPesquisa.sendKeys("MG");

		WebElement btnPesquisar = MyUtils.encontrarElemento(wait15, By.xpath("//input[@type = 'submit' and @value = 'Pesquisar']"));
		btnPesquisar.click();

        MyUtils.appendLogArea(logArea, "Aguarde enquanto a lista de bens é carregada...");

		do {
			try {
				MyUtils.encontrarElemento(wait15, By.xpath("//input[@type = 'submit' and @value = 'Voltar ']"));
				break;
			} catch (Exception e) {
				MyUtils.appendLogArea(logArea, "Ainda aguardando a lista de bens...");
			}
		} while (true);

		List<WebElement> linhas = MyUtils.encontrarElementos(wait15, By.xpath("//table[@border = '1']/tbody/tr[./td]"));
		int cont = 1;

		for (WebElement linha : linhas) {
			BemTransferidoSPU bem = new BemTransferidoSPU();
			bem.setUr(linha.findElement(By.xpath("./td[1]")).getText().trim());
			bem.setNbp(linha.findElement(By.xpath("./td[2]")).getText().trim());
			bem.setParcela(linha.findElement(By.xpath("./td[3]//b")).getText().trim());
			MyUtils.appendLogArea(logArea, "Gravando registro " + cont++ + " de " + linhas.size() + " - NBP/Parcela: " + bem.getNbp() + "/" + bem.getParcela(), true, false);
			bem.setConta(linha.findElement(By.xpath("./td[4]")).getText().trim());
			bem.setDescricao(linha.findElement(By.xpath("./td[5]")).getText().trim());
			bem.setCodigoTrecho(linha.findElement(By.xpath("./td[6]")).getText().trim());
			bem.setTrechoInicio(linha.findElement(By.xpath("./td[7]")).getText().trim());
			bem.setTrechoFim(linha.findElement(By.xpath("./td[8]")).getText().trim());
			bem.setLogradouro(linha.findElement(By.xpath("./td[9]")).getText().trim());
			bem.setComplemento(linha.findElement(By.xpath("./td[10]")).getText().trim());
			bem.setMunicipio(linha.findElement(By.xpath("./td[11]")).getText().trim());
			bem.setCep(linha.findElement(By.xpath("./td[12]")).getText().trim());
			bem.setUf(linha.findElement(By.xpath("./td[13]")).getText().trim());
			bem.setArea(linha.findElement(By.xpath("./td[15]")).getText().trim());
			bem.setSituacaoBp(linha.findElement(By.xpath("./td[16]")).getText().trim());
			bem.setBpTerreno(linha.findElement(By.xpath("./td[17]")).getText().trim());
			bem.setParcBpTerreno(linha.findElement(By.xpath("./td[18]")).getText().trim());
			bem.setOrigem(linha.findElement(By.xpath("./td[19]")).getText().trim());
			bem.setNProcesso(linha.findElement(By.xpath("./td[20]")).getText().trim());
			bem.setMatriculaRgi(linha.findElement(By.xpath("./td[21]")).getText().trim());
			bem.setCheckList(linha.findElement(By.xpath("./td[22]")).getText().trim());
			bem.setCheckAno(linha.findElement(By.xpath("./td[23]")).getText().trim());
			bem.setTermoTransf(linha.findElement(By.xpath("./td[24]")).getText().trim());
			bem.setTermoAno(linha.findElement(By.xpath("./td[25]")).getText().trim());
			bem.setTermoDataSit(linha.findElement(By.xpath("./td[26]")).getText().trim());
			bem.setTermoOficioInv(linha.findElement(By.xpath("./td[27]")).getText().trim());
			bem.setTermoOficioNum(linha.findElement(By.xpath("./td[28]")).getText().trim());
			bem.setSituacaoSPU(linha.findElement(By.xpath("./td[29]")).getText().trim());
			bem.setDestinProv(linha.findElement(By.xpath("./td[30]")).getText().trim());
			bem.setInteressePub(linha.findElement(By.xpath("./td[31]")).getText().trim());
			bem.setRegistroCri(linha.findElement(By.xpath("./td[32]")).getText().trim());
			bem.setValorHistArtCult(linha.findElement(By.xpath("./td[33]")).getText().trim());
			bem.setAvalOrgData(linha.findElement(By.xpath("./td[34]")).getText().trim());
			bem.setIncorporado(linha.findElement(By.xpath("./td[35]")).getText().trim());
			bem.setRip(linha.findElement(By.xpath("./td[36]")).getText().trim());

			cadastroServico.gravarEntidade(bem);
		}

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
        driver.quit();
	}
}
