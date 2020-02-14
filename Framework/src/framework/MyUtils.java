package framework;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.sql.rowset.CachedRowSet;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import com.sun.rowset.CachedRowSetImpl;

@SuppressWarnings("restriction")
public class MyUtils {

	public static boolean arquivoExiste(String arquivo) {
		File file = new File(arquivo);
		return file.exists();
	}
	
	public static Properties obterPropriedades(String nomeArquivo) {
		Properties retorno = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(nomeArquivo);
			retorno.load(input);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao ler o arquivo de propriedades da aplicação. Verifique se o arquivo '" + nomeArquivo + "' existe no diretório da aplicação: \n \n" + e.getMessage());
			e.printStackTrace();
		}

		return retorno;
	}

	public static void salvarPropriedades(Properties prop, String nomeArquivo) {
		OutputStream output = null;

		try {
			output = new FileOutputStream(nomeArquivo);
			prop.store(output, "");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao gravar o arquivo de propriedades da aplicação. Verifique se o arquivo '" + nomeArquivo + "' existe no diretório da aplicação: \n \n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public static Vector<Vector<Object>>obterDados(ResultSet rs) throws Exception {
		Vector<Vector<Object>> retorno = new Vector<Vector<Object>>();

		while (rs.next()) {
			Vector<Object> linha = new Vector<Object>();
			linha.add(Boolean.FALSE);
			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
				linha.add(rs.getObject(i));
			}
			retorno.add(linha);
		}

		return retorno;
	}

	public static Vector<Vector<Object>>obterDados(List<Object[]> dados) throws Exception {
		Vector<Vector<Object>> retorno = new Vector<Vector<Object>>();

		for (Object[] dado : dados) {
			Vector<Object> linha = new Vector<Object>();
			linha.add(Boolean.FALSE);
			for (int i = 0; i < dado.length; i++) {
				linha.add(dado[i]);
			}
			retorno.add(linha);
		}

		return retorno;
	}

	public static <T> Vector<Vector<Object>>obterDados(List<T> dados, String... campos) throws Exception {
		Vector<Vector<Object>> retorno = new Vector<Vector<Object>>();

		for (T dado : dados) {
			Vector<Object> linha = new Vector<Object>();
			linha.add(Boolean.FALSE);
			for (int i = 0; i < campos.length; i++) {
				Object obj = null;
				try {
					obj = PropertyUtils.getNestedProperty(dado, campos[i]);
				} catch (NestedNullException e) {
				}
				linha.add(obj);
			}
			retorno.add(linha);
		}

		return retorno;
	}

//	public static <T> Vector<Vector<Object>>obterDados(List<T> dados, String... campos) throws Exception {
//		Vector<Vector<Object>> retorno = new Vector<Vector<Object>>();
//
//		for (T dado : dados) {
//			Vector<Object> linha = new Vector<Object>();
//			linha.add(Boolean.FALSE);
//			for (int i = 0; i < campos.length; i++) {
//				Method metodo = dado.getClass().getMethod(campos[i]);
//				linha.add(metodo.invoke(dado));
//			}
//			retorno.add(linha);
//		}
//
//		return retorno;
//	}

	public static Vector<Object>obterTitulosColunas(List<MyTableColumn> colunas) throws Exception {
		Vector<Object> retorno = new Vector<Object>();

		for (MyTableColumn coluna : colunas) {
			retorno.add(coluna.getCaption());
		}

		return retorno;
	}

	public static int comboBoxItemIndex(MyComboBox comboBox, Integer intId, String stringId) {
		int retorno = 0;
		for (int i = 0; i < comboBox.getItemCount(); i++) {
			ComboBoxItem item = (ComboBoxItem) comboBox.getItemAt(i);
			if ((intId != null && item.getIntId().equals(intId)) || (stringId != null && item.getStringId().equals(stringId))) {
				retorno = i;
				break;
			}
		}
		return retorno;
	}

	public static WebElement encontrarElemento(Wait<WebDriver> wait, By by) {
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

	public static List<WebElement> encontrarElementos(Wait<WebDriver> wait, By by) {
		return wait.until(new Function<WebDriver, List<WebElement>>() {
			@Override
			public List<WebElement> apply(WebDriver t) {
				List<WebElement> elements = t.findElements(by);
				if (elements == null) {
					System.out.println("Elemento não encontrado...");
				}
				return elements;
			}
		});
	}

	public static boolean tabelaExiste(Connection conexao, String nomeTabela) throws Exception {
		boolean retorno = false;
		String sql = "select * from sqlite_master where tbl_name = '" + nomeTabela + "'";

		try {
			ResultSet rs = MyUtils.executeQuery(conexao, sql);
			retorno = rs.next();
		} catch (Exception e) {
			System.out.println("Erro: " + e.getMessage());
			e.printStackTrace();
		}
		
		return retorno;
	}

	public static boolean tabelaExiste(EntityManager conexao, String nomeTabela) {
		boolean retorno = false;
		String sql = "select * from sqlite_master sm where tbl_name = '" + nomeTabela + "'";

		try {
			List<Object[]> rs = JPAUtils.executeNativeQuery(conexao, sql);
			retorno = !rs.isEmpty();
		} catch (Exception e) {
			System.out.println("Erro: " + e.getMessage());
			e.printStackTrace();
		}
		
		return retorno;
	}

	public static void appendLogArea(JTextArea logArea, String msg) {
		System.out.println(msg);
		logArea.append(msg + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	public static Integer idItemSelecionado(MyComboBox comboBox) {
		return ((ComboBoxItem) comboBox.getSelectedItem()).getIntId();
	}

	public static String idStringItemSelecionado(MyComboBox comboBox) {
		return ((ComboBoxItem) comboBox.getSelectedItem()).getStringId();
	}
	
	public static void insereOpcoesComboBox(Connection conexao, MyComboBox comboBox, String sql) {
		insereOpcoesComboBox(conexao, comboBox, sql, new ArrayList<ComboBoxItem>());
	}
	
	public static void insereOpcoesComboBox(Connection conexao, MyComboBox comboBox, String sql, List<ComboBoxItem> itensInicioLista) {
		// limpa os itens da lista
		comboBox.removeAllItems();

		// insere os itens adicionais ao início da lista
		for (ComboBoxItem itemInicioLista : itensInicioLista) {
			comboBox.addItem(itemInicioLista);
		}

		// executa a query para inserir os itens a serem lidos de tabela
		try {
			ResultSet rs = MyUtils.executeQuery(conexao, sql);
			while (rs.next()) {
				comboBox.addItem(new ComboBoxItem(rs.getInt(1), null, rs.getString(2)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ResultSet executeQuery(Connection conexao, String sql) throws Exception {
		Statement cmd = conexao.createStatement();
		CachedRowSet rset = new CachedRowSetImpl();
		rset.populate(cmd.executeQuery(sql));
		cmd.close();
		return rset;
	}

	public static void execute(Connection conexao, String sql) throws Exception {
		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		cmd.close();
	}

	public static String verificacaoDeEspacoEmDisco(long espacoMinimo) {
		File arquivo = new File(".").getAbsoluteFile();
		File raiz = arquivo.getParentFile();
		while (raiz.getParentFile() != null) {
		    raiz = raiz.getParentFile();
		}
		long espacoEmDisco = raiz.getUsableSpace() / 1024 / 1024;
		if (espacoEmDisco < espacoMinimo) {
			return "O espaço no disco " + raiz.getPath() + " é de " + espacoEmDisco + " mb. Libere pelo menos " + espacoMinimo + " mb de espaço em disco antes de iniciar este processo.";
		} else {
			return null;
		}
	}

	public static Date obterData(String data, String mascara) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(mascara);
		sdf.setLenient(false);
		return sdf.parse(data);
	}
	
	public static String formatarData(Date data, String formato) {
		SimpleDateFormat f = new SimpleDateFormat(formato, new Locale("pt", "BR"));
		return f.format(data);
	}

	public static void fecharPopup(WebDriver driver) {
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
	}

	public static void selecionarUnidade(WebDriver driver, Wait<WebDriver> wait, String unidade) {
		driver.switchTo().defaultContent();
    	Select cbxUnidade = new Select(MyUtils.encontrarElemento(wait, By.id("selInfraUnidades")));
    	cbxUnidade.selectByVisibleText(unidade);
	}

	public static boolean isMacOS() {
		if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isWindows() {
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
			return true;
		} else {
			return false;
		}
	}

	public static String chromeWebDriverPath() {
		if (isMacOS()) {
			return "../commons/resources/chromedriver/macos/chromedriver";
		} else {
			return "../commons/resources/chromedriver/windows/chromedriver.exe";
		}
	}

	public static String firefoxWebDriverPath() {
		if (isMacOS()) {
			return "../commons/resources/firefoxdriver/macos/geckodriver";
		} else {
			return "../commons/resources/firefoxdriver/windows/geckodriver.exe";
		}
	}

	public static <T> void insereOpcoesComboBox(MyComboBox comboBox, List<T> opcoes) {
		comboBox.removeAllItems();

		for (T opcao : opcoes) {
			ItemComboBox item = (ItemComboBox) opcao;
			comboBox.addItem(new ComboBoxItem(item.getIntegerItemValue(), item.getStringItemValue(), item.getItemLabel()));
		}
	}

	public static String obterValorCelula(Cell cell) {
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
	            case STRING:
	                return cell.getStringCellValue();
	            case BOOLEAN:
	                return ((Object) cell.getBooleanCellValue()).toString();
	            case NUMERIC:	
	                return (new BigDecimal(((Object) cell.getNumericCellValue()).toString())).toPlainString();
				default:
					break;
            }
        }

        return null;
    }

	public static String obterValorCelula(Row row, int colNumber) {
		if (colNumber < 0) return "";
		if (row.getCell(colNumber) == null) return "";
		String retorno = obterValorCelula(row.getCell(colNumber));
		if (retorno == null) return "";
		else return retorno;
    }

	public static String obterValorCelula(Row row, int colNumber, DataFormatter df) {
		if (colNumber < 0) return "";
		if (row.getCell(colNumber) == null) return "";
		String retorno = df.formatCellValue(row.getCell(colNumber));
		if (retorno == null) return "";
		else return retorno;
    }

	public static String retiraAcento(String s) {
		return s.replace("Ç", "C")
				.replace("Á", "A")
				.replace("À", "A")
				.replace("É", "E")
				.replace("Í", "I")
				.replace("Ó", "O")
				.replace("Ú", "U")
				.replace("Â", "A")
				.replace("Ê", "E")
				.replace("Ô", "O")
				.replace("Ã", "A")
				.replace("Õ", "O")
				.replace("Ü", "U")
				.replace("º", "o")
				.replace("°", "o");
	}
	
	public static <T> T entidade(List<T> listaEntidades) {
		if (listaEntidades == null || listaEntidades.isEmpty()) return null;
		else return listaEntidades.iterator().next();
	}

	public static String emptyStringIfNull(Object obj) {
		if (obj == null) return "";
		else return obj.toString();
	}
	
	public static void esperarCarregamento(int esperaInicialEmMilissegundos, Wait<WebDriver> wait, String xpath) throws Exception {
        TimeUnit.MILLISECONDS.sleep(esperaInicialEmMilissegundos);

        WebElement infCarregando = null;
        do {
        	try {
        		infCarregando = MyUtils.encontrarElemento(wait, By.xpath(xpath));
        	} catch (Exception e) {
        		infCarregando = null;
        	}
        	try {
	        	if (infCarregando == null || !infCarregando.isDisplayed()) {
	        		break;
	        	}
        	} catch (StaleElementReferenceException e) {
        		break;
        	}
        } while (true);
	}

	public static ArrayList<File> obterArquivos(String nomeDiretorio) {
		return obterArquivos(nomeDiretorio, false, "pdf");
	}

	public static ArrayList<File> obterArquivos(String nomeDiretorio, boolean incluirPastas, String... extensoes) {
		ArrayList<File> retorno = new ArrayList<File>();
		File diretorio = new File(nomeDiretorio);
		for (File arquivo : diretorio.listFiles()) {
			if (!incluirPastas && arquivo.isDirectory()) continue;
			if (!arquivo.isDirectory() && extensoes != null && extensoes.length > 0 && !Arrays.asList(extensoes).stream().anyMatch(FilenameUtils.getExtension(arquivo.getName())::equalsIgnoreCase)) continue;
			retorno.add(arquivo);
		}
		return retorno;
	}

	public static void criarDiretorioBackup(String caminho) {
		File diretorio = new File(caminho + "\\bkp");
		if (!diretorio.exists()) {
			diretorio.mkdir();
		}
	}

	public static void acceptSecurityAlert(WebDriver driver) {
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

	public static void apagarArquivo(String nomeArquivo, int numeroTentativas) throws Exception {
		File arquivo = null;
		int tentativa = 0;

		do {
			arquivo = new File(nomeArquivo);

			if (arquivo.exists()) {
				if (arquivo.delete()) {
					break;
				}
			} else {
				break;
			}
			TimeUnit.SECONDS.sleep(1);
		} while (tentativa++ < numeroTentativas);
		
		// verifica se, mesmo depois de excluído, o arquivo ainda existe; se sim, retorna erro
		if (arquivo.exists()) {
			throw new Exception ("A exclusão do arquivo " + nomeArquivo + " falhou.");
		}
	}
	
	public static void renomearArquivo(String nomeArquivoAnterior, String nomeArquivoNovo, int numeroTentativas, boolean adicionarNumeracao) throws Exception {
		File arquivoAnterior = null;
		File arquivoNovo = null;
		int tentativa = 0;

		do {
			arquivoAnterior = new File(nomeArquivoAnterior);

			if (arquivoAnterior.exists() && arquivoAnterior.length() > 0) {
				arquivoNovo = new File(nomeArquivoNovo);

				if (arquivoNovo.exists()) {
					if (adicionarNumeracao) {
						int cont = 1;

						do {
							String nomeArquivoNovoNumerado = nomeArquivoNovo.substring(0, nomeArquivoNovo.lastIndexOf(".")) + " (" + (cont++) + ")" + nomeArquivoNovo.substring(nomeArquivoNovo.lastIndexOf("."));
							arquivoNovo = new File(nomeArquivoNovoNumerado);
						} while (arquivoNovo.exists());
					} else {
						apagarArquivo(nomeArquivoNovo, 2);
					}
				}

				if (arquivoAnterior.renameTo(arquivoNovo)) {
					arquivoNovo = new File(nomeArquivoNovo);
					break;
				}
			}
			TimeUnit.SECONDS.sleep(1);
		} while (tentativa++ < numeroTentativas);
		
		// verifica se, depois de renomeado, o arquivo novo existe no caminho de destino
		if (!arquivoNovo.exists()) {
			throw new Exception ("A movimentação do arquivo " + nomeArquivoAnterior + " para o arquivo " + nomeArquivoNovo + " falhou.");
		}
	}

	public static void salvarConfiguracaoLocal(String propriedade, String valor, String mensagemSucesso) {
		String arquivoPropriedades = System.getProperty("user.home");
		// se retornou o nome do diretório, continua
		if (!MyUtils.emptyStringIfNull(arquivoPropriedades).trim().equals("")) {
			// continua se o diretório existir
			if (MyUtils.arquivoExiste(arquivoPropriedades)) {
				// adiciona o nome da pasta escondida de ferramentas SPU
				arquivoPropriedades += "\\.ferramentasspu";
				// se a pasta não existe, cria antes de continuar
				if (!MyUtils.arquivoExiste(arquivoPropriedades)) {
					(new File(arquivoPropriedades)).mkdir();
				}

				// verifica se o arquivo de propriedades existe
				arquivoPropriedades += "\\ferramentasspu.properties";
				Properties props = new Properties();
				if (MyUtils.arquivoExiste(arquivoPropriedades)) {
					props = MyUtils.obterPropriedades(arquivoPropriedades);
				}
				props.setProperty(propriedade, valor);
				MyUtils.salvarPropriedades(props, arquivoPropriedades);
				if (mensagemSucesso != null) JOptionPane.showMessageDialog(null, mensagemSucesso);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Não foi possível obter o nome da pasta do usuário desta estação de trabalho");
		}
	}

	public static String obterConfiguracaoLocal(String propriedade, String valorDefault) {
		String arquivoPropriedades = System.getProperty("user.home");
		String retorno = null;
		// se retornou o nome do diretório, continua
		if (!MyUtils.emptyStringIfNull(arquivoPropriedades).trim().equals("")) {
			// continua se o diretório existir
			if (MyUtils.arquivoExiste(arquivoPropriedades)) {
				// adiciona o nome da pasta escondida de ferramentas SPU
				arquivoPropriedades += "\\.ferramentasspu";
				// continua se a pasta existe
				if (MyUtils.arquivoExiste(arquivoPropriedades)) {
					// verifica se o arquivo de propriedades existe
					arquivoPropriedades += "\\ferramentasspu.properties";
					if (MyUtils.arquivoExiste(arquivoPropriedades)) {
						Properties props = MyUtils.obterPropriedades(arquivoPropriedades);
						retorno = props.getProperty(propriedade);
					}
				}
			}
		}

		return (retorno == null ? valorDefault : retorno);
	}
}
