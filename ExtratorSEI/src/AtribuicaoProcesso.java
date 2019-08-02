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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

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
		super("Atribuição de Processos no SEI");
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;

		Map<String, String> propriedades = obterPropriedades();

		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usuário:");
		JTextField txtUsuario = new JTextField(15);
		lblUsuario.setLabelFor(txtUsuario);

		JCheckBox chkDistribuirPorQuantidade = new JCheckBox("Distribuir por quantidade");
		JCheckBox chkDistribuirNaoVisualizado = new JCheckBox("Distribuir não visualizado", true);
		JCheckBox chkSalvarDistribuicao = new JCheckBox("Salvar distribuição feita", true);

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
							appendLogArea(logArea, "Erro ao processar a carga: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
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

		appendLogArea(logArea, "Iniciando o navegador web...");
		System.out.println("Começando aqui...");
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

    	// clica no botão de controle de processos para abrir a página principal do SEI, onde está o menu de opções
    	WebElement btnControleProcessos = encontrarElemento(wait5, By.id("lnkControleProcessos"));
    	btnControleProcessos.click();
    	
        // obtem a unidade selecionada
        String unidadeSelecionada = driver.findElement(By.xpath("//select[@id = 'selInfraUnidades']/option[@selected = 'selected']")).getText();

		// mudar para visualização detalhada, para trazer todos os processos juntos em uma única lista
		WebElement lnkVisualizacaoDetalhada = encontrarElemento(wait5, By.xpath("//a[contains(text(), 'Visualização detalhada')]"));
		lnkVisualizacaoDetalhada.click();
		processosPorUsuario = new LinkedHashMap<String, Integer>();

        // percorre a lista de processos recebidos em busca dos que ainda não foram visualizados
        while (true) {
        	List<WebElement> processos = driver.findElements(By.xpath("//table[@id = 'tblProcessosDetalhado']/tbody/tr"));
        	int l = 0;
        	
        	for (WebElement processo : processos) {
        		if (l++ == 0) continue;
        		String numeroProcesso = processo.findElement(By.xpath("./td[3]")).getText();
        		String atribuidoPara = processo.findElement(By.xpath("./td[4]")).getText().replace("(", "").replace(")", "");
        		// System.out.println("Processo: " + numeroProcesso + " - Atribuído para: " + atribuidoPara);
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

        // retirar da lista de quantidade, os usuários para os quais os processos não são atribuíveis
        retirarUsuariosNaoAtribuiveis(processosPorUsuario, unidadeSelecionada);
        ExtracaoInformacoesSEI extratorSEI = new ExtracaoInformacoesSEI(conexao, driver, false, logArea);

        // percorre a lista de processos que precisam ser atribuídos para verificar quais já passaram pela unidade e redistribuí-los para o usuário anterior
        for (String processoNaoAtribuido : processosNaoAtribuidos) {
        	ProcessoAndamento processoAndamento = new ProcessoAndamento() {{ setNumeroProcesso(processoNaoAtribuido); }};

    		driver.switchTo().defaultContent();
    		// encontra o botão de pesquisa de processos
    		WebElement caixaPesquisa = driver.findElement(By.id("txtPesquisaRapida"));
    		caixaPesquisa.sendKeys(processoNaoAtribuido);
    		caixaPesquisa.sendKeys(Keys.RETURN);

    		int ultimoSequencialGravado = extratorSEI.obterUltimoSequencialGravado(conexao, processoAndamento, unidadeSelecionada);
    		ProcessoAndamento ultimoAndamentoGravado = extratorSEI.obterUltimoAndamentoGravado(conexao, processoNaoAtribuido);

    		// antes de atribuir, lê o log de andamentos para pegar as últimas atualizações disponíveis
    		extratorSEI.extrairInformacoesProcesso(ultimoSequencialGravado, processoAndamento, unidadeSelecionada, ultimoAndamentoGravado);

    		// obtem o usuário para o qual será atribuído o processo
    		String[] resultadoAtribuicao = obterUsuarioASerAtribuido(processoNaoAtribuido, unidadeSelecionada, distribuirPorQuantidade);
        	String cpfAtribuicao = resultadoAtribuicao[0];

        	// se não foi possível atribuir automaticamente, retorna a informação para o usuário
        	if (cpfAtribuicao.equals("")) {
        		appendLogArea(logArea, resultadoAtribuicao[1]);
        		continue;
        	}

    		driver.switchTo().defaultContent();
    		driver.switchTo().frame("ifrVisualizacao");
    		WebElement botaoAtribuir = encontrarElemento(wait5, By.xpath("//img[@title = 'Atribuir Processo']"));
    		botaoAtribuir.click();

    		// encontra a opção correspondente ao usuário a ser atribuído
    		WebElement opcaoUsuario = null;
    		try {
    			opcaoUsuario = encontrarElemento(wait5, By.xpath("//select[@id = 'selAtribuicao']/option[contains(text(), '" + cpfAtribuicao + "')]"));
    		} catch (Exception e) {
    			appendLogArea(logArea, "Não foi encontrada na lista a opção referente ao CPF " + cpfAtribuicao + ". A atribuição deve ser feita manualmente.");
    			continue;
    		}

    		Select selecaoUsuario = new Select(driver.findElement(By.xpath("//select[@id = 'selAtribuicao']")));
    		selecaoUsuario.selectByValue(opcaoUsuario.getAttribute("value"));

    		WebElement botaoSalvar = driver.findElement(By.id("sbmSalvar"));
    		appendLogArea(logArea, "Processo " + processoNaoAtribuido + " atribuído para " + cpfAtribuicao);
    		atualizarQuantidadeProcessos(cpfAtribuicao);
    		if (salvarDistribuicao) {
    			botaoSalvar.click();
    		}
        }

        System.out.println(processosPorUsuario.toString());
        
		appendLogArea(logArea, "Fim do processamento...");

        driver.close();
        driver.quit();
	}
	
	private void retirarUsuariosNaoAtribuiveis(Map<String, Integer> mapaDeProcessosPorUsuario, String unidadeSelecionada) throws SQLException {
		for (Iterator<Map.Entry<String, Integer>> it = mapaDeProcessosPorUsuario.entrySet().iterator(); it.hasNext();) {
			String cpf = it.next().getKey();
			if (!usuarioPodeReceberProcessos(cpf, unidadeSelecionada)) {
				it.remove();
			}
		}
	}

	private boolean usuarioPodeReceberProcessos(String cpf, String unidadeSelecionada) throws SQLException {
		boolean retorno;
		String sql = "";
		sql += "select us.cpf from usuario us ";
		sql += " inner join unidade un using (unidadeid) ";
		sql += " where us.cpf = '" + cpf + "'";
		sql += "   and un.nome = '" + unidadeSelecionada + "'";

		Statement consulta = conexao.createStatement();
		ResultSet rs = consulta.executeQuery(sql);
		retorno = rs.next();
		consulta.close();

		return retorno;
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
		// buscar o último andamento onde houve classificação temática, desde que este último andamento tenha data superior à data gravada na tabela processo x unidade
		Statement consulta = conexao.createStatement();
		ResultSet rs;
		String sql = null;
		String tema = null;
		String dataHoraUltimaClassificacaoTematica = "1900-01-01";

		// obtem a última data em que a classificação temática foi processada
		sql =  "select pu.datahoraultimaclassificacaotematica ";
		sql += "  from processounidade pu ";
		sql += " inner join processo p using (processoid) ";
		sql += " inner join unidade u using (unidadeid) ";
		sql += "where p.processo = '" + numeroProcesso + "' ";
		sql += "  and u.nome = '" + unidade + "'";

		rs = consulta.executeQuery(sql);
		while (rs.next()) {
			tema = rs.getString("datahoraultimaclassificacaotematica");
		}

		// obtem a classificação temática mais recente depois da última que tenha sido realizada para o processo
		sql  = "select trim(substr(descricao, instr(descricao, ':') + 1)) as tema ";
		sql += "  from processoandamento ";
		sql += " where descricao like 'tema:%' ";
		sql += "   and datahora > '" + dataHoraUltimaClassificacaoTematica + "' ";
		sql += "   and lower(trim(substr(descricao, instr(descricao, ':') + 1))) in (select lower(gt.descricao) from grupotematico gt ";
		sql += "																	  inner join unidade u using (unidadeid) ";
		sql += "																	  where u.nome = '" + unidade + "') ";
		sql += " order by datahora desc ";
		sql += " limit 1 ";

		// se não retornou nada, indica que não houve nenhuma classificação temática após a última data processada
		rs = consulta.executeQuery(sql);
		boolean nenhumResultado = true;

		while (rs.next()) {
			nenhumResultado = false;
			tema = rs.getString("tema");
		}

		if (nenhumResultado) {
			return new String[] { null, "O processo '" + numeroProcesso + "' não possui nenhuma classificação temática para a unidade '" + unidade + "' após " + dataHoraUltimaClassificacaoTematica + "." };
		}

		// busca os usuários que compõem o grupo temático
		sql  = "";
		sql += "select cpf from usuariogrupotematico ugt ";
		sql += " inner join grupotematico gt using (grupotematicoid) ";
		sql += " inner join unidade un using (unidadeid) ";
		sql += " inner join usuario us using (usuarioid) ";
		sql += " where lower(gt.descricao) = '" + tema + "' ";
		sql += "   and un.nome = '" + unidade + "' ";
		sql += "   and ugt.usuarioid = pt.usuarioidatribuido ";
		sql += "   and ugt.ativo = true ";

		rs = consulta.executeQuery(sql);
		String usuariosGrupoTematico = "";

		while (rs.next()) {
			usuariosGrupoTematico += rs.getString("cpf").concat("|");
		}

		// se encontrou o tema, busca o último usuário do grupo temático para quem este processo esteve atribuído
		sql  = "";
		sql += "select u.cpf ";
		sql += "  from usuario u ";
		sql += " inner join processotramite pt on pt.usuarioidatribuido = u.usuarioid ";
		sql += " inner join processo p using (processoid) ";
		sql += " inner join unidade un on pt.unidadeid = un.unidadeid ";
		sql += " where un.nome = '" + unidade + "' ";
		sql += "   and p.processo = '" + numeroProcesso + "' ";
		sql += "   and instr('" + usuariosGrupoTematico + "', u.cpf) > 0 ";
		sql += " order by pt.datainicio desc ";
		sql += " limit 1 ";

		String cpf = null;
		String msgRetorno = null;

		while (rs.next()) {
			cpf = rs.getString("cpf");
		}

		consulta.close();

		// atribui o processo ao usuário que possui menos processos abertos
		if (cpf == null && distribuirPorQuantidade) {
			Integer quantidade = null;
			msgRetorno = "Não foi possível determinar o usuário para o processo '" + numeroProcesso + "'. Verifique se há algum usuário vinculado ao grupo temático '" + tema + "' na unidade '" + unidade + "'.";

			for (String cpfUsuario : processosPorUsuario.keySet()) {
				if (usuariosGrupoTematico.contains(cpfUsuario) && (quantidade == null || processosPorUsuario.get(cpfUsuario).intValue() < quantidade.intValue())) {
					quantidade = processosPorUsuario.get(cpfUsuario).intValue();
					cpf = cpfUsuario;
					msgRetorno = null;
				}
			}
		}

		return new String[] { cpf, msgRetorno };
	}

	private static WebElement encontrarElemento(Wait<WebDriver> wait, By by) {
		return wait.until(new Function<WebDriver, WebElement>() {
			@Override
			public WebElement apply(WebDriver t) {
				WebElement element = t.findElement(by);
				if (element == null) {
					System.out.println("Elemento não encontrado...");
				}
				return element;
			}
		});
	}

	private void appendLogArea(JTextArea logArea, String msg) {
		logArea.append(msg + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
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
			JOptionPane.showMessageDialog(null, "Erro ao obter propriedades para a carga de informações do SEI. Verifique se o arquivo 'extratorsei.properties' existe no diretório da aplicação: \n \n" + e.getMessage());
			e.printStackTrace();
		}

		return retorno;
	}
}
