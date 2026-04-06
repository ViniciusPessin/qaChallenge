package br.com.qa.web.tests;

import br.com.qa.web.base.BaseTest;
import br.com.qa.web.pages.BlogSearchPage;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes automatizados para a funcionalidade de busca do Blog do Agi.
 *
 * Cenários cobertos:
 *  1. Busca com termo válido deve retornar artigos relevantes
 *  2. Busca com termo inválido deve exibir mensagem de "nenhum resultado"
 *  3. URL de busca deve conter o parâmetro da pesquisa (rastreabilidade)
 *  4. Cada resultado deve ter título não vazio
 */
@Epic("Blog do Agi")
@Feature("Busca de Artigos")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BlogSearchTest extends BaseTest {

    private BlogSearchPage searchPage;

    @BeforeEach
    void initPage() {
        searchPage = new BlogSearchPage(driver);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CENÁRIO 1 — Busca com termo válido retorna resultados
    // Justificativa: É o fluxo principal e mais crítico. O usuário acessa o blog
    // para encontrar conteúdo; se a busca falhar nesse fluxo, a principal
    // funcionalidade de descoberta de conteúdo está comprometida.
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @Order(1)
    @Story("Busca com resultado")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Cenário 1 - Busca por 'crédito' deve retornar ao menos 1 artigo")
    @Description("Ao buscar por um termo relevante ao domínio financeiro (crédito), " +
                 "o blog deve exibir artigos correspondentes na página de resultados.")
    void deveBuscarTermoValidoERetornarResultados() {
        String searchTerm = "crédito";

        searchPage
            .open()
            .clickSearchIcon()
            .typeSearchTerm(searchTerm)
            .submitSearch();

        int resultCount = searchPage.getResultCount();

        assertThat(resultCount)
            .as("A busca por '%s' deveria retornar ao menos 1 resultado", searchTerm)
            .isGreaterThan(0);

        String firstTitle = searchPage.getFirstResultTitle();
        assertThat(firstTitle)
            .as("O título do primeiro resultado não deve estar vazio")
            .isNotBlank();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CENÁRIO 2 — Busca com termo inválido exibe mensagem de sem resultado
    // Justificativa: Tão importante quanto encontrar resultados é tratar
    // corretamente a ausência deles. Uma mensagem adequada melhora a UX e
    // indica que o sistema se comporta de forma esperada em edge cases.
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @Order(2)
    @Story("Busca sem resultado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Cenário 2 - Busca por termo inválido deve exibir mensagem de 'nenhum resultado'")
    @Description("Ao buscar por uma string aleatória e sem sentido, " +
                 "o blog deve indicar ao usuário que não há artigos correspondentes.")
    void deveBuscarTermoInvalidoEExibirMensagemSemResultado() {
        String invalidTerm = "xyzxyz99999qaqaqa";

        searchPage
            .open()
            .clickSearchIcon()
            .typeSearchTerm(invalidTerm)
            .submitSearch();

        int resultCount = searchPage.getResultCount();
        boolean noResultsShown = searchPage.isNoResultsMessageDisplayed();

        assertThat(resultCount == 0 || noResultsShown)
            .as("Busca por '%s' deveria retornar 0 resultados ou exibir mensagem de 'sem resultado'", invalidTerm)
            .isTrue();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CENÁRIO 3 — URL de busca contém o parâmetro ?s=
    // Justificativa: A URL com o parâmetro de busca permite compartilhamento
    // e rastreabilidade. É um requisito comum em blogs WordPress e garante
    // que o mecanismo de busca nativo está sendo acionado corretamente.
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @Order(3)
    @Story("Parâmetro de busca na URL")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Cenário 3 - URL após busca deve conter parâmetro '?s=' com o termo pesquisado")
    @Description("Após submeter uma busca, a URL deve refletir o termo digitado no parâmetro 's', " +
                 "garantindo rastreabilidade e possibilidade de bookmarking.")
    void urlDeveConterParametroDeBusca() {
        String searchTerm = "investimento";

        searchPage
            .open()
            .clickSearchIcon()
            .typeSearchTerm(searchTerm)
            .submitSearch();

        String currentUrl = searchPage.getCurrentUrl();

        assertThat(currentUrl)
            .as("A URL deveria conter o parâmetro de busca '?s='")
            .contains("?s=");

        assertThat(currentUrl.toLowerCase())
            .as("A URL deveria conter o termo pesquisado '%s'", searchTerm)
            .contains(searchTerm.toLowerCase().replace(" ", "+"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CENÁRIO 4 — Todos os resultados têm título não vazio
    // Justificativa: Garante integridade dos dados. Um resultado sem título
    // indica problema de template ou de SEO — impacta tanto UX quanto indexação.
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @Order(4)
    @Story("Integridade dos resultados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Cenário 4 - Todos os artigos retornados devem possuir título visível")
    @Description("Cada card de artigo exibido nos resultados de busca deve ter um título " +
                 "visível e não vazio, garantindo a integridade da renderização do template.")
    void todosResultadosDevemTerTituloNaoVazio() {
        String searchTerm = "financiamento";

        searchPage
            .open()
            .clickSearchIcon()
            .typeSearchTerm(searchTerm)
            .submitSearch();

        var items = searchPage.getResultItems();

        Assumptions.assumeTrue(!items.isEmpty(),
            "Pulando teste: nenhum resultado encontrado para '" + searchTerm + "'");

        items.forEach(item ->
            assertThat(item.getText().trim())
                .as("Todos os resultados devem ter título não vazio")
                .isNotBlank()
        );
    }
}
