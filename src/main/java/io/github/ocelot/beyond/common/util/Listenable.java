package io.github.ocelot.beyond.common.util;

import java.util.Collection;

/**
 * <p>Allows classes with listeners to stay consistent.</p>
 *
 * @param <T> The type of listener to add/remove
 * @author Ocelot
 */
public interface Listenable<T>
{
    /**
     * Adds the specified listener to the listening list
     *
     * @param listener The listener to add
     */
    default void addListener(T listener)
    {
        this.getListeners().add(listener);
    }

    /**
     * Removes the specified listener from the listening list
     *
     * @param listener The listener to remove
     */
    default void removeListener(T listener)
    {
        this.getListeners().remove(listener);
    }

    /**
     * @return All listeners added to this listenable
     */
    Collection<T> getListeners();
}
