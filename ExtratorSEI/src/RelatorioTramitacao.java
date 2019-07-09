import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewDialog;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class RelatorioTramitacao { 
	public void exibirPreview() {
		try {
			ClassicEngineBoot.getInstance().start();

			// load report definition
			ResourceManager gerenciador = new ResourceManager();
			gerenciador.registerDefaults();
			Resource res = gerenciador.createDirectly(
				new File("./resources/reports/ControleProcessosSEI.prpt"), MasterReport.class);
			MasterReport relatorio = (MasterReport) res.getResource();

			final PreviewDialog preview = new PreviewDialog(relatorio);
			preview.setAlwaysOnTop(false);
			preview.addWindowListener(new WindowAdapter() {
				public void windowClosing (final WindowEvent event) {
					preview.setVisible(false);
				}
			});

			preview.pack();
			preview.setVisible(true);
		} catch (ResourceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}