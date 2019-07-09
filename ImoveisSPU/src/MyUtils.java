import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

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

	public static <T> Vector<Vector<Object>>obterDados(List<T> dados, String... campos) throws Exception {
		Vector<Vector<Object>> retorno = new Vector<Vector<Object>>();

		for (T dado : dados) {
			Vector<Object> linha = new Vector<Object>();
			linha.add(Boolean.FALSE);
			for (int i = 0; i < campos.length; i++) {
				Method metodo = dado.getClass().getMethod(campos[i]);
				linha.add(metodo.invoke(dado));
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

	public static Properties obterPropriedades() {
		Properties retorno = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("imoveisspu.properties");
			retorno.load(input);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter propriedades para criação de despachos no SEI. Verifique se o arquivo 'despachosei.properties' existe no diretório da aplicação: \n \n" + e.getMessage());
			e.printStackTrace();
		}

		return retorno;
	}

	public static void salvarPropriedades(Properties prop) {
		OutputStream output = null;

		try {
			output = new FileOutputStream("imoveisspu.properties");
			prop.store(output, "");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao gravar propriedades para a carga de informações do SEI. Verifique se o arquivo 'extratorsei.properties' existe no diretório da aplicação: \n \n" + e.getMessage());
			e.printStackTrace();
		}
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
	
	public static <T> void insereOpcoesComboBox(MyComboBox comboBox, List<T> opcoes) {
		for (T opcao : opcoes) {
			ItemComboBox item = (ItemComboBox) opcao;
			comboBox.addItem(new ComboBoxItem(item.getIntegerItemValue(), item.getStringItemValue(), item.getItemLabel()));
		}
	}
	
	public static void execute(Connection conexao, String sql) throws Exception {
		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		cmd.close();
	}

	public static String dataFormatada() {
		return dataFormatada(null, null);
	}
	
	public static String dataFormatada(Date data) {
		return dataFormatada(data, null);
	}
	
	public static String dataFormatada(String formato) {
		return dataFormatada(null, formato);
	}
	
	public static String dataFormatada(Date data, String formato) {
		if (formato == null) formato = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(formato);
		if (data == null) data = new Date();
		return sdf.format(data);
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
}
