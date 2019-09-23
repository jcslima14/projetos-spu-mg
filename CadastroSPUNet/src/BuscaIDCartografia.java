import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Date;
import java.util.function.Function;

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

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
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
public class BuscaIDCartografia extends JInternalFrame {

	private JTextField txtIdInicial = new JTextField();
	private JLabel lblIdInicial = new JLabel("ID Inicial:") {{ setLabelFor(txtIdInicial); }};
	private JTextField txtIdFinal = new JTextField();
	private JLabel lblIdFinal = new JLabel("ID Final:") {{ setLabelFor(txtIdFinal); }};
	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private SPUNetServico cadastroServico;

	public BuscaIDCartografia(String tituloJanela, EntityManager conexao) {
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
		painelDados.add(lblIdInicial);
		painelDados.add(txtIdInicial);
		painelDados.add(lblIdFinal);
		painelDados.add(txtIdFinal);
		painelDados.add(lblNavegador);
		painelDados.add(cbbNavegador);
		painelDados.add(botaoProcessar); 
		painelDados.add(new JPanel());

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
							buscarIDSPUNet(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()));
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

	private void buscarIDSPUNet(JTextArea logArea, String usuario, String senha) throws Exception {
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
        	acceptSecurityAlert(driver);
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

        int idInicial = Integer.parseInt(txtIdInicial.getText());
        int idFinal = Integer.parseInt(txtIdFinal.getText());

        // inicia o loop para leitura dos arquivos do diretório
        for (int idPesquisar = idInicial; idPesquisar <= idFinal; idPesquisar++) {
            MyUtils.esperarCarregamento(500, wait5, "//p[contains(text(), 'Carregando')]"); 

	        MyUtils.appendLogArea(logArea, MyUtils.formatarData(new Date(),  "dd/MM/yyyy HH:mm:ss") + " - Processando id " + idPesquisar + " de " + idFinal);

            // clica no menu da aplicação
            driver.get("http://spunet.planejamento.gov.br/#/geometadados/" + idPesquisar);

	        MyUtils.esperarCarregamento(1000, wait5, "//p[contains(text(), 'Carregando')]");

	        WebElement txtTituloProdutoCartografico = null;

	        // encontrar o título do produto cartográfico
	        try {
	        	txtTituloProdutoCartografico = MyUtils.encontrarElemento(wait5, By.xpath("//td[./span[text() = ' - Título do Produto Cartográfico']]/following-sibling::td/label"));
	        } catch (Exception e) {
	        	MyUtils.appendLogArea(logArea, "A página não possui o elemento com o título do produto cartográfico");
	        	continue;
	        }

	        String tituloProdutoCartografico = txtTituloProdutoCartografico.getText().trim();

	        if (tituloProdutoCartografico.equals("")) {
	        	MyUtils.appendLogArea(logArea, "O ID pesquisado não foi encontrado");
	        	continue;
	        }
	        
	        Geoinformacao geo = MyUtils.entidade(cadastroServico.obterGeoinformacao(null, null, tituloProdutoCartografico));
	        
	        if (geo == null) {
	        	MyUtils.appendLogArea(logArea, "Não foi encontrado na base de dados o registro para o produto cartográfico '" + tituloProdutoCartografico + "'");
	        	continue;
	        }

	        if (geo.getIdSPUNet() != null) {
	        	MyUtils.appendLogArea(logArea, "O produto cartográfico '" + tituloProdutoCartografico + "' já está com o ID do SPUNet atualizado na base de dados");
	        	continue;
	        }
	        
	        geo.setIdSPUNet(idPesquisar);
	        cadastroServico.gravarEntidade(geo);
	        
	        MyUtils.appendLogArea(logArea, "O produto cartográfico '" + tituloProdutoCartografico + "' foi atualizado com o ID " + idPesquisar);
        }

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
        driver.quit();
	}

	private void acceptSecurityAlert(WebDriver driver) {
	    Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofSeconds(30))          
	                                                            .pollingEvery(Duration.ofSeconds(3))          
	                                                            .ignoring(NoSuchElementException.class);    
	    Alert alert = wait.until(new Function<WebDriver, Alert>() {       

	        public Alert apply(WebDriver driver) {
	            try {
	                return driver.switchTo().alert();
	            } catch(NoAlertPresentException e) {
	                return null;
	            }
	        }  
	    });

	    alert.accept();
	}
}
