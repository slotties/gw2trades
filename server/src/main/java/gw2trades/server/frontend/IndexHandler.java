package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.Order;
import gw2trades.repository.api.Query;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.repository.api.model.SearchResult;
import gw2trades.server.VelocityRenderer;
import gw2trades.server.model.SeoMeta;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class IndexHandler implements Handler<RoutingContext> {
    private static final int PAGE_SIZE = 30;

    private final ItemRepository itemRepository;
    private final VelocityRenderer renderer;

    public IndexHandler(ItemRepository itemRepository, VelocityRenderer renderer) {
        this.itemRepository = itemRepository;
        this.renderer = renderer;
    }

    @Override
    public void handle(RoutingContext event) {
        MultiMap params = event.request().params();
        String orderBy = params.get("orderBy");
        String orderDir = params.get("orderDir");
        String pageStr = params.get("page");
        int page;
        try {
            page = Integer.valueOf(pageStr);
        } catch (NumberFormatException e) {
            page = 1;
        }
        String nameQuery = params.get("name");

        if (page < 1) {
            // Bad call, redirect to main page.
            HttpServerResponse response = event.response();
            response.setStatusCode(301);
            response.putHeader("Location", "/");
            response.end();
            return;
        }

        Query query = createQuery(nameQuery);
        Order order = orderBy != null ? Order.by(orderBy, !"asc".equals(orderDir)) : null;
        SearchResult<ListingStatistics> results;
        try {
            results = getStatistics(query, order, page);
        } catch (IOException e) {
            event.fail(e);
            return;
        }

        int lastPage = (int) Math.ceil((float) results.getTotalResults() / (float) PAGE_SIZE);

        SeoMeta seoMeta = new SeoMeta("index.title.default");
        seoMeta.setDescription("index.description");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("seoMeta", seoMeta);
        ctx.put("view", "index");
        ctx.put("lastPage", lastPage);
        ctx.put("currentPage", page);
        ctx.put("listingStatistics", results.getResults());
        ctx.put("orderBy", StringEscapeUtils.escapeHtml(orderBy));
        ctx.put("orderDir", StringEscapeUtils.escapeHtml(orderDir));
        ctx.put("query", query);

        event.response().end(renderer.render("frame.vm", event, ctx));
    }

    private Query createQuery(String name) {
        Query query = null;
        if (name != null) {
            query = new Query();
            query.setName(name);
        }

        return query;
    }

    private SearchResult<ListingStatistics> getStatistics(Query query, Order order, int forPage) throws IOException {
        // forPage is 1-based, for easier readability in the URL.
        int fromPage = (forPage - 1) * PAGE_SIZE;
        int toPage = fromPage + PAGE_SIZE;

        return this.itemRepository.listStatistics(query, order, fromPage, toPage);
    }
}
