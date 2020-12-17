package utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import javax.sql.rowset.CachedRowSet;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import com.sun.rowset.CachedRowSetImpl;

import framework.components.MyComboBox;
import framework.models.ComboBoxItem;

public class ExtratorSEIUtils {

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
			ResultSet rs = executeQuery(conexao, sql);
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

	public static void selecionarUnidade(WebDriver driver, Wait<WebDriver> wait, String unidade) {
		driver.switchTo().defaultContent();
    	Select cbxUnidade = new Select(encontrarElemento(wait, By.id("selInfraUnidades")));
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
		} else if (isWindows()) {
			return "../commons/resources/chromedriver/windows/chromedriver.exe";
		} else {
			return "../commons/resources/chromedriver/linux/chromedriver";
		}
	}

	public static String firefoxWebDriverPath() {
		if (isMacOS()) {
			return "../commons/resources/firefoxdriver/macos/geckodriver";
		} else if (isWindows()) {
			return "../commons/resources/firefoxdriver/windows/geckodriver.exe";
		} else {
			return "../commons/resources/firefoxdriver/linux/geckodriver";
		}
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
}
