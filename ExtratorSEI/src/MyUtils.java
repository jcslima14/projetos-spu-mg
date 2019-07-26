import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

public class MyUtils {
	public static Properties obterPropriedades() {
		Properties retorno = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("extratorsei.properties");
			retorno.load(input);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter propriedades para extração de dados do SEI. Verifique se o arquivo 'extratorsei.properties' existe no diretório da aplicação: \n \n" + e.getMessage());
			e.printStackTrace();
		}

		return retorno;
	}

	public static void salvarPropriedades(Properties prop) {
		OutputStream output = null;

		try {
			output = new FileOutputStream("extratorsei.properties");
			prop.store(output, "");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao gravar propriedades para extração de dados do SEI. Verifique se o arquivo 'extratorsei.properties' existe no diretório da aplicação: \n \n" + e.getMessage());
			e.printStackTrace();
		}
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
}
