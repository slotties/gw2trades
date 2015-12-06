package gw2trades.repository.api;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Query {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Query &&
                ((Query) obj).name.equals(this.name);
    }
}
