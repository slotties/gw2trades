package gw2trades.server.model;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class SeoMeta {
    private String title;
    private Object[] titleArgs;
    private String imageUrl;
    private String description;
    private Object[] descriptionArgs;
    private String keywords;

    public SeoMeta(String title) {
        // Title is mandatory.
        this.title = title;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setTitleArgs(Object[] titleArgs) {
        this.titleArgs = titleArgs;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescriptionArgs(Object[] descriptionArgs) {
        this.descriptionArgs = descriptionArgs;
    }

    public Object[] getDescriptionArgs() {
        return descriptionArgs;
    }

    public Object[] getTitleArgs() {
        return titleArgs;
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
