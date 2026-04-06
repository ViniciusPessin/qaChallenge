package br.com.qa.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

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
    private final By searchIcon  = By.cssSelector("a.slide-search.astra-search-icon");

    private final By searchInput = By.cssSelector("form.search-form input[name='s']");

    private final By resultsSection = By.cssSelector(".search-results, main, #main, .site-main");

    private final By resultItems   = By.cssSelector("article.post, .post-item, .search-result, h2.entry-title, h3.entry-title");

    private final By noResultsMsg  = By.cssSelector(".no-results, .not-found, .nothing-found, p.search-no-results");

    // ── Construtor ────────────────────────────────────────────────────────────
    public BlogSearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // ── Ações ─────────────────────────────────────────────────────────────────

    public BlogSearchPage open() {
        driver.get(BASE_URL);
        return this;
    }

    public BlogSearchPage clickSearchIcon() {
    try {
        WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(searchIcon));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
        // aguarda o input ficar visível após o clique
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
    } catch (Exception e) {
        System.out.println("[INFO] Ícone de busca não encontrado, continuando...");
    }
    return this;
}

    public BlogSearchPage typeSearchTerm(String term) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        input.clear();
        input.sendKeys(term);
        return this;
    }

    public BlogSearchPage submitSearch() {
        WebElement input = driver.findElement(searchInput);
        input.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.presenceOfElementLocated(resultsSection));
        return this;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<WebElement> getResultItems() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(resultItems));
            return driver.findElements(resultItems);
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean isNoResultsMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(noResultsMsg));
            return driver.findElement(noResultsMsg).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getFirstResultTitle() {
        List<WebElement> items = getResultItems();
        if (items.isEmpty()) return "";
        return items.get(0).getText().trim();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public int getResultCount() {
        return getResultItems().size();
    }
}
