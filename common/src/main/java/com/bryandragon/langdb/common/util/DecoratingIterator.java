package com.bryandragon.langdb.common.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public class DecoratingIterator<T> implements Iterator<T> {
  private final Iterator<T> it;
  private final Function<T, T> fn;

  public DecoratingIterator(Iterator<T> it, Function<T, T> fn) {
    Objects.requireNonNull(it);
    Objects.requireNonNull(fn);

    this.it = it;
    this.fn = fn;
  }

  @Override
  public boolean hasNext() {
    return it.hasNext();
  }

  @Override
  public T next() {
    if (!hasNext()) {
      return null;
    }
    T item = it.next();
    if (item != null) {
      return fn.apply(item);
    }
    return null;
  }
}
