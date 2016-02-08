package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.repository.api.model.SearchResult;
import gw2trades.server.VelocityRenderer;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class SitemapHandler implements Handler<RoutingContext> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ItemRepository itemRepository;
    private final VelocityRenderer renderer;

    public SitemapHandler(ItemRepository itemRepository, VelocityRenderer renderer) {
        this.itemRepository = itemRepository;
        this.renderer = renderer;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            SearchResult<ListingStatistics> stats = itemRepository.listStatistics(null, null, 0, Integer.MAX_VALUE);
            String now = DATE_FORMATTER.format(LocalDate.now());

            // TODO: optimize by streaming the XML instead of buffering it fully into the heap
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("stats", stats);
            ctx.put("now", now);
            event.response().end(renderer.render("sitemapXml.vm", event, ctx));
        } catch (IOException e) {
            event.fail(e);
        }
    }
}
