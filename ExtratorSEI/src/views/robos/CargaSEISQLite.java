package views.robos;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import framework.utils.MyUtils;
import framework.utils.SpringUtilities;
import models.ProcessoAndamento;
import utils.ExtracaoInformacoesSEI;
import utils.ExtratorSEIUtils;

@SuppressWarnings("serial")
public class CargaSEISQLite extends JInternalFrame {

	private Connection conexao;
	
	public CargaSEISQLite(String tituloJanela, Connection conexao) {
		super("Carga de Informações do SEI");
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		
		this.conexao = conexao;

		// exit the JVM when the window is closed

//		Map<String, String> propriedades = obterPropriedades();

//		try {
//			List<ProcessoAndamento> processosParaGerarFluxo = obterProcessosParaGeracaoFluxo(conexao, "DIIUP-SPU-MG");
//			int cont = 0;
//			for (ProcessoAndamento processoParaGerarFluxo : processosParaGerarFluxo) {
//				System.out.println("Processo " + processoParaGerarFluxo.getNumeroProcesso() + " - " + ++cont + " de " + processosParaGerarFluxo.size());
//				gerarFluxoProcesso(conexao, processoParaGerarFluxo, "DIIUP-SPU-MG");
//			}
//			System.out.println("Fim...");
//		} catch (Exception e2) {
//			e2.printStackTrace();
//		}
		
		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usuário:");
		JTextField txtUsuario = new JTextField(15);
		lblUsuario.setLabelFor(txtUsuario);
		
		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15);
		lblSenha.setLabelFor(txtSenha);
		
		JLabel lblDataInicial = new JLabel("Data Inicial:");
		JTextField txtDataInicial = new JTextField(8);
		lblDataInicial.setLabelFor(txtDataInicial);
		try {
			txtDataInicial.setText(obterDataUltimaCargaSEI(conexao));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		JLabel lblDataFinal = new JLabel("Data Final:");
		JTextField txtDataFinal = new JTextField(8);
		lblDataFinal.setLabelFor(txtDataFinal);
		SimpleDateFormat formato = new SimpleDateFormat("ddMMyyyy");
		txtDataFinal.setText(formato.format(new Date()));

		JCheckBox chkCarregarMarcadores = new JCheckBox("Carregar marcadores", true);

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoCarregar = new JButton("Carregar"); 
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
		painelDados.add(lblDataInicial);
		painelDados.add(txtDataInicial);
		painelDados.add(lblDataFinal);
		painelDados.add(txtDataFinal);
		painelDados.add(txtDataFinal);
		painelDados.add(chkCarregarMarcadores);
		painelDados.add(new JPanel());
		painelDados.add(chkExibirNavegador);
		painelDados.add(new JPanel());
		painelDados.add(botaoCarregar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
                espacoEmDisco == null ? 7 : 8, 2, //rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad

		add(painelDados, BorderLayout.WEST);
		JTextArea logArea = new JTextArea(30, 100);
		JScrollPane areaDeRolagem = new JScrollPane(logArea);
		add(areaDeRolagem, BorderLayout.SOUTH);

		botaoCarregar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				String msg = validaDatas(txtDataInicial.getText(), txtDataFinal.getText());
				if (msg != null) {
					JOptionPane.showMessageDialog(null, msg);
					return;
				}
				logArea.setText("");
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							executarCargaInformacoes(CargaSEISQLite.this.conexao, logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), txtDataInicial.getText(), txtDataFinal.getText(), chkCarregarMarcadores.isSelected(), chkExibirNavegador.isSelected());
						} catch (Exception e) {
							MyUtils.appendLogArea(logArea, "Erro ao processar a carga: \n \n" + e.getMessage() + "\n" + MyUtils.stackTraceToString(e));
							e.printStackTrace();
						}
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

