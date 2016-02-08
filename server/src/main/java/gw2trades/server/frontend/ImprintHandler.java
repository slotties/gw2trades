package gw2trades.server.frontend;

import gw2trades.server.VelocityRenderer;
import gw2trades.server.model.SeoMeta;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ImprintHandler implements Handler<RoutingContext> {
    private final VelocityRenderer renderer;

    public ImprintHandler(VelocityRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(RoutingContext event) {
        SeoMeta meta = new SeoMeta("impressum.title");
        meta.setDescription("impressum.description");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("view", "impressum");
        ctx.put("seoMeta", meta);

        event.response().end(renderer.render("frame.vm", event, ctx));
    }
}
