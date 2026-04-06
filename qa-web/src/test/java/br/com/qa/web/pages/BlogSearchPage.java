package br.com.qa.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object que representa a funcionalidade de busca do Blog do Agi.
 *
 * URL: https://blogdoagi.com.br/
 *
 * O padrão Page Object centraliza os seletores e ações da página,
 * desacoplando-os dos testes e facilitando a manutenção.
 */
public class BlogSearchPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── URL base ──────────────────────────────────────────────────────────────
    private static final String BASE_URL = "https://blogdoagi.com.br/";

    // ── Seletores ─────────────────────────────────────────────────────────────
    // Ícone de lupa (abre o campo de busca) no canto superior direito
    private final By searchIcon    = By.cssSelector("button.search-toggle, .search-icon, [aria-label='Buscar'], [aria-label='Search'], .dashicons-search");
    // Campo de input de texto da busca
    private final By searchInput   = By.cssSelector("input[type='search'], input.search-field, input[name='s']");
    // Contêiner geral dos resultados de busca
    private final By resultsSection = By.cssSelector(".search-results, main, #main, .site-main");
    // Itens individuais de resultado (artigos)
    private final By resultItems   = By.cssSelector("article.post, .post-item, .search-result, h2.entry-title, h3.entry-title");
    // Mensagem exibida quando não há resultados
    private final By noResultsMsg  = By.cssSelector(".no-results, .not-found, .nothing-found, p.search-no-results");

    // ── Construtor ────────────────────────────────────────────────────────────
    public BlogSearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // ── Ações ─────────────────────────────────────────────────────────────────

    /** Abre a página inicial do blog. */
    public BlogSearchPage open() {
        driver.get(BASE_URL);
        return this;
    }

    /** Clica no ícone de lupa para revelar o campo de busca. */
    public BlogSearchPage clickSearchIcon() {
        try {
            WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(searchIcon));
            icon.click();
        } catch (Exception e) {
            // Alguns temas WordPress já exibem o campo diretamente — ignora se a lupa não existir
            System.out.println("[INFO] Ícone de busca não encontrado ou já visível, continuando...");
        }
        return this;
    }

    /** Digita o termo no campo de busca. */
    public BlogSearchPage typeSearchTerm(String term) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        input.clear();
        input.sendKeys(term);
        return this;
    }

    /** Submete a busca pressionando ENTER. */
    public BlogSearchPage submitSearch() {
        WebElement input = driver.findElement(searchInput);
        input.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.presenceOfElementLocated(resultsSection));
        return this;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /** Retorna os itens de resultado encontrados na página. */
    public List<WebElement> getResultItems() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(resultItems));
            return driver.findElements(resultItems);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Retorna true se a mensagem "nenhum resultado" estiver visível. */
    public boolean isNoResultsMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(noResultsMsg));
            return driver.findElement(noResultsMsg).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Retorna o título do primeiro resultado encontrado. */
    public String getFirstResultTitle() {
        List<WebElement> items = getResultItems();
        if (items.isEmpty()) return "";
        return items.get(0).getText().trim();
    }

    /** Retorna a URL atual (útil para verificar redirect após busca). */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** Retorna o número de resultados na página. */
    public int getResultCount() {
        return getResultItems().size();
    }
}
