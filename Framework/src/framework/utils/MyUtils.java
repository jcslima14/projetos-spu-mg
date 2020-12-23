package framework.utils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import framework.components.MyComboBox;
import framework.components.MyTableColumn;
import framework.enums.NivelMensagem;
import framework.exceptions.MyValidationException;
import framework.models.ComboBoxItem;
import framework.models.ItemComboBox;

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

	public static boolean tabelaExiste(EntityManager conexao, String nomeTabela) {
		boolean retorno = false;
		String sql = "select * from sqlite_master sm where tbl_name = '" + nomeTabela + "'";
		if(MyUtils.isPostgreSQL(conexao)) {
			sql = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + nomeTabela + "'";			
		}		

		try {
			List<Object[]> rs = JPAUtils.executeNativeQuery(conexao, sql);
			retorno = !rs.isEmpty();
		} catch (Exception e) {
			System.out.println("Erro: " + e.getMessage());
			e.printStackTrace();
		}
		
		return retorno;
	}

	public static JTextPane obterPainelNotificacoes() {
		JTextPane painel = new JTextPane();
		painel.setContentType("text/html");
		return painel;
	}

	public static void appendLogArea(JTextPane logArea, String msg, NivelMensagem estilo, boolean adicionarDataHora, boolean logarNoConsole, String fontStyle) {
		String tag = "span";
		String style = "display: block; ";

		if (estilo.equals(NivelMensagem.DESTAQUE_NEGRITO) || estilo.equals(NivelMensagem.DESTAQUE_NEGRITO_ITALICO)) style += "font-weight: bold;";
		if (estilo.equals(NivelMensagem.DESTAQUE_ITALICO) || estilo.equals(NivelMensagem.DESTAQUE_NEGRITO_ITALICO)) style += "font-style: italic;";
		if (estilo.equals(NivelMensagem.EXCECAO)) tag = "pre";
		if (estilo.equals(NivelMensagem.ERRO)) style += "color: white; background-color: red; padding: 5; border-radius: 5px;";
		if (estilo.equals(NivelMensagem.ALERTA)) style += "background-color: yellow; padding: 5; border-radius: 5px;";
		if (estilo.equals(NivelMensagem.OK)) style += "color: white; background-color: green; padding: 5; border-radius: 5px;";

		if (!estilo.equals(NivelMensagem.EXCECAO)) style += "font-family: Tahoma; ";
		
		if (adicionarDataHora) {
			msg = formatarData(new Date(), "dd/MM/yyyy HH:mm:ss.SSS") + " - " + msg;
		}
		if (logarNoConsole) {
			System.out.println(msg);
		}
		HTMLDocument doc = (HTMLDocument) logArea.getStyledDocument();
		try {
			doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()), "<" + tag + " style='" + style + "'>" + msg + "</" + tag + "><br/>");
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
		logArea.setStyledDocument(doc);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	public static void appendLogArea(JTextPane logArea, String msg, NivelMensagem estilo) {
		appendLogArea(logArea, msg, estilo, false, true, null);
	}
	
	public static void appendLogArea(JTextPane logArea, String msg) {
		appendLogArea(logArea, msg, NivelMensagem.NORMAL);
	}

	public static void appendLogArea(JTextArea logArea, String msg, boolean adicionarDataHora, boolean logarNoConsole) {
		if (adicionarDataHora) {
			msg = formatarData(new Date(), "dd/MM/yyyy HH:mm:ss.SSS") + " - " + msg;
		}
		if (logarNoConsole) {
			System.out.println(msg);
		}
		logArea.append(msg + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	public static void appendLogArea(JTextArea logArea, String msg) {
		appendLogArea(logArea, msg, false, true);
	}

	public static Integer idItemSelecionado(MyComboBox comboBox) {
		return ((ComboBoxItem) comboBox.getSelectedItem()).getIntId();
	}

	public static String idStringItemSelecionado(MyComboBox comboBox) {
		return ((ComboBoxItem) comboBox.getSelectedItem()).getStringId();
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

	public static String formatarNumero(Number numero, String formato) {
		DecimalFormat f = new DecimalFormat(formato);
		return f.format(numero);
	}
	
	public static boolean isPostgreSQL(EntityManager conexao) {
		String driver = (String) conexao.getEntityManagerFactory().getProperties().get("javax.persistence.jdbc.driver");
		return driver.contains("postgresql");
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

	public static void criarDiretorioBackup(String caminho, String subpasta) {
		File diretorio = new File(caminho + File.separator + subpasta);
		if (!diretorio.exists()) {
			diretorio.mkdir();
		}
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
				arquivoPropriedades += File.separator + ".ferramentasspu";
				// se a pasta não existe, cria antes de continuar
				if (!MyUtils.arquivoExiste(arquivoPropriedades)) {
					(new File(arquivoPropriedades)).mkdir();
				}

				// verifica se o arquivo de propriedades existe
				arquivoPropriedades += File.separator + "ferramentasspu.properties";
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
				arquivoPropriedades += File.separator + ".ferramentasspu";
				// continua se a pasta existe
				if (MyUtils.arquivoExiste(arquivoPropriedades)) {
					// verifica se o arquivo de propriedades existe
					arquivoPropriedades += File.separator + "ferramentasspu.properties";
					if (MyUtils.arquivoExiste(arquivoPropriedades)) {
						Properties props = MyUtils.obterPropriedades(arquivoPropriedades);
						retorno = props.getProperty(propriedade);
					}
				}
			}
		}

		return (retorno == null ? valorDefault : retorno);
	}

	public static String stackTraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	public static ActionListener openFileDialogWindow(String diretorioPadrao, JFileChooser fileChooser, JLabel labelFileName, JComponent clazz, Runnable extraCode) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (diretorioPadrao != null && !diretorioPadrao.trim().equals("")) {
					File dirPadrao = new File(diretorioPadrao);
					if (dirPadrao.exists()) {
						fileChooser.setCurrentDirectory(dirPadrao);
					}
				}
				Action detalhes = fileChooser.getActionMap().get("viewTypeDetails");
				detalhes.actionPerformed(null);
				int retorno = fileChooser.showOpenDialog(clazz);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (fileChooser.getSelectedFile().exists()) {
						if (labelFileName != null) labelFileName.setText(fileChooser.getSelectedFile().getAbsolutePath());
						if (extraCode != null) extraCode.run();
					}
				}
			}
		};
	}

	public static ActionListener executarProcessoComLog(JTextArea logArea, Runnable processo) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					logArea.setText("");
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								processo.run();
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Erro ao executar o processo: \n \n" + e.getMessage());
								MyUtils.appendLogArea(logArea, "Erro ao executar o processo: \n \n" + e.getMessage() + "\n" + MyUtils.stackTraceToString(e));
								e.printStackTrace();
							}
						}
					}).start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	public static ActionListener executarProcessoComLog(JTextPane logArea, Runnable processo) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								logArea.setText("");
								processo.run();
							} catch (MyValidationException e) {
								JOptionPane.showMessageDialog(null, e.getMessage());
							} catch (Exception e) {
								MyUtils.appendLogArea(logArea, "Erro ao executar o processo: \n \n" + e.getMessage() + "\n" + MyUtils.stackTraceToString(e), NivelMensagem.EXCECAO);
								e.printStackTrace();
							}
						}
					}).start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}
}
