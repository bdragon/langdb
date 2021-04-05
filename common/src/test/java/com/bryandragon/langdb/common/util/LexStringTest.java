package com.bryandragon.langdb.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexStringTest {
  @ParameterizedTest
  @MethodSource("testFillArguments")
  void testFill(String lo, String hi, List<String> want) {
    assertEquals(want, LexString.fill(lo, hi));
  }

  static Stream<Arguments> testFillArguments() {
    return Stream.of(
        Arguments.of("a", "a", List.of("a")),
        Arguments.of("b", "a", List.of("b")),
        Arguments.of("x", "C", List.of("x")),
        Arguments.of("X", "C", List.of("X")),
        Arguments.of("a", "b", List.of("a", "b")),
        Arguments.of("a", "e", List.of("a", "b", "c", "d", "e")),
        Arguments.of("Qa", "Qa", List.of("Qa")),
        Arguments.of("Qa", "Qb", List.of("Qa", "Qb")),
        Arguments.of("Qb", "Qa", List.of("Qb")),
        Arguments.of("aaa", "aac", List.of("aaa", "aab", "aac")),
        Arguments.of("qx", "rc", List.of("qx", "qy", "qz", "ra", "rb", "rc")),
        Arguments.of("Qx", "Rc", List.of("Qx", "Qy", "Qz", "Ra", "Rb", "Rc")),
        Arguments.of("QX", "RC", List.of("QX", "QY", "QZ", "RA", "RB", "RC")),
        Arguments.of("Rx", "Qc", List.of("Rx")),
        Arguments.of(
            "qaa",
            "qcc",
            List.of(
                "qaa", "qab", "qac", "qad", "qae", "qaf", "qag", "qah", "qai", "qaj", "qak", "qal",
                "qam", "qan", "qao", "qap", "qaq", "qar", "qas", "qat", "qau", "qav", "qaw", "qax",
                "qay", "qaz", "qba", "qbb", "qbc", "qbd", "qbe", "qbf", "qbg", "qbh", "qbi", "qbj",
                "qbk", "qbl", "qbm", "qbn", "qbo", "qbp", "qbq", "qbr", "qbs", "qbt", "qbu", "qbv",
                "qbw", "qbx", "qby", "qbz", "qca", "qcb", "qcc")),
        Arguments.of(
            "Qaaa",
            "Qabx",
            List.of(
                "Qaaa", "Qaab", "Qaac", "Qaad", "Qaae", "Qaaf", "Qaag", "Qaah", "Qaai", "Qaaj",
                "Qaak", "Qaal", "Qaam", "Qaan", "Qaao", "Qaap", "Qaaq", "Qaar", "Qaas", "Qaat",
                "Qaau", "Qaav", "Qaaw", "Qaax", "Qaay", "Qaaz", "Qaba", "Qabb", "Qabc", "Qabd",
                "Qabe", "Qabf", "Qabg", "Qabh", "Qabi", "Qabj", "Qabk", "Qabl", "Qabm", "Qabn",
                "Qabo", "Qabp", "Qabq", "Qabr", "Qabs", "Qabt", "Qabu", "Qabv", "Qabw", "Qabx")),
        Arguments.of(
            "QM",
            "QZ",
            List.of(
                "QM", "QN", "QO", "QP", "QQ", "QR", "QS", "QT", "QU", "QV", "QW", "QX", "QY",
                "QZ")));
  }

  @Test
  void testNextAlpha() {
    assertEquals("b", LexString.nextAlpha("a"));
    assertEquals("B", LexString.nextAlpha("A"));
    assertEquals("a", LexString.nextAlpha("z"));
    assertEquals("A", LexString.nextAlpha("Z"));
    assertEquals("Qb", LexString.nextAlpha("Qa"));
    assertEquals("Ra", LexString.nextAlpha("Qz"));
    assertEquals("RA", LexString.nextAlpha("QZ"));
    assertEquals("Raaa", LexString.nextAlpha("Qzzz"));
  }
}
