import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class AtribuicaoProcesso extends JInternalFrame {

	private Connection conexao;
	
	private Map<String, Integer> processosPorUsuario;
	
	public AtribuicaoProcesso(String tituloJanela, Connection conexao) {
		super("Atribui��o de Processos no SEI");
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;

		Map<String, String> propriedades = obterPropriedades();

		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usu�rio:");
		JTextField txtUsuario = new JTextField(15);
		lblUsuario.setLabelFor(txtUsuario);

		JCheckBox chkDistribuirPorQuantidade = new JCheckBox("Distribuir por quantidade");
		JCheckBox chkDistribuirNaoVisualizado = new JCheckBox("Distribuir n�o visualizado", true);
		JCheckBox chkSalvarDistribuicao = new JCheckBox("Salvar distribui��o feita", true);

		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15);
		lblSenha.setLabelFor(txtSenha);

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = new JButton("Processar"); 
		JButton botaoSair = new JButton("Sair"); 

		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(chkDistribuirPorQuantidade);
		painelDados.add(new JPanel());
		painelDados.add(chkDistribuirNaoVisualizado);
		painelDados.add(new JPanel());
		painelDados.add(chkSalvarDistribuicao);
		painelDados.add(new JPanel());
		painelDados.add(botaoProcessar); 
		painelDados.add(botaoSair); 

		SpringUtilities.makeGrid(painelDados,
                6, 2, //rows, cols
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
							atribuirProcessos(AtribuicaoProcesso.this.conexao, logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), propriedades, chkDistribuirPorQuantidade.isSelected(), chkDistribuirNaoVisualizado.isSelected(), chkSalvarDistribuicao.isSelected());
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

	private void atribuirProcessos(Connection conexao, JTextArea logArea, String usuario, String senha, Map<String, String> propriedades, boolean distribuirPorQuantidade, boolean distribuirNaoVisualizado, boolean salvarDistribuicao) throws Exception {
		List<String> processosNaoAtribuidos = new ArrayList<String>();

		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
        WebDriver driver = new ChromeDriver();

        // And now use this to visit Google
        driver.get(propriedades.get("endereco_sei"));

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

    	// clica no bot�o de controle de processos para abrir a p�gina principal do SEI, onde est� o menu de op��es
    	WebElement btnControleProcessos = MyUtils.encontrarElemento(wait5, By.id("lnkControleProcessos"));
    	btnControleProcessos.click();
    	
        // obtem a unidade selecionada
        String unidadeSelecionada = driver.findElement(By.xpath("//select[@id = 'selInfraUnidades']/option[@selected = 'selected']")).getText();

		// mudar para visualiza��o detalhada, para trazer todos os processos juntos em uma �nica lista
		WebElement lnkVisualizacaoDetalhada = MyUtils.encontrarElemento(wait5, By.xpath("//a[contains(text(), 'Visualiza��o detalhada')]"));
		lnkVisualizacaoDetalhada.click();
		processosPorUsuario = new LinkedHashMap<String, Integer>();

        // percorre a lista de processos recebidos em busca dos que ainda n�o foram visualizados
        while (true) {
        	List<WebElement> processos = driver.findElements(By.xpath("//table[@id = 'tblProcessosDetalhado']/tbody/tr[./td]"));
        	
        	for (WebElement processo : processos) {
        		String numeroProcesso = processo.findElement(By.xpath("./td[3]")).getText();
        		String atribuidoPara = processo.findElement(By.xpath("./td[4]")).getText().replace("(", "").replace(")", "");
        		boolean processoNaoVisualizado = processo.findElement(By.xpath("./td[3]/a")).getAttribute("class").contains("processoNaoVisualizado");
        		if (distribuirNaoVisualizado) processoNaoVisualizado = false;

        		if (atribuidoPara.length() <= 1 && !processoNaoVisualizado) {
        			processosNaoAtribuidos.add(numeroProcesso);
        		} else {
        			if (!processoNaoVisualizado) {
        				contarProcessosPorUsuario(processosPorUsuario, atribuidoPara);
        			}
        		}
        	}

        	List<WebElement> proximaPagina = driver.findElements(By.xpath("//a[@id = 'lnkInfraProximaPaginaSuperior']"));

        	if (proximaPagina.size() == 0) {
        		break;
        	} else {
        		proximaPagina.iterator().next().click();
        	}
        }

        System.out.println(processosPorUsuario.toString());

        // retirar da lista de quantidade, os usu�rios para os quais os processos n�o s�o atribu�veis
        // retirarUsuariosNaoAtribuiveis(processosPorUsuario, unidadeSelecionada);
        ExtracaoInformacoesSEI extratorSEI = new ExtracaoInformacoesSEI(conexao, driver, false, logArea);

        // percorre a lista de processos que precisam ser atribu�dos para verificar quais j� passaram pela unidade e redistribu�-los para o usu�rio anterior
        for (String processoNaoAtribuido : processosNaoAtribuidos) {
    		if (!(processoNaoAtribuido.equals("10154.113338/2019-49") 
			   || processoNaoAtribuido.equals("10154.113337/2019-02")
			   || processoNaoAtribuido.equals("10154.113334/2019-61")
			   || processoNaoAtribuido.equals("10154.113331/2019-27")
			   || processoNaoAtribuido.equals("10154.113327/2019-69")
			   )) continue;

        	ProcessoAndamento processoAndamento = new ProcessoAndamento() {{ setNumeroProcesso(processoNaoAtribuido); }};

    		driver.switchTo().defaultContent();
    		// encontra o bot�o de pesquisa de processos
    		WebElement caixaPesquisa = driver.findElement(By.id("txtPesquisaRapida"));
    		caixaPesquisa.sendKeys(processoNaoAtribuido);
    		caixaPesquisa.sendKeys(Keys.RETURN);

    		int ultimoSequencialGravado = extratorSEI.obterUltimoSequencialGravado(conexao, processoAndamento, unidadeSelecionada);
    		ProcessoAndamento ultimoAndamentoGravado = extratorSEI.obterUltimoAndamentoGravado(conexao, processoNaoAtribuido);

    		// antes de atribuir, l� o log de andamentos para pegar as �ltimas atualiza��es dispon�veis
    		extratorSEI.extrairInformacoesProcesso(ultimoSequencialGravado, processoAndamento, unidadeSelecionada, ultimoAndamentoGravado);

    		// obtem o usu�rio para o qual ser� atribu�do o processo
    		String[] resultadoAtribuicao = obterUsuarioASerAtribuido(processoNaoAtribuido, unidadeSelecionada, distribuirPorQuantidade);
        	String cpfAtribuicao = resultadoAtribuicao[1];

        	// se n�o foi poss�vel atribuir automaticamente, retorna a informa��o para o usu�rio
        	if (cpfAtribuicao == null) {
        		MyUtils.appendLogArea(logArea, resultadoAtribuicao[0]);
        		continue;
        	}

    		driver.switchTo().defaultContent();
    		driver.switchTo().frame("ifrArvore");
    		WebElement linkNumeroProcesso = MyUtils.encontrarElemento(wait5, By.xpath("//span[contains(text(), '" + processoNaoAtribuido + "')]"));
    		linkNumeroProcesso.click();

    		driver.switchTo().defaultContent();
    		driver.switchTo().frame("ifrVisualizacao");
    		WebElement botaoAtribuir = MyUtils.encontrarElemento(wait5, By.xpath("//img[@title = 'Atribuir Processo']"));
    		botaoAtribuir.click();

    		// encontra a op��o correspondente ao usu�rio a ser atribu�do
    		WebElement opcaoUsuario = null;
    		try {
    			opcaoUsuario = MyUtils.encontrarElemento(wait5, By.xpath("//select[@id = 'selAtribuicao']/option[contains(text(), '" + cpfAtribuicao + "')]"));
    		} catch (Exception e) {
    			MyUtils.appendLogArea(logArea, "N�o foi encontrada na lista a op��o referente ao CPF " + cpfAtribuicao + ". A atribui��o deve ser feita manualmente.");
    			continue;
    		}

    		Select selecaoUsuario = new Select(driver.findElement(By.xpath("//select[@id = 'selAtribuicao']")));
    		selecaoUsuario.selectByValue(opcaoUsuario.getAttribute("value"));

    		WebElement botaoSalvar = driver.findElement(By.id("sbmSalvar"));
    		MyUtils.appendLogArea(logArea, "Processo " + processoNaoAtribuido + " atribu�do para " + cpfAtribuicao);
    		atualizarQuantidadeProcessos(cpfAtribuicao);
    		if (salvarDistribuicao) {
    			botaoSalvar.click();
    			atualizarDataHoraTema(conexao, processoAndamento, unidadeSelecionada, resultadoAtribuicao[3]);
    		}
        }

        System.out.println(processosPorUsuario.toString());
        
        MyUtils.appendLogArea(logArea, "Fim do processamento...");

        driver.close();
        driver.quit();
	}

	private void contarProcessosPorUsuario(Map<String, Integer> mapaDeProcessosPorUsuario, String atribuidoPara) {
		Integer qtdProcessos = mapaDeProcessosPorUsuario.get(atribuidoPara);
		mapaDeProcessosPorUsuario.put(atribuidoPara, (qtdProcessos == null ? 1 : qtdProcessos.intValue() + 1));
	}

	private void atualizarQuantidadeProcessos(String cpfAtribuicao) {
		for (String cpfUsuario : processosPorUsuario.keySet()) {
			if (cpfUsuario.equals(cpfAtribuicao)) {
				processosPorUsuario.put(cpfAtribuicao, processosPorUsuario.get(cpfAtribuicao).intValue() + 1);
				break;
			}
		}
	}

	private String[] obterUsuarioASerAtribuido(String numeroProcesso, String unidade, boolean distribuirPorQuantidade) throws SQLException {
		// buscar o �ltimo andamento onde houve classifica��o tem�tica, desde que este �ltimo andamento tenha data superior � data gravada na tabela processo x unidade
		Statement consulta = conexao.createStatement();
		ResultSet rs;
		String sql = null;
		String tema = null;
		String dataHoraUltimaClassificacaoTematica = null;
		String dataHora = "";

		// obtem a �ltima data em que a classifica��o tem�tica foi processada
		sql =  "select pu.datahoraultimaclassificacaotematica ";
		sql += "  from processounidade pu ";
		sql += " inner join processo p using (processoid) ";
		sql += " inner join unidade u using (unidadeid) ";
		sql += " where p.numeroprocesso = '" + numeroProcesso + "' ";
		sql += "   and u.nome = '" + unidade + "'";

		rs = consulta.executeQuery(sql);
		while (rs.next()) {
			dataHoraUltimaClassificacaoTematica = rs.getString("datahoraultimaclassificacaotematica");
		}

		if (dataHoraUltimaClassificacaoTematica == null) dataHoraUltimaClassificacaoTematica = "1900-01-01";
		
		// obtem a classifica��o tem�tica mais recente depois da �ltima que tenha sido realizada para o processo
		sql  = "select trim(substr(descricao, instr(descricao, ':') + 1)) as tema, datahora ";
		sql += "  from processoandamento ";
		sql += " where numeroprocesso = '" + numeroProcesso + "' ";
		sql += "   and descricao like 'tema:%' ";
		sql += "   and datahora > '" + dataHoraUltimaClassificacaoTematica + "' ";
		sql += "   and lower(trim(substr(descricao, instr(descricao, ':') + 1))) in (select lower(gt.descricao) from grupotematico gt ";
		sql += "																	  inner join unidade u using (unidadeid) ";
		sql += "																	  where u.nome = '" + unidade + "') ";
		sql += " order by datahora desc ";
		sql += " limit 1 ";

		// se n�o retornou nada, indica que n�o houve nenhuma classifica��o tem�tica ap�s a �ltima data processada
		rs = consulta.executeQuery(sql);
		boolean nenhumResultado = true;

		while (rs.next()) {
			nenhumResultado = false;
			tema = rs.getString("tema");
			dataHora = rs.getString("datahora");
		}

		if (nenhumResultado && tema == null) {
			tema = unidade;
			dataHora = "1900-01-01";
			// return new String[] { "O processo '" + numeroProcesso + "' n�o possui nenhuma classifica��o tem�tica para a unidade '" + unidade + "' ap�s " + dataHoraUltimaClassificacaoTematica + ".", null, null, null };
		}

		// busca os usu�rios que comp�em o grupo tem�tico
		sql  = "";
		sql += "select cpf from usuariogrupotematico ugt ";
		sql += " inner join grupotematico gt using (grupotematicoid) ";
		sql += " inner join unidade un using (unidadeid) ";
		sql += " inner join usuario us using (usuarioid) ";
		sql += " where lower(gt.descricao) = '" + tema.toLowerCase() + "' ";
		sql += "   and un.nome = '" + unidade + "' ";
		sql += "   and ugt.ativo = true ";

		rs = consulta.executeQuery(sql);
		String usuariosGrupoTematico = "";

		while (rs.next()) {
			usuariosGrupoTematico += rs.getString("cpf").concat("|");
		}

		// se encontrou o tema, busca o �ltimo usu�rio do grupo tem�tico para quem este processo esteve atribu�do
		sql  = "";
		sql += "select descricao, replace(descricao, 'Processo atribu�do para ', '') as cpf ";
		sql += "  from processoandamento pa ";
		sql += " where pa.numeroprocesso = '" + numeroProcesso + "' ";
		sql += "   and pa.unidade = '" + unidade + "' ";
		sql += "   and (pa.descricao like 'processo atribu�do para%' or pa.descricao like 'removida atribui��o do processo') ";
		sql += " order by pa.datahora desc, pa.sequencial desc ";

		rs = consulta.executeQuery(sql);

		String cpf = null;
		String msgRetorno = null;

		List<String> listaAtribuicoes = new ArrayList<String>();
		boolean incluiProximoRegistro = true;
		
		// percorre a lista de atribui��es e retiradas, eliminando as atribui��es que foram retiradas do processo
		while (rs.next()) {
			if (rs.getString("descricao").startsWith("Removida")) {
				incluiProximoRegistro = false;
			} else {
				if (incluiProximoRegistro) {
					listaAtribuicoes.add(rs.getString("cpf"));
					incluiProximoRegistro = true;
				}
			}
		}

		// percorre a lista de CPFs atribuidos ao processo, buscando o primeiro que perten�a ao grupo tem�tico classificado
		for (String cpfAtribuido : listaAtribuicoes) {
			if (usuariosGrupoTematico.contains(cpfAtribuido.concat("|"))) {
				cpf = cpfAtribuido;
				break;
			}
		}

		consulta.close();

		// atribui o processo ao usu�rio que possui menos processos abertos
		if (cpf == null && distribuirPorQuantidade) {
			Integer quantidade = null;
			msgRetorno = "N�o foi poss�vel determinar o usu�rio para o processo '" + numeroProcesso + "'. Verifique se h� algum usu�rio vinculado ao grupo tem�tico '" + tema + "' na unidade '" + unidade + "'.";

			for (String cpfUsuario : processosPorUsuario.keySet()) {
				if (usuariosGrupoTematico.contains(cpfUsuario.concat("|")) && (quantidade == null || processosPorUsuario.get(cpfUsuario).intValue() < quantidade.intValue())) {
					quantidade = processosPorUsuario.get(cpfUsuario).intValue();
					cpf = cpfUsuario;
					msgRetorno = null;
				}
			}
		}

		return new String[] { msgRetorno, cpf, tema, dataHora };
	}

	private Map<String, String> obterPropriedades() {
		Map<String, String> retorno = new LinkedHashMap<String, String>();

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("extratorsei.properties");
			prop.load(input);
			retorno.put("endereco_sei", prop.getProperty("endereco_sei"));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter propriedades para a carga de informa��es do SEI. Verifique se o arquivo 'extratorsei.properties' existe no diret�rio da aplica��o: \n \n" + e.getMessage());
			e.printStackTrace();
		}

		return retorno;
	}

	private static void atualizarDataHoraTema(Connection conexao, ProcessoAndamento processo, String unidade, String dataHora) throws Exception {
		String sql = "";
		sql += "update processounidade ";
		sql += "   set datahoraultimaclassificacaotematica = '" + dataHora + "'";
		sql += " where processoid = " + processo.sqlBuscaProcesso();
		sql += "   and unidadeid = " + processo.sqlBuscaUnidade(unidade);

		Statement inclusao = conexao.createStatement();
		inclusao.execute(sql);
		inclusao.close();
	}
}
