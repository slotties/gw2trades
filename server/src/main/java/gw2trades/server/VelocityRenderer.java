package gw2trades.server;

import gw2trades.server.i18n.LocaleHandler;
import gw2trades.server.i18n.Translator;
import gw2trades.server.util.GuildWars2Util;
import io.vertx.ext.web.RoutingContext;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.MathTool;

import java.io.StringWriter;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class VelocityRenderer {
    private static final GuildWars2Util GW2_UTIL = new GuildWars2Util();
    private static final MathTool MATH_TOOL = new MathTool();
    private static final EscapeTool ESCAPE_TOOL = new EscapeTool();

    private final VelocityEngine velocityEngine;

    public VelocityRenderer(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public String render(String tpl, RoutingContext routingContext, Map<String, ?> context) {
        Translator translator = new Translator(
                ResourceBundle.getBundle("messages", LocaleHandler.getLocale(routingContext))
        );

        StringWriter output = new StringWriter(1024 * 8);
        VelocityContext ctx = new VelocityContext(context);
        ctx.put("i18n", translator);
        ctx.put("currentLocale", LocaleHandler.getLocale(routingContext));

        // Fill in some tools
        ctx.put("mathtool", MATH_TOOL);
        ctx.put("escapetool", ESCAPE_TOOL);
        ctx.put("gw2", GW2_UTIL);

        velocityEngine.mergeTemplate(tpl, "UTF-8", ctx, output);
        output.flush();

        return output.toString();
    }
}
