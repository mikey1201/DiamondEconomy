package com.mikey1201.managers;

public interface Messages {
    String get(String path, String... replacements);
    String get(String path);
}
