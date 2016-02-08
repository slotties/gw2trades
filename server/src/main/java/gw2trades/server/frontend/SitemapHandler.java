package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.repository.api.model.SearchResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class SitemapHandler implements Handler<RoutingContext> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ItemRepository itemRepository;
    private final VelocityEngine velocityEngine;

    public SitemapHandler(ItemRepository itemRepository, VelocityEngine velocityEngine) {
        this.itemRepository = itemRepository;
        this.velocityEngine = velocityEngine;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            SearchResult<ListingStatistics> stats = itemRepository.listStatistics(null, null, 0, Integer.MAX_VALUE);
            String now = DATE_FORMATTER.format(LocalDate.now());

            // TODO: optimize by streaming the XML instead of buffering it fully into the heap
            StringWriter output = new StringWriter(1024 * 1024);
            VelocityContext ctx = new VelocityContext();
            ctx.put("stats", stats);
            ctx.put("now", now);
            velocityEngine.mergeTemplate("sitemapXml.vm", "UTF-8", ctx, output);
            output.flush();
            event.response().end(output.toString());
        } catch (IOException e) {
            event.fail(e);
        }
    }
}
