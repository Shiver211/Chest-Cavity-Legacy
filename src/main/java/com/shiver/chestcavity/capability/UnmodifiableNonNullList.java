package com.shiver.chestcavity.capability;

import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * 只读的 NonNullList 包装器，禁止任何集合结构与元素替换操作以保护内部集合安全。
 *
 * @param <E> 元素类型。
 */
public final class UnmodifiableNonNullList<E> extends NonNullList<E> {

    private final List<E> unmodifiableDelegate;

    public UnmodifiableNonNullList(List<E> delegate, E defaultElement) {
        super(Collections.unmodifiableList(delegate), defaultElement);
        this.unmodifiableDelegate = Collections.unmodifiableList(delegate);
    }

    @Nonnull
    @Override
    public E get(int index) {
        return unmodifiableDelegate.get(index);
    }

    @Override
    public int size() {
        return unmodifiableDelegate.size();
    }

    @Override
    public boolean isEmpty() {
        return unmodifiableDelegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return unmodifiableDelegate.contains(o);
    }

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return unmodifiableDelegate.iterator();
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        return unmodifiableDelegate.toArray();
    }

    @Nonnull
    @Override
    public <T> T[] toArray(@Nonnull T[] a) {
        return unmodifiableDelegate.toArray(a);
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return unmodifiableDelegate.containsAll(c);
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends E> c) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends E> c) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Nonnull
    @Override
    public ListIterator<E> listIterator() {
        return unmodifiableDelegate.listIterator();
    }

    @Nonnull
    @Override
    public ListIterator<E> listIterator(int index) {
        return unmodifiableDelegate.listIterator(index);
    }

    @Nonnull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return unmodifiableDelegate.subList(fromIndex, toIndex);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException("Chest cavity organs list is unmodifiable; use IChestCavity#setOrgan or getOrganInventory() instead.");
    }

    @Override
    public int indexOf(Object o) {
        return unmodifiableDelegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return unmodifiableDelegate.lastIndexOf(o);
    }

    @Override
    public boolean equals(Object o) {
        return unmodifiableDelegate.equals(o);
    }

    @Override
    public int hashCode() {
        return unmodifiableDelegate.hashCode();
    }
}

