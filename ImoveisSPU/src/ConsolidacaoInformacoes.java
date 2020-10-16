import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import framework.utils.SpringUtilities;

@SuppressWarnings("serial")
public class ConsolidacaoInformacoes extends JInternalFrame {

	private EntityManager conexao;
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);

	public ConsolidacaoInformacoes(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;

		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeCompactGrid(painelDados,
	            1, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.NORTH);
		add(areaDeRolagem, BorderLayout.CENTER);

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					logArea.setText("");
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								consolidarInformacoes();
							} catch (Exception e) {
								appendLogArea(logArea, "Erro ao importar a planilha de dados: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
								e.printStackTrace();
							}
						}

						private String stackTraceToString(Exception e) {
							StringWriter sw = new StringWriter();
							e.printStackTrace(new PrintWriter(sw));
							return sw.toString();
						}
					}).start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void consolidarInformacoes() throws Exception {
		zerarValoresConsolidados();
		gravarCidiTotal();
		gravarCidiAlienado();
		gravarCidiAlienadoNaoPago();
		gravarCidiBaixa();
		gravarCidiPoligonos();
		gravarSpiunetTotal();
		gravarSpiunetAlugado();
		gravarSpiunetPoligonos();
		gravarSiapaTotal();
		gravarSiapaPoligonos();
		appendLogArea(logArea, "Fim da consolidação!");
	}

	private void zerarValoresConsolidados() throws Exception {
		appendLogArea(logArea, "Zerando as colunas consolidadas atualmente...");
		
		String sql = "";
		sql += "update municipio ";
		sql += "   set ciditotal = 0 ";
		sql += "     , cidialienado = 0 ";
		sql += "     , cidialienadonaopago = 0 ";
		sql += "     , cidibaixa = 0 ";
		sql += "     , cidipoligono = 0 ";
		sql += "     , spiunettotal = 0 ";
		sql += "     , spiunetalugado = 0 ";
		sql += "     , spiunetpoligono = 0 ";
		sql += "     , siapatotal = 0 ";
		sql += "     , siapapoligono = 0 ";

		JPAUtils.executeUpdate(conexao, sql);
	}

	private void gravarCidiTotal() throws Exception {
		appendLogArea(logArea, "Consolidando CIDI Total...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from cidi t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " where substr(nbp, 1, 3) not in ('220','229','320','329','330','435','436','444','445','446','729','719','829','137','139') ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set ciditotal = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - CIDI Total: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarCidiPoligonos() throws Exception {
		appendLogArea(logArea, "Consolidando CIDI Polígonos...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from cidi t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " where substr(nbp, 1, 3) not in ('220','229','320','329','330','435','436','444','445','446','729','719','829','137','139') ";
		sql += "   and exists (select 1 from cidipoligono cp ";
		sql += "        	    where cp.nbp = t.nbp || parcela ";
		sql += "				   or cp.nbp = t.nbp || '-' || parcela ";
		sql += "           		   or (cp.nbp = t.nbp and not exists (select 1 from cidi c2 where t.nbp = c2.nbp and t.parcela <> c2.parcela))) ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set cidipoligono = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - CIDI Polígonos: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarCidiAlienado() throws Exception {
		appendLogArea(logArea, "Consolidando CIDI Alienado...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from cidi t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " where substr(nbp, 1, 3) not in ('220','229','320','329','330','435','436','444','445','446','729','719','829','137','139') ";
		sql += "   and termo_transf = '7014' ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set cidialienado = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - CIDI Alienado: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarCidiAlienadoNaoPago() throws Exception {
		appendLogArea(logArea, "Consolidando CIDI Alienado Não Pago...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from cidialienacao t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " where substr(cod_bp, 1, 3) not in ('220','229','320','329','330','435','436','444','445','446','729','719','829','137','139') ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set cidialienadonaopago = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - CIDI Alienado Não Pago: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarCidiBaixa() throws Exception {
		appendLogArea(logArea, "Consolidando CIDI Baixa...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from cididevolucao t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " where substr(nbp, 1, 3) not in ('220','229','320','329','330','435','436','444','445','446','729','719','829','137','139') ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set cidibaixa = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - CIDI Baixa: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarSpiunetTotal() throws Exception {
		appendLogArea(logArea, "Consolidando Spiunet Total...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from (select distinct municipio, ripimovel from spiunet) t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set spiunettotal = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - Spiunet Total: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarSpiunetPoligonos() throws Exception {
		appendLogArea(logArea, "Consolidando Spiunet Polígonos...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from (select distinct municipio, ripimovel from spiunet) t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " where exists (select 1 from rippoligono rp where t.ripimovel = replace(replace(rp.rip, ' ', ''), '-', '')) ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set spiunetpoligono = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - Spiunet Polígonos: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarSpiunetAlugado() throws Exception {
		appendLogArea(logArea, "Consolidando Spiunet Alugado...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from (select distinct municipio, ripimovel, tipoproprietario from spiunet) t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " where tipoproprietario = 'OUTROS' ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set spiunetalugado = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - Spiunet Alugado: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarSiapaTotal() throws Exception {
		appendLogArea(logArea, "Consolidando Siapa Total...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from siapa t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set siapatotal = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - Siapa Total: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void gravarSiapaPoligonos() throws Exception {
		appendLogArea(logArea, "Consolidando Siapa Polígonos...");

		String sql = "";
		sql += "select '-' as municipio, 0 as quantidade ";
		sql += " union all ";
		sql += "select coalesce(m.municipio, mc.nomecorreto, t.municipio || ' (sem correção)') as municipio, count(*) as quantidade ";
		sql += "  from siapa t ";
		sql += "  left join municipio m on t.municipio = m.municipio ";
		sql += "  left join municipiocorrecao mc on t.municipio = mc.nomeincorreto ";
		sql += " where exists (select 1 from rippoligono rp where t.ripimovel = replace(replace(rp.rip, ' ', ''), '-', '')) ";
		sql += " group by 1 ";
		sql += " order by 1 ";

		List<Object[]> dados = JPAUtils.executeNativeQuery(conexao, sql);

		for (Object[] dado : dados) {
			sql = "";
			sql += "update municipio ";
			sql += "   set siapapoligono = " + dado[1].toString();
			sql += " where municipio = '" + dado[0].toString().replace("'", "''") + "'";

			JPAUtils.executeUpdate(conexao, sql);
			
			appendLogArea(logArea, "Município: " + dado[0] + " - Siapa Polígonos: " + dado[1].toString());
		}

		appendLogArea(logArea, "---------------------------------------------------------------");
	}

	private void appendLogArea(JTextArea logArea, String msg) {
		System.out.println(msg);
		logArea.append(msg + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}
}