	private String validaDatas(String dataInicial, String dataFinal) {
		if (dataInicial == null || dataInicial.trim().equals("")) return "A data inicial deve ser informada!";
		if (dataFinal == null || dataFinal.trim().equals("")) return "A data final deve ser informada!";
		SimpleDateFormat formato = new SimpleDateFormat("ddMMyyyy");
		formato.setLenient(false);
		Date di;
		Date df;
		try {
			di = formato.parse(dataInicial.replace("/", ""));
		} catch (ParseException e) {
			return "A data inicial informada não é válida!";
		}
		try {
			df = formato.parse(dataFinal.replace("/", ""));
		} catch (ParseException e) {
			return "A data final informada não é válida!";
		}
		if (di.after(df)) return "A data final não pode ser superior à data inicial!";
		if (df.after(new Date())) return "A data final não pode ser superior à data de hoje!";
		return null;
	}

	private void executarCargaInformacoes(Connection conexao, JTextArea logArea, String usuario, String senha, String dataInicial, String dataFinal, boolean carregarMarcadores, boolean exibirNavegador) throws Exception {
		Map<String, String> processosNaoVisualizados = new LinkedHashMap<String, String>();
		ChromeOptions opcoes = new ChromeOptions();
		if (!exibirNavegador) opcoes.addArguments("--headless");

		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		System.out.println("Começando aqui...");
		System.setProperty("webdriver.chrome.driver", ExtratorSEIUtils.chromeWebDriverPath());
        WebDriver driver = new ChromeDriver(opcoes);

        // And now use this to visit Google
        Properties propriedades = MyUtils.obterPropriedades("extratorsei.properties");
        driver.get(propriedades.getProperty("endereco_sei"));

        Wait<WebDriver> wait5 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(5))
        		.pollingEvery(Duration.ofSeconds(1))
        		.ignoring(NoSuchElementException.class);

