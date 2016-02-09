package gw2trades.server.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.server.frontend.exception.ItemNotFoundException;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class HistoryHandler implements Handler<RoutingContext> {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ItemRepository repository;
    private final ObjectMapper objectMapper;

    public HistoryHandler(ItemRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
    }

    private int resolveItemId(HttpServerRequest request) {
        String uri = request.path();
        int idStartIdx = uri.lastIndexOf('/');
        if (idStartIdx < 0) {
            return -1;
        }

        try {
            return Integer.valueOf(uri.substring(idStartIdx + 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public void handle(RoutingContext event) {
        int itemId = resolveItemId(event.request());
        if (itemId < 0) {
            event.fail(new ItemNotFoundException());
            return;
        }

        String from = event.request().params().get("from");
        String to = event.request().params().get("to");
        if (from == null || to == null) {
            event.fail(new IllegalArgumentException("bad input dates"));
            return;
        }

        LocalDateTime fromTs;
        LocalDateTime toTs;
        try {
            fromTs = LocalDateTime.parse(from, DATE_FORMAT);
            toTs = LocalDateTime.parse(to, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            event.fail(new IllegalArgumentException("bad input dates"));
            return;
        }

        try {
            List<ListingStatistics> stats = repository.getHistory(itemId, fromTs, toTs);
            String json = objectMapper.writeValueAsString(stats);

            HttpServerResponse response = event.response();
            response.putHeader("Content-Type", "application/json");
            response.end(json);
        } catch (IOException e) {
            event.fail(e);
        }
    }
}
