import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUtils {

	public static Date obterData(String data, String mascara) throws Exception {
		return (new SimpleDateFormat(mascara)).parse(data);
	}
	
	public static String formatarData(Date data, String formato) {
		SimpleDateFormat f = new SimpleDateFormat(formato);
		return f.format(data);
	}
}