        // Find the text input element by its name
        WebElement weUsuario = driver.findElement(By.id("txtUsuario"));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = driver.findElement(By.id("pwdSenha"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = driver.findElement(By.id("sbmLogin"));
        botaoAcessar.click();
        
        ExtracaoInformacoesSEI extrator = new ExtracaoInformacoesSEI(conexao, driver, carregarMarcadores, logArea);
        
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

        // XXX: deste ponto em diante, deve existir um loop para buscar todas as unidades disponíveis para o usuário
        List<WebElement> unidades = driver.findElements(By.xpath("//select[@id = 'selInfraUnidades']/option"));
        List<String> unidadesValues = new ArrayList<String>();

        for (WebElement unidade : unidades) {
        	unidadesValues.add(unidade.getAttribute("value"));
        }

        for (String unidadeValue : unidadesValues) {
        	// clica no botão de controle de processos para abrir a página principal do SEI, onde está o menu de opções
        	WebElement btnControleProcessos = ExtratorSEIUtils.encontrarElemento(wait5, By.id("lnkControleProcessos"));
        	btnControleProcessos.click();
        	
        	Select cbxUnidade = new Select(ExtratorSEIUtils.encontrarElemento(wait5, By.id("selInfraUnidades")));
        	cbxUnidade.selectByValue(unidadeValue);
        	
	        // obtem a unidade selecionada
	        String unidadeSelecionada = driver.findElement(By.xpath("//select[@id = 'selInfraUnidades']/option[@selected = 'selected']")).getText();
	        gravarUnidadeSelecionada(conexao, unidadeSelecionada);
	        
	        // percorre a lista de processos recebidos em busca dos que ainda não foram visualizados
	        while (true) {
	        	List<WebElement> processos = driver.findElements(By.xpath("//table[@id = 'tblProcessosRecebidos']/tbody/tr/td[3]/a[contains(@class, 'processoNaoVisualizado')]"));
	        	
	        	for (WebElement processo : processos) {
	        		processosNaoVisualizados.put(processo.getText(), processo.getText());
	        	}
	        	
	        	List<WebElement> proximaPagina = driver.findElements(By.xpath("//a[@id = 'lnkRecebidosProximaPaginaSuperior']"));
	        	
	        	if (proximaPagina.size() == 0) {
	        		break;
	        	} else {
	        		proximaPagina.iterator().next().click();
	        	}
	        }
	        
	        // Find the text input element by its name
	        WebElement opcaoEstatisticas = driver.findElement(By.xpath("//a[(text() = 'Estatísticas' or .= 'Estatísticas')]"));
	        Actions passarMouse = new Actions(driver);
	        passarMouse.moveToElement(opcaoEstatisticas).click().build().perform();
	        
	        // Find the text input element by its name
	        WebElement opcaoRelatorio = driver.findElement(By.xpath("//a[(text() = 'Unidade' or .= 'Unidade')]"));
	        opcaoRelatorio.click();
	
	        WebElement periodoDe = driver.findElement(By.id("txtPeriodoDe"));
	        periodoDe.sendKeys(dataInicial);
	
	        WebElement periodoA = driver.findElement(By.id("txtPeriodoA"));
	        periodoA.sendKeys(dataFinal);
	
	        WebElement botaoPesquisar = driver.findElement(By.id("sbmPesquisar"));
	        botaoPesquisar.click();
	
	        String tituloJanelaPrincipal = driver.getWindowHandle();
	
	        // tenta encontrar a tabela de resultados, se não encontrar, é porque não houve resultados no período pesquisado
	        WebElement totalProcessosPeriodo = null;
	        try {
	        	totalProcessosPeriodo = ExtratorSEIUtils.encontrarElemento(wait5, By.xpath("//table[@summary = 'Tabela de Processos com tramitação no período']/tbody/tr[@class = 'totalEstatisticas']/td[2]/a"));
	        } catch (Exception e) {
	        	MyUtils.appendLogArea(logArea, "Não foram encontrados, na unidade " + unidadeSelecionada + ", dados referentes ao período de " + dataInicial + " a " + dataFinal);
	        }
	        
	        // se não foram encontrados processos no período pesquisado, retorna para pesquisar a próxima unidade
	        if (totalProcessosPeriodo == null) continue;
	        
	        totalProcessosPeriodo.click();
	
	        // navega até a nova janela aberta
	        for (String tituloJanela : driver.getWindowHandles()) {
	        	driver.switchTo().window(tituloJanela);
	        }

	        String tituloJanelaResultados = driver.getWindowHandle();
	        TimeUnit.SECONDS.sleep(2);
//	        boolean inicioEncontrado = false;

	        while (true) {
		        // procura cada link para o processo de modo a clicar no mesmo e buscar o histórico de andamento
		        List<WebElement> processos = driver.findElements(By.xpath("//table/tbody/tr/td[4]/a"));

		        for (WebElement processo : processos) {
//		        	if (processo.getText().equals("04926.000942/2018-44")) inicioEncontrado = true;
//		        	if (!inicioEncontrado) continue;
		        	ProcessoAndamento paProcesso = new ProcessoAndamento() {{ setNumeroProcesso(processo.getText()); }};
		        	int ultimoSequencialGravado = extrator.obterUltimoSequencialGravado(conexao, paProcesso, unidadeSelecionada);

		            // obter o andamento do último sequencial gravado para verificar se é igual ao que vai ser lido agora
		        	ProcessoAndamento ultimoAndamentoGravado = extrator.obterUltimoAndamentoGravado(conexao, paProcesso.getNumeroProcesso());

		        	// se o processo estiver na lista de não visualizados, pula este registro
		        	if (processosNaoVisualizados.get(paProcesso.getNumeroProcesso()) != null) {
		        		continue;
		        	}

		        	// verifica se o processo possui indicador de que o andamento foi alterado
		        	if (!processoOk(conexao, paProcesso.getNumeroProcesso())) {
		        		MyUtils.appendLogArea(logArea, "Processo " + paProcesso.getNumeroProcesso() + " - Processo já existe na base com indicador de problema no andamento - Processamento ignorado");
		        		continue;
		        	}

		        	MyUtils.appendLogArea(logArea, "Processo " + paProcesso.getNumeroProcesso() + " - último registro gravado: " + ultimoSequencialGravado);

		        	processo.click();

		        	// abriu uma nova aba, então navega até ela
		            for (String tituloJanela : driver.getWindowHandles()) {
		            	driver.switchTo().window(tituloJanela);
		            }

		            extrator.extrairInformacoesProcesso(ultimoSequencialGravado, paProcesso, unidadeSelecionada, ultimoAndamentoGravado);

		            driver.close();
		            driver.switchTo().window(tituloJanelaResultados);
		        } // fim do loop em uma página de processos

		        List<WebElement> proximaPagina = driver.findElements(By.xpath("//a[@id = 'lnkInfraProximaPaginaSuperior']"));

		        if (proximaPagina.size() == 0) {
		        	break;
		        } else {
		        	proximaPagina.iterator().next().click();
		        }
	        } // fim do loop de leitura de todos os processos

	        driver.close();
	        driver.switchTo().window(tituloJanelaPrincipal);
        }

        // XXX: fim do loop de unidades disponíveis para o usuário

        // atualiza a última data de carga na tabela de parâmetros
        atualizaUltimaDataCarga(conexao, dataFinal);
        MyUtils.appendLogArea(logArea, "Fim da carga...");

        driver.close();
        driver.quit();
	}
	
	private boolean processoOk(Connection conexao, String numeroProcesso) throws Exception {
		String sql = "select indicadorandamentoalterado from processo "
				   + " where numeroprocesso = '" + numeroProcesso + "' ";
		boolean retorno = true;

		Statement consulta = conexao.createStatement();
		ResultSet rs = consulta.executeQuery(sql);
		while (rs.next()) {
			retorno = !rs.getBoolean("indicadorandamentoalterado");
		}

		consulta.close();

		return retorno;
	}

	private void gravarUnidadeSelecionada(Connection conexao, String unidadeSelecionada) throws SQLException {
		String sql = "";
		sql += "insert into unidade (nome)";
		sql += "select '" + unidadeSelecionada + "'";
		sql += " where not exists (select 1 from unidade where nome = '" + unidadeSelecionada + "')";

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		cmd.close();
	}

//	private static int refazerFluxoProcesso(Connection conexao, String numeroProcesso, String unidadeSelecionada) throws Exception {
//		String sql = "select gerafluxoprocesso('" + numeroProcesso + "', '" + unidadeSelecionada + "')";
//		int retorno = 0;
//
//		Statement consulta = conexao.createStatement();
//		ResultSet rs = consulta.executeQuery(sql);
//		while (rs.next()) {
//			retorno = rs.getInt(1);
//		}
//		
//		return retorno;
//	}

	private static void atualizaUltimaDataCarga(Connection conexao, String dataFinal) throws Exception {
		String sql = "";
		sql += "update parametro ";
		sql += "   set conteudo = '" + dataFinal + "'";
		sql += " where parametroid = 1";

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();
	}

	private static String obterDataUltimaCargaSEI(Connection conexao) throws SQLException {
		String sql = "select conteudo from parametro where parametroid = 1";
		String dataUltimaCarga = "";

		try {
			Statement consulta = conexao.createStatement();
			ResultSet rs = consulta.executeQuery(sql);
			while (rs.next()) {
				dataUltimaCarga = rs.getString("conteudo");
			}
			consulta.close();
		} catch (SQLException e) {
			System.out.println("Erro: " + e.getMessage());
			e.printStackTrace();
		}

		return dataUltimaCarga;
	}

//	private List<ProcessoAndamento> obterProcessosParaGeracaoFluxo(Connection conexao, String unidade) throws Exception {
//		StringBuilder sql = new StringBuilder("");
//		sql.append("select numeroprocesso ");
//		sql.append("  from processo p ");
//		sql.append(" where exists (select 1 from processoandamento pa where pa.numeroprocesso = p.numeroprocesso and pa.desconsiderar = true)");
//
//		Statement consulta = conexao.createStatement();
//		ResultSet rs = consulta.executeQuery(sql.toString());
//
//		List<ProcessoAndamento> retorno = new ArrayList<ProcessoAndamento>();
//		
//		while (rs.next()) {
//			retorno.add(new ProcessoAndamento(null, rs.getString("numeroprocesso"), null, null, null, null, null));
//		}
//		
//		return retorno;
//	}
}
