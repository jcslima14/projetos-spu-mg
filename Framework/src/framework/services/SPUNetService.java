package framework.services;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import framework.exceptions.MyException;
import framework.utils.MyUtils;

public class SPUNetService extends SeleniumService {
	public SPUNetService(String navegador, String endereco) throws Exception {
		super(navegador, endereco, true, "", 20);
	}
	
	public SPUNetService(String navegador, String endereco, boolean exibirNavegador) throws Exception {
		super(navegador, endereco, exibirNavegador, "", 20);
	}
	
	public SPUNetService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload) throws Exception {
		super(navegador, endereco, exibirNavegador, pastaDeDownload, 20);
	}
	
	public SPUNetService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload, int timeoutImplicito) throws Exception {
		super(navegador, endereco, exibirNavegador, pastaDeDownload, timeoutImplicito);
	}

	public void login(String usuario, String senha) throws Exception {
		this.driver.switchTo().defaultContent();

        WebElement weUsuario = encontrarElemento(15, 1, By.id("username"));
        espera(10, 1).until(ExpectedConditions.elementToBeClickable(weUsuario));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = encontrarElemento(15, 1, By.id("password"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = encontrarElemento(15, 1, By.xpath("//button[contains(text(), 'Acessar')]"));
        botaoAcessar.click();

        if (navegador.equalsIgnoreCase("firefox")) {
        	acceptSecurityAlert();
        }

        fecharPopup();

        // aceitar formulário inseguro
        WebElement enviarInformacao = null;
        try {
        	enviarInformacao = encontrarElemento(10, 2, By.xpath("//body[@class = 'insecure-form']//button[@id = 'proceed-button']"));
        } catch (Exception e) {
        }

        if (enviarInformacao != null) {
        	enviarInformacao.click();
        }
        
        this.janelaPrincipal = driver.getWindowHandle();
	}

	public void acessarPaginaTriagem() throws Exception {
        esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");

        // navega diretamente para a página de triagem
        driver.get("http://spunet.planejamento.gov.br/#/servicos/triagem");
        esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public void acessarPaginaCadastroGeometadado() throws Exception {
        esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");

        // navega diretamente para a página de triagem
        driver.get("http://spunet.planejamento.gov.br/#/geometadados/cadastrar");
        esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public void responderDemanda(String numeroAtendimento, String resposta, String complemento, File arquivo) throws Exception {
        pesquisarDemanda(numeroAtendimento); 

        List<WebElement> linhasRetornadas = encontrarElementos(5, 1, By.xpath("//table/tbody/tr"));
        String validacao = validarDemandaParaResponder(linhasRetornadas);
        if (validacao == null) {
        	abrirDemandaParaResponder(linhasRetornadas.iterator().next());
            preencherRespostaDemanda(resposta, complemento);
            realizarUploadArquivoResposta(arquivo);
            fecharDemanda();

            esperarCarregamento(2000, 5, 1, "//p[contains(text(), 'Carregando')]");
        } else {
        	throw new MyException(validacao);
        }
	}

	private String validarDemandaParaResponder(List<WebElement> linhasRetornadas) {
		if (linhasRetornadas.size() != 1) {
			return "Foram retornadas " + linhasRetornadas.size() + " linhas ao pesquisar. Não será possível responder automaticamente.";
		}
		
    	WebElement txtSituacao = linhasRetornadas.iterator().next().findElement(By.xpath("./td[6]"));
    	if (!txtSituacao.getText().trim().equalsIgnoreCase("Em Análise Técnica")) {
    		return "A solicitação não está em análise técnica e não pode ser respondida automaticamente";
    	}

    	return null;
	}

	private void fecharDemanda() {
		// botao para fechar a janela após clicar em enviar
		WebElement btnFechar = encontrarElemento(5, 1, By.xpath("//button[@ng-click='fechar()' and ./label[text() = 'fechar']]"));
		moverMouseParaElemento(btnFechar);
		btnFechar.click();
	}

	private void realizarUploadArquivoResposta(File arquivo) throws InterruptedException, Exception {
		WebElement txtUpload = encontrarElemento(5, 1, By.xpath("//input[@type = 'file']"));
		
		TimeUnit.MILLISECONDS.sleep(500);

		executarJavaScript("arguments[0].style.visibility = 'visible'; arguments[0].style.overflow = 'visible'; arguments[0].style.height = '1px'; arguments[0].style.width = '1px'; arguments[0].style.opacity = 1", txtUpload);

		TimeUnit.MILLISECONDS.sleep(500);
		
		txtUpload.sendKeys(arquivo.getAbsolutePath());
		
		esperarCarregamento(2000, 5, 1, "//p[contains(text(), 'Carregando')]");

		// busca o botão de enviar para clicar
		WebElement btnEnviar = encontrarElemento(5, 1, By.xpath("//button[text() = 'Enviar']"));
		moverMouseParaElemento(btnEnviar);
		btnEnviar.click();

		esperarCarregamento(2000, 15, 1, "//p[contains(text(), 'Carregando')]");
	}

	private void preencherRespostaDemanda(String resposta, String complemento) {
		WebElement optResposta = encontrarElemento(5, 1, By.xpath("//md-radio-button[@aria-label = '" + resposta + "']"));
		optResposta.click();
		
		if (!MyUtils.emptyStringIfNull(complemento).equals("")) {
		    WebElement txtInformacoesComplementares = encontrarElemento(5, 1, By.xpath("//textarea[@ng-model = 'analiseRequerimento.justificativa']"));
		    txtInformacoesComplementares.sendKeys(MyUtils.emptyStringIfNull(complemento));
		}
	}

	private void abrirDemandaParaResponder(WebElement linha) throws InterruptedException, Exception {
		WebElement btnExpandirOpcoes = linha.findElement(By.xpath("//md-fab-trigger"));
		executarJavaScript("arguments[0].click();", btnExpandirOpcoes);
		TimeUnit.MILLISECONDS.sleep(500);
		
		WebElement btnDetalhar = linha.findElement(By.xpath("//a[@ng-click = 'irParaDetalhar(item);']"));
		executarJavaScript("arguments[0].click();", btnDetalhar);
		TimeUnit.MILLISECONDS.sleep(500);
		
		esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	private void pesquisarDemanda(String numeroAtendimento) throws Exception {
		WebElement txtNumeroAtendimento = encontrarElemento(15, 1, By.xpath("//input[@ng-model = 'filtro.nuAtendimento']"));
        txtNumeroAtendimento.clear();
        txtNumeroAtendimento.sendKeys(numeroAtendimento);

        WebElement btnPesquisar = encontrarElemento(15, 1, By.xpath("//button[@aria-label = 'Pesquisar']"));
        btnPesquisar.click();

        esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");
	}
	
	public void preencherGeometadadoSecaoIdentificacao(String identFormatoProdutoCDG, String identProdutoCDG, String identTituloProduto, String identDataCriacao, String identDataDigitalizacao, String identResumo,
			String identStatus, String identInstituicao) throws Exception {
        WebElement cbbFormatoProduto = encontrarElemento(15, 1, By.name("idProdutoCdg"));
        cbbFormatoProduto.sendKeys(Keys.ESCAPE);
        cbbFormatoProduto.click();
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement optFormatoProduto = encontrarElemento(15, 1, By.xpath("//md-option[./div[text() = '" + identFormatoProdutoCDG + "']]"));
        executarJavaScript("arguments[0].click();", optFormatoProduto);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement cbbProduto = encontrarElemento(15, 1, By.name("idTipoProduto"));
        cbbProduto.click();

        WebElement optProduto = encontrarElemento(15, 1, By.xpath("//md-option[./div[text() = '" + identProdutoCDG + "']]"));
        executarJavaScript("arguments[0].click();", optProduto);
        TimeUnit.MILLISECONDS.sleep(500);
        try {
        	optProduto.sendKeys(Keys.ESCAPE);
        } catch (Exception e) {
        }

        WebElement optColecao = encontrarElemento(15, 1, By.xpath("//md-radio-button[@name = 'radioTipo' and @aria-label = 'Não']"));
        optColecao.click();
        TimeUnit.MILLISECONDS.sleep(500);

        WebElement txtTitulo = encontrarElemento(15, 1, By.xpath("//input[@name = 'dsTituloProdCartografico']"));
        txtTitulo.sendKeys(identTituloProduto);
        
        do {
        	TimeUnit.MILLISECONDS.sleep(200);
        } while (!txtTitulo.getAttribute("value").equals(identTituloProduto));

        WebElement txtDataCriacao = encontrarElemento(15, 1, By.xpath("//md-datepicker[@id = 'metadadosDtCriacao']//input"));
        txtDataCriacao.click();
        txtDataCriacao.sendKeys(Keys.ESCAPE);
        txtDataCriacao.sendKeys(identDataCriacao);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement txtDataPublicacao = encontrarElemento(15, 1, By.xpath("//md-datepicker[@id = 'metadadosDtPublicacao']//input"));
        txtDataPublicacao.click();
        txtDataPublicacao.sendKeys(Keys.ESCAPE);
        txtDataPublicacao.sendKeys(identDataDigitalizacao);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement txtResumo = encontrarElemento(15, 1, By.xpath("//textarea[@name = 'dsResumo']"));
        txtResumo.sendKeys(identResumo);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement cbbStatus = encontrarElemento(15, 1, By.xpath("//md-select[@id = 'idStatusProduto']"));
        cbbStatus.click();
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement optStatus = encontrarElemento(15, 1, By.xpath("//md-option[./div[text() = '" + identStatus + "']]"));
        executarJavaScript("arguments[0].click();", optStatus);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement cbbInstituicao = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetIdentificacaoForm']//md-select[@name = 'coResponsavel']"));
        cbbInstituicao.click();

        WebElement optInstituicao = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + identInstituicao + "']]"));
        executarJavaScript("arguments[0].click();", optInstituicao);
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");

        try {
        	optInstituicao.sendKeys(Keys.ESCAPE);
        } catch (Exception e) {
        }
        
        WebElement btnContinuar = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetIdentificacaoForm']//button[text() = 'CONTINUAR/GRAVAR']"));
        btnContinuar.click();
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public void preencherGeometadadoSecaoSistemaReferencia(String sisrefDatum, String sisrefProjecao, String sisrefObservacao) throws Exception {
        WebElement cbbSistemaReferencia = encontrarElemento(15, 1, By.xpath("//md-select[@name = 'coSistemaReferencia']"));
        cbbSistemaReferencia.click();

        WebElement optSistemaReferencia = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + sisrefDatum + "']]"));
        executarJavaScript("arguments[0].click();", optSistemaReferencia);
        TimeUnit.MILLISECONDS.sleep(500);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");

        if (!sisrefDatum.equalsIgnoreCase("sem datum")) {
	        WebElement cbbProjecao = encontrarElemento(15, 1, By.name("coProjecao"));
	        cbbProjecao.click();

	        WebElement optProjecao = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + sisrefProjecao + "']]"));
	        executarJavaScript("arguments[0].click();", optProjecao);
	        TimeUnit.MILLISECONDS.sleep(200);

	        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");
        }

        if (!sisrefObservacao.equals("")) {
	        WebElement txtObservacao = encontrarElemento(15, 1, By.xpath("//textarea[@ng-model = 'metadados.dssisrefobservacao']"));
	        txtObservacao.sendKeys(sisrefObservacao);
	        
	        do {
	        	TimeUnit.MILLISECONDS.sleep(200);
	        } while (!txtObservacao.getAttribute("value").equals(sisrefObservacao));
        }
        
        WebElement btnContinuar = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetSistemaReferenciaForm']//button[text() = 'CONTINUAR/GRAVAR']"));
        btnContinuar.click();
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 15, 1, "//p[contains(text(), 'Carregando')]");
	}

	public void preencherGeometadadoSecaoIdentificacaoCDG(String identcdgTipoReprEspacial, String escalaAjustada, String observacaoEscala, String identcdgIdioma, String identcdgCategoria, String identcdgUF, 
			String identcdgMunicipio, String identcdgDatum) throws Exception {
        WebElement cbbTipoRepresentacaoEspacial = encontrarElemento(15, 1, By.name("coRepresentacaoEspacial"));
        cbbTipoRepresentacaoEspacial.click();

        WebElement optTipoRepresentacaoEspacial = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + identcdgTipoReprEspacial + "']]"));
        executarJavaScript("arguments[0].click();", optTipoRepresentacaoEspacial);
        TimeUnit.MILLISECONDS.sleep(500);

        if (!escalaAjustada.contentEquals("")) {
	        WebElement optEscala = encontrarElemento(15, 1, By.xpath("//div[contains(@ng-show, 'idRepresentacaoEspacial') and @aria-hidden = 'false']/md-radio-group[@name = 'radioDAU']/md-radio-button[@aria-label = 'Escala']"));
	        optEscala.click();
	        TimeUnit.MILLISECONDS.sleep(200);

	        WebElement cbbListaEscala = encontrarElemento(15, 1, By.xpath("//div[contains(@ng-show, 'idRepresentacaoEspacial') and @aria-hidden = 'false']//md-select[@name = 'vlEscala']"));
	        // passarMouse.moveToElement(cbbListaEscala).perform();
	        executarJavaScript("arguments[0].click();", cbbListaEscala);
	        TimeUnit.MILLISECONDS.sleep(200);

	        WebElement optListaEscala = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + escalaAjustada + "']]"));
	        executarJavaScript("arguments[0].click();", optListaEscala);
	        TimeUnit.MILLISECONDS.sleep(200);
        } else {
	        WebElement txtObservacaoEscala = encontrarElemento(15, 1, By.xpath("//textarea[@ng-model = 'metadados.dsidentcdgobservacao']"));
	        txtObservacaoEscala.sendKeys(observacaoEscala);

	        do {
	        	TimeUnit.MILLISECONDS.sleep(200);
	        } while (!txtObservacaoEscala.getAttribute("value").equals(observacaoEscala));
        }

        WebElement cbbIdioma = encontrarElemento(15, 1, By.name("coIdiomaIdCdg"));
        // passarMouse.moveToElement(cbbIdioma).perform();
        cbbIdioma.click();

        WebElement optIdioma = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + identcdgIdioma + "']]"));
        executarJavaScript("arguments[0].click();", optIdioma);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement cbbCategoriaTematica = encontrarElemento(15, 1, By.name("coCategoriaTematica"));
        cbbCategoriaTematica.click();

        WebElement optCategoriaTematica = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + identcdgCategoria + "']]"));
        executarJavaScript("arguments[0].click();", optCategoriaTematica);
        TimeUnit.MILLISECONDS.sleep(500);

        int tentativas = 1;
        
        do {
	        WebElement cbbUF = encontrarElemento(15, 1, By.name("geocodigoUf"));
	        cbbUF.click();

	        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,100)", "");
	        
	        WebElement optUF = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + identcdgUF + "']]"));
	        // executarJavaScript("arguments[0].click();", optUF);
	        // passarMouse.moveToElement(optUF).perform();
	        
	        if (optUF.getAttribute("selected") != null) {
	        	optUF.click();
	        }

	        TimeUnit.MILLISECONDS.sleep(200);
	        optUF.click();
	        TimeUnit.MILLISECONDS.sleep(500);

	        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");

	        TimeUnit.MILLISECONDS.sleep(500);
	        optUF.sendKeys(Keys.ESCAPE);
	        TimeUnit.MILLISECONDS.sleep(200);

	        WebElement cbbMunicipio = encontrarElemento(15, 1, By.name("geocodigoMunicipioIdentificacaoCdg"));
	        // executarJavaScript("arguments[0].click();", cbbMunicipio);
	        TimeUnit.MILLISECONDS.sleep(500);
	        cbbMunicipio.click();
	        TimeUnit.MILLISECONDS.sleep(2000);

	        WebElement optMunicipio = null;
	        try {
	        	optMunicipio = encontrarElemento(5, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-optgroup/md-option[./div[text() = '" + identcdgMunicipio + "']]"));
	        } catch (Exception e) {
	        	if (tentativas > 10) {
	        		throw new MyException("Falhou tentativa " + (tentativas++) + " de obter a lista de municípios");
	        	}
	        	encontrarElemento(5, 1, By.xpath("//body")).click();
	        	TimeUnit.MILLISECONDS.sleep(200);
	        	continue;
	        }

	        executarJavaScript("arguments[0].click();", optMunicipio);
	        TimeUnit.MILLISECONDS.sleep(200);
	        
	        optMunicipio.sendKeys(Keys.ESCAPE);
	        TimeUnit.MILLISECONDS.sleep(200);

	        break;
        } while (true);
        
        WebElement cbbDatum = encontrarElemento(15, 1, By.xpath("//md-select[@name = 'coDatum']"));
        cbbDatum.click();

        WebElement optDatum = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + identcdgDatum + "']]"));
        executarJavaScript("arguments[0].click();", optDatum);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement btnContinuar = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetIdentificacaoCdgForm']//button[text() = 'CONTINUAR/GRAVAR']"));
        btnContinuar.click();
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public void preencherGeometadadoSecaoQualidade(String qualidadeNivel, String qualidadeLinhagemAjustada) throws Exception {
        WebElement cbbNivelHierarquico = encontrarElemento(15, 1, By.xpath("//md-select[@name = 'coNivelHierarquico']"));
        cbbNivelHierarquico.click();

        WebElement optNivelHierarquico = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + qualidadeNivel + "']]"));
        executarJavaScript("arguments[0].click();", optNivelHierarquico);
        TimeUnit.MILLISECONDS.sleep(500);

        WebElement txtLinhagem = encontrarElemento(15, 1, By.name("dsLinhagem"));
        txtLinhagem.sendKeys(qualidadeLinhagemAjustada);

        do {
        	TimeUnit.MILLISECONDS.sleep(200);
        } while (!txtLinhagem.getAttribute("value").equals(qualidadeLinhagemAjustada));

        WebElement btnContinuar = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetQualidadeForm']//button[text() = 'CONTINUAR/GRAVAR']"));
        btnContinuar.click();
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");
        TimeUnit.MILLISECONDS.sleep(500);
	}

	public void preencherGeometadadoSecaoDistribuicao(String distribuicaoFormato, String distribuicaoInstituicao, String distribuicaoFuncao) throws Exception {
        WebElement cbbFormatoDistribuicao = encontrarElemento(15, 1, By.name("coFormatoDistribuicao"));
        cbbFormatoDistribuicao.click();

        WebElement optFormatoDistribuicao = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + distribuicaoFormato + "']]"));
        executarJavaScript("arguments[0].click();", optFormatoDistribuicao);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement cbbInstituicao = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetDistribuicaoForm']//md-select[@name = 'coResponsavel']"));
        cbbInstituicao.click();

        WebElement optInstituicao = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + distribuicaoInstituicao + "']]"));
        executarJavaScript("arguments[0].click();", optInstituicao);
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");

        WebElement cbbFuncao = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetDistribuicaoForm']//md-select[@name = 'coFuncao']"));
        cbbFuncao.click();

        WebElement optFuncao = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + distribuicaoFuncao + "']]"));
        executarJavaScript("arguments[0].click();", optFuncao);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement btnContinuar = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetDistribuicaoForm']//button[text() = 'CONTINUAR/GRAVAR']"));
        btnContinuar.click();
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public void preencherGeometadadoSecaoMetametadados(String metadadoIdioma, String metadadoInstituicao, String metadadoFuncao) throws Exception {
        WebElement cbbIdioma = encontrarElemento(15, 1, By.name("coIdiomaMetadados"));
        cbbIdioma.click();

        WebElement optIdioma = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + metadadoIdioma + "']]"));
        executarJavaScript("arguments[0].click();", optIdioma);
        TimeUnit.MILLISECONDS.sleep(500);

        WebElement cbbInstituicao = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetMetadadosForm']//md-select[@name = 'coResponsavel']"));
        cbbInstituicao.click();

        WebElement optInstituicao = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + metadadoInstituicao + "']]"));
        executarJavaScript("arguments[0].click();", optInstituicao);
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");

        WebElement cbbFuncao = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetMetadadosForm']//md-select[@name = 'coFuncao']"));
        cbbFuncao.click();

        WebElement optFuncao = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + metadadoFuncao + "']]"));
        executarJavaScript("arguments[0].click();", optFuncao);
        TimeUnit.MILLISECONDS.sleep(200);

        WebElement btnContinuar = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetMetadadosForm']//button[text() = 'CONTINUAR/GRAVAR']"));
        btnContinuar.click();
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public void preencherGeometadadoSecaoInformacoesAdicionais(String infadicTipoArticulacao, String infadicCamadaInf) throws Exception {
        WebElement cbbTipoArticulacao = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetInfoAdicionaisForm']//md-select[@name = 'coTipoDeArticulacao']"));
        cbbTipoArticulacao.click();

        WebElement optTipoArticulacao = encontrarElemento(15, 1, By.xpath("//div[@aria-hidden = 'false']/md-select-menu/md-content/md-option[./div[text() = '" + infadicTipoArticulacao + "']]"));
        executarJavaScript("arguments[0].click();", optTipoArticulacao);
        TimeUnit.MILLISECONDS.sleep(200);

        if (!infadicCamadaInf.trim().equals("")) {
	        WebElement optCamadaInformacao = encontrarElemento(15, 1, By.xpath("//md-checkbox[./*[text() = '" + infadicCamadaInf + "']]"));
	        executarJavaScript("arguments[0].click();", optCamadaInformacao);
	        TimeUnit.MILLISECONDS.sleep(200);
        }

        WebElement btnEncaminhar = encontrarElemento(15, 1, By.xpath("//form[@name = 'cadastroMetInfoAdicionaisForm']//button[text() = 'ENCAMINHAR']"));
        btnEncaminhar.click();
        TimeUnit.MILLISECONDS.sleep(200);

        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public Integer salvarGeometadado() throws Exception {
        WebElement btnOK = encontrarElemento(15, 1, By.xpath("//button[text() = 'OK']"));
        btnOK.click();
        TimeUnit.MILLISECONDS.sleep(200);
        
        esperarCarregamento(200, 5, 1, "//p[contains(text(), 'Carregando')]");

        String[] url = driver.getCurrentUrl().split("\\/");
        
        return Integer.parseInt(url[url.length - 1]);
	}

	public void navegarPaginaMetadadoPorId(int idPesquisar) throws Exception {
        driver.get("http://spunet.planejamento.gov.br/#/geometadados/" + idPesquisar);

        esperarCarregamento(1000, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public String retornarTituloProdutoCartografico() throws Exception {
        WebElement txtTituloProdutoCartografico = null;

        // encontrar o título do produto cartográfico
        try {
        	txtTituloProdutoCartografico = encontrarElemento(5, 1, By.xpath("//td[./span[text() = ' - Título do Produto Cartográfico']]/following-sibling::td/label"));
        } catch (Exception e) {
        	throw new MyException("A página não possui o elemento com o título do produto cartográfico");
        }

        String tituloProdutoCartografico = txtTituloProdutoCartografico.getText().trim();

        if (tituloProdutoCartografico.equals("")) {
        	throw new MyException("O ID pesquisado não foi encontrado");
        }
        
        return tituloProdutoCartografico;
	}
}
