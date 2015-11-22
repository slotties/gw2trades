package gw2trades.server.model;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class SeoMeta {
    private String title;
    private String imageUrl;

    public SeoMeta(String title) {
        // Title is mandatory.
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
