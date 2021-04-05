package com.bryandragon.langdb.common.util;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecoratingIteratorTest {
  @Test
  void testSimple() {
    Iterator<String> it = List.of("a", "b", "c").iterator();
    DecoratingIterator<String> subject = new DecoratingIterator<>(it, s -> s + "!");

    assertTrue(subject.hasNext());
    assertEquals("a!", subject.next());

    assertTrue(subject.hasNext());
    assertEquals("b!", subject.next());

    assertTrue(subject.hasNext());
    assertEquals("c!", subject.next());

    assertFalse(subject.hasNext());
    assertNull(subject.next());
  }
}
