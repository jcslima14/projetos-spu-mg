import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import javax.sql.rowset.CachedRowSet;
import javax.swing.JTextArea;

import org.apache.poi.ss.usermodel.Cell;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;

import com.sun.rowset.CachedRowSetImpl;

public class MyUtils {

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

	public static Vector<Object>obterTitulosColunas(List<MyTableColumn> colunas) throws Exception {
		Vector<Object> retorno = new Vector<Object>();

		for (MyTableColumn coluna : colunas) {
			retorno.add(coluna.getCaption());
		}

		return retorno;
	}

	public static int itemSelecionado(MyComboBox comboBox, Integer intId, String stringId) {
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
	
	public static String obterValorCelula(Cell celula) {
		if (celula == null) return "";
		else return celula.getStringCellValue();
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
		return (new SimpleDateFormat(mascara)).parse(data);
	}
	
	public static String formatarData(Date data, String formato) {
		SimpleDateFormat f = new SimpleDateFormat(formato);
		return f.format(data);
	}
}
