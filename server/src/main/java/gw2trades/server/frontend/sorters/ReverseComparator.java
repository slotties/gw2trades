package gw2trades.server.frontend.sorters;

import java.util.Comparator;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ReverseComparator<T> implements Comparator<T> {
    private Comparator<T> wrappedComparator;

    public ReverseComparator(Comparator<T> wrappedComparator) {
        this.wrappedComparator = wrappedComparator;
    }

    @Override
    public int compare(T o1, T o2) {
        return this.wrappedComparator.compare(o1, o2) * -1;
    }
}
